document.addEventListener('DOMContentLoaded', () => {
    const state = {
        results: [],
        submitting: false
    };

    const ui = {
        loading: document.getElementById('inherit-loading'),
        error: document.getElementById('inherit-error'),
        errorMessage: document.getElementById('inherit-error-message'),
        empty: document.getElementById('inherit-empty'),
        content: document.getElementById('inherit-content'),
        complete: document.getElementById('inherit-complete'),
        completeTitle: document.getElementById('inherit-complete-title'),
        completeMessage: document.getElementById('inherit-complete-message'),
        resultsBody: document.getElementById('inherit-results-body'),
        selectAll: document.getElementById('inherit-select-all'),
        selectionSummary: document.getElementById('inherit-selection-summary'),
        retryButton: document.getElementById('btn-retry-inherit'),
        saveButton: document.getElementById('btn-save-inherit'),
        declineButton: document.getElementById('btn-decline-inherit')
    };

    function showOnly(target) {
        [ui.loading, ui.error, ui.empty, ui.content, ui.complete].forEach(element => {
            element.classList.toggle('hidden', element !== target);
        });
    }

    function formatTimestamp(timestamp) {
        const match = String(timestamp).match(/^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})/);
        if (!match) {
            return String(timestamp);
        }
        return `${match[1]}/${match[2]}/${match[3]} ${match[4]}:${match[5]}`;
    }

    function formatWpm(wpm) {
        const value = Number(wpm);
        return Number.isFinite(value) ? value.toFixed(1) : '--';
    }

    function formatAccuracy(accuracy) {
        const value = Number(accuracy);
        return Number.isFinite(value) ? `${(value * 100).toFixed(1)}%` : '--';
    }

    function getResultCheckboxes() {
        return [...ui.resultsBody.querySelectorAll('input[data-result-id]')];
    }

    function getSelectedIds() {
        return getResultCheckboxes()
            .filter(checkbox => checkbox.checked)
            .map(checkbox => Number(checkbox.dataset.resultId));
    }

    function updateSelectionState() {
        const checkboxes = getResultCheckboxes();
        const selectedCount = checkboxes.filter(checkbox => checkbox.checked).length;

        ui.selectAll.checked = checkboxes.length > 0 && selectedCount === checkboxes.length;
        ui.selectAll.indeterminate = selectedCount > 0 && selectedCount < checkboxes.length;
        ui.selectionSummary.textContent = `${checkboxes.length}件中 ${selectedCount}件を選択中`;
        ui.saveButton.disabled = state.submitting || selectedCount === 0;
    }

    function createResultRow(result) {
        const row = document.createElement('tr');
        row.className = 'hover:bg-gray-50';

        const selectCell = document.createElement('td');
        selectCell.className = 'px-6 py-4';

        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.checked = true;
        checkbox.dataset.resultId = String(result.id);
        checkbox.className = 'h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500';
        checkbox.setAttribute('aria-label', `${formatTimestamp(result.timestamp)}の結果を選択`);
        checkbox.addEventListener('change', updateSelectionState);
        selectCell.appendChild(checkbox);

        const timestampCell = document.createElement('td');
        timestampCell.className = 'px-4 py-4 text-gray-700';
        const time = document.createElement('time');
        time.dateTime = String(result.timestamp);
        time.textContent = formatTimestamp(result.timestamp);
        timestampCell.appendChild(time);

        const wpmCell = document.createElement('td');
        wpmCell.className = 'px-4 py-4 text-right font-mono font-semibold text-gray-900';
        wpmCell.textContent = formatWpm(result.wpm);

        const accuracyCell = document.createElement('td');
        accuracyCell.className = 'px-6 py-4 text-right font-mono font-semibold text-gray-900';
        accuracyCell.textContent = formatAccuracy(result.accuracy);

        row.append(selectCell, timestampCell, wpmCell, accuracyCell);
        return row;
    }

    function renderResults() {
        ui.resultsBody.replaceChildren(...state.results.map(createResultRow));
        ui.selectAll.checked = true;
        ui.selectAll.indeterminate = false;
        updateSelectionState();
    }

    function setSubmitting(submitting) {
        state.submitting = submitting;
        ui.selectAll.disabled = submitting;
        ui.declineButton.disabled = submitting;
        getResultCheckboxes().forEach(checkbox => {
            checkbox.disabled = submitting;
        });

        ui.saveButton.textContent = submitting ? '処理中…' : '選択した結果を保存';
        updateSelectionState();
    }

    function showCompletion(title, message) {
        ui.completeTitle.textContent = title;
        ui.completeMessage.textContent = message;
        showOnly(ui.complete);
    }

    async function loadCandidates() {
        showOnly(ui.loading);

        try {
            const results = await API.get('/inherit');
            if (!Array.isArray(results)) {
                throw new Error('Unexpected response format');
            }

            state.results = results;
            if (state.results.length === 0) {
                showOnly(ui.empty);
                return;
            }

            renderResults();
            showOnly(ui.content);
        } catch (error) {
            console.error(error);
            ui.errorMessage.textContent = 'ログイン状態または通信状況を確認して、もう一度お試しください。';
            showOnly(ui.error);
        }
    }

    async function submitDecision(ids, isDecline) {
        if (state.submitting) {
            return;
        }

        setSubmitting(true);
        try {
            const claimedCount = await API.post('/inherit/post', ids);
            if (!Number.isInteger(claimedCount) || claimedCount < 0) {
                throw new Error('Unexpected response value');
            }

            if (isDecline) {
                showCompletion('保存しないことを選択しました', 'ゲスト結果は期限経過後に自動で削除されます。');
                return;
            }

            const requestedCount = ids.length;
            const skippedCount = requestedCount - claimedCount;
            const message = skippedCount > 0
                ? `${claimedCount}件を保存しました。${skippedCount}件は期限切れまたは取得済みのため保存されませんでした。`
                : `${claimedCount}件の結果をアカウントに保存しました。`;
            showCompletion('ゲスト結果を保存しました', message);
        } catch (error) {
            console.error(error);
            ui.errorMessage.textContent = '処理を完了できませんでした。結果は確定していない可能性があるため、もう一度お試しください。';
            showOnly(ui.error);
        } finally {
            setSubmitting(false);
        }
    }

    ui.selectAll.addEventListener('change', () => {
        getResultCheckboxes().forEach(checkbox => {
            checkbox.checked = ui.selectAll.checked;
        });
        updateSelectionState();
    });

    ui.retryButton.addEventListener('click', loadCandidates);

    ui.saveButton.addEventListener('click', () => {
        const selectedIds = getSelectedIds();
        if (selectedIds.length === 0) {
            return;
        }

        if (window.confirm(`選択した${selectedIds.length}件をアカウントに保存しますか？`)) {
            submitDecision(selectedIds, false);
        }
    });

    ui.declineButton.addEventListener('click', () => {
        if (window.confirm('ゲスト結果をアカウントに保存せず、この確認を終了しますか？')) {
            submitDecision([], true);
        }
    });

    loadCandidates();
});
