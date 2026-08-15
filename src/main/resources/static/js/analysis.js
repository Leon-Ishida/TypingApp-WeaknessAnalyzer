document.addEventListener('DOMContentLoaded', () => {
    const ui = {
        startDate: document.getElementById('analysis-start-date'),
        endDate: document.getElementById('analysis-end-date'),
        btnLoad: document.getElementById('btn-load-analysis'),
        content: document.getElementById('analysis-content'),
        empty: document.getElementById('analysis-empty'),
        mistakeTopsContainer: document.getElementById('mistake-tops-container')
    };

    let wpmChart = null;
    let accChart = null;

    // 初回ロード時にデータ取得
    loadAnalysisData();

    ui.btnLoad?.addEventListener('click', loadAnalysisData);

    async function loadAnalysisData() {
        const body = {
            startDate: ui.startDate.value || null,
            lastDate: ui.endDate.value || null
        };

        try {
            const data = await API.post('/aggregate', body);
            
            const scoreTrends = data.scoreTrends || [];
            const mistakeTrends = data.mistakeTrends || {};

            if (scoreTrends.length === 0) {
                ui.content.classList.add('hidden');
                ui.empty.classList.remove('hidden');
                return;
            }

            ui.empty.classList.add('hidden');
            ui.content.classList.remove('hidden');

            renderCharts(scoreTrends);
            renderTopMistakes(mistakeTrends);
        } catch (e) {
            console.error(e);
            alert('分析データの取得に失敗しました。');
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
                    borderWidth: 2,
                    fill: true,
                    tension: 0.3
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
                    borderColor: '#22c55e',
                    backgroundColor: 'rgba(34, 197, 94, 0.1)',
                    borderWidth: 2,
                    fill: true,
                    tension: 0.3
                }]
            },
            options: { 
                responsive: true, 
                maintainAspectRatio: false,
                scales: { y: { min: 0, max: 100 } }
            }
        });
    }

    function renderTopMistakes(mistakesData) {
        ui.mistakeTopsContainer.innerHTML = '';
        
        Object.keys(TRENDS_MAP).forEach(mapKey => {
            const obj = mistakesData[mapKey] || {};
            const info = TRENDS_MAP[mapKey];
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
            ui.mistakeTopsContainer.appendChild(card);
        });
    }
});