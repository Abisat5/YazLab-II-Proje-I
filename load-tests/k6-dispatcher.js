import http from "k6/http";
import { check, sleep } from "k6";
import { Counter, Rate, Trend } from "k6/metrics";


const errorRate = new Rate("errors");
const profileDuration = new Trend("profile_duration");
const usersDuration = new Trend("users_duration");
const conversationCreateDuration = new Trend("conversation_create_duration");
const messageSendDuration = new Trend("message_send_duration");
const messageListDuration = new Trend("message_list_duration");
const completedFlows = new Counter("completed_flows");

const BASE = __ENV.BASE_URL || "http://localhost:8080";
const PASSWORD = __ENV.TEST_PASSWORD || "123456";
const ADMIN_USERNAME = __ENV.ADMIN_USERNAME || "admin";
const ADMIN_PASSWORD = __ENV.ADMIN_PASSWORD || PASSWORD;
const MAX_TARGET = Number(__ENV.MAX_TARGET || 500);
const MESSAGE_PREFIX = __ENV.MESSAGE_PREFIX || "k6-load";

/**
 * örnekler:
 *   k6 run load-tests/k6-dispatcher.js
 *   k6 run -e BASE_URL=http://localhost:8080 -e MAX_TARGET=200 load-tests/k6-dispatcher.js
 *   k6 run --summary-export=load-tests/results/k6-summary-200.json load-tests/k6-dispatcher.js
 */
export const options = {
  stages: [
    { duration: "15s", target: MAX_TARGET },
    { duration: "30s", target: MAX_TARGET },
    { duration: "15s", target: 0 },
  ],
  thresholds: {
    http_req_failed: ["rate<0.10"],
    http_req_duration: ["p(95)<3000", "avg<1500"],
    errors: ["rate<0.10"],
    profile_duration: ["p(95)<2500"],
    users_duration: ["p(95)<2500"],
    conversation_create_duration: ["p(95)<3000"],
    message_send_duration: ["p(95)<3000"],
    message_list_duration: ["p(95)<3000"],
  },
};

function jsonHeaders(token) {
  const headers = { "Content-Type": "application/json" };
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  return headers;
}

function safeJson(response) {
  try {
    return response.json();
  } catch (_) {
    return null;
  }
}

function markResult(response, metric, label) {
  const ok = check(response, {
    [`${label} status uygun`]: (r) => r.status >= 200 && r.status < 300,
  });
  metric.add(response.timings.duration);
  errorRate.add(!ok);
  return ok;
}

function registerIfNeeded(username, password) {
  const response = http.post(
      `${BASE}/auth/register`,
      JSON.stringify({ username, password }),
      {
        headers: jsonHeaders(),
        responseCallback: http.expectedStatuses(201, 400, 409),
      }
  );



  const accepted = check(response, {
    "register accepted": (r) => r.status === 201 || r.status === 400 || r.status ==409,
  });
  errorRate.add(!accepted);
  return response;
}

function login(username, password) {
  const response = http.post(
    `${BASE}/auth/login`,
    JSON.stringify({ username, password }),
    { headers: jsonHeaders() }
  );

  const ok = check(response, {
    "login status 200": (r) => r.status === 200,
    "login token var": (r) => {
      const body = safeJson(r);
      return !!(body && body.token);
    },
  });
  errorRate.add(!ok);

  if (!ok) {
    return null;
  }

  return safeJson(response).token;
}

function ensureToken(username, password) {
  registerIfNeeded(username, password);
  return login(username, password);
}

function buildUsernames() {
  const vu = __VU;
  const iter = __ITER;
  const sender = `load_sender_${vu}`;
  const recipient = `load_target_${vu}_${iter}`;
  return { sender, recipient };
}

export function setup() {
  registerIfNeeded(ADMIN_USERNAME, ADMIN_PASSWORD);
  const adminToken = login(ADMIN_USERNAME, ADMIN_PASSWORD);
  if (!adminToken) {
    throw new Error("Admin token alinamadi. Yuk testi baslatilamadi.");
  }
  return { adminToken };
}

export default function (data) {
  const { sender, recipient } = buildUsernames();

  const senderToken = ensureToken(sender, PASSWORD);
  const recipientToken = ensureToken(recipient, PASSWORD);

  if (!senderToken || !recipientToken) {
    errorRate.add(true);
    sleep(1);
    return;
  }

  const profileResponse = http.get(`${BASE}/profile`, {
    headers: jsonHeaders(senderToken),
  });
  markResult(profileResponse, profileDuration, "GET /profile");

  const usersResponse = http.get(`${BASE}/users`, {
    headers: jsonHeaders(data.adminToken),
  });
  markResult(usersResponse, usersDuration, "GET /users");

  const conversationResponse = http.post(
    `${BASE}/conversations`,
    JSON.stringify({ participantUsername: recipient }),
    { headers: jsonHeaders(senderToken) }
  );
  const conversationOk = markResult(
    conversationResponse,
    conversationCreateDuration,
    "POST /conversations"
  );

  if (!conversationOk) {
    sleep(1);
    return;
  }

  const conversationBody = safeJson(conversationResponse) || {};
  const conversationId = conversationBody.id || conversationBody._id;

  if (!conversationId) {
    errorRate.add(true);
    sleep(1);
    return;
  }

  const messageResponse = http.post(
    `${BASE}/conversations/${conversationId}/messages`,
    JSON.stringify({ text: `${MESSAGE_PREFIX}-${__VU}-${__ITER}` }),
    { headers: jsonHeaders(senderToken) }
  );
  markResult(
    messageResponse,
    messageSendDuration,
    "POST /conversations/{id}/messages"
  );

  const conversationListResponse = http.get(`${BASE}/conversations`, {
    headers: jsonHeaders(senderToken),
  });
  const listOk = check(conversationListResponse, {
    "GET /conversations status uygun": (r) => r.status >= 200 && r.status < 300,
    "GET /conversations body dizi": (r) => Array.isArray(safeJson(r)),
  });
  errorRate.add(!listOk);

  const messageListResponse = http.get(
    `${BASE}/conversations/${conversationId}/messages`,
    { headers: jsonHeaders(senderToken) }
  );
  const messagesOk = markResult(
    messageListResponse,
    messageListDuration,
    "GET /conversations/{id}/messages"
  );

  if (messagesOk) {
    completedFlows.add(1);
  }

  sleep(0.5);
}
