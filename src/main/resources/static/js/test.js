// test.html専用のロジック
document.addEventListener('DOMContentLoaded', () => {
    let words = [];
    let currentIndex = 0;
    let startedAt = 0;
    let rawResult = {};
    
    const ui = {
        start: document.getElementById('test-start-ui'),
        active: document.getElementById('test-active-ui'),
        result: document.getElementById('test-result-ui'),
        wordDisplay: document.getElementById('test-word-display'),
        input: document.getElementById('test-input'),
        progressLabel: document.getElementById('test-progress-label'),
        progressFill: document.getElementById('test-progress-fill'),
        resWpm: document.getElementById('res-wpm'),
        resAcc: document.getElementById('res-acc'),
        resTbody: document.getElementById('res-tbody')
    };

    document.getElementById('btn-start-test')?.addEventListener('click', async () => {
        try {
            // common.jsの API.get を使用
            const data = await API.get('/test/start');
            words = data.words;
            startedAt = data.startedAt;
            currentIndex = 0;
            rawResult = {};
            
            ui.start.classList.add('hidden');
            ui.result.classList.add('hidden');
            ui.active.classList.remove('hidden');
            
            showNextWord();
            ui.input.focus();
        } catch (e) {
            alert('テストの開始に失敗しました。');
        }
    });

    ui.input?.addEventListener('keydown', async (e) => {
        if (e.key === 'Enter' && ui.input.value.trim() !== '') {
            const currentWord = words[currentIndex];
            const answer = ui.input.value.trim();
            rawResult[currentWord] = answer;
            
            if (answer === currentWord) {
                ui.input.classList.add('correct');
                ui.input.classList.remove('wrong');
            } else {
                ui.input.classList.add('wrong');
                ui.input.classList.remove('correct');
            }
            
            ui.input.disabled = true;
            currentIndex++;
            
            setTimeout(() => {
                ui.input.disabled = false;
                ui.input.value = '';
                ui.input.classList.remove('correct', 'wrong');
                if (currentIndex < words.length) {
                    showNextWord();
                    ui.input.focus();
                } else {
                    finishTest();
                }
            }, 300); // 待機時間
        }
    });

    function showNextWord() {
        ui.wordDisplay.textContent = words[currentIndex];
        ui.progressLabel.textContent = `${currentIndex} / ${words.length}`;
        ui.progressFill.style.width = `${(currentIndex / words.length) * 100}%`;
    }

    async function finishTest() {
        const usedTimeMillis = Date.now() - startedAt;
        const requestBody = {
            rawResult: rawResult,
            usedTimeMillis: usedTimeMillis,
            isTest: true
        };

        try {
            // common.jsの API.post を使用 (CSRF自動付与)
            const resultData = await API.post('/test/results', requestBody);
            
            ui.active.classList.add('hidden');
            ui.result.classList.remove('hidden');
            
            ui.resWpm.textContent = resultData.wpm.toFixed(1);
            ui.resAcc.textContent = Math.round(resultData.accuracy * 100);
            
            // テーブル描画ロジックは元のコードから移植
            renderResultTable(resultData.analyzedResult);
            
        } catch (e) {
            alert('結果の送信に失敗しました。');
        }
    }

    function renderResultTable(results) {
        ui.resTbody.innerHTML = '';
        Object.entries(results).forEach(([word, res]) => {
            const isCorrect = res.mistakes.length === 0;
            const tr = document.createElement('tr');
            
            let mistakeTags = '';
            if (!isCorrect) {
                res.mistakes.forEach(m => {
                    const typeInfo = Object.values(TRENDS_MAP).find(t => t.label === m.mistakeType.japaneseName) || { cls: 'bg-gray-200 text-gray-700', label: m.mistakeType.japaneseName || m.mistakeType };
                    let detail = '';
                    if (m.mistakeType === 'SUBSTITUTION' || m.mistakeType.japaneseName === '置換') detail = `${m.expected}→${m.actual}`;
                    else if (m.mistakeType === 'DELETION' || m.mistakeType.japaneseName === '削除') detail = `${m.expected}抜け`;
                    else if (m.mistakeType === 'INSERTION' || m.mistakeType.japaneseName === '挿入') detail = `${m.insertion}不要`;
                    else if (m.mistakeType === 'TRANSPOSITION' || m.mistakeType.japaneseName === '交換') detail = `順序逆`;
                    mistakeTags += `<span class="mistake-tag ${typeInfo.cls}">${typeInfo.label} ${detail}</span>`;
                });
            }
            
            tr.innerHTML = `
                <td class="py-3 px-4 font-mono">${word}</td>
                <td class="py-3 px-4 font-mono ${isCorrect ? 'text-gray-900' : 'text-red-600'}">${res.answer}</td>
                <td class="py-3 px-4">
                    ${isCorrect 
                        ? '<span class="inline-flex items-center gap-1 text-green-600 font-medium"><svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>正解</span>' 
                        : '<span class="inline-flex items-center gap-1 text-red-500 font-medium"><svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>ミス</span>'}
                </td>
                <td class="py-3 px-4">${mistakeTags}</td>
            `;
            ui.resTbody.appendChild(tr);
        });
    }
    
    document.getElementById('btn-retry-test')?.addEventListener('click', () => {
        ui.result.classList.add('hidden');
        ui.start.classList.remove('hidden');
    });
});