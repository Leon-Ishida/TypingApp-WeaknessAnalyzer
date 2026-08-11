// ── 設定と状態管理 ──────────────────────────────────────────────────────────
const API_CONFIG = {
  ENDPOINTS: {
    TEST_START: '/test/start',
    TEST_SUBMIT: '/test/results',
    ANALYZE: '/aggregate',
    PRACTICE_START: '/practice/start'
  },
  DTO_KEYS: {
    REQ_START_DATE: 'startDate',
    REQ_END_DATE: 'lastDate',
    PRACTICE_REQ_MODE: 'mode'
  }
};

let isLoggedIn = false;
let sessionTestResult = null;
let isPracticeMode = false;
let words = [];
let startedAt = 0;
let currentIndex = 0;
let rawResult = {};
let timerInterval = null;
let totalPauseMs = 0;
const INTERVAL_MS = 800;

let wpmChart = null;
let accChart = null;

let elements = {};

const MISTAKE_TYPE_MAP = {
  SUBSTITUTION: { label: '置換', cls: 'mistake-sub', color: '#fef08a' },
  TRANSPOSITION: { label: '交換', cls: 'mistake-trans', color: '#fed7aa' },
  DELETION: { label: '削除', cls: 'mistake-del', color: '#fecaca' },
  INSERTION: { label: '挿入', cls: 'mistake-ins', color: '#bfdbfe' }
};

const TRENDS_MAP = {
  topSub: { label: '置換', cls: 'mistake-sub', color: '#fef08a' },
  topTrans: { label: '交換', cls: 'mistake-trans', color: '#fed7aa' },
  topDel: { label: '削除', cls: 'mistake-del', color: '#fecaca' },
  topIns: { label: '挿入', cls: 'mistake-ins', color: '#bfdbfe' }
};

// ── 初期化 ────────────────────────────────────────────────────────────────────
function init() {
  // DOM要素の取得を関数の内部に移動し、null参照エラーを完全に排除する
  elements = {
    navBtns: document.querySelectorAll('.nav-btn'),
    views: document.querySelectorAll('.view'),
    authStatus: document.getElementById('auth-status'),
    btnToggleLogin: document.getElementById('btn-toggle-login'),

    testStartUI: document.getElementById('test-start-ui'),
    testActiveUI: document.getElementById('test-active-ui'),
    testResultUI: document.getElementById('test-result-ui'),
    btnStartTest: document.getElementById('btn-start-test'),
    wordDisplay: document.getElementById('test-word-display'),
    testInput: document.getElementById('test-input'),
    testProgressFill: document.getElementById('test-progress-fill'),
    testProgressLabel: document.getElementById('test-progress-label'),
    testElapsedLabel: document.getElementById('test-elapsed-label'),
    resWpm: document.getElementById('res-wpm'),
    resAcc: document.getElementById('res-acc'),
    resTbody: document.getElementById('res-tbody'),
    btnRetryTest: document.getElementById('btn-retry-test'),
    btnGoAnalysis: document.getElementById('btn-go-analysis'),
    guestWarning: document.getElementById('guest-warning'),

    analysisStartDate: document.getElementById('analysis-start-date'),
    analysisEndDate: document.getElementById('analysis-end-date'),
    btnLoadAnalysis: document.getElementById('btn-load-analysis'),
    analysisAuthNote: document.getElementById('analysis-auth-note'),
    analysisContent: document.getElementById('analysis-content'),
    analysisEmpty: document.getElementById('analysis-empty'),
    mistakeTopsContainer: document.getElementById('mistake-tops-container'),
    btnAnalysisToPractice: document.getElementById('btn-analysis-to-practice'),

    practiceSetupUI: document.getElementById('practice-setup-ui'),
    practiceStartDate: document.getElementById('practice-start-date'),
    practiceEndDate: document.getElementById('practice-end-date'),
    practiceAuthNote: document.getElementById('practice-auth-note'),
    btnStartPractice: document.getElementById('btn-start-practice')
  };

  elements.navBtns.forEach(btn => btn.addEventListener('click', () => switchView(btn.dataset.target)));
  elements.btnToggleLogin.addEventListener('click', toggleLogin);
  
  elements.btnStartTest.addEventListener('click', () => startTestSession(API_CONFIG.ENDPOINTS.TEST_START));
  elements.testInput.addEventListener('keydown', handleKeydown);
  elements.btnRetryTest.addEventListener('click', resetTestView);
  elements.btnGoAnalysis.addEventListener('click', () => switchView('view-analysis'));
  
  elements.btnLoadAnalysis.addEventListener('click', loadAnalysis);
  elements.btnAnalysisToPractice.addEventListener('click', () => switchView('view-practice'));

  elements.btnStartPractice.addEventListener('click', startPracticeSession);

  const today = new Date().toISOString().split('T')[0];
  const lastWeek = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];
  elements.analysisStartDate.value = lastWeek;
  elements.analysisEndDate.value = today;
  elements.practiceStartDate.value = lastWeek;
  elements.practiceEndDate.value = today;

  updateAuthUI();
}

