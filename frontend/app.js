const TOKEN_KEY = "yazlab_token";

function qs(id) {
  return document.getElementById(id);
}

function getToken() {
  return localStorage.getItem(TOKEN_KEY) || "";
}

function setToken(token) {
  if (!token) localStorage.removeItem(TOKEN_KEY);
  else localStorage.setItem(TOKEN_KEY, token);
  renderSession();
}

function decodeJwtPayload(token) {
  try {
    const parts = (token || "").split(".");
    if (parts.length < 2) return null;
    const b64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    const padded = b64 + "===".slice((b64.length + 3) % 4);
    const json = atob(padded);
    return JSON.parse(json);
  } catch (_) {
    return null;
  }
}

function showBanner(message, kind = "warn") {
  const b = qs("banner");
  if (!b) return;
  if (!message) {
    b.className = "banner hidden";
    b.textContent = "";
    return;
  }
  b.className = `banner ${kind}`;
  b.textContent = message;
}

function explainHttpError(e) {
  const status = e && e.status ? `${e.status}` : "";
  const msg = e && e.data && e.data.error ? e.data.error : "";
  if (status === "401") return "401 Yetkisiz: token yok/gecersiz. Once login ol.";
  if (status === "403") return "403 Yasak: bu endpoint icin rol yetkin yok (admin gerektiriyor olabilir).";
  if (status === "400") return `400 Hatali istek: ${msg || "eksik/yanlis body olabilir."}`;
  return `${status} ${msg}`.trim();
}

function renderSession() {
  const token = getToken();
  const el = qs("sessionUser");
  const logoutBtn = qs("btnLogout");
  if (!token) {
    el.textContent = "Giriş yapılmadı";
    logoutBtn.disabled = true;
    document.querySelectorAll(".auth-required").forEach((x) => (x.disabled = true));
    showBanner("Korumalı endpoint’ler için önce giriş yapmalısın. Admin işlemleri için kullanıcı adı: admin", "warn");
    return;
  }

  const payload = decodeJwtPayload(token);
  const who = payload && (payload.sub || payload.username) ? (payload.sub || payload.username) : "unknown";
  const role = payload && (payload.role || payload.authorities) ? (payload.role || payload.authorities) : "unknown";
  el.textContent = `Kullanıcı: ${who} | Rol: ${role}`;
  logoutBtn.disabled = false;
  document.querySelectorAll(".auth-required").forEach((x) => (x.disabled = false));
  showBanner("", "warn");
}

function log(line) {
  const out = qs("logOut");
  const ts = new Date().toLocaleTimeString();
  out.textContent = `[${ts}] ${line}\n` + out.textContent;
}

async function api(path, { method = "GET", body, auth = true } = {}) {
  const headers = { "Content-Type": "application/json" };
  if (auth) {
    const token = getToken();
    if (token) headers["Authorization"] = `Bearer ${token}`;
  }

  const resp = await fetch(`/api${path}`, {
    method,
    headers,
    body: body != null ? JSON.stringify(body) : undefined,
  });

  const ct = resp.headers.get("content-type") || "";
  const isJson = ct.includes("application/json");
  const text = await resp.text();

  let data = text;
  // Some upstream responses may return JSON body without json content-type.
  const looksLikeJson = text && (text.trim().startsWith("{") || text.trim().startsWith("["));
  if (isJson || looksLikeJson) {
    try {
      data = JSON.parse(text);
    } catch (_) {
      data = text;
    }
  }

  if (!resp.ok) {
    log(`${method} ${path} -> ${resp.status} ${resp.statusText}`);
    throw { status: resp.status, statusText: resp.statusText, data };
  }

  log(`${method} ${path} -> ${resp.status}`);
  return data;
}

function pretty(x) {
  return typeof x === "string" ? x : JSON.stringify(x, null, 2);
}

