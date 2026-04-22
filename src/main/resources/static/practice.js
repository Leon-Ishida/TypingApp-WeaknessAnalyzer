// ── 練習テストページ ──────────────────────────────────────────────────────────

let words        = [];
let currentIndex = 0;
let rawResult    = {};
let startedAt    = 0;
let timerInterval = null;
let totalPauseMs = 0;
let selectedMode = null;
const INTERVAL_MS = 800;

// ── ユーティリティ ────────────────────────────────────────────────────────────

function showScreen(id) {
  document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
  document.getElementById(id).classList.add('active');
}

function formatElapsed(ms) {
  const s = Math.floor(ms / 1000);
  const m = Math.floor(s / 60);
  return String(m).padStart(2, '0') + ':' + String(s % 60).padStart(2, '0');
}

// ── モード選択 ────────────────────────────────────────────────────────────────

document.getElementById('mode-weakness').addEventListener('click', () => selectAndStart('weakness'));
document.getElementById('mode-frequent').addEventListener('click', () => selectAndStart('frequent'));

async function selectAndStart(mode) {
  selectedMode = mode;
  const errorEl = document.getElementById('select-error');
  errorEl.style.display = 'none';

  // カードのハイライト
  document.querySelectorAll('.mode-card').forEach(c => c.classList.remove('selected'));
  document.getElementById('mode-' + mode).classList.add('selected');

  try {
    const res = await fetch('/api/practice/start');
    if (res.status === 404) {
      errorEl.textContent = 'テスト結果がまだありません。先に通常テストを受けてください。';
      errorEl.style.display = 'block';
      return;
    }
    if (!res.ok) throw new Error(`サーバーエラー: ${res.status}`);

    const data = await res.json();

    if (mode === 'weakness') {
      words = data.weaknessWords;
    } else {
      words = data.frequentMistakeWords;
    }

    currentIndex = 0;
    rawResult    = {};
    totalPauseMs = 0;
    startedAt    = Date.now();

    document.getElementById('mode-label').textContent =
      mode === 'weakness' ? '傾向ベース' : '頻度ベース';

    showScreen('screen-practice');
    renderQuestion();
    startElapsedTimer();

  } catch (e) {
    errorEl.textContent = e.message || '練習データの取得に失敗しました';
    errorEl.style.display = 'block';
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
  if (answer.trim() === '') return;
  if (input.disabled) return;

  const word = words[currentIndex];
  rawResult[word] = answer;
  currentIndex++;

  // 正誤フィードバック
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
      submitPractice();
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

// ── 結果送信（分析のみ、保存しない）──────────────────────────────────────────

async function submitPractice() {
  clearInterval(timerInterval);

  const usedTimeMillis = Date.now() - startedAt - totalPauseMs;
  document.getElementById('typing-input').disabled = true;

  try {
    const res = await fetch('/api/analyze', {
      method:  'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ rawResult, usedTimeMillis })
    });
    if (!res.ok) throw new Error(`サーバーエラー: ${res.status}`);
    const data = await res.json();
    renderResult(data);

  } catch (e) {
    // エラー時はモード選択画面に戻す
    showScreen('screen-select');
    const errorEl = document.getElementById('select-error');
    errorEl.textContent = '結果の送信に失敗しました: ' + (e.message || '不明なエラー');
    errorEl.style.display = 'block';
  }
}

// ── 結果画面 ──────────────────────────────────────────────────────────────────

const MISTAKE_TYPE_MAP = {
  SUBSTITUTION:  { label: '置換', cls: 'mistake-sub'   },
  TRANSPOSITION: { label: '交換', cls: 'mistake-trans' },
  DELETION:      { label: '削除', cls: 'mistake-del'   },
  INSERTION:     { label: '挿入', cls: 'mistake-ins'   },
};

function formatMistakeDetail(mistake) {
  const type = mistake.mistakeType;
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
      const pos = (before || after) ? `(${before}_${after})` : '';
      return `+${ins}${pos}`;
    }
    default:
      return type;
  }
}

function renderResult(data) {
  document.getElementById('result-wpm').textContent = data.wpm.toFixed(1);
  document.getElementById('result-acc').textContent =
    (data.accuracy * 100).toFixed(1);

  const tbody = document.getElementById('result-tbody');
  tbody.innerHTML = '';

  for (const [word, wordResult] of Object.entries(data.results)) {
    const isCorrect = wordResult.mistakes.length === 0;
    const tr = document.createElement('tr');

    const tdWord = document.createElement('td');
    tdWord.style.fontFamily = 'var(--mono)';
    tdWord.style.color = 'var(--bright)';
    tdWord.textContent = word;

    const tdAnswer = document.createElement('td');
    tdAnswer.style.fontFamily = 'var(--mono)';
    tdAnswer.className = isCorrect ? 'answer-correct' : 'answer-wrong';
    tdAnswer.textContent = wordResult.answer;

    const tdJudge = document.createElement('td');
    const badge = document.createElement('span');
    badge.className = isCorrect ? 'badge badge-correct' : 'badge badge-wrong';
    badge.textContent = isCorrect ? 'OK' : 'NG';
    tdJudge.appendChild(badge);

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