function switchView(targetId) {
  elements.navBtns.forEach(btn => {
    btn.classList.toggle('nav-active', btn.dataset.target === targetId);
  });
  elements.views.forEach(view => {
    view.classList.toggle('active', view.id === targetId);
  });
  
  resetTestView();
  
  if (targetId === 'view-analysis') {
    loadAnalysis();
  }
  
  if (targetId === 'view-practice') {
    if (elements.practiceSetupUI) elements.practiceSetupUI.classList.remove('hidden');
  }
}

function toggleLogin() {
  isLoggedIn = !isLoggedIn;
  updateAuthUI();
  if (isLoggedIn && sessionTestResult) {
    sessionTestResult = null;
  }
  if (elements.views[1] && elements.views[1].classList.contains('active')) {
    loadAnalysis();
  }
}

function updateAuthUI() {
  elements.authStatus.textContent = isLoggedIn ? 'ログイン済み' : '未ログイン';
  elements.btnToggleLogin.textContent = isLoggedIn ? 'ログアウト' : 'ログインする';
  elements.guestWarning.style.display = isLoggedIn ? 'none' : 'block';
  
  const dateInputs = [
    elements.analysisStartDate, elements.analysisEndDate,
    elements.practiceStartDate, elements.practiceEndDate
  ];
  
  dateInputs.forEach(input => { input.disabled = !isLoggedIn; });
  elements.btnLoadAnalysis.disabled = !isLoggedIn;
  
  if (!isLoggedIn) {
    elements.analysisAuthNote.classList.remove('hidden');
    elements.practiceAuthNote.classList.remove('hidden');
  } else {
    elements.analysisAuthNote.classList.add('hidden');
    elements.practiceAuthNote.classList.add('hidden');
  }
}

function formatElapsed(ms) {
  const s = Math.floor(ms / 1000);
  const m = Math.floor(s / 60);
  return String(m).padStart(2, '0') + ':' + String(s % 60).padStart(2, '0');
}

function formatMistakeDetail(mistake) {
  const type = mistake.mistakeType;
  const ch = c => (c && c.charCodeAt(0) !== 0) ? c : '';

  switch (type) {
    case 'SUBSTITUTION': return `${ch(mistake.expected)}→${ch(mistake.actual)}`;
    case 'TRANSPOSITION': return `${ch(mistake.expected)}↔${ch(mistake.actual)}`;
    case 'DELETION': return `-${ch(mistake.expected)}`;
    case 'INSERTION':
      const ins = ch(mistake.insertion);
      const before = ch(mistake.expected);
      const actual = ch(mistake.actual);
      const pos = (before || actual) ? `(${before}_${actual})` : '';
      return `+${ins}${pos}`;
    default: return type;
  }
}

// JavaのRecordやクラスがシリアライズされたキー文字列をクリーンアップする関数
function formatMistakeKey(keyString) {
  if (!keyString) return '';
  return keyString
    .replace(/^[a-zA-Z]+Pair\[/, '')
    .replace(/\]$/, '')
    .replace(/expected=/, '正:')
    .replace(/actual=/, '誤:')
    .replace(/insertion=/, '挿入:');
}

