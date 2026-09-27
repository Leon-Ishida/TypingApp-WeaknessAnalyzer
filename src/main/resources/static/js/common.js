// 共通のCSRFトークン取得とfetchラッパー
const API = {
    // metaタグからSpring SecurityのCSRFトークンを取得
    getCSRFToken() {
        const tokenMeta = document.querySelector('meta[name="_csrf"]');
        const headerMeta = document.querySelector('meta[name="_csrf_header"]');
        if (tokenMeta && headerMeta) {
            return {
                header: headerMeta.content,
                token: tokenMeta.content
            };
        }
        return null;
    },

    // CSRFトークンを自動付与してPOSTリクエストを行うラッパー関数
    async post(url, body) {
        const headers = {
            'Content-Type': 'application/json'
        };
        
        const csrf = this.getCSRFToken();
        if (csrf) {
            headers[csrf.header] = csrf.token; // ここでヘッダーに埋め込む
        }

        const response = await fetch(url, {
            method: 'POST',
            headers: headers,
            body: JSON.stringify(body)
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
    },

    async get(url) {
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
    }
};

// 共通フォーマット関数群 (元のコードから抽出)
const TRENDS_MAP = {
    topSub: { label: '置換', cls: 'mistake-sub', color: '#fef08a' },
    topTrans: { label: '交換', cls: 'mistake-trans', color: '#fed7aa' },
    topDel: { label: '削除', cls: 'mistake-del', color: '#fecaca' },
    topIns: { label: '挿入', cls: 'mistake-ins', color: '#bfdbfe' }
};

function formatMistakeKey(keyString) {
    if (!keyString) return '';
    return keyString
      .replace(/^[a-zA-Z]+Pair\[/, '')
      .replace(/\]$/, '')
      .replace(/expected=/, '正:')
      .replace(/actual=/, '誤:')
      .replace(/insertion=/, '挿入:');
}