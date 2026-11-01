/* 登录页逻辑 */
(function () {
    if (API.getToken()) {
        location.href = 'index.html';
        return;
    }

    const form = document.getElementById('loginForm');
    const error = document.getElementById('loginError');
    const button = form.querySelector('button[type="submit"]');

    form.addEventListener('submit', async function (event) {
        event.preventDefault();
        error.textContent = '';
        button.disabled = true;

        const formData = new FormData(form);
        try {
            const data = await API.post('/admin/employee/login', {
                username: formData.get('username'),
                password: formData.get('password')
            });
            API.setSession(data.token, data.name || data.userName || '管理员');
            location.href = 'index.html';
        } catch (e) {
            error.textContent = e.message;
            button.disabled = false;
        }
    });
})();
