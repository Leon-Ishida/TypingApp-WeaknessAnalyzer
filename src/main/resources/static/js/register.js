document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('register-form');
    const usernameInput = document.getElementById('username');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const passwordConfirmInput = document.getElementById('passwordConfirm');
    const errorContainer = document.getElementById('js-error-message');
    const submitBtn = document.getElementById('submit-btn');

    form.addEventListener('submit', async (e) => {
        // 1. ブラウザのデフォルトの画面遷移（フォーム送信）をブロック
        e.preventDefault();

        // 2. クライアント側のバリデーション（パスワード一致チェック）
        if (passwordInput.value !== passwordConfirmInput.value) {
            showError('パスワードと確認用パスワードが一致しません。');
            passwordConfirmInput.focus();
            return;
        }

        // 送信中はボタンを無効化して二重送信を防ぐ
        submitBtn.disabled = true;
        submitBtn.textContent = '登録中...';
        errorContainer.classList.add('hidden');

        // 3. 送信するデータ（JSON）の組み立て
        // ※バックエンドの RegistRequest DTO と同じプロパティ名に合わせます
        const requestBody = {
            userName: usernameInput.value.trim(),
            email: emailInput.value.trim(),
            password: passwordInput.value
        };

        try {
            // CSRFトークンを common.js の関数を使って取得
            const csrf = API.getCSRFToken();
            const headers = { 'Content-Type': 'application/json' };
            if (csrf) {
                headers[csrf.header] = csrf.token;
            }

            // 4. APIへ POST通信
            const response = await fetch('/auth/regist', {
                method: 'POST',
                headers: headers,
                body: JSON.stringify(requestBody)
            });

            // 5. ステータスコードに応じた処理
            if (response.status === 201) {
                // 登録成功 -> ログイン画面へリダイレクト
                window.location.href = '/login';
            } else if (response.status === 409 || response.status === 400) {
                // 登録失敗 (重複など) -> サーバーから返された JSON を読み取ってエラーメッセージを表示
                const errorData = await response.json();
                showError(errorData.message || '登録に失敗しました。入力内容を確認してください。');
                resetButton();
            } else {
                // その他のエラー (500など)
                showError('サーバーエラーが発生しました。しばらく経ってからお試しください。');
                resetButton();
            }
        } catch (error) {
            console.error('通信エラー:', error);
            showError('ネットワークエラーが発生しました。');
            resetButton();
        }
    });

    // エラーメッセージを表示する補助関数
    function showError(message) {
        errorContainer.textContent = message;
        errorContainer.classList.remove('hidden');
    }

    // ボタンの状態を戻す補助関数
    function resetButton() {
        submitBtn.disabled = false;
        submitBtn.textContent = '登録する';
    }
});