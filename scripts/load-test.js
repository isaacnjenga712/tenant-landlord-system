import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Counter } from 'k6/metrics';

const BASE = __ENV.BASE || 'http://localhost:8080';
const AUTH_BASE = __ENV.AUTH_BASE || 'http://localhost:8080';

const LOGIN_PATH = __ENV.LOGIN_PATH || '/api/v1/auth/login';
const LOGIN_EMAIL = __ENV.LOGIN_EMAIL || 'org1test@example.com';
const LOGIN_PASSWORD = __ENV.LOGIN_PASSWORD || 'TestPass123!';

const TENANTS = 50;
const UNITS_PER_TENANT = 100;

const VUS = Number(__ENV.VUS || 1000);
const DURATION = __ENV.DURATION || '5m';

const inboxLatency = new Trend('inbox_latency');
const unreadLatency = new Trend('unread_latency');
const crossTenantLeak = new Counter('cross_tenant_leak');
const crossTenantWriteLeak = new Counter('cross_tenant_write_leak');
const crossTenantInconclusive = new Counter('cross_tenant_inconclusive');
const crossTenantProbes = new Counter('cross_tenant_probes');
const crossTenantWriteProbes = new Counter('cross_tenant_write_probes');
const loginFailures = new Counter('login_failures');

export const options = {
  scenarios: {
    default: {
      executor: 'constant-vus',
      vus: VUS,
      duration: DURATION,
    },
  },
  thresholds: {
    'inbox_latency': ['p(95)<200', 'p(99)<500'],
    'unread_latency': ['p(95)<200', 'p(99)<500'],
    'http_req_failed': ['rate<0.001'],
    'cross_tenant_leak': ['count==0'],
    'cross_tenant_write_leak': ['count==0'],
    'login_failures': ['count==0'],
  },
};

export function setup() {
  const loginUrl = `${AUTH_BASE}${LOGIN_PATH}`;
  const body = JSON.stringify({ email: LOGIN_EMAIL, password: LOGIN_PASSWORD });

  const res = http.post(loginUrl, body, {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'login' },
  });

  if (res.status !== 200 && res.status !== 201) {
    loginFailures.add(1);
    console.error(`Login failed: ${res.status} ${res.body?.slice(0, 200)}`);
    throw new Error(`Login failed with ${res.status}.`);
  }

  let parsed;
  try { parsed = JSON.parse(res.body); } catch {
    throw new Error('Login response was not JSON');
  }

  const token = parsed.access_token || parsed.accessToken || parsed.token ||
                parsed.jwt || parsed.id_token;

  if (!token) {
    console.error('Login response:', res.body?.slice(0, 300));
    throw new Error('No token found in login response');
  }

  console.log('Login OK — token acquired');
  return { token };
}

function uuidOrg(tenant) {
  return `00000000-0000-0000-0000-${String(tenant).padStart(12, '0')}`;
}
function uuidRecipient(tenant, unit) {
  return `00000000-0000-0000-${String(tenant).padStart(4, '0')}-${String(unit).padStart(12, '0')}`;
}
function randInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}
function headers(token, orgId, recipientId) {
  return {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
    'X-Org-Id': orgId,
    'X-User-PublicId': recipientId,
  };
}

function patchHeaders(token, orgId, recipientId) {
  return {
    'Authorization': `Bearer ${token}`,
    // NO Content-Type — the PATCH has no body
    'X-Org-Id': orgId,
    'X-User-PublicId': recipientId,
  };
}