function escapeHtml(s) {
  return String(s)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function renderJsonBox(el, data) {
  el.textContent = pretty(data);
}

function renderUsers(el, users, onSelectUsername) {
  if (!Array.isArray(users)) {
    el.innerHTML = `<div class="muted">Yanıt dizi değil:</div><pre class="out">${escapeHtml(pretty(users))}</pre>`;
    return;
  }
  if (users.length === 0) {
    el.innerHTML = `<div class="muted">Kayıt yok.</div>`;
    return;
  }
  const rows = users
    .map((u) => {
      const rawUser = String(u.username ?? "").trim();
      const username = escapeHtml(rawUser);
      const id = escapeHtml(u.id ?? u._id ?? "");
      const enc = encodeURIComponent(rawUser);
      return `<tr class="user-row">
        <td>
          <span class="click user-pick" data-user="${enc}" title="Tıkla: bu kullanıcıyla sohbet">${username}</span>
        </td>
        <td>
          <span class="pill">${id || "-"}</span>
          <button type="button" class="btn btn-primary btn-sm user-chat" data-user="${enc}">Mesajla</button>
        </td>
      </tr>`;
    })
    .join("");
  el.innerHTML = `
    <table class="table">
      <thead><tr><th>username</th><th>id / işlem</th></tr></thead>
      <tbody>${rows}</tbody>
    </table>
    <div class="hint muted" style="margin-top:8px;">Kullanıcı adına veya <strong>Mesajla</strong>ya tıkla → konuşma açılır ve mesajlar yüklenir.</div>
  `;

  function pick(raw) {
    if (!raw || !onSelectUsername) return;
    onSelectUsername(raw);
  }

  el.querySelectorAll(".user-pick, .user-chat").forEach((node) => {
    node.addEventListener("click", (ev) => {
      ev.stopPropagation();
      const enc = node.getAttribute("data-user");
      if (!enc) return;
      try {
        pick(decodeURIComponent(enc));
      } catch (_) {
        pick(enc);
      }
    });
  });
}

function pickConversationId(c) {
  return c && (c.id || c._id) ? (c.id || c._id) : "";
}

function renderConversations(el, list, onPick) {
  if (!Array.isArray(list)) {
    el.innerHTML = `<div class="muted">Yanıt dizi değil:</div><pre class="out">${escapeHtml(pretty(list))}</pre>`;
    return;
  }
  if (list.length === 0) {
    el.innerHTML = `<div class="muted">Konuşma yok.</div>`;
    return;
  }
  const rows = list
    .map((c) => {
      const id = escapeHtml(pickConversationId(c));
      const participants = Array.isArray(c.participants) ? c.participants.join(", ") : (c.participants ?? "");
      return `<tr>
        <td><span class="click" data-conv-id="${id}">${id || "-"}</span></td>
        <td>${escapeHtml(participants)}</td>
      </tr>`;
    })
    .join("");

  el.innerHTML = `
    <table class="table">
      <thead><tr><th>conversationId</th><th>participants</th></tr></thead>
      <tbody>${rows}</tbody>
    </table>
  `;

  el.querySelectorAll("[data-conv-id]").forEach((node) => {
    node.addEventListener("click", () => {
      const id = node.getAttribute("data-conv-id") || "";
      if (onPick) onPick(id);
    });
  });
}

function renderMessages(el, list) {
  if (!Array.isArray(list)) {
    el.innerHTML = `<div class="muted">Yanıt dizi değil:</div><pre class="out">${escapeHtml(pretty(list))}</pre>`;
    return;
  }
  if (list.length === 0) {
    el.innerHTML = `<div class="muted">Mesaj yok.</div>`;
    return;
  }
  const rows = list
    .map((m) => {
      const from = escapeHtml(m.senderUsername ?? m.sender ?? m.username ?? "");
      const text = escapeHtml(m.text ?? "");
      const at = escapeHtml(m.createdAt ?? "");
      return `<tr><td>${from}</td><td>${text}</td><td><span class="pill">${at || "-"}</span></td></tr>`;
    })
    .join("");
  el.innerHTML = `
    <table class="table">
      <thead><tr><th>from</th><th>text</th><th>createdAt</th></tr></thead>
      <tbody>${rows}</tbody>
    </table>
  `;
}

function fillConvIdFromResponse(data) {
  // message-service Conversation model typically has id field (Mongo)
  if (!data || typeof data !== "object") return;
  const id = data.id || data._id;
  if (id) qs("convId").value = id;
}

async function loadConversationMessagesById(conversationId) {
  if (!conversationId) return;
  const data = await api(`/conversations/${encodeURIComponent(conversationId)}/messages`);
  renderMessages(qs("convOut"), data);
}

async function handleConversationRowPick(id) {
  if (!id) return;
  qs("convId").value = id;
  showBanner(`Secilen konusma: ${id}. Mesajlar getiriliyor...`, "warn");
  try {
    await loadConversationMessagesById(id);
  } catch (e) {
    qs("convOut").innerHTML = `<div class="muted">Hata:</div><pre class="out">${escapeHtml(pretty(e.data || e))}</pre>`;
    showBanner(explainHttpError(e), "danger");
  }
}

async function refreshConversationListUI() {
  try {
    const data = await api("/conversations");
    renderConversations(qs("convList"), data, handleConversationRowPick);
  } catch (_) {
    /* liste yoksa sessiz */
  }
}

async function startChatWithUser(participantUsername) {
  const u = String(participantUsername || "").trim();
  if (!u) return;
  const payload = decodeJwtPayload(getToken());
  const me = payload && (payload.sub || payload.username) ? String(payload.sub || payload.username) : "";
  if (me && u.toLowerCase() === me.toLowerCase()) {
    showBanner("Kendinle sohbet baslatilamaz. Baska bir kullanici sec.", "danger");
    return;
  }
  qs("convParticipant").value = u;
  showBanner(`${u} ile iletisim kuruluyor...`, "warn");
  try {
    const data = await api("/conversations", { method: "POST", body: { participantUsername: u } });
    fillConvIdFromResponse(data);
    const cid = qs("convId").value.trim();
    if (cid) await loadConversationMessagesById(cid);
    showBanner(`${u} ile sohbet acildi. Asagidan mesaj yazip gonderebilirsin.`, "warn");
    await refreshConversationListUI();
  } catch (e) {
    qs("convOut").innerHTML = `<div class="muted">Hata:</div><pre class="out">${escapeHtml(pretty(e.data || e))}</pre>`;
    showBanner(explainHttpError(e), "danger");
  }
}

async function main() {
  renderSession();

  qs("btnLogout").addEventListener("click", () => {
    setToken("");
    log("Çıkış yapıldı (token temizlendi).");
  });

  qs("btnClear").addEventListener("click", () => {
    qs("logOut").textContent = "";
  });

  qs("btnFillAdmin").addEventListener("click", () => {
    qs("authUsername").value = "admin";
    qs("authPassword").value = "123456";
    showBanner("Admin bilgileri dolduruldu. Login'e bas.", "warn");
  });

  qs("btnAuthTest").addEventListener("click", async () => {
    try {
      const data = await api("/auth/test", { auth: false });
      log("Auth test OK: " + pretty(data));
    } catch (e) {
      log("Auth test hata: " + pretty(e.data || e));
    }
  });

  qs("btnRegister").addEventListener("click", async () => {
    const username = qs("authUsername").value.trim();
    const password = qs("authPassword").value;
    if (!username || !password) {
      showBanner("Register icin username ve password gerekli.", "danger");
      return;
    }
    try {
      const data = await api("/auth/register", { method: "POST", auth: false, body: { username, password } });
      log("Register OK: " + pretty(data));
      showBanner("Register basarili. Simdi login olabilirsin.", "warn");
    } catch (e) {
      log("Register hata: " + pretty(e.data || e));
      showBanner(`Register basarisiz: ${explainHttpError(e)}`, "danger");
    }
  });

  qs("btnLogin").addEventListener("click", async () => {
    const username = qs("authUsername").value.trim();
    const password = qs("authPassword").value;
    if (!username || !password) {
      showBanner("Login icin username ve password gerekli.", "danger");
      return;
    }
    try {
      const data = await api("/auth/login", { method: "POST", auth: false, body: { username, password } });
      const token = data && data.token ? data.token : "";
      if (!token) throw { data: data || "token yok" };
      setToken(token);
      log("Login OK (token kaydedildi).");
      showBanner("Login basarili. Korumali endpointler aktif.", "warn");
    } catch (e) {
      log("Login hata: " + pretty(e.data || e));
      showBanner(`Login basarisiz: ${explainHttpError(e)}. Admin icin: admin / 123456`, "danger");
    }
  });

  qs("btnProfile").addEventListener("click", async () => {
    try {
      const data = await api("/profile");
      renderJsonBox(qs("profileOut"), data);
    } catch (e) {
      renderJsonBox(qs("profileOut"), e.data || e);
    }
  });

  qs("btnUsers").addEventListener("click", async () => {
    try {
      const data = await api("/users");
      renderUsers(qs("usersOut"), data, startChatWithUser);
    } catch (e) {
      qs("usersOut").innerHTML = `<div class="muted">Hata:</div><pre class="out">${escapeHtml(pretty(e.data || e))}</pre>`;
      showBanner(explainHttpError(e), "danger");
    }
  });

  qs("btnListConversations").addEventListener("click", async () => {
    try {
      const data = await api("/conversations");
      renderConversations(qs("convList"), data, handleConversationRowPick);
      qs("convOut").innerHTML = `<div class="muted">Konuşma seçmek için soldan ID'ye tıkla (mesajlar otomatik gelir).</div>`;
    } catch (e) {
      qs("convOut").innerHTML = `<div class="muted">Hata:</div><pre class="out">${escapeHtml(pretty(e.data || e))}</pre>`;
    }
  });

  qs("btnCreateConversation").addEventListener("click", async () => {
    const participantUsername = qs("convParticipant").value.trim();
    if (!participantUsername) {
      qs("convOut").innerHTML = `<div class="muted">Hata:</div><pre class="out">participantUsername gerekli (ornek: alice2)</pre>`;
      return;
    }
    try {
      const data = await api("/conversations", { method: "POST", body: { participantUsername } });
      qs("convOut").innerHTML = `<div class="muted">Oluşturuldu:</div><pre class="out">${escapeHtml(pretty(data))}</pre>`;
      fillConvIdFromResponse(data);
      await refreshConversationListUI();
    } catch (e) {
      qs("convOut").innerHTML = `<div class="muted">Hata:</div><pre class="out">${escapeHtml(pretty(e.data || e))}</pre>`;
      showBanner(explainHttpError(e), "danger");
    }
  });

  qs("btnSendMessage").addEventListener("click", async () => {
    const conversationId = qs("convId").value.trim();
    const text = qs("msgText").value;
    if (!conversationId) {
      qs("convOut").textContent = "conversationId gerekli";
      return;
    }
    try {
      await api(`/conversations/${encodeURIComponent(conversationId)}/messages`, {
        method: "POST",
        body: { text },
      });
      qs("convOut").innerHTML = `<div class="muted">Mesaj gönderildi, liste yenileniyor...</div>`;
      await loadConversationMessagesById(conversationId);
    } catch (e) {
      qs("convOut").innerHTML = `<div class="muted">Hata:</div><pre class="out">${escapeHtml(pretty(e.data || e))}</pre>`;
      showBanner(explainHttpError(e), "danger");
    }
  });

  qs("btnListMessages").addEventListener("click", async () => {
    const conversationId = qs("convId").value.trim();
    if (!conversationId) {
      qs("convOut").textContent = "conversationId gerekli";
      return;
    }
    try {
      await loadConversationMessagesById(conversationId);
    } catch (e) {
      qs("convOut").innerHTML = `<div class="muted">Hata:</div><pre class="out">${escapeHtml(pretty(e.data || e))}</pre>`;
      showBanner(explainHttpError(e), "danger");
    }
  });

  qs("btnDeleteConversation").addEventListener("click", async () => {
    const conversationId = qs("convId").value.trim();
    if (!conversationId) {
      qs("convOut").textContent = "conversationId gerekli";
      return;
    }
    try {
      await api(`/conversations/${encodeURIComponent(conversationId)}`, { method: "DELETE" });
      qs("convOut").innerHTML = `<div class="pill">Silindi (204)</div>`;
    } catch (e) {
      qs("convOut").innerHTML = `<div class="muted">Hata:</div><pre class="out">${escapeHtml(pretty(e.data || e))}</pre>`;
      showBanner(explainHttpError(e), "danger");
    }
  });
}

main().catch((e) => log("UI init hata: " + pretty(e)));

