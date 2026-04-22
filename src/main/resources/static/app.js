// ── 状態変数 ──────────────────────────────────────────────────────────────────
let words        = [];   // GET /api/test/start で取得した出題単語リスト
let startedAt    = 0;    // サーバーから受け取ったエポックミリ秒
let currentIndex = 0;    // 現在何問目か（0始まり）
let rawResult    = {};   // { word: answer } 挿入順オブジェクト
let timerInterval = null;
let totalPauseMs = 0;    // インターバルの合計時間（WPM計算から除外）
const INTERVAL_MS = 800; // Enter後のフィードバック表示時間（ミリ秒）

// ── ユーティリティ ────────────────────────────────────────────────────────────

function showScreen(id) {
  document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
  document.getElementById(id).classList.add('active');
}

function showError(msg) {
  const el = document.getElementById('error-banner');
  el.textContent = msg;
  el.style.display = 'block';
}

function hideError() {
  document.getElementById('error-banner').style.display = 'none';
}

function formatElapsed(ms) {
  const s = Math.floor(ms / 1000);
  const m = Math.floor(s / 60);
  return String(m).padStart(2, '0') + ':' + String(s % 60).padStart(2, '0');
}

// ── スタート画面 → テスト開始 ─────────────────────────────────────────────────

async function startTest() {
  const btn = document.getElementById('btn-start');
  btn.disabled = true;
  btn.innerHTML = '<span class="spinner"></span>読み込み中...';
  hideError();

  try {
    const res = await fetch('/api/test/start');
    if (!res.ok) throw new Error(`サーバーエラー: ${res.status}`);
    const data = await res.json();

    words        = data.words;
    startedAt    = data.startedAt;
    currentIndex = 0;
    rawResult    = {};
    totalPauseMs = 0;

    showScreen('screen-test');
    renderQuestion();
    startElapsedTimer();

  } catch (e) {
    showError(e.message || 'サーバーに接続できませんでした');
  } finally {
    btn.disabled = false;
    btn.textContent = 'テスト開始';
  }
}

// ── テスト画面 ────────────────────────────────────────────────────────────────

function renderQuestion() {
  const total = words.length;

  document.getElementById('word-display').textContent = words[currentIndex];
  document.getElementById('progress-label').textContent =
    `${currentIndex + 1} / ${total}`;
  document.getElementById('progress-fill').style.width =
    `${(currentIndex / total) * 100}%`;

  const input = document.getElementById('typing-input');
  input.value = '';
  input.focus();
}

function handleKeydown(e) {
  if (e.key !== 'Enter') return;
  e.preventDefault();

  const input = document.getElementById('typing-input');
  const answer = input.value;
  // 空文字での Enter は無視
  if (answer.trim() === '') return;

  // インターバル中の Enter は無視
  if (input.disabled) return;

  const word = words[currentIndex];
  rawResult[word] = answer;
  currentIndex++;

  // 正誤フィードバック表示
  const isCorrect = (answer === word);
  input.disabled = true;
  input.classList.remove('correct', 'wrong');
  input.classList.add(isCorrect ? 'correct' : 'wrong');

  const pauseStart = Date.now();

  setTimeout(() => {
    totalPauseMs += (Date.now() - pauseStart);
    input.disabled = false;
    input.classList.remove('correct', 'wrong');

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
    document.getElementById('elapsed-label').textContent =
      formatElapsed(Date.now() - startedAt);
  }, 500);
}

// ── POST /api/test/submit ──────────────────────────────────────────────────────

async function submitTest() {
  clearInterval(timerInterval);

  const usedTimeMillis = Date.now() - startedAt - totalPauseMs;

  // 二重送信防止
  document.getElementById('typing-input').disabled = true;

  try {
    const res = await fetch('/api/test/submit', {
      method:  'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ rawResult, usedTimeMillis })
    });
    if (!res.ok) throw new Error(`サーバーエラー: ${res.status}`);
    const data = await res.json();
    renderResult(data);

  } catch (e) {
    // submit失敗時はスタート画面に戻してエラー表示
    goToStart();
    showError('結果の送信に失敗しました: ' + (e.message || '不明なエラー'));
  }
}

// ── 結果画面 ──────────────────────────────────────────────────────────────────

// MistakeTypeの日本語名とCSSクラスの対応
const MISTAKE_TYPE_MAP = {
  SUBSTITUTION: { label: '置換', cls: 'mistake-sub'   },
  TRANSPOSITION: { label: '交換', cls: 'mistake-trans' },
  DELETION:     { label: '削除', cls: 'mistake-del'   },
  INSERTION:    { label: '挿入', cls: 'mistake-ins'   },
};

/**
 * MistakeDetailを人間が読める文字列に変換する
 * 置換: e→i  交換: el↔le  削除: -l  挿入: +w(e→l間)
 */
function formatMistakeDetail(mistake) {
  const type = mistake.mistakeType;

  // null文字（'\0', charCode 0）を空文字として扱う
  const ch = c => (c && c.charCodeAt(0) !== 0) ? c : '';

  switch (type) {
    case 'SUBSTITUTION':
      return `${ch(mistake.expected)}→${ch(mistake.actual)}`;
    case 'TRANSPOSITION':
      return `${ch(mistake.expected)}↔${ch(mistake.actual)}`;
    case 'DELETION':
      return `-${ch(mistake.expected)}`;
    case 'INSERTION': {
      const ins = ch(mistake.insertion);
      const before = ch(mistake.expected);
      const after  = ch(mistake.actual);
      // 挿入位置の前後を括弧で補足
      const pos = (before || after) ? `(${before}_${after})` : '';
      return `+${ins}${pos}`;
    }
    default:
      return type;
  }
}

function renderResult(data) {
  // WPM・正答率
  document.getElementById('result-wpm').textContent = data.wpm.toFixed(1);
  document.getElementById('result-acc').textContent =
    (data.accuracy * 100).toFixed(1);

  // 単語ごとの結果テーブル
  const tbody = document.getElementById('result-tbody');
  tbody.innerHTML = '';

  for (const [word, wordResult] of Object.entries(data.results)) {
    const isCorrect = wordResult.mistakes.length === 0;
    const tr = document.createElement('tr');

    // 問題
    const tdWord = document.createElement('td');
    tdWord.style.fontFamily = 'var(--mono)';
    tdWord.style.color = 'var(--bright)';
    tdWord.textContent = word;

    // 解答
    const tdAnswer = document.createElement('td');
    tdAnswer.style.fontFamily = 'var(--mono)';
    tdAnswer.className = isCorrect ? 'answer-correct' : 'answer-wrong';
    tdAnswer.textContent = wordResult.answer;

    // 判定バッジ
    const tdJudge = document.createElement('td');
    const badge = document.createElement('span');
    badge.className = isCorrect ? 'badge badge-correct' : 'badge badge-wrong';
    badge.textContent = isCorrect ? 'OK' : 'NG';
    tdJudge.appendChild(badge);

    // ミス詳細
    const tdMistakes = document.createElement('td');
    if (isCorrect) {
      tdMistakes.style.color = 'var(--muted)';
      tdMistakes.style.fontFamily = 'var(--mono)';
      tdMistakes.textContent = '—';
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
    tbody.appendChild(tr);
  }

  showScreen('screen-result');
}

// ── スタート画面に戻る ────────────────────────────────────────────────────────

function goToStart() {
  clearInterval(timerInterval);
  document.getElementById('typing-input').disabled = false;
  showScreen('screen-start');
}