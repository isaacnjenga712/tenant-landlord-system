import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const crossTenantLeak = new Rate('cross_tenant_leak');
const inboxLatency    = new Trend('inbox_latency');
const unreadLatency   = new Trend('unread_latency');

export const options = {
  scenarios: {
    steady_load: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '2m',  target: 1000 },
        { duration: '10m', target: 1000 },
        { duration: '2m',  target: 0    },
      ],
      gracefulRampDown: '60s',
    },
  },
  thresholds: {
    'http_req_duration':  ['p(95)<200', 'p(99)<500'],
    'http_req_failed':    ['rate<0.001'],
    'cross_tenant_leak':  ['rate<0.001'],
  },
};

const BASE = __ENV.BASE_URL || 'http://localhost:8188';
const TENANTS = 50;
const UNITS_PER_TENANT = 100;

function uuidOrg(tenant) {
  const dec = tenant.toString().padStart(12, '0');
  return `00000000-0000-0000-0000-${dec}`;
}

function uuidRecipient(tenant, unit) {
  const t = tenant.toString().padStart(4, '0');
  const u = unit.toString().padStart(12, '0');
  return `00000000-0000-0000-${t}-${u}`;
}

function randInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

export function setup() {
  const orgId = uuidOrg(1);
  const recipientId = uuidRecipient(1, 1);
  const res = http.get(`${BASE}/api/v1/notifications/inbox?page=0&size=20`, {
    headers: { 'X-Org-Id': orgId, 'X-User-PublicId': recipientId },
  });
  if (res.status !== 200) {
    throw new Error(`setup: expected 200, got ${res.status}`);
  }
  const body = JSON.parse(res.body);
  console.log(`setup OK: recipient has total=${body.total}`);
  if (body.total < 1000) {
    throw new Error(`setup: expected >= 1000 notifications, got ${body.total}`);
  }
  return { ok: true };
}

export default function () {
  const t = randInt(1, TENANTS);
  const u = randInt(1, UNITS_PER_TENANT);
  const orgId = uuidOrg(t);
  const recipientId = uuidRecipient(t, u);

  const roll = Math.random();

  if (roll < 0.60) {
    // ---- Inbox list (paginated) ----
    const res = http.get(`${BASE}/api/v1/notifications/inbox?page=0&size=20`, {
      headers: { 'X-Org-Id': orgId, 'X-User-PublicId': recipientId },
      tags: { name: 'inbox_list' },
    });
    inboxLatency.add(res.timings.duration);
    check(res, {
      'inbox 200': (r) => r.status === 200,
      'inbox has 20 items': (r) => {
        try { return JSON.parse(r.body).items.length === 20; } catch { return false; }
      },
    });

  } else if (roll < 0.85) {
    // ---- Unread count ----
    const res = http.get(`${BASE}/api/v1/notifications/inbox/unread-count`, {
      headers: { 'X-Org-Id': orgId, 'X-User-PublicId': recipientId },
      tags: { name: 'unread_count' },
    });
    unreadLatency.add(res.timings.duration);
    check(res, { 'unread 200': (r) => r.status === 200 });

  } else if (roll < 0.95) {
    // ---- Cross-tenant negative ----
    let otherT;
    do { otherT = randInt(1, TENANTS); } while (otherT === t);
    const res = http.get(`${BASE}/api/v1/notifications/inbox?page=0&size=20`, {
      headers: { 'X-Org-Id': uuidOrg(otherT), 'X-User-PublicId': recipientId },
      tags: { name: 'cross_tenant_negative' },
    });
    let leak = false;
    if (res.status !== 200) leak = true;
    else {
      try { leak = JSON.parse(res.body).total > 0; } catch { leak = true; }
    }
    crossTenantLeak.add(leak);
    check(res, {
      'cross-tenant empty': (r) => {
        try { return JSON.parse(r.body).total === 0; } catch { return false; }
      },
    });

  } else {
    // ---- Write path ----
    const listRes = http.get(`${BASE}/api/v1/notifications/inbox?page=0&size=5`, {
      headers: { 'X-Org-Id': orgId, 'X-User-PublicId': recipientId },
      tags: { name: 'fetch_for_write' },
    });
    if (listRes.status === 200) {
      try {
        const body = JSON.parse(listRes.body);
        if (body.items && body.items.length > 0) {
          const id = body.items[0].id;
          http.patch(`${BASE}/api/v1/notifications/inbox/${id}/read`, null, {
            headers: { 'X-Org-Id': orgId, 'X-User-PublicId': recipientId },
            tags: { name: 'mark_read' },
          });
        }
      } catch (_) { /* ignore */ }
    }
  }

  sleep(0.1);
}