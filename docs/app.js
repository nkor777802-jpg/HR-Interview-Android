(function () {
  "use strict";

  const D = window.HR_DEMO;
  const KEYS = {
    agreement: "hr-interview-demo-agreement",
    theme: "hr-interview-demo-theme",
    history: "hr-interview-demo-history",
    questions: "hr-interview-demo-questions",
    competences: "hr-interview-demo-competences",
    sound: "hr-interview-demo-sound",
    lang: "hr-interview-demo-lang"
  };

  const TABS = [
    { route: "home", label: "Главная" },
    { route: "setup", label: "Интервью" },
    { route: "history", label: "История" },
    { route: "settings", label: "Настройки" }
  ];

  const MONTHS = [
    "января", "февраля", "марта", "апреля", "мая", "июня",
    "июля", "августа", "сентября", "октября", "ноября", "декабря"
  ];

  const root = document.getElementById("app");
  const mediaDark = window.matchMedia ? window.matchMedia("(prefers-color-scheme: dark)") : null;

  const state = {
    route: "home",
    detailId: null,
    editQuestionId: null,
    theme: loadJson(KEYS.theme, "SYSTEM"),
    historyQuery: "",
    fromRoute: "home",
    bankCompetence: null,
    bankPosition: null,
    bankQuery: "",
    toast: null,
    dialog: null,
    agreeChecked: false,
    historyFilter: null,
    soundOn: loadJson(KEYS.sound, true),
    lang: loadJson(KEYS.lang, "ru"),
    setup: {
      candidate: "",
      vacancy: "",
      positionType: "SPECIALIST",
      selectedIds: D.SUGGESTED.SPECIALIST.slice()
    },
    session: null,
    resultSaved: false,
    newCompetenceName: "",
    editor: null
  };

  function loadJson(key, fallback) {
    try {
      const raw = localStorage.getItem(key);
      if (raw == null) return fallback;
      return JSON.parse(raw);
    } catch (e) {
      return fallback;
    }
  }

  function saveJson(key, value) {
    try {
      localStorage.setItem(key, JSON.stringify(value));
    } catch (e) { /* quota */ }
  }

  function uid() {
    if (crypto && crypto.randomUUID) return crypto.randomUUID();
    return "id_" + Date.now() + "_" + Math.random().toString(16).slice(2);
  }

  function competenceName(id) {
    const all = getCompetences();
    const found = all.find(function (c) { return c.id === id; });
    return found ? found.name : id;
  }

  function typeTitle(id) {
    const t = D.QUESTION_TYPES.find(function (x) { return x.id === id; });
    return t ? t.title : id;
  }

  function positionTitle(id) {
    const t = D.POSITION_TYPES.find(function (x) { return x.id === id; });
    return t ? t.title : id;
  }

  function getCompetences() {
    const extra = loadJson(KEYS.competences, []);
    return D.COMPETENCES.concat(extra);
  }

  function getQuestions() {
    const overrides = loadJson(KEYS.questions, { disabled: {}, user: [] });
    const built = D.BUILTIN_QUESTIONS.map(function (q) {
      const disabled = overrides.disabled && overrides.disabled[q.id] === true;
      return Object.assign({}, q, {
        isEnabled: !disabled,
        competenceName: competenceName(q.competenceId)
      });
    });
    const user = (overrides.user || []).map(function (q) {
      return Object.assign({}, q, { competenceName: competenceName(q.competenceId) });
    });
    return built.concat(user);
  }

  function persistQuestionEnabled(id, enabled) {
    const overrides = loadJson(KEYS.questions, { disabled: {}, user: [] });
    overrides.disabled = overrides.disabled || {};
    if (id.indexOf("builtin_") === 0) {
      if (enabled) delete overrides.disabled[id];
      else overrides.disabled[id] = true;
    } else {
      overrides.user = (overrides.user || []).map(function (q) {
        if (q.id === id) return Object.assign({}, q, { isEnabled: enabled });
        return q;
      });
    }
    saveJson(KEYS.questions, overrides);
  }

  function saveUserQuestion(q) {
    const overrides = loadJson(KEYS.questions, { disabled: {}, user: [] });
    overrides.user = overrides.user || [];
    const i = overrides.user.findIndex(function (x) { return x.id === q.id; });
    if (i >= 0) overrides.user[i] = q;
    else overrides.user.push(q);
    saveJson(KEYS.questions, overrides);
  }

  function deleteUserQuestion(id) {
    const overrides = loadJson(KEYS.questions, { disabled: {}, user: [] });
    overrides.user = (overrides.user || []).filter(function (q) { return q.id !== id; });
    saveJson(KEYS.questions, overrides);
  }

  function getHistory() {
    const list = loadJson(KEYS.history, []);
    return list.slice().sort(function (a, b) { return b.createdAt - a.createdAt; });
  }

  function saveHistoryItem(summary) {
    const list = getHistory().filter(function (x) { return x.id !== summary.id; });
    list.unshift(summary);
    saveJson(KEYS.history, list);
  }

  function shuffle(arr) {
    const a = arr.slice();
    for (let i = a.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      const t = a[i];
      a[i] = a[j];
      a[j] = t;
    }
    return a;
  }

  function selectQuestions(positionType, selectedCompetenceIds, bank) {
    const selected = {};
    selectedCompetenceIds.forEach(function (id) { selected[id] = true; });
    const filtered = [];
    const seen = {};
    bank.forEach(function (q) {
      if (!q.isEnabled) return;
      if (q.positionTypes.indexOf(positionType) < 0) return;
      if (!selected[q.competenceId]) return;
      if (seen[q.id]) return;
      seen[q.id] = true;
      filtered.push(q);
    });

    if (filtered.length === 0) {
      const fallback = [];
      const seen2 = {};
      bank.forEach(function (q) {
        if (!q.isEnabled) return;
        if (q.positionTypes.indexOf(positionType) < 0) return;
        if (seen2[q.id]) return;
        seen2[q.id] = true;
        fallback.push(q);
      });
      return shuffle(fallback).slice(0, D.TARGET_COUNT);
    }

    const queues = {};
    filtered.forEach(function (q) {
      if (!queues[q.competenceId]) queues[q.competenceId] = [];
      queues[q.competenceId].push(q);
    });
    Object.keys(queues).forEach(function (cid) {
      queues[cid] = shuffle(queues[cid]);
    });

    const order = [];
    const orderSeen = {};
    selectedCompetenceIds.forEach(function (id) {
      if (orderSeen[id]) return;
      orderSeen[id] = true;
      if (queues[id]) order.push(id);
    });

    const result = [];
    const usedIds = {};
    while (result.length < D.TARGET_COUNT) {
      let added = false;
      for (let i = 0; i < order.length; i++) {
        const queue = queues[order[i]];
        while (queue && queue.length) {
          const next = queue.shift();
          if (usedIds[next.id]) continue;
          usedIds[next.id] = true;
          result.push(next);
          added = true;
          break;
        }
        if (result.length === D.TARGET_COUNT) break;
      }
      if (!added) break;
    }
    return result;
  }

  function competenceScores(answers) {
    const groups = {};
    answers.forEach(function (a) {
      if (!groups[a.competenceId]) groups[a.competenceId] = [];
      groups[a.competenceId].push(a);
    });
    return Object.keys(groups).map(function (id) {
      const items = groups[id];
      const avg = items.reduce(function (s, x) { return s + x.score; }, 0) / items.length;
      return { competenceId: id, name: items[0].competenceName, average: avg };
    }).sort(function (a, b) { return b.average - a.average; });
  }

  function overallScore(drafts) {
    const scored = drafts.map(function (d) { return d.score; }).filter(function (s) { return s >= 1 && s <= 5; });
    if (!scored.length) return 0;
    return scored.reduce(function (a, b) { return a + b; }, 0) / scored.length;
  }

  function formatScore(value) {
    return (Math.round(value * 10) / 10).toFixed(1).replace(".", ",");
  }

  function scoreTone(value) {
    if (value >= 4) return "success";
    if (value >= 3) return "warning";
    return "accent";
  }

  function formatDate(ms) {
    const d = new Date(ms);
    const h = String(d.getHours()).padStart(2, "0");
    const m = String(d.getMinutes()).padStart(2, "0");
    return d.getDate() + " " + MONTHS[d.getMonth()] + " " + d.getFullYear() + ", " + h + ":" + m;
  }

  function applyTheme() {
    let light = false;
    if (state.theme === "LIGHT") light = true;
    else if (state.theme === "SYSTEM") light = !(mediaDark && mediaDark.matches);
    if (root) root.classList.toggle("theme-light", light);
  }

  function el(tag, attrs, children) {
    const node = document.createElement(tag);
    if (attrs) {
      Object.keys(attrs).forEach(function (k) {
        const v = attrs[k];
        if (v == null || v === false) return;
        if (k === "class") node.className = v;
        else if (k === "html") node.innerHTML = v;
        else if (k.indexOf("on") === 0 && typeof v === "function") node.addEventListener(k.slice(2).toLowerCase(), v);
        else if (k === "checked" || k === "disabled") node[k] = !!v;
        else node.setAttribute(k, v === true ? "" : String(v));
      });
    }
    (children || []).forEach(function (c) {
      if (c == null) return;
      if (typeof c === "string") node.appendChild(document.createTextNode(c));
      else node.appendChild(c);
    });
    return node;
  }

  function icon(name, cls) {
    const paths = {
      home: "M4 10.5 12 4l8 6.5V20a1 1 0 0 1-1 1h-5v-6H10v6H5a1 1 0 0 1-1-1z",
      notes: "M7 4h8l4 4v12a1 1 0 0 1-1 1H7a1 1 0 0 1-1-1V5a1 1 0 0 1 1-1zm8 1v4h4",
      history: "M12 6v6l4 2M12 21a9 9 0 1 1 0-18 9 9 0 0 1 0 18z",
      settings: "M12 15.5A3.5 3.5 0 1 0 12 8.5a3.5 3.5 0 0 0 0 7zM4.5 12l1.2-2.1L4.5 8l2-1 .8-2.2L9.5 4l1.2 1.8L12.9 5l1 2.1 2.2.3.8 2.2-1.2 2.1 1.2 2.1-.8 2.2-2.2.3-1 2.1-2.2-.8L9.5 20l-2.2-.5L6.5 17l-2 1 .8-2.2L4.5 14z",
      quiz: "M8 6h12M8 12h12M8 18h12M4 6h.01M4 12h.01M4 18h.01",
      forum: "M5 5h14v10H8l-3 3z",
      add: "M12 5v14M5 12h14"
    };
    const svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
    svg.setAttribute("viewBox", "0 0 24 24");
    svg.setAttribute("fill", "none");
    svg.setAttribute("stroke", "currentColor");
    svg.setAttribute("stroke-width", "1.7");
    svg.setAttribute("stroke-linecap", "round");
    svg.setAttribute("stroke-linejoin", "round");
    svg.setAttribute("class", cls || "ui-icon");
    svg.setAttribute("width", "22");
    svg.setAttribute("height", "22");
    const p = document.createElementNS("http://www.w3.org/2000/svg", "path");
    p.setAttribute("d", paths[name] || paths.home);
    svg.appendChild(p);
    return svg;
  }

  function drawCable(canvas, h) {
    const dpr = window.devicePixelRatio || 1;
    const w = canvas.clientWidth || canvas.parentElement.clientWidth || 350;
    canvas.width = Math.max(1, Math.floor(w * dpr));
    canvas.height = Math.max(1, Math.floor(h * dpr));
    canvas.style.width = "100%";
    canvas.style.height = h + "px";
    const ctx = canvas.getContext("2d");
    if (!ctx) return;
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    const accent = getComputedStyle(root || document.documentElement).getPropertyValue("--accent").trim() || "#E36A12";
    const navy = getComputedStyle(root || document.documentElement).getPropertyValue("--navy").trim() || "#8FB4D9";
    function rgba(hex, a) {
      const h2 = hex.replace("#", "");
      const n = h2.length === 3
        ? h2.split("").map(function (c) { return c + c; }).join("")
        : h2;
      const r = parseInt(n.slice(0, 2), 16);
      const g = parseInt(n.slice(2, 4), 16);
      const b = parseInt(n.slice(4, 6), 16);
      return "rgba(" + r + "," + g + "," + b + "," + a + ")";
    }
    function strand(yRatio, amplitude, phase, color, stroke) {
      ctx.beginPath();
      ctx.lineWidth = stroke;
      ctx.strokeStyle = color;
      ctx.lineCap = "round";
      const y0 = h * yRatio;
      ctx.moveTo(-8, y0);
      for (let x = 0; x <= w + 16; x += 8) {
        const y = y0 + Math.sin((x / w) * 6.28 + phase) * (h * amplitude);
        ctx.lineTo(x, y);
      }
      ctx.stroke();
    }
    strand(0.22, 0.10, 0.2, rgba(navy, 0.16), 2.2);
    strand(0.38, 0.14, 1.1, rgba(navy, 0.22), 2.6);
    strand(0.52, 0.12, 2.4, rgba(accent, 0.32), 2.4);
    strand(0.66, 0.10, 0.7, rgba(navy, 0.18), 2.0);
    strand(0.80, 0.08, 1.8, rgba(navy, 0.12), 1.6);
    ctx.fillStyle = rgba(accent, 0.28);
    ctx.beginPath();
    ctx.arc(w * 0.12, h * 0.50, h * 0.08, 0, Math.PI * 2);
    ctx.fill();
    ctx.strokeStyle = rgba(navy, 0.12);
    ctx.lineWidth = 1.4;
    ctx.beginPath();
    ctx.arc(w * 0.88, h * 0.34, h * 0.16, 0, Math.PI * 2);
    ctx.stroke();
  }

  function brandMark() {
    return el("div", { class: "brand-mark" }, [
      el("img", {
        class: "app-icon",
        src: "assets/ic_launcher_new.png",
        alt: "HR Интервью",
        width: "40",
        height: "40"
      })
    ]);
  }

  function appBrand(compact) {
    const deco = el("canvas", { class: "brand-deco" });
    const wrap = el("div", { class: "brand-wrap phone-header" + (compact ? " session-header" : "") }, [
      el("div", { class: "app-brand" }, [
        brandMark(),
        el("div", { class: "app-brand-text" }, [
          el("div", { class: "app-brand-title" }, ["HR Интервью"]),
          el("div", { class: "app-brand-sub" }, ["Веб-демо приложения"])
        ])
      ]),
      deco
    ]);
    requestAnimationFrame(function () { drawCable(deco, compact ? 10 : 18); });
    return wrap;
  }

  function brandHero(title, subtitle, action, onAction) {
    const canvas = el("canvas", { class: "cable-canvas" });
    const card = el("div", { class: "card hero" }, [
      canvas,
      el("div", { class: "hero-inner" }, [
        el("div", { class: "accent-line" }),
        el("h1", { class: "title-hero" }, [title]),
        el("p", { class: "subtitle" }, [subtitle]),
        action ? el("div", { class: "mt18" }, [
          el("button", { class: "btn btn-primary", type: "button", onClick: onAction }, [action])
        ]) : null
      ])
    ]);
    requestAnimationFrame(function () { drawCable(canvas, 56); });
    return card;
  }

  function pageHeader(title, subtitle) {
    const canvas = el("canvas", { class: "cable-canvas" });
    const inner = [
      el("div", { class: "accent-line" }),
      el("h2", { class: "title-page" }, [title])
    ];
    if (subtitle) inner.push(el("p", { class: "subtitle-sm" }, [subtitle]));
    const card = el("div", { class: "card page-header" }, [
      canvas,
      el("div", { class: "header-inner" }, inner)
    ]);
    requestAnimationFrame(function () { drawCable(canvas, 36); });
    return card;
  }

  function badge(text, tone) {
    return el("span", { class: "badge badge-" + tone }, [text]);
  }

  function chip(text, selected, accent, onClick) {
    let cls = "chip";
    if (selected && accent) cls += " accent";
    else if (selected) cls += " selected";
    return el("button", { class: cls, type: "button", onClick: onClick }, [text]);
  }

  function toast(msg) {
    state.toast = msg;
    render();
    setTimeout(function () {
      if (state.toast === msg) {
        state.toast = null;
        render();
      }
    }, 2600);
  }

  function go(route, extra) {
    state.route = route;
    if (extra && extra.detailId !== undefined) state.detailId = extra.detailId;
    if (extra && extra.editQuestionId !== undefined) state.editQuestionId = extra.editQuestionId;
    if (extra && extra.from) state.fromRoute = extra.from;
    render({ resetScroll: true });
  }

  function backLink(to) {
    return el("button", {
      class: "btn-text",
      type: "button",
      style: "align-self:flex-start;padding-left:0;margin:-4px 0 0;",
      onClick: function () { go(to); }
    }, ["← Назад"]);
  }

  function showBar() {
    return TABS.some(function (t) { return t.route === state.route; });
  }

  function interviewMiniCard(item, onClick) {
    return el("button", { class: "card clickable mini-card", type: "button", onClick: onClick }, [
      el("div", { class: "row top between" }, [
        el("div", { class: "grow" }, [
          el("div", { class: "mini-name" }, [item.candidateName]),
          el("p", { class: "body-sm mt8" }, [item.vacancy])
        ]),
        badge(formatScore(item.overallScore) + " / 5", scoreTone(item.overallScore))
      ]),
      el("p", { class: "body-sm mt8" }, [formatDate(item.createdAt)])
    ]);
  }

  function viewAgreement() {
    return [
      pageHeader("Соглашение"),
      el("div", { class: "card" }, [el("p", { class: "legal-body" }, [D.AGREEMENT_BODY])]),
      el("label", { class: "check-row card" }, [
        el("input", {
          type: "checkbox",
          checked: state.agreeChecked,
          onChange: function (e) { state.agreeChecked = e.target.checked; render(); }
        }),
        el("span", null, ["Я принимаю условия"])
      ]),
      el("button", {
        class: "btn btn-primary",
        type: "button",
        disabled: !state.agreeChecked,
        onClick: function () {
          if (!state.agreeChecked) {
            toast("Отметьте согласие с условиями");
            return;
          }
          saveJson(KEYS.agreement, true);
          go("home");
        }
      }, ["Принять и продолжить"])
    ];
  }

  function viewHome() {
    const history = getHistory();
    const recent = history.slice(0, 3);
    const nodes = [
      brandHero(
        "HR Интервью",
        "Структурированное интервью без лишних записей",
        "Начать интервью",
        function () { go("setup", { from: "home" }); }
      ),
      el("div", { class: "home-stats" }, [
        el("button", { class: "card clickable", type: "button", onClick: function () { go("history", { from: "home" }); } }, [
          el("p", { class: "body-md muted" }, ["Проведено"]),
          el("div", { class: "headline-md mt8", style: "margin-top:6px;" }, [String(history.length)])
        ]),
        el("button", { class: "card clickable", type: "button", onClick: function () { go("bank", { from: "home" }); } }, [
          icon("quiz", "ui-icon bank-card-icon"),
          el("div", { class: "section-title mt8" }, ["Банк вопросов"]),
          el("p", { class: "body-sm" }, ["Компетенции"])
        ])
      ]),
      el("div", { class: "row between" }, [
        el("h2", { class: "title-page", style: "margin:0;color:var(--text);font-size:20px;" }, ["Последние интервью"]),
        history.length ? el("button", { class: "btn-text", type: "button", onClick: function () { go("history", { from: "home" }); } }, ["Все"]) : null
      ])
    ];
    if (!recent.length) {
      nodes.push(el("div", { class: "empty" }, [
        el("div", { class: "empty-icon" }, [icon("forum", "ui-icon")]),
        el("h3", { class: "title-page", style: "color:var(--text);margin-top:0;" }, ["Пока нет проведённых интервью"]),
        el("p", { class: "subtitle-sm" }, ["Здесь появятся результаты ваших интервью"]),
        el("div", { class: "mt18" }, [
          el("button", { class: "btn btn-primary", type: "button", onClick: function () { go("setup", { from: "home" }); } }, ["Провести первое интервью"])
        ])
      ]));
    } else {
      recent.forEach(function (item) {
        nodes.push(interviewMiniCard(item, function () { go("detail", { detailId: item.id, from: "home" }); }));
      });
    }
    return nodes;
  }

  function viewSetup() {
    const s = state.setup;
    const comps = getCompetences().filter(function (c) { return c.isEnabled; });
    return [
      pageHeader("Новое интервью", "Одинаковый набор компетенций даст сопоставимый набор вопросов."),
      el("div", { class: "card" }, [
        el("div", { class: "section-title" }, ["Кандидат"]),
        el("div", { class: "field-wrap" }, [
          el("label", { class: "field-label" }, ["Имя или ФИО"]),
          el("input", {
            class: "field", type: "text", value: s.candidate, autocomplete: "name",
            onInput: function (e) { s.candidate = e.target.value; }
          })
        ]),
        el("div", { class: "section-title mt12" }, ["Вакансия"]),
        el("div", { class: "field-wrap" }, [
          el("label", { class: "field-label" }, ["Например: Мастер производства"]),
          el("input", {
            class: "field", type: "text", value: s.vacancy, autocomplete: "off",
            onInput: function (e) { s.vacancy = e.target.value; }
          })
        ])
      ]),
      el("div", { class: "card" }, [
        el("div", { class: "section-title" }, ["Тип позиции"]),
        el("p", { class: "body-sm mt8" }, ["От типа зависят рекомендуемые компетенции и набор вопросов."]),
        el("div", { class: "chips mt10" }, D.POSITION_TYPES.map(function (t) {
          return chip(t.title, s.positionType === t.id, s.positionType === t.id, function () {
            s.positionType = t.id;
            s.selectedIds = (D.SUGGESTED[t.id] || []).slice();
            render();
          });
        }))
      ]),
      el("div", { class: "card" }, [
        el("div", { class: "section-title" }, ["Компетенции"]),
        el("p", { class: "body-md muted mt8" }, ["Отметьте, что нужно оценить. Рекомендации зависят от типа позиции."]),
        el("div", { class: "chips chips-grid mt10" }, comps.map(function (c) {
          const selected = s.selectedIds.indexOf(c.id) >= 0;
          return chip(c.name, selected, false, function () {
            if (selected) s.selectedIds = s.selectedIds.filter(function (id) { return id !== c.id; });
            else s.selectedIds.push(c.id);
            render();
          });
        }))
      ]),
      el("button", {
        class: "btn btn-primary", type: "button", onClick: startInterview
      }, ["Начать интервью"])
    ];
  }

  function startInterview() {
    const name = state.setup.candidate.trim();
    const job = state.setup.vacancy.trim();
    if (!name || !job) {
      toast("Укажите кандидата и вакансию");
      return;
    }
    if (!state.setup.selectedIds.length) {
      toast("Выберите хотя бы одну компетенцию");
      return;
    }
    const questions = selectQuestions(state.setup.positionType, state.setup.selectedIds, getQuestions());
    if (!questions.length) {
      toast("Нет активных вопросов для выбранных условий. Проверьте банк вопросов.");
      return;
    }
    state.session = {
      candidateName: name,
      vacancy: job,
      positionType: state.setup.positionType,
      selectedCompetenceIds: state.setup.selectedIds.slice(),
      drafts: questions.map(function (q) {
        return { question: q, score: 0, comment: "" };
      }),
      currentIndex: 0,
      finalComment: "",
      createdAt: Date.now(),
      savedId: null
    };
    state.resultSaved = false;
    go("session");
  }

  function viewSession() {
    const s = state.session;
    if (!s || !s.drafts.length) {
      go("setup");
      return [el("p", null, ["Нет активной сессии"])];
    }
    const total = Math.max(s.drafts.length, 1);
    const index = Math.max(0, Math.min(s.currentIndex, s.drafts.length - 1));
    const draft = s.drafts[index];
    const progress = Math.round(((index + 1) / total) * 100);
    return [
      el("div", { class: "session-main" }, [
        el("div", { class: "session-progress" }, [
          el("div", { class: "progress-head" }, [
            el("div", { class: "section-title" }, ["Вопрос " + (index + 1) + " из " + total]),
            el("div", { class: "label-lg session-pct" }, [progress + "%"])
          ]),
          el("div", { class: "bar" }, [
            el("i", { style: "width:" + progress + "%" }),
            el("span", { class: "cap" })
          ])
        ]),
        el("div", { class: "chips session-chips" }, [
          chip(draft.question.competenceName, true, false),
          chip(typeTitle(draft.question.questionType), true, true)
        ]),
        el("div", { class: "card q-card" }, [
          el("p", { class: "q-title" }, [draft.question.text])
        ]),
        el("div", { class: "card session-score-card" }, [
          el("div", { class: "section-title" }, ["Оцените ответ"]),
          el("div", { class: "scores" }, [1, 2, 3, 4, 5].map(function (n) {
            return el("button", {
              class: "score-btn" + (draft.score === n ? " selected" : ""),
              type: "button",
              onClick: function () {
                draft.score = n;
                render();
              }
            }, [String(n)]);
          })),
          el("div", { class: "score-legend" }, [
            el("span", null, ["1 — слабый"]),
            el("span", null, ["3 — приемлемый"]),
            el("span", null, ["5 — сильный"])
          ])
        ]),
        el("div", { class: "card session-comment-card" }, [
          el("div", { class: "section-title" }, ["Комментарий HR"]),
          el("textarea", {
            class: "textarea",
            id: "hr-comment",
            placeholder: "Зафиксируйте важные моменты ответа...",
            onInput: function (e) { draft.comment = e.target.value; }
          }, [draft.comment])
        ])
      ]),
      sessionActions()
    ];
  }

  function sessionActions() {
    const s = state.session;
    if (!s) return null;
    const total = s.drafts.length;
    const index = s.currentIndex;
    const draft = s.drafts[index];
    const scored = draft && draft.score >= 1 && draft.score <= 5;
    const last = index >= total - 1;
    const row = [];
    if (index > 0) {
      row.push(el("button", {
        class: "btn btn-secondary grow", type: "button", onClick: function () {
          s.currentIndex = Math.max(0, s.currentIndex - 1);
          render({ resetScroll: true });
        }
      }, ["Назад"]));
    }
    row.push(el("button", {
      class: "btn btn-primary grow" + (scored ? " pulse" : ""),
      type: "button",
      onClick: function () {
        if (!scored) {
          toast("Сначала поставьте оценку от 1 до 5");
          return;
        }
        if (last) go("result");
        else {
          s.currentIndex += 1;
          render({ resetScroll: true });
        }
      }
    }, [last ? "Завершить интервью" : "Далее"]));
    return el("div", { class: "session-bar in-content" }, row);
  }

  function sessionToSummary(s, id) {
    return {
      id: id,
      candidateName: s.candidateName,
      vacancy: s.vacancy,
      positionType: s.positionType,
      createdAt: s.createdAt,
      overallScore: overallScore(s.drafts),
      finalComment: s.finalComment,
      selectedCompetenceIds: s.selectedCompetenceIds,
      answers: s.drafts.map(function (draft, index) {
        return {
          id: uid(),
          questionId: draft.question.id,
          questionText: draft.question.text,
          competenceId: draft.question.competenceId,
          competenceName: draft.question.competenceName,
          questionType: draft.question.questionType,
          score: draft.score,
          comment: draft.comment,
          orderIndex: index
        };
      })
    };
  }

  function viewResultContent(summary, editable, saved) {
    const scores = competenceScores(summary.answers);
    const strengths = scores.slice(0, 2);
    const weak = scores.slice().sort(function (a, b) { return a.average - b.average; }).slice(0, 2);
    const nodes = [
      pageHeader("Итог интервью", formatDate(summary.createdAt)),
      el("div", { class: "card" }, [
        el("div", { class: "row top between" }, [
          el("div", { class: "grow" }, [
            el("div", { class: "headline-sm" }, [summary.candidateName]),
            el("div", { class: "section-title mt8", style: "margin-top:4px;" }, [summary.vacancy]),
            el("p", { class: "muted mt8", style: "margin-top:6px;" }, [positionTitle(summary.positionType)])
          ]),
          badge(formatScore(summary.overallScore) + " / 5", scoreTone(summary.overallScore))
        ]),
            el("p", { class: "label-lg mt8" }, ["Общая оценка"]),
            el("div", { class: "score-big" }, [formatScore(summary.overallScore) + " / 5"])
      ]),
      el("div", { class: "card" }, [
        el("div", { class: "section-title" }, ["Оценки по компетенциям"]),
        el("div", { class: "mt12" }, scores.map(function (item) {
          const pct = Math.max(0, Math.min(100, (item.average / 5) * 100));
          return el("div", { class: "comp-row" }, [
            el("div", { class: "row center between" }, [
              el("p", { class: "body-lg grow" }, [item.name]),
              badge(formatScore(item.average), scoreTone(item.average))
            ]),
            el("div", { class: "bar" }, [el("i", { style: "width:" + pct + "%" })])
          ]);
        }))
      ]),
      el("div", { class: "card" }, [
        el("div", { class: "section-title" }, ["Сильные стороны"]),
        el("div", { class: "mt8 gap8" }, strengths.length
          ? strengths.map(function (it) { return el("p", { class: "body-lg" }, ["• " + it.name + " — " + formatScore(it.average)]); })
          : [el("p", null, ["Недостаточно оценок"])]),
        el("div", { class: "section-title mt16" }, ["Зоны внимания"]),
        el("div", { class: "mt8" }, [badge("Решение принимает HR", "neutral")]),
        el("p", { class: "body-md muted mt8" }, ["Приложение не принимает решение о найме. Низкие оценки — повод уточнить факты на следующем этапе."]),
        el("div", { class: "mt8 gap8" }, weak.length
          ? weak.map(function (it) { return el("p", { class: "body-lg" }, ["• " + it.name + " — " + formatScore(it.average)]); })
          : [el("p", null, ["Недостаточно оценок"])])
      ]),
      el("div", { class: "card" }, [
        el("div", { class: "section-title" }, ["Итоговый комментарий HR"]),
        editable
          ? el("textarea", {
            class: "textarea mt8",
            placeholder: "Ключевые выводы для себя и коллег...",
            onInput: function (e) {
              if (state.session) state.session.finalComment = e.target.value;
              summary.finalComment = e.target.value;
            }
          }, [summary.finalComment || ""])
          : el("p", { class: "muted mt8" }, [summary.finalComment && summary.finalComment.trim() ? summary.finalComment : "Комментарий не добавлен"])
      ])
    ];

    if (!editable) {
      nodes.push(el("div", { class: "card" }, [
        el("div", { class: "section-title" }, ["Вопросы и оценки"]),
        el("div", { class: "mt12 gap8" }, summary.answers.map(function (answer) {
          const block = [
            el("p", { class: "section-title" }, [(answer.orderIndex + 1) + ". " + answer.questionText]),
            el("p", { class: "muted mt8", style: "margin-top:4px;" }, [
              answer.competenceName + " · " + typeTitle(answer.questionType) + " · оценка " + answer.score
            ])
          ];
          if (answer.comment && answer.comment.trim()) {
            block.push(el("p", { class: "muted" }, [answer.comment]));
          }
          return el("div", { style: "margin-bottom:14px;" }, block);
        }))
      ]));
    }

    if (editable) {
      if (!saved) {
        nodes.push(el("button", {
          class: "btn btn-primary", type: "button", onClick: function () {
            const id = state.session.savedId || uid();
            state.session.savedId = id;
            state.session.finalComment = summary.finalComment || state.session.finalComment || "";
            saveHistoryItem(sessionToSummary(state.session, id));
            state.resultSaved = true;
            toast("Результат сохранён на устройстве");
            render();
          }
        }, ["Сохранить результат"]));
      } else {
        nodes.push(el("p", { style: "color:var(--navy);" }, ["Результат сохранён на устройстве"]));
      }
      nodes.push(el("button", {
        class: "btn btn-secondary", type: "button", onClick: function () { go("setup"); }
      }, ["Новое интервью"]));
      nodes.push(el("button", {
        class: "btn btn-secondary", type: "button", onClick: function () { go("home"); }
      }, ["На главную"]));
    }

    nodes.push(el("div", { class: "spacer" }));
    return nodes;
  }

  function viewResult() {
    if (!state.session) {
      go("home");
      return [];
    }
    const summary = sessionToSummary(state.session, state.session.savedId || "draft");
    return viewResultContent(summary, true, state.resultSaved);
  }

  function viewHistory() {
    const q = state.historyQuery.trim().toLowerCase();
    const items = getHistory().filter(function (it) {
      if (state.historyFilter && it.positionType !== state.historyFilter) return false;
      if (!q) return true;
      return it.candidateName.toLowerCase().indexOf(q) >= 0 || it.vacancy.toLowerCase().indexOf(q) >= 0;
    });
    const nodes = [
      pageHeader("История", "Сохранённые интервью на устройстве"),
      el("div", { class: "field-wrap", style: "margin:0;" }, [
        el("label", { class: "field-label" }, ["Поиск по кандидату или вакансии"]),
        el("input", {
          class: "field", type: "search", value: state.historyQuery,
          onInput: function (e) {
            state.historyQuery = e.target.value;
            render();
          }
        })
      ]),
      el("div", { class: "chips" }, [
        chip("Все", state.historyFilter == null, false, function () { state.historyFilter = null; render(); })
      ].concat(D.POSITION_TYPES.map(function (t) {
        return chip(t.title, state.historyFilter === t.id, state.historyFilter === t.id, function () {
          state.historyFilter = t.id;
          render();
        });
      })))
    ];
    if (!items.length) {
      nodes.push(el("div", { class: "empty" }, [
        el("div", { class: "empty-icon" }, [icon("history", "ui-icon")]),
        el("h3", { class: "title-page", style: "color:var(--text);margin:0;" }, ["История пока пуста"]),
        el("p", { class: "subtitle-sm" }, ["Сохранённые интервью появятся здесь."])
      ]));
    } else {
      items.forEach(function (item) {
        nodes.push(interviewMiniCard(item, function () { go("detail", { detailId: item.id, from: "history" }); }));
      });
    }
    return nodes;
  }

  function viewDetail() {
    const item = getHistory().find(function (x) { return x.id === state.detailId; });
    const backTo = state.fromRoute === "history" ? "history" : "home";
    if (!item) return [backLink(backTo), pageHeader("Итог интервью"), el("p", { class: "muted" }, ["Запись не найдена"])];
    return [backLink(backTo)].concat(viewResultContent(item, false, true));
  }

  function viewSettings() {
    return [
      pageHeader("Настройки"),
      el("div", { class: "card" }, [
        el("div", { class: "section-title" }, ["Общие настройки"]),
        el("p", { class: "body-sm mt8" }, ["Тема"]),
        el("div", { class: "chips mt8" }, D.THEME_MODES.map(function (m) {
          return chip(m.title, state.theme === m.id, state.theme === m.id, function () {
            state.theme = m.id;
            saveJson(KEYS.theme, m.id);
            applyTheme();
            render();
          });
        })),
        el("p", { class: "body-sm mt12" }, ["Язык"]),
        el("div", { class: "chips mt8" }, [
          chip("Русский", state.lang === "ru", true, function () {
            state.lang = "ru";
            saveJson(KEYS.lang, "ru");
            render();
          }),
          chip("English", state.lang === "en", false, function () {
            toast("В веб-демо доступен русский язык");
          })
        ]),
        el("div", { class: "switch-row mt12" }, [
          el("span", { class: "grow body-lg" }, ["Звук"]),
          el("button", {
            class: "switch" + (state.soundOn ? " on" : ""), type: "button",
            onClick: function () {
              state.soundOn = !state.soundOn;
              saveJson(KEYS.sound, state.soundOn);
              render();
            }
          })
        ])
      ]),
      el("div", { class: "card" }, [
        el("div", { class: "section-title" }, ["Данные и безопасность"]),
        settingsRow("Резервное копирование", function () {
          toast("В веб-демо данные хранятся локально в браузере");
        }),
        settingsRow("Экспорт истории", function () {
          try {
            const blob = new Blob([JSON.stringify(getHistory(), null, 2)], { type: "application/json" });
            const a = document.createElement("a");
            a.href = URL.createObjectURL(blob);
            a.download = "hr-interview-history.json";
            a.click();
          } catch (e) {
            toast("Не удалось экспортировать");
          }
        }),
        el("p", { class: "muted mt8" }, ["История интервью хранится только на этом устройстве."]),
        el("div", { class: "mt12" }, [
          el("button", {
            class: "btn btn-primary", type: "button", onClick: function () {
              state.dialog = "clear";
              render();
            }
          }, ["Очистить историю"])
        ])
      ]),
      el("div", { class: "card" }, [
        el("div", { class: "section-title" }, ["Интервью"]),
        settingsRow("Банк вопросов", function () { go("bank", { from: "settings" }); }),
        settingsRow("Компетенции", function () { go("competences", { from: "settings" }); })
      ]),
      el("div", { class: "card" }, [
        el("div", { class: "section-title" }, ["О приложении"]),
        el("p", { class: "body-sm mt8" }, ["Версия 1.0.0"]),
        el("p", { class: "body-sm" }, ["Веб-демо структурированного интервью для HR."]),
        settingsRow("Документация", function () { go("terms", { from: "settings" }); }),
        settingsRow("Пользовательское соглашение", function () { go("terms", { from: "settings" }); }),
        settingsRow("Политика конфиденциальности", function () { go("privacy", { from: "settings" }); }),
        el("div", { class: "mt12" }, [
          el("button", {
            class: "btn btn-secondary", type: "button", onClick: function () {
              if (window.HR_PRESENTATION && window.HR_PRESENTATION.leave) window.HR_PRESENTATION.leave();
            }
          }, ["На экран приветствия"])
        ])
      ])
    ];
  }

  function settingsRow(title, onClick) {
    return el("button", { class: "settings-row", type: "button", onClick: onClick }, [
      el("span", { class: "body-lg" }, [title]),
      el("span", { class: "chev" }, [">"])
    ]);
  }

  function viewLegal(title, body) {
    return [
      backLink("settings"),
      pageHeader(title),
      el("div", { class: "card" }, [el("p", { class: "legal-body" }, [body])])
    ];
  }

  function viewBank() {
    const allQ = getQuestions();
    const comps = getCompetences();
    const backTo = state.fromRoute === "settings" ? "settings" : "home";
    const qtext = (state.bankQuery || "").trim().toLowerCase();

    if (!state.bankCompetence) {
      const nodes = [
        backLink(backTo),
        pageHeader("Банк вопросов", "Выберите компетенцию, чтобы открыть вопросы."),
        el("div", { class: "field-wrap", style: "margin:0;" }, [
          el("label", { class: "field-label" }, ["Поиск"]),
          el("input", {
            class: "field", type: "search", value: state.bankQuery || "",
            onInput: function (e) { state.bankQuery = e.target.value; render(); }
          })
        ])
      ];
      comps.forEach(function (c) {
        if (qtext && c.name.toLowerCase().indexOf(qtext) < 0) return;
        const count = allQ.filter(function (q) { return q.competenceId === c.id; }).length;
        nodes.push(el("button", {
          class: "card clickable mini-card", type: "button",
          onClick: function () { state.bankCompetence = c.id; render({ resetScroll: true }); }
        }, [
          el("div", { class: "row center between" }, [
            el("div", { class: "grow row center" }, [
              icon("quiz", "ui-icon bank-card-icon"),
              el("div", { class: "section-title", style: "margin-left:8px;" }, [c.name])
            ]),
            badge(String(count), "neutral")
          ]),
          el("p", { class: "body-sm mt8" }, [count + " вопрос(ов)"])
        ]));
      });
      return nodes;
    }

    const questions = allQ.filter(function (q) {
      if (q.competenceId !== state.bankCompetence) return false;
      if (state.bankPosition && q.positionTypes.indexOf(state.bankPosition) < 0) return false;
      return true;
    });
    const comp = comps.find(function (c) { return c.id === state.bankCompetence; });
    const nodes = [
      el("button", {
        class: "btn-text", type: "button",
        style: "align-self:flex-start;padding-left:0;",
        onClick: function () { state.bankCompetence = null; render({ resetScroll: true }); }
      }, ["← К компетенциям"]),
      pageHeader(comp ? comp.name : "Вопросы", "Встроенные вопросы можно только отключить."),
      el("div", { class: "chips" }, [
        chip("Все", state.bankPosition == null, false, function () { state.bankPosition = null; render(); })
      ].concat(D.POSITION_TYPES.map(function (t) {
        return chip(t.title, state.bankPosition === t.id, state.bankPosition === t.id, function () {
          state.bankPosition = t.id;
          render();
        });
      })))
    ];
    questions.forEach(function (q) {
      const cardKids = [
        el("p", { class: "section-title", style: "font-weight:500;" }, [q.text]),
        el("p", { class: "body-sm mt8" }, [
          typeTitle(q.questionType) + " · " + q.positionTypes.map(positionTitle).join(", ")
        ]),
        el("div", { class: "row center mt8" }, [
          badge(q.isBuiltIn ? "Встроенный" : "Пользовательский", q.isBuiltIn ? "neutral" : "accent"),
          el("span", { class: "grow" }),
          el("span", { class: "label-md" }, ["Активен"]),
          el("button", {
            class: "switch" + (q.isEnabled ? " on" : ""),
            type: "button",
            onClick: function (e) {
              e.stopPropagation();
              persistQuestionEnabled(q.id, !q.isEnabled);
              render();
            }
          })
        ])
      ];
      if (!q.isBuiltIn) {
        cardKids.push(el("button", {
          class: "btn-text", type: "button", onClick: function (e) {
            e.stopPropagation();
            deleteUserQuestion(q.id);
            render();
          }
        }, ["Удалить"]));
      }
      nodes.push(el(q.isBuiltIn ? "div" : "button", {
        class: q.isBuiltIn ? "card" : "card clickable",
        type: q.isBuiltIn ? undefined : "button",
        onClick: q.isBuiltIn ? undefined : function () { go("bank_edit", { editQuestionId: q.id }); }
      }, cardKids));
    });
    return nodes;
  }

  function viewQuestionEditor() {
    const e = state.editor;
    const comps = getCompetences();
    return [
      backLink("bank"),
      pageHeader(state.editQuestionId ? "Редактирование" : "Новый вопрос"),
      el("div", { class: "field-wrap", style: "margin:0;" }, [
        el("label", { class: "field-label" }, ["Текст вопроса"]),
        el("textarea", {
          class: "textarea",
          disabled: e.isBuiltIn,
          onInput: function (ev) { if (!e.isBuiltIn) e.text = ev.target.value; }
        }, [e.text])
      ]),
      el("div", { class: "section-title" }, ["Компетенция"]),
      el("div", { class: "chips" }, comps.map(function (c) {
        return chip(c.name, e.competenceId === c.id, false, function () {
          if (!e.isBuiltIn) { e.competenceId = c.id; render(); }
        });
      })),
      el("div", { class: "section-title" }, ["Тип вопроса"]),
      el("div", { class: "chips" }, D.QUESTION_TYPES.map(function (t) {
        return chip(t.title, e.type === t.id, e.type === t.id, function () {
          if (!e.isBuiltIn) { e.type = t.id; render(); }
        });
      })),
      el("div", { class: "section-title" }, ["Типы позиций"]),
      el("div", { class: "chips" }, D.POSITION_TYPES.map(function (t) {
        const on = e.positions.indexOf(t.id) >= 0;
        return chip(t.title, on, false, function () {
          if (e.isBuiltIn) return;
          if (on) e.positions = e.positions.filter(function (id) { return id !== t.id; });
          else e.positions.push(t.id);
          render();
        });
      })),
      el("div", { class: "switch-row" }, [
        el("span", { class: "grow body-lg" }, ["Активен"]),
        el("button", {
          class: "switch" + (e.enabled ? " on" : ""), type: "button",
          onClick: function () { e.enabled = !e.enabled; render(); }
        })
      ]),
      el("button", {
        class: "btn btn-primary", type: "button", onClick: function () {
          const body = e.text.trim();
          if (!body || !e.competenceId || !e.positions.length) {
            toast("Заполните текст, компетенцию и типы позиций");
            return;
          }
          const id = e.id || ("user_" + uid());
          saveUserQuestion({
            id: id,
            text: body,
            competenceId: e.competenceId,
            questionType: e.type,
            positionTypes: e.positions.slice(),
            isBuiltIn: false,
            isEnabled: e.enabled,
            sortKey: 10000
          });
          go("bank");
        }
      }, ["Сохранить"])
    ];
  }

  function viewCompetences() {
    const items = getCompetences();
    const nodes = [
      backLink("settings"),
      pageHeader("Компетенции"),
      el("div", { class: "field-wrap", style: "margin:0;" }, [
        el("label", { class: "field-label" }, ["Новая компетенция"]),
        el("input", {
          class: "field", type: "text", value: state.newCompetenceName,
          onInput: function (e) { state.newCompetenceName = e.target.value; }
        })
      ]),
      el("button", {
        class: "btn btn-primary", type: "button", onClick: function () {
          const name = state.newCompetenceName.trim();
          if (!name) return;
          const extra = loadJson(KEYS.competences, []);
          extra.push({ id: "user_comp_" + uid(), name: name, isBuiltIn: false, isEnabled: true });
          saveJson(KEYS.competences, extra);
          state.newCompetenceName = "";
          render();
        }
      }, ["Добавить"])
    ];
    items.forEach(function (item) {
      nodes.push(el("div", { class: "card" }, [
        el("div", { class: "row center" }, [
          el("div", { class: "grow row center" }, [
            icon("quiz", "ui-icon bank-card-icon"),
            el("div", { class: "grow", style: "margin-left:8px;" }, [
              el("div", { class: "section-title" }, [item.name]),
              el("p", { class: "body-sm" }, [item.isBuiltIn ? "Встроенная компетенция для оценки ответа." : "Пользовательская компетенция"])
            ])
          ]),
          item.isBuiltIn ? null : el("button", {
            class: "btn-text", type: "button", onClick: function () {
              const extra = loadJson(KEYS.competences, []).filter(function (c) { return c.id !== item.id; });
              saveJson(KEYS.competences, extra);
              render();
            }
          }, ["Удалить"])
        ])
      ]));
    });
    return nodes;
  }

  function navBar() {
    return el("nav", { class: "bottom-nav phone-bottom-nav", "aria-label": "Основное меню" }, TABS.map(function (tab) {
      const selected = state.route === tab.route;
      const icons = { home: "home", setup: "notes", history: "history", settings: "settings" };
      return el("button", {
        class: "nav-item" + (selected ? " selected" : ""),
        type: "button",
        onClick: function () { go(tab.route); }
      }, [
        el("span", { class: "nav-mark" }),
        el("span", { class: selected ? "nav-pill" : "" }, [icon(icons[tab.route], "nav-icon")]),
        el("span", null, [tab.label])
      ]);
    }));
  }

  function render(opts) {
    const keepScroll = !(opts && opts.resetScroll);
    const prevScroll = root.querySelector(".session-main") || root.querySelector(".phone-content") || root.querySelector(".scroll");
    const y = prevScroll && keepScroll && state.route === "session" ? prevScroll.scrollTop : 0;
    applyTheme();
    const accepted = loadJson(KEYS.agreement, false);
    const route = accepted ? state.route : "agreement";
    if (!accepted) state.route = "agreement";

    if (route === "bank_new" && !state.editor) {
      state.editor = {
        id: null, text: "", competenceId: getCompetences()[0].id, type: "SITUATIONAL",
        positions: ["MANAGER", "SPECIALIST", "WORKER"], enabled: true, isBuiltIn: false
      };
      state.editQuestionId = null;
    }
    if (route === "bank_edit") {
      const q = getQuestions().find(function (x) { return x.id === state.editQuestionId; });
      if (q && (!state.editor || state.editor.id !== q.id)) {
        state.editor = {
          id: q.id, text: q.text, competenceId: q.competenceId, type: q.questionType,
          positions: q.positionTypes.slice(), enabled: q.isEnabled, isBuiltIn: q.isBuiltIn
        };
      }
    }

    const hasNav = showBar() && route !== "agreement";
    const shell = el("div", {
      class: "app-shell"
        + (hasNav ? " has-nav" : " no-nav")
        + (route === "session" ? " has-session" : "")
        + (route === "bank" ? " has-fab" : "")
    });
    shell.appendChild(appBrand(route === "session"));
    const scroll = el("div", {
      class: "scroll phone-content"
        + (route === "session" ? " session-scroll" : "")
        + (route === "history" ? " tight-top" : "")
        + (route === "setup" ? " is-setup" : "")
    });

    let content = [];
    if (route === "agreement") content = viewAgreement();
    else if (route === "home") content = viewHome();
    else if (route === "setup") content = viewSetup();
    else if (route === "session") content = viewSession();
    else if (route === "result") content = viewResult();
    else if (route === "history") content = viewHistory();
    else if (route === "detail") content = viewDetail();
    else if (route === "settings") content = viewSettings();
    else if (route === "bank") content = viewBank();
    else if (route === "bank_new" || route === "bank_edit") content = viewQuestionEditor();
    else if (route === "competences") content = viewCompetences();
    else if (route === "terms") content = viewLegal(D.AGREEMENT_TITLE, D.AGREEMENT_BODY);
    else if (route === "privacy") content = viewLegal(D.PRIVACY_TITLE, D.PRIVACY_BODY);
    else content = viewHome();

    content.forEach(function (n) { if (n) scroll.appendChild(n); });
    shell.appendChild(scroll);

    if (route === "bank") {
      shell.appendChild(el("button", {
        class: "fab", type: "button", title: "Добавить вопрос", onClick: function () {
          state.editor = {
            id: null, text: "", competenceId: getCompetences()[0].id, type: "SITUATIONAL",
            positions: ["MANAGER", "SPECIALIST", "WORKER"], enabled: true, isBuiltIn: false
          };
          go("bank_new");
        }
      }, ["+"]));
    }

    if (showBar() && route !== "agreement") shell.appendChild(navBar());

    if (state.toast) shell.appendChild(el("div", { class: "toast", role: "status" }, [state.toast]));

    if (state.dialog === "clear") {
      shell.appendChild(el("div", { class: "dialog-backdrop" }, [
        el("div", { class: "dialog" }, [
          el("h3", null, ["Удалить историю?"]),
          el("p", null, ["Все сохранённые интервью, оценки и комментарии будут удалены с устройства. Банк вопросов останется."]),
          el("div", { class: "dialog-actions" }, [
            el("button", { class: "btn-text", type: "button", onClick: function () { state.dialog = null; render(); } }, ["Отмена"]),
            el("button", {
              class: "btn-text", type: "button", onClick: function () {
                saveJson(KEYS.history, []);
                state.dialog = null;
                render();
              }
            }, ["Удалить"])
          ])
        ])
      ]));
    }

    root.innerHTML = "";
    root.appendChild(shell);

    if (route === "session") {
      const ta = document.getElementById("hr-comment");
      if (ta && state.session) {
        const draft = state.session.drafts[state.session.currentIndex];
        if (draft) ta.value = draft.comment;
      }
    }
    if (opts && opts.resetScroll) {
      window.scrollTo(0, 0);
      scroll.scrollTop = 0;
      const main = root.querySelector(".session-main");
      if (main) main.scrollTop = 0;
    } else if (route === "session" && keepScroll) {
      const main = root.querySelector(".session-main");
      if (main) main.scrollTop = y;
      else scroll.scrollTop = y;
    }
  }

  if (mediaDark && mediaDark.addEventListener) {
    mediaDark.addEventListener("change", function () {
      if (state.theme === "SYSTEM") { applyTheme(); render(); }
    });
  }

  applyTheme();
  render();

  (function initPresentation() {
    const reduce = window.matchMedia && window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    function showDemo() {
      document.body.classList.remove("is-landing", "is-leaving");
      if (reduce) {
        document.body.classList.remove("is-entering");
        document.body.classList.add("is-demo");
        return;
      }
      document.body.classList.add("is-entering");
      window.setTimeout(function () {
        document.body.classList.remove("is-entering");
        document.body.classList.add("is-demo");
      }, 500);
    }
    function showLanding() {
      document.body.classList.remove("is-demo", "is-entering");
      if (reduce) {
        document.body.classList.remove("is-leaving");
        document.body.classList.add("is-landing");
        return;
      }
      document.body.classList.add("is-leaving");
      window.setTimeout(function () {
        document.body.classList.remove("is-leaving");
        document.body.classList.add("is-landing");
      }, 500);
    }
    window.HR_PRESENTATION = { enter: showDemo, leave: showLanding };
    const btn = document.getElementById("enter-demo");
    if (btn) btn.addEventListener("click", showDemo);
  })();
})();