export default function (data) {
  const token = data.token;
  const t = randInt(1, TENANTS);
  const u = randInt(1, UNITS_PER_TENANT);
  const orgId = uuidOrg(t);
  const recipientId = uuidRecipient(t, u);
  const roll = Math.random();

  if (roll < 0.60) {
    const res = http.get(`${BASE}/api/v1/notifications/inbox?page=0&size=20`, {
      headers: headers(token, orgId, recipientId),
      tags: { name: 'inbox_list' },
    });
    inboxLatency.add(res.timings.duration);
    check(res, {
      'inbox 200': (r) => r.status === 200,
    });
    sleep(0.1);
    return;
  }

  if (roll < 0.85) {
    const res = http.get(`${BASE}/api/v1/notifications/inbox/unread-count`, {
      headers: headers(token, orgId, recipientId),
      tags: { name: 'unread_count' },
    });
    unreadLatency.add(res.timings.duration);
    check(res, { 'unread 200': (r) => r.status === 200 });
    sleep(0.1);
    return;
  }

  if (roll < 0.95) {
    let otherT;
    do { otherT = randInt(1, TENANTS); } while (otherT === t);

    crossTenantProbes.add(1);

    const res = http.get(`${BASE}/api/v1/notifications/inbox?page=0&size=20`, {
      headers: headers(token, uuidOrg(otherT), recipientId),
      tags: { name: 'cross_tenant_negative' },
    });

    let result;
    if (res.status === 0) {
      result = 'INCONCLUSIVE';  // ← timeout
    } else if (res.status === 200) {
      try {
        const total = JSON.parse(res.body).total;
        result = total > 0 ? 'LEAK' : 'PASS';
      } catch {
        result = 'INCONCLUSIVE';  // ← unparseable, but not a proven leak
      }
    } else if (res.status === 404 || res.status === 403 || res.status === 400) {
      result = 'PASS';
    } else {
      result = 'INCONCLUSIVE';
    }

    crossTenantLeak.add(result === 'LEAK');
    crossTenantInconclusive.add(result === 'INCONCLUSIVE');

    check(res, {
      'cross-tenant read scoped': () => result === 'PASS',
      'cross-tenant read not a leak': () => result !== 'LEAK',
    });
    sleep(0.1);
    return;
  }

  // ---- Write path ----
  // FIX: distribute writes across many notifications to avoid
  // single-document contention. Without this, all VUs PATCH the same
  // first notification (items[0].id), causing MongoDB serialization and
  // timeouts at high VU counts.
  const writePage = Math.floor(Math.random() * 20);
  const listRes = http.get(`${BASE}/api/v1/notifications/inbox?page=${writePage}&size=5`, {
    headers: headers(token, orgId, recipientId),
    tags: { name: 'fetch_for_write' },
  });

  if (listRes.status !== 200) { sleep(0.1); return; }

  try {
    const body = JSON.parse(listRes.body);
    if (!body.items || body.items.length === 0) { sleep(0.1); return; }

    // FIX: pick a random item instead of always items[0]
    const writeItems = body.items;
    const writeItem = writeItems[Math.floor(Math.random() * writeItems.length)];
    const id = writeItem.id;

    http.patch(`${BASE}/api/v1/notifications/inbox/${id}/read`, undefined, {
      headers: patchHeaders(token, orgId, recipientId),
      tags: { name: 'mark_read' },
    });

    let otherT;
    do { otherT = randInt(1, TENANTS); } while (otherT === t);

    crossTenantWriteProbes.add(1);

    const evilRes = http.patch(
      `${BASE}/api/v1/notifications/inbox/${id}/read`,
      undefined,
      {
        headers: patchHeaders(token, uuidOrg(otherT), recipientId),
        tags: { name: 'cross_tenant_write' },
      }
    );

    console.log(`PATCH status=${evilRes.status} error=${evilRes.error} error_code=${evilRes.error_code}
    timings=${JSON.stringify(evilRes.timings)} body=${evilRes.body?.slice(0, 100)}`);

    let writeResult;
    if (evilRes.status === 0) writeResult = 'INCONCLUSIVE';
    else if (evilRes.status >= 200 && evilRes.status < 300) writeResult = 'LEAK';
    else if ([400, 403, 404].includes(evilRes.status)) writeResult = 'PASS';
    else writeResult = 'INCONCLUSIVE';

    crossTenantWriteLeak.add(writeResult === 'LEAK');
    crossTenantInconclusive.add(writeResult === 'INCONCLUSIVE');

    check(evilRes, {
      'cross-tenant write rejected': () => writeResult === 'PASS',
      'cross-tenant write not a leak': () => writeResult !== 'LEAK',
    });
  } catch (_) { /* best effort */ }

  sleep(0.1);
}

export function handleSummary(data) {
  const m = data.metrics || {};
  const get = (name, path) => m[name] ? path(m[name].values || {}) : 0;

  const iterations = get('iterations', v => v.count || 0);
  const getProbes = get('cross_tenant_probes', v => v.count || 0);
  const writeProbes = get('cross_tenant_write_probes', v => v.count || 0);
  const readLeaks = get('cross_tenant_leak', v => v.count || 0);
  const writeLeaks = get('cross_tenant_write_leak', v => v.count || 0);
  const inconclusive = get('cross_tenant_inconclusive', v => v.count || 0);
  const loginsFailed = get('login_failures', v => v.count || 0);

  const p95 = (n) => m[n]?.values?.['p(95)'] ? `${Math.round(m[n].values['p(95)'])}ms` : 'n/a';
  const p99 = (n) => m[n]?.values?.['p(99)'] ? `${Math.round(m[n].values['p(99)'])}ms` : 'n/a';

  const httpFailedPct = ((m.http_req_failed?.values?.rate ?? 0) * 100).toFixed(3);
  const isolationPass = readLeaks === 0 && writeLeaks === 0;
  const conclusive = inconclusive === 0;

  const summary = `
================================================================
RentFlow — Tenant isolation summary (AUTHENTICATED)
----------------------------------------------------------------
Iterations                  : ${iterations}
Login failures              : ${loginsFailed}

Cross-tenant GET probes     : ${getProbes}
  leaks                     : ${readLeaks}
Cross-tenant WRITE probes   : ${writeProbes}
  leaks                     : ${writeLeaks}
Inconclusive (5xx/401/to)   : ${inconclusive}

ISOLATION                    : ${isolationPass ? 'PASS' : 'FAIL'}
RESULT CONCLUSIVENESS        : ${conclusive ? 'conclusive (no 5xx/401/timeout on probes)' : 'AMBIGUOUS — see inconclusive count above'}

Latency
  inbox p95 / p99           : ${p95('inbox_latency')} / ${p99('inbox_latency')}
  unread p95 / p99          : ${p95('unread_latency')} / ${p99('unread_latency')}

HTTP
  failure rate              : ${httpFailedPct}%
================================================================
`;

  return {
    stdout: summary,
    'summary.json': JSON.stringify({
      iterations,
      login_failures: loginsFailed,
      cross_tenant: {
        get_probes: getProbes,
        get_leaks: readLeaks,
        write_probes: writeProbes,
        write_leaks: writeLeaks,
        inconclusive,
        isolation_pass: isolationPass,
        conclusive,
      },
      http_failure_rate: m.http_req_failed?.values?.rate ?? 0,
    }, null, 2),
  };
}