function resetTestView() {
  elements.testStartUI.classList.remove('hidden');
  elements.testActiveUI.classList.add('hidden');
  elements.testResultUI.classList.add('hidden');
  if(elements.practiceSetupUI) elements.practiceSetupUI.classList.remove('hidden');
  
  elements.testInput.value = '';
  elements.testInput.disabled = false;
  elements.testInput.classList.remove('correct', 'wrong');
}

// ── テスト・練習 実行エンジン ────────────────────────────────────────────────
async function startTestSession(url, method = 'GET', body = null) {
  isPracticeMode = (url === API_CONFIG.ENDPOINTS.PRACTICE_START);
  try {
    const options = { method };
    if (body) {
      options.headers = { 'Content-Type': 'application/json' };
      options.body = JSON.stringify(body);
    }
    
    const res = await fetch(url, options);
    if (!res.ok) throw new Error(`HTTP status ${res.status}`);
    const data = await res.json();
    
    if (Array.isArray(data)) {
      words = data;
      startedAt = Date.now();
    } else if (data && data.words && Array.isArray(data.words)) {
      words = data.words;
      startedAt = data.startedAt || Date.now();
    } else {
      console.warn("Unexpected API response format:", data);
      words = [];
      startedAt = Date.now();
    }
    
    currentIndex = 0;
    rawResult = {};
    totalPauseMs = 0;

    elements.testStartUI.classList.add('hidden');
    if(elements.practiceSetupUI) elements.practiceSetupUI.classList.add('hidden');
    elements.testResultUI.classList.add('hidden');
    elements.testActiveUI.classList.remove('hidden');
    
    elements.testInput.disabled = false;
    elements.testInput.classList.remove('correct', 'wrong');

    renderQuestion();
    startElapsedTimer();
  } catch (e) {
    alert(`通信エラーが発生しました。サーバー（${url}）が起動しているか、またはURL・キー名が一致しているか確認してください。`);
    console.error(e);
  }
}

async function startPracticeSession() {
  const reqBody = {};
  if (elements.practiceStartDate.value) reqBody[API_CONFIG.DTO_KEYS.REQ_START_DATE] = elements.practiceStartDate.value;
  if (elements.practiceEndDate.value) reqBody[API_CONFIG.DTO_KEYS.REQ_END_DATE] = elements.practiceEndDate.value;
  reqBody[API_CONFIG.DTO_KEYS.PRACTICE_REQ_MODE] = document.querySelector('input[name="practice_mode"]:checked').value;

  const body = isLoggedIn ? reqBody : { [API_CONFIG.DTO_KEYS.PRACTICE_REQ_MODE]: reqBody[API_CONFIG.DTO_KEYS.PRACTICE_REQ_MODE] };
  startTestSession(API_CONFIG.ENDPOINTS.PRACTICE_START, 'POST', body);
}

function renderQuestion() {
  const total = words.length;
  if (total === 0) {
      alert("出題データがありません。");
      resetTestView();
      return;
  }
  elements.wordDisplay.textContent = words[currentIndex];
  elements.testProgressLabel.textContent = `${currentIndex + 1} / ${total}`;
  elements.testProgressFill.style.width = `${(currentIndex / total) * 100}%`;
  elements.testInput.value = '';
  elements.testInput.focus();
}

function handleKeydown(e) {
  if (e.key !== 'Enter') return;
  e.preventDefault();

  const answer = elements.testInput.value;
  if (answer.trim() === '') return;
  if (elements.testInput.disabled) return;

  const word = words[currentIndex];
  rawResult[word] = answer;
  currentIndex++;

  const isCorrect = (answer === word);
  elements.testInput.disabled = true;
  elements.testInput.classList.remove('correct', 'wrong');
  elements.testInput.classList.add(isCorrect ? 'correct' : 'wrong');

  const pauseStart = Date.now();

  setTimeout(() => {
    totalPauseMs += (Date.now() - pauseStart);
    elements.testInput.disabled = false;
    elements.testInput.classList.remove('correct', 'wrong');

    if (currentIndex === words.length) {
      submitTest();
    } else {
      renderQuestion();
    }
  }, INTERVAL_MS);
}

