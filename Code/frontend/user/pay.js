const token = localStorage.getItem('sky_user_token') || '';
const orderId = new URLSearchParams(location.search).get('orderId');

if (!token || !orderId) {
    location.href = 'index.html';
}

const $ = selector => document.querySelector(selector);
const money = value => Number(value || 0).toFixed(2);

async function request(path, options = {}) {
    const headers = Object.assign({authentication: token}, options.headers || {});
    const response = await fetch('/api' + path, Object.assign({}, options, {headers}));
    const result = await response.json();
    if (result.code !== 1) {
        throw new Error(result.msg || '请求失败');
    }
    return result.data;
}

async function loadOrder() {
    try {
        const order = await request('/user/order/' + orderId);
        $('#orderNumber').textContent = order.number;
        $('#consignee').textContent = order.consignee + ' ' + order.phone;
        $('#amount').textContent = '¥' + money(order.amount);
        $('#status').textContent = order.status === 1 ? '待付款' : '当前订单无需支付';
        if (order.status !== 1) {
            $('#payBtn').disabled = true;
            $('#status').textContent = '当前订单无需支付';
        }
    } catch (e) {
        $('#message').textContent = e.message;
        $('#payBtn').disabled = true;
    }
}

$('#payBtn').onclick = async () => {
    $('#payBtn').disabled = true;
    $('#message').textContent = '';
    try {
        await request('/user/order/payment/' + orderId, {method: 'PUT'});
        $('#status').textContent = '支付成功';
        setTimeout(() => location.href = 'index.html?tab=order', 600);
    } catch (e) {
        $('#message').textContent = e.message;
        $('#payBtn').disabled = false;
    }
};

loadOrder();
