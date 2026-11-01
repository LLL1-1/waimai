const API = (() => {
    const TOKEN = 'sky_user_token';
    async function request(path, options = {}) {
        const headers = Object.assign({}, options.headers || {});
        if (options.body) headers['Content-Type'] = 'application/json; charset=utf-8';
        const token = localStorage.getItem(TOKEN);
        if (token) headers.authentication = token;
        const response = await fetch('/api' + path, Object.assign({}, options, {headers}));
        const result = await response.json();
        if (result.code !== 1) throw new Error(result.msg || '请求失败');
        return result.data;
    }
    return {
        token: () => localStorage.getItem(TOKEN) || '',
        setToken: token => localStorage.setItem(TOKEN, token),
        clear: () => localStorage.removeItem(TOKEN),
        get: path => request(path),
        post: (path, body) => request(path, {method: 'POST', body: JSON.stringify(body)}),
        put: (path, body) => request(path, {method: 'PUT', body: JSON.stringify(body || {})}),
        del: path => request(path, {method: 'DELETE'})
    };
})();

const state = {type: 1, categoryId: null, shopStatus: 1, cart: []};
const $ = s => document.querySelector(s);
const $$ = s => Array.from(document.querySelectorAll(s));
const money = v => Number(v || 0).toFixed(2);
const statusText = {1: '待付款', 2: '待接单', 3: '已接单', 4: '派送中', 5: '已完成', 6: '已取消'};

let toastTimer;
function toast(msg) {
    const el = $('#toast');
    el.textContent = msg;
    el.classList.add('show');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => el.classList.remove('show'), 2000);
}

function showMain() {
    $('#loginPanel').classList.add('hidden');
    $('#mainPanel').classList.remove('hidden');
    $('#logoutBtn').classList.remove('hidden');
    $('#userName').textContent = '用户';
}

function switchTab(tab) {
    $$('.tabs button').forEach(b => b.classList.toggle('active', b.dataset.tab === tab));
    $$('.tab-panel').forEach(p => p.classList.add('hidden'));
    $('#tab-' + tab).classList.remove('hidden');
    if (tab === 'cart') loadCart();
    if (tab === 'order') loadOrders();
    if (tab === 'address') loadAddresses();
}

async function loadShopStatus() {
    try {
        const status = await API.get('/user/shop/status');
        state.shopStatus = status;
        $('#shopStatus').textContent = status === 1 ? '营业中' : '已打烊';
        $('#shopStatus').classList.toggle('off', status !== 1);
    } catch (e) { /* 未登录也能查，失败不阻塞 */ }
}

async function loadCategories(type) {
    state.type = type;
    const list = await API.get('/user/category/list?type=' + type);
    $('#categoryList').innerHTML = list.map((item, index) =>
        `<button data-id="${item.id}" class="${index === 0 ? 'active' : ''}">${item.name}</button>`).join('');
    $('#categoryList').onclick = event => {
        const btn = event.target.closest('button[data-id]');
        if (!btn) return;
        $$('#categoryList button').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        loadMenu(Number(btn.dataset.id));
    };
    if (list.length) loadMenu(list[0].id);
}

async function loadMenu(categoryId) {
    state.categoryId = categoryId;
    const path = state.type === 1 ? '/user/dish/list?categoryId=' : '/user/setmeal/list?categoryId=';
    const list = await API.get(path + categoryId);
    $('#menuList').innerHTML = list.length ? list.map(item => `
        <div class="card">
            <img src="${item.image || '/images/placeholder.png'}" alt="">
            <h4>${item.name}</h4>
            <p>¥${money(item.price)}</p>
            <button class="primary" data-id="${item.id}">加入购物车</button>
        </div>`).join('') : '<p class="hint">暂无起售商品</p>';

    $('#menuList').onclick = async event => {
        const btn = event.target.closest('button[data-id]');
        if (!btn) return;
        if (state.shopStatus !== 1) return toast('店铺已打烊');
        try {
            if (state.type === 1) {
                const flavor = prompt('口味（可留空）', '');
                if (flavor === null) return;
                await API.post('/user/shoppingCart/add', {
                    dishId: Number(btn.dataset.id),
                    dishFlavor: flavor.trim() || null
                });
            } else {
                await API.post('/user/shoppingCart/add', {setmealId: Number(btn.dataset.id)});
            }
            toast('已加入购物车');
            await loadCart();
        } catch (e) { toast(e.message); }
    };
}

async function loadCart() {
    state.cart = await API.get('/user/shoppingCart/list');
    const total = state.cart.reduce((sum, item) => sum + Number(item.amount) * item.number, 0);
    const count = state.cart.reduce((sum, item) => sum + item.number, 0);
    $('#cartCount').textContent = count;
    $('#cartTotal').textContent = '¥' + money(total);
    $('#cartList').innerHTML = state.cart.length ? state.cart.map(item => `
        <div class="cart-row">
            <span class="name">${item.name}${item.dishFlavor ? '（' + item.dishFlavor + '）' : ''}</span>
            <span>¥${money(item.amount)}</span>
            <button class="link" data-sub="${item.id}">-</button>
            <span>${item.number}</span>
            <button class="link" data-add="${item.id}">+</button>
        </div>`).join('') : '<p class="hint">购物车是空的</p>';

    $('#cartList').onclick = async event => {
        const add = event.target.closest('[data-add]');
        const sub = event.target.closest('[data-sub]');
        if (!add && !sub) return;
        const item = state.cart.find(x => x.id === Number((add || sub).dataset.add || (add || sub).dataset.sub));
        if (!item) return;
        const body = item.dishId
            ? {dishId: item.dishId, dishFlavor: item.dishFlavor}
            : {setmealId: item.setmealId};
        try {
            await API.post('/user/shoppingCart/' + (add ? 'add' : 'sub'), body);
            await loadCart();
        } catch (e) { toast(e.message); }
    };
}

