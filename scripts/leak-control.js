// ============================================================================
// Negative-control test — proves the leak detector actually detects leaks
// ============================================================================
// Sends 100 probes to the deliberately-vulnerable endpoint
// (LeakControlController, loaded only under the "leaktest" profile).
// Asserts 100/100 leak detections. If this fails, the main load test's
// "0 leaks" result is meaningless — the classifier is broken.
//
// Run with:
//   LEAK_TEST_MODE=true k6 run scripts/leak-control.js
//
// Prerequisites:
//   - notification-engine running with SPRING_PROFILES_ACTIVE=docker,leaktest
//   - A valid JWT (same user as the main load test)
//   - At least 1 notification in the test user's inbox
// ============================================================================

import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';

const BASE          = __ENV.BASE        || 'http://localhost:8081';
const AUTH_BASE     = __ENV.AUTH_BASE   || 'http://localhost:8080';
const LOGIN_EMAIL   = __ENV.LOGIN_EMAIL || 'org1test@example.com';
const LOGIN_PASSWORD = __ENV.LOGIN_PASSWORD || 'TestPass123!';
const LOGIN_PATH    = __ENV.LOGIN_PATH  || '/api/v1/auth/login';

const TEST_USER_PUBLIC_ID = __ENV.TEST_USER_PUBLIC_ID || '739ed6b8-5bb5-476b-921b-78b166a67b73';

const LEAKS_DETECTED = new Counter('leak_detected');
const LEAKS_MISSED   = new Counter('leak_missed');

export const options = {
  scenarios: {
    negative_control: {
      executor: 'shared-iterations',
      vus: 10,
      iterations: 100,
    },
  },
  thresholds: {
    // If the classifier works, EVERY probe to the known-bad endpoint should
    // be detected as a leak. If this fails, the main load test is suspect.
    'leak_detected': ['count==100'],
    'leak_missed':   ['count==0'],
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
    throw new Error(`Login failed: ${res.status} ${res.body?.slice(0, 200)}`);
  }
  const parsed = JSON.parse(res.body);
  const token = parsed.accessToken || parsed.access_token || parsed.token;
  if (!token) throw new Error('No token in login response');
  console.log('Login OK — token acquired for negative-control run');
  return { token };
}

export default function (data) {
  const token = data.token;

  // Send a cross-tenant probe: forged X-Org-Id, valid user from another org
  const res = http.get(
    `${BASE}/api/v1/leaktest/inbox?page=0&size=20`,
    {
      headers: {
        'Authorization':  `Bearer ${token}`,
        'X-Org-Id':       '00000000-0000-0000-0000-000000000002', // org B (forged)
        'X-User-PublicId': TEST_USER_PUBLIC_ID,                   // org A's recipient
      },
      tags: { name: 'leaktest_probe' },
    }
  );

  // The endpoint is deliberately vulnerable — if it returns data, that IS
  // the leak the classifier should detect. (200 + total > 0 = leak.)
  let result;
  if (res.status === 200) {
    try {
      const total = JSON.parse(res.body).total;
      result = total > 0 ? 'LEAK' : 'PASS';
    } catch {
      result = 'INCONCLUSIVE';
    }
  } else if (res.status === 404 || res.status === 403 || res.status === 400) {
    result = 'PASS';
  } else {
    result = 'INCONCLUSIVE';
  }

  if (result === 'LEAK') LEAKS_DETECTED.add(1);
  else LEAKS_MISSED.add(1);

  check(res, {
    'control endpoint returns a leak (expected)': () => result === 'LEAK',
  });
}

export function handleSummary(data) {
  const detected = data.metrics.leak_detected?.values?.count ?? 0;
  const missed   = data.metrics.leak_missed?.values?.count   ?? 0;

  console.log(`
================================================================
Negative-control summary
----------------------------------------------------------------
Probes attempted : 100
Leaks detected   : ${detected}
Leaks missed     : ${missed}

RESULT: ${detected === 100 && missed === 0
  ? 'PASS — classifier fires 100/100 on a known-bad endpoint'
  : 'FAIL — the classifier is broken; the main load test result is suspect'}
================================================================
`);

  return {
    stdout: '',
    'leak-control-summary.json': JSON.stringify({
      probes: 100,
      detected,
      missed,
      pass: detected === 100 && missed === 0,
    }, null, 2),
  };
}