function startElapsedTimer() {
  clearInterval(timerInterval);
  timerInterval = setInterval(() => {
    elements.testElapsedLabel.textContent = formatElapsed(Date.now() - startedAt);
  }, 500);
}

async function submitTest() {
  clearInterval(timerInterval);
  const usedTimeMs = Date.now() - startedAt - totalPauseMs;
  elements.testInput.disabled = true;

  try {
    const res = await fetch(API_CONFIG.ENDPOINTS.TEST_SUBMIT, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ 
        rawResult: rawResult, 
        usedTimeMillis: usedTimeMs,
        isTest: !isPracticeMode
      }) 
    });
    if (!res.ok) throw new Error(`HTTP status ${res.status}`);
    const data = await res.json();
    
    if (!isLoggedIn) {
      sessionTestResult = data;
    }
    
    renderResult(data);
  } catch (e) {
    alert('結果の送信に失敗しました。');
    console.error(e);
    resetTestView();
  }
}

function renderResult(data) {
  elements.testActiveUI.classList.add('hidden');
  elements.testResultUI.classList.remove('hidden');

  if (isLoggedIn || isPracticeMode) {
    elements.guestWarning.classList.add('hidden');
  } else {
    elements.guestWarning.classList.remove('hidden');
  }

  elements.resWpm.textContent = (data.wpm || 0).toFixed(1);
  elements.resAcc.textContent = ((data.accuracy || 0) * 100).toFixed(1);
  elements.resTbody.innerHTML = '';
  
  if(!data.analyzedResult) return;

  for (const [word, wordResult] of Object.entries(data.analyzedResult)) {
    const isCorrect = !wordResult.mistakes || wordResult.mistakes.length === 0;
    const tr = document.createElement('tr');
    
    const tdWord = document.createElement('td');
    tdWord.className = 'py-3 px-4 mono font-bold text-gray-800';
    tdWord.textContent = word;
    
    const tdAnswer = document.createElement('td');
    tdAnswer.className = `py-3 px-4 mono ${isCorrect ? 'text-green-600' : 'text-red-600'}`;
    tdAnswer.textContent = wordResult.answer;
    
    const tdJudge = document.createElement('td');
    tdJudge.className = 'py-3 px-4';
    tdJudge.innerHTML = isCorrect 
      ? `<span class="bg-green-100 text-green-700 px-2 py-1 rounded text-xs font-bold">OK</span>`
      : `<span class="bg-red-100 text-red-700 px-2 py-1 rounded text-xs font-bold">NG</span>`;
      
    const tdMistakes = document.createElement('td');
    tdMistakes.className = 'py-3 px-4';
    if (isCorrect) {
      tdMistakes.textContent = '—';
      tdMistakes.classList.add('text-gray-400');
    } else {
      wordResult.mistakes.forEach(mistake => {
        const info = MISTAKE_TYPE_MAP[mistake.mistakeType];
        if (!info) return;
        const tag = document.createElement('span');
        tag.className = `mistake-tag ${info.cls}`;
        tag.textContent = `${info.label}: ${formatMistakeDetail(mistake)}`;
        tdMistakes.appendChild(tag);
      });
    }

    tr.append(tdWord, tdAnswer, tdJudge, tdMistakes);
    elements.resTbody.appendChild(tr);
  }
}