async function submitOrder() {
    if (!state.cart.length) return toast('购物车是空的');
    if (state.shopStatus !== 1) return toast('店铺已打烊');
    try {
        const address = await API.get('/user/addressBook/default');
        const result = await API.post('/user/order/submit', {
            addressBookId: address.id, remark: '', payMethod: 1,
            tablewareNumber: 1, tablewareStatus: 1
        });
        await loadCart();
        location.href = 'pay.html?orderId=' + result.id;
    } catch (e) { toast(e.message); }
}

async function loadOrders() {
    const data = await API.get('/user/order/page?page=1&pageSize=20');
    $('#orderList').innerHTML = data.records.length ? data.records.map(order => `
        <div class="order-row">
            <div class="line"><strong>${order.number}</strong><span>${statusText[order.status] || order.status}</span></div>
            <div class="line"><span>${order.consignee} ${order.phone}</span><strong>¥${money(order.amount)}</strong></div>
            ${order.status === 1 ? `<button class="link" data-pay="${order.id}">去支付</button>` : ''}
            ${order.status === 1 || order.status === 2 ? `<button class="link" data-cancel="${order.id}">取消订单</button>` : ''}
        </div>`).join('') : '<p class="hint">还没有订单</p>';

    $('#orderList').onclick = async event => {
        const pay = event.target.closest('[data-pay]');
        if (pay) {
            location.href = 'pay.html?orderId=' + pay.dataset.pay;
            return;
        }
        const btn = event.target.closest('[data-cancel]');
        if (!btn || !confirm('确定取消订单？')) return;
        try {
            await API.put('/user/order/cancel/' + btn.dataset.cancel, {});
            toast('已取消');
            await loadOrders();
        } catch (e) { toast(e.message); }
    };
}

async function loadAddresses() {
    const list = await API.get('/user/addressBook/list');
    $('#addressList').innerHTML = list.length ? list.map(item => `
        <div class="address-row">
            <span>${item.consignee} ${item.phone}</span>
            <span>${item.provinceName || ''}${item.cityName || ''}${item.districtName || ''}${item.detail}</span>
            ${item.isDefault === 1 ? '<strong>默认</strong>' : `<button class="link" data-default="${item.id}">设为默认</button>`}
        </div>`).join('') : '<p class="hint">还没有地址</p>';

    $('#addressList').onclick = async event => {
        const btn = event.target.closest('[data-default]');
        if (!btn) return;
        try {
            await API.put('/user/addressBook/default?id=' + btn.dataset.default, {});
            toast('已设为默认');
            await loadAddresses();
        } catch (e) { toast(e.message); }
    };
}

async function saveAddress() {
    try {
        await API.post('/user/addressBook', {
            consignee: $('#consignee').value,
            phone: $('#phone').value,
            provinceName: $('#provinceName').value,
            cityName: $('#cityName').value,
            districtName: $('#districtName').value,
            detail: $('#detail').value,
            isDefault: $('#isDefault').checked ? 1 : 0
        });
        toast('地址已保存');
        await loadAddresses();
    } catch (e) { toast(e.message); }
}

async function login() {
    const code = $('#loginCode').value.trim();
    if (!code) return toast('请输入 mock code');
    try {
        const data = await API.post('/user/user/login', {code});
        API.setToken(data.token);
        showMain();
        initData();
    } catch (e) {
        $('#loginError').textContent = e.message;
    }
}

async function initData() {
    await loadShopStatus();
    await loadCategories(1);
    await loadCart();
}

$('#loginBtn').onclick = login;
$('#logoutBtn').onclick = () => {
    API.clear();
    location.reload();
};
$$('.tabs button').forEach(btn => btn.onclick = () => switchTab(btn.dataset.tab));
$('#dishTypeBtn').onclick = () => {
    $('#dishTypeBtn').classList.add('primary');
    $('#setmealTypeBtn').classList.remove('primary');
    loadCategories(1);
};
$('#setmealTypeBtn').onclick = () => {
    $('#setmealTypeBtn').classList.add('primary');
    $('#dishTypeBtn').classList.remove('primary');
    loadCategories(2);
};
$('#cleanCartBtn').onclick = async () => {
    await API.del('/user/shoppingCart/clean');
    await loadCart();
};
$('#submitOrderBtn').onclick = submitOrder;
$('#saveAddressBtn').onclick = saveAddress;

(async function start() {
    if (!API.token()) return;
    showMain();
    const tab = new URLSearchParams(location.search).get('tab');
    if (tab) switchTab(tab);
    initData();
})();
