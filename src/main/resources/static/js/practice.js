document.addEventListener('DOMContentLoaded', () => {
    const ui = {
        setup: document.getElementById('practice-setup-ui'),
        active: document.getElementById('practice-active-ui'),
        result: document.getElementById('practice-result-ui'),
        startDate: document.getElementById('practice-start-date'),
        endDate: document.getElementById('practice-end-date'),
        btnStart: document.getElementById('btn-start-practice'),
        btnRetry: document.getElementById('btn-retry-practice'),
        wordDisplay: document.getElementById('practice-word-display'),
        input: document.getElementById('practice-input'),
        progressLabel: document.getElementById('practice-progress-label'),
        progressFill: document.getElementById('practice-progress-fill'),
        resWpm: document.getElementById('prac-res-wpm'),
        resAcc: document.getElementById('prac-res-acc')
    };

    let words = [];
    let currentIndex = 0;
    let startedAt = 0;
    let rawResult = {};

    ui.btnStart?.addEventListener('click', async () => {
        const mode = document.querySelector('input[name="practice_mode"]:checked').value;
        const body = {
            startDate: ui.startDate.value || null,
            lastDate: ui.endDate.value || null,
            mode: mode
        };

        try {
            // 練習問題の生成を要求
            const generatedWords = await API.post('/practice/start', body);
            
            // 練習問題が取得できない場合はエラーハンドリング
            if (!generatedWords || generatedWords.length === 0) {
                alert('練習問題の生成に必要なデータがありません。テストを何度か実行してください。');
                return;
            }

            words = generatedWords;
            startedAt = Date.now();
            currentIndex = 0;
            rawResult = {};

            ui.setup.classList.add('hidden');
            ui.result.classList.add('hidden');
            ui.active.classList.remove('hidden');

            showNextWord();
            ui.input.focus();
        } catch (e) {
            console.error(e);
            alert('練習の開始に失敗しました。');
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
                    finishPractice();
                }
            }, 300);
        }
    });

    function showNextWord() {
        ui.wordDisplay.textContent = words[currentIndex];
        ui.progressLabel.textContent = `${currentIndex} / ${words.length}`;
        ui.progressFill.style.width = `${(currentIndex / words.length) * 100}%`;
    }

    async function finishPractice() {
        const usedTimeMillis = Date.now() - startedAt;
        const requestBody = {
            rawResult: rawResult,
            usedTimeMillis: usedTimeMillis,
            isTest: false // 練習モードなので isTest を false に設定 (DBに保存させない)
        };

        try {
            // 練習結果の分析だけを行う (isTest=falseなので保存はされない仕様)
            const resultData = await API.post('/test/results', requestBody);
            
            ui.active.classList.add('hidden');
            ui.result.classList.remove('hidden');
            
            ui.resWpm.textContent = resultData.wpm.toFixed(1);
            ui.resAcc.textContent = Math.round(resultData.accuracy * 100);
        } catch (e) {
            alert('練習結果の集計に失敗しました。');
        }
    }

    ui.btnRetry?.addEventListener('click', () => {
        ui.result.classList.add('hidden');
        ui.setup.classList.remove('hidden');
    });
});