// ── 分析画面 ──────────────────────────────────────────────────────────────────
async function loadAnalysis() {
  const reqBody = {};
  if (elements.analysisStartDate.value) reqBody[API_CONFIG.DTO_KEYS.REQ_START_DATE] = elements.analysisStartDate.value;
  if (elements.analysisEndDate.value) reqBody[API_CONFIG.DTO_KEYS.REQ_END_DATE] = elements.analysisEndDate.value;
  
  const body = isLoggedIn ? reqBody : {};
  
  try {
    const res = await fetch(API_CONFIG.ENDPOINTS.ANALYZE, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });
    
    if (!res.ok) throw new Error(`HTTP status ${res.status}`);
    const data = await res.json();

    const scoreTrends = data.scoreTrends || [];
    const mistakeTrends = data.mistakeTrends || {};

    if (scoreTrends.length === 0) {
      elements.analysisContent.classList.add('hidden');
      elements.analysisEmpty.classList.remove('hidden');
      return;
    }

    elements.analysisEmpty.classList.add('hidden');
    elements.analysisContent.classList.remove('hidden');

    renderCharts(scoreTrends);
    renderTopMistakes(mistakeTrends);
  } catch (e) {
    console.error('分析データの取得に失敗しました。', e);
  }
}

function renderCharts(scoreTrends) {
  const labels = scoreTrends.map((_, i) => `Test ${i+1}`);
  
  if (wpmChart) wpmChart.destroy();
  const ctxWpm = document.getElementById('wpm-chart').getContext('2d');
  wpmChart = new Chart(ctxWpm, {
    type: 'line',
    data: {
      labels,
      datasets: [{
        label: 'WPM',
        data: scoreTrends.map(d => d.wpm || 0), 
        borderColor: '#3b82f6',
        backgroundColor: 'rgba(59, 130, 246, 0.1)',
        tension: 0.3,
        fill: true
      }]
    },
    options: { responsive: true, maintainAspectRatio: false }
  });

  if (accChart) accChart.destroy();
  const ctxAcc = document.getElementById('acc-chart').getContext('2d');
  accChart = new Chart(ctxAcc, {
    type: 'line',
    data: {
      labels,
      datasets: [{
        label: '正答率 (%)',
        data: scoreTrends.map(d => (d.accuracy || 0) * 100), 
        borderColor: '#10b981',
        backgroundColor: 'rgba(16, 185, 129, 0.1)',
        tension: 0.3,
        fill: true
      }]
    },
    options: { responsive: true, maintainAspectRatio: false, scales: { y: { min: 0, max: 100 } } }
  });
}

function renderTopMistakes(mistakesData) {
  elements.mistakeTopsContainer.innerHTML = '';
  
  // MistakeTopTrendsのMap構造（topSub, topTrans, topDel, topIns）に基づく反復処理
  Object.keys(TRENDS_MAP).forEach(mapKey => {
    const obj = mistakesData[mapKey] || {};
    const info = TRENDS_MAP[mapKey];
    
    // MapがJSON化されたオブジェクトを [key, count] の配列に変換してソート
    const sortedList = Object.entries(obj).sort((a, b) => b[1] - a[1]);
    
    const card = document.createElement('div');
    card.className = 'bg-gray-50 rounded-lg p-4 border border-gray-100';
    
    let html = `<div class="font-bold text-gray-700 mb-3 flex items-center gap-2">
      <span class="w-3 h-3 rounded-full" style="background-color: ${info.color}"></span>
      ${info.label}ミス
    </div>`;
    
    if (sortedList.length === 0) {
      html += `<p class="text-xs text-gray-400 text-center py-2">データなし</p>`;
    } else {
      html += `<ul class="space-y-2">`;
      // 上位3件のみを描画
      sortedList.slice(0, 3).forEach(([keyString, count], i) => {
        const displayStr = formatMistakeKey(keyString);
        html += `
          <li class="flex justify-between items-center text-sm">
            <span class="text-gray-500 font-medium">${i+1}. <span class="mono bg-white px-2 py-0.5 rounded border border-gray-200">${displayStr}</span></span>
            <span class="text-xs font-bold text-gray-400">${count}回</span>
          </li>`;
      });
      html += `</ul>`;
    }
    
    card.innerHTML = html;
    elements.mistakeTopsContainer.appendChild(card);
  });
}

// 確実なDOM生成後に初期化を実行
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', init);
} else {
  init();
}
