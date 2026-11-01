/* 管理端 API 封装：统一加 token、解析 Result、处理登录失效 */
const API = (() => {
    const TOKEN_KEY = 'sky_admin_token';
    const NAME_KEY = 'sky_admin_name';

    function getToken() {
        return localStorage.getItem(TOKEN_KEY) || '';
    }

    function getName() {
        return localStorage.getItem(NAME_KEY) || '';
    }

    function setSession(token, name) {
        localStorage.setItem(TOKEN_KEY, token || '');
        localStorage.setItem(NAME_KEY, name || '');
    }

    function clearSession() {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(NAME_KEY);
    }

    async function request(path, options = {}) {
        const headers = Object.assign({}, options.headers || {});
        if (options.body && !(options.body instanceof FormData)) {
            headers['Content-Type'] = 'application/json; charset=utf-8';
        }
        const token = getToken();
        if (token) {
            headers.token = token;
        }

        let response;
        try {
            response = await fetch('/api' + path, Object.assign({}, options, {headers}));
        } catch (e) {
            throw new Error('网络请求失败，请确认后端和 Nginx 已启动');
        }

        const text = await response.text();
        let result = {};
        try {
            result = text ? JSON.parse(text) : {};
        } catch (e) {
            throw new Error('响应格式错误');
        }

        if (result.code !== 1) {
            const msg = result.msg || ('请求失败（' + response.status + '）');
            if (response.status === 401 || msg.includes('未登录')) {
                clearSession();
                if (!location.pathname.endsWith('login.html')) {
                    location.href = 'login.html';
                }
            }
            throw new Error(msg);
        }
        return result.data;
    }

    function get(path) {
        return request(path, {method: 'GET'});
    }

    function post(path, body) {
        return request(path, {method: 'POST', body: JSON.stringify(body)});
    }

    function put(path, body) {
        return request(path, {method: 'PUT', body: JSON.stringify(body)});
    }

    function del(path) {
        return request(path, {method: 'DELETE'});
    }

    async function upload(file) {
        const formData = new FormData();
        formData.append('file', file);
        const response = await fetch('/api/admin/common/upload', {
            method: 'POST',
            headers: {token: getToken()},
            body: formData
        });
        const result = await response.json();
        if (result.code !== 1) {
            throw new Error(result.msg || '图片上传失败');
        }
        return result.data;
    }

    return {getToken, getName, setSession, clearSession, get, post, put, del, upload};
})();
