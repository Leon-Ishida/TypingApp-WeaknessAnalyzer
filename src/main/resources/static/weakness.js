// ── 弱点分析ページ ────────────────────────────────────────────────────────────

(async function loadWeakness() {
  const loading   = document.getElementById('weakness-loading');
  const content   = document.getElementById('weakness-content');
  const errorEl   = document.getElementById('weakness-error');

  try {
    const res = await fetch('/api/practice/weakness');

    if (res.status === 404) {
      showWeaknessError(errorEl, loading, 'テスト結果がまだありません。先にテストを受けてください。');
      return;
    }
    if (!res.ok) throw new Error(`サーバーエラー: ${res.status}`);

    const data = await res.json();

    renderMistakeList('sub-list',   data.topSub,   'weakness-item-sub',   formatSubPair);
    renderMistakeList('trans-list', data.topTrans,  'weakness-item-trans', formatTransPair);
    renderMistakeList('del-list',   data.topDel,    'weakness-item-del',   formatDelChar);
    renderMistakeList('ins-list',   data.topIns,    'weakness-item-ins',   formatInsPair);

    loading.style.display = 'none';
    content.style.display = 'block';

  } catch (e) {
    showWeaknessError(errorEl, loading, e.message || '弱点分析の取得に失敗しました');
  }
})();

// ── ミスリストの描画 ──────────────────────────────────────────────────────────

function renderMistakeList(containerId, mistakeMap, itemClass, formatter) {
  const container = document.getElementById(containerId);
  container.innerHTML = '';

  const entries = Object.entries(mistakeMap);

  if (entries.length === 0) {
    const empty = document.createElement('p');
    empty.className = 'weakness-empty';
    empty.textContent = 'このタイプのミスはありません';
    container.appendChild(empty);
    return;
  }

  for (const [key, count] of entries) {
    const item = document.createElement('div');
    item.className = `weakness-item ${itemClass}`;

    const pair = document.createElement('span');
    pair.className = 'weakness-pair';
    pair.textContent = formatter(key);

    const countEl = document.createElement('span');
    countEl.className = 'weakness-count';
    countEl.textContent = `${count}回`;

    item.append(pair, countEl);
    container.appendChild(item);
  }
}

// ── フォーマッタ ──────────────────────────────────────────────────────────────

function formatSubPair(key) {
  // JSON key: "SubstitutionPair[expected=e, actual=i]" or object {expected, actual}
  if (typeof key === 'string') {
    // Map のキーが文字列化される場合をパース
    const m = key.match(/expected=(.),\s*actual=(.)/);
    if (m) return `${m[1]} → ${m[2]}`;
    return key;
  }
  return `${key.expected} → ${key.actual}`;
}

function formatTransPair(key) {
  if (typeof key === 'string') {
    const m = key.match(/char1=(.),\s*char2=(.)/);
    if (m) return `${m[1]} ↔ ${m[2]}`;
    return key;
  }
  return `${key.char1} ↔ ${key.char2}`;
}

function formatDelChar(key) {
  return `-${key}`;
}

function formatInsPair(key) {
  console.log("key:", key);
  if (typeof key === 'string') {
    const m = key.match(/beforeChar=(.*?),\s*afterChar=(.*?),\s*insertionChar=(.)/);
    if (m) {
      const before = (m[1] === 'null') ? '' : m[1];
      const after  = (m[2] === 'null') ? '' : m[2];
      return `+${m[3]}(${before}_${after})`;
    }
    return key;
  }
  const before = key.beforeChar === null ? '' : key.beforeChar;
  const after  = key.afterChar  === null ? '' : key.afterChar;
  return `+${key.insertionChar}(${before}_${after})`;
}

// ── エラー表示 ────────────────────────────────────────────────────────────────

function showWeaknessError(errorEl, loading, msg) {
  loading.style.display = 'none';
  errorEl.textContent = msg;
  errorEl.style.display = 'block';
}
