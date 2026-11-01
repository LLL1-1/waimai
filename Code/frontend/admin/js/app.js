/* global API */
(function () {
    'use strict';

    if (!API.getToken()) {
        location.href = 'login.html';
        return;
    }

    const state = {
        tab: 'category',
        categories: [],
        dishCategories: [],
        setmealCategories: [],
        dishes: [],
        category: {page: 1, pageSize: 10, name: '', type: ''},
        employee: {page: 1, pageSize: 10, name: ''},
        dish: {page: 1, pageSize: 10, name: '', categoryId: '', status: ''},
        setmeal: {page: 1, pageSize: 10, name: '', categoryId: '', status: ''},
        order: {page: 1, pageSize: 10, status: '', number: ''}
    };

    const $ = (selector, root = document) => root.querySelector(selector);
    const $$ = (selector, root = document) => Array.from(root.querySelectorAll(selector));

    function escapeHtml(value) {
        return String(value == null ? '' : value).replace(/[&<>"']/g, ch => ({
            '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
        }[ch]));
    }

    function money(value) {
        return Number(value || 0).toFixed(2);
    }

    function formatTime(value) {
        return value ? String(value).replace('T', ' ').substring(0, 19) : '-';
    }

    function badge(status, onText, offText) {
        onText = onText || '启用';
        offText = offText || '禁用';
        return status === 1
            ? `<span class="badge on">${onText}</span>`
            : `<span class="badge off">${offText}</span>`;
    }

    function option(value, current, label) {
        return `<option value="${value}" ${String(value) === String(current) ? 'selected' : ''}>${label}</option>`;
    }

    function formToObject(form) {
        const data = {};
        new FormData(form).forEach((value, key) => {
            data[key] = value;
        });
        return data;
    }

    let toastTimer = null;

    function toast(message) {
        const element = $('#toast');
        element.textContent = message;
        element.classList.add('show');
        clearTimeout(toastTimer);
        toastTimer = setTimeout(() => element.classList.remove('show'), 2200);
    }

    function run(fn) {
        Promise.resolve().then(fn).catch(err => toast(err.message));
    }

    function fillSelect(select, items, placeholder) {
        select.innerHTML = `<option value="">${placeholder}</option>` +
            (items || []).map(item => `<option value="${item.id}">${escapeHtml(item.name)}</option>`).join('');
    }

    function renderPager(element, total, page, pageSize, onChange) {
        const pages = Math.max(1, Math.ceil((total || 0) / pageSize));
        element.innerHTML = `
            <button type="button" data-page="prev" ${page <= 1 ? 'disabled' : ''}>上一页</button>
            <span>第 ${page} / ${pages} 页，共 ${total || 0} 条</span>
            <button type="button" data-page="next" ${page >= pages ? 'disabled' : ''}>下一页</button>`;
        $$('button[data-page]', element).forEach(button => {
            button.onclick = () => {
                if (button.dataset.page === 'prev' && page > 1) onChange(page - 1);
                if (button.dataset.page === 'next' && page < pages) onChange(page + 1);
            };
        });
    }

    function openModal(title, bodyHtml, onSubmit, onMount) {
        const root = $('#modalRoot');
        root.innerHTML = `
            <div class="modal-mask">
                <div class="modal">
                    <h3>${escapeHtml(title)}</h3>
                    <form id="modalForm">
                        <div class="modal-body">${bodyHtml}</div>
                        <div class="modal-error" id="modalError"></div>
                        <div class="modal-actions">
                            <button type="button" class="btn ghost" id="modalCancel">取消</button>
                            <button type="submit" class="btn primary" id="modalSubmit">保存</button>
                        </div>
                    </form>
                </div>
            </div>`;

        const form = $('#modalForm');
        const submit = $('#modalSubmit');
        const error = $('#modalError');

        $('#modalCancel').onclick = closeModal;
        root.querySelector('.modal-mask').addEventListener('click', event => {
            if (event.target.classList.contains('modal-mask')) closeModal();
        });

        form.addEventListener('submit', async event => {
            event.preventDefault();
            error.textContent = '';
            submit.disabled = true;
            try {
                await onSubmit(form);
                closeModal();
            } catch (err) {
                error.textContent = err.message;
                submit.disabled = false;
            }
        });

        if (onMount) onMount(form);
    }

    function closeModal() {
        $('#modalRoot').innerHTML = '';
    }

    function openInfoModal(title, bodyHtml) {
        const root = $('#modalRoot');
        root.innerHTML = `
            <div class="modal-mask">
                <div class="modal">
                    <h3>${escapeHtml(title)}</h3>
                    <div class="modal-body">${bodyHtml}</div>
                    <div class="modal-actions">
                        <button type="button" class="btn primary" id="modalClose">关闭</button>
                    </div>
                </div>
            </div>`;
        $('#modalClose').onclick = closeModal;
        root.querySelector('.modal-mask').addEventListener('click', event => {
            if (event.target.classList.contains('modal-mask')) closeModal();
        });
    }

    function bindImageUpload(form) {
        const fileInput = form.querySelector('.image-file');
        const imageInput = form.querySelector('[name="image"]');
        const preview = form.querySelector('.image-preview');
        if (!fileInput || !imageInput || !preview) return;

        fileInput.addEventListener('change', async () => {
            const file = fileInput.files[0];
            if (!file) return;
            try {
                toast('图片上传中...');
                const path = await API.upload(file);
                imageInput.value = path;
                preview.src = path;
                toast('图片上传成功');
            } catch (err) {
                toast(err.message);
            }
        });

        imageInput.addEventListener('input', () => {
            preview.src = imageInput.value;
        });
    }

    async function loadCategoryOptions() {
        const list = await API.get('/admin/category/list');
        state.categories = list || [];
        state.dishCategories = state.categories.filter(item => item.type === 1);
        state.setmealCategories = state.categories.filter(item => item.type === 2);
        fillSelect($('#dishCategory'), state.dishCategories, '全部分类');
        fillSelect($('#setmealCategory'), state.setmealCategories, '全部分类');
    }

    function categoryOptions(selected, type) {
        const items = (type === 2 ? state.setmealCategories : state.dishCategories);
        return items.map(item => option(item.id, selected, escapeHtml(item.name))).join('');
    }

    /* ---------------- 分类 ---------------- */
    function bindCategory() {
        $('#categorySearch').onclick = () => {
            state.category.page = 1;
            state.category.name = $('#categoryName').value.trim();
            state.category.type = $('#categoryType').value;
            run(loadCategories);
        };
        $('#categoryName').addEventListener('keydown', event => {
            if (event.key === 'Enter') $('#categorySearch').click();
        });
        $('#categoryAdd').onclick = () => run(() => openCategoryModal(null));

        $('#categoryBody').addEventListener('click', async event => {
            const button = event.target.closest('button[data-act]');
            if (!button) return;
            const id = Number(button.dataset.id);
            try {
                if (button.dataset.act === 'edit') {
                    await openCategoryModal(await API.get('/admin/category/' + id));
                } else if (button.dataset.act === 'status') {
                    await API.post('/admin/category/status/' + (button.dataset.status === '1' ? 0 : 1) + '?id=' + id);
                    toast('状态已更新');
                    await refreshCategoryData();
                } else if (button.dataset.act === 'del') {
                    if (!confirm('确定删除这个分类？')) return;
                    await API.del('/admin/category?id=' + id);
                    toast('删除成功');
                    await refreshCategoryData();
                }
            } catch (err) {
                toast(err.message);
            }
        });
    }

    async function refreshCategoryData() {
        await loadCategoryOptions();
        await loadCategories();
    }

    async function loadCategories() {
        const params = new URLSearchParams({
            page: state.category.page,
            pageSize: state.category.pageSize
        });
        if (state.category.name) params.set('name', state.category.name);
        if (state.category.type) params.set('type', state.category.type);

        const data = await API.get('/admin/category/page?' + params.toString());
        const rows = data.records || [];
        $('#categoryBody').innerHTML = rows.length ? rows.map(item => `
            <tr>
                <td>${item.id}</td>
                <td>${escapeHtml(item.name)}</td>
                <td>${item.type === 2 ? '套餐分类' : '菜品分类'}</td>
                <td>${item.sort}</td>
                <td>${badge(item.status)}</td>
                <td>${formatTime(item.updateTime)}</td>
                <td>
                    <button class="link-btn" data-act="edit" data-id="${item.id}">编辑</button>
                    <button class="link-btn" data-act="status" data-id="${item.id}" data-status="${item.status}">${item.status === 1 ? '禁用' : '启用'}</button>
                    <button class="link-btn danger" data-act="del" data-id="${item.id}">删除</button>
                </td>
            </tr>`).join('') : '<tr><td colspan="7" style="text-align:center;color:#9aa3b2;padding:28px">暂无数据</td></tr>';

        renderPager($('#categoryPager'), data.total, state.category.page, state.category.pageSize, page => {
            state.category.page = page;
            run(loadCategories);
        });
    }

    async function openCategoryModal(item) {
        const isEdit = !!item;
        const body = `
            <label>分类名称<input name="name" required value="${escapeHtml(item ? item.name : '')}"></label>
            <div class="form-row">
                <label>类型
                    <select name="type">
                        ${option(1, item ? item.type : 1, '菜品分类')}
                        ${option(2, item ? item.type : 1, '套餐分类')}
                    </select>
                </label>
                <label>排序<input name="sort" type="number" value="${item ? item.sort : 0}"></label>
            </div>
            <label>状态
                <select name="status">
                    ${option(1, item ? item.status : 1, '启用')}
                    ${option(0, item ? item.status : 1, '禁用')}
                </select>
            </label>`;

        openModal(isEdit ? '编辑分类' : '新增分类', body, async form => {
            const data = formToObject(form);
            const payload = {
                name: data.name.trim(),
                type: Number(data.type),
                sort: Number(data.sort || 0),
                status: Number(data.status)
            };
            if (isEdit) {
                payload.id = item.id;
                await API.put('/admin/category', payload);
            } else {
                await API.post('/admin/category', payload);
            }
            toast('保存成功');
            await refreshCategoryData();
        });
    }

    /* ---------------- 员工 ---------------- */
    function bindEmployee() {
        $('#employeeSearch').onclick = () => {
            state.employee.page = 1;
            state.employee.name = $('#employeeName').value.trim();
            run(loadEmployees);
        };
        $('#employeeName').addEventListener('keydown', event => {
            if (event.key === 'Enter') $('#employeeSearch').click();
        });
        $('#employeeAdd').onclick = () => run(() => openEmployeeModal(null));

        $('#employeeBody').addEventListener('click', async event => {
            const button = event.target.closest('button[data-act]');
            if (!button) return;
            const id = Number(button.dataset.id);
            try {
                if (button.dataset.act === 'edit') {
                    await openEmployeeModal(await API.get('/admin/employee/' + id));
                } else if (button.dataset.act === 'status') {
                    await API.post('/admin/employee/status/' + (button.dataset.status === '1' ? 0 : 1) + '?id=' + id);
                    toast('状态已更新');
                    await loadEmployees();
                }
            } catch (err) {
                toast(err.message);
            }
        });
    }

    async function loadEmployees() {
        const params = new URLSearchParams({
            page: state.employee.page,
            pageSize: state.employee.pageSize
        });
        if (state.employee.name) params.set('name', state.employee.name);

        const data = await API.get('/admin/employee/page?' + params.toString());
        const rows = data.records || [];
        $('#employeeBody').innerHTML = rows.length ? rows.map(item => `
            <tr>
                <td>${item.id}</td>
                <td>${escapeHtml(item.username)}</td>
                <td>${escapeHtml(item.name)}</td>
                <td>${escapeHtml(item.phone)}</td>
                <td>${escapeHtml(item.sex)}</td>
                <td>${badge(item.status)}</td>
                <td>${formatTime(item.updateTime)}</td>
                <td>
                    <button class="link-btn" data-act="edit" data-id="${item.id}">编辑</button>
                    <button class="link-btn" data-act="status" data-id="${item.id}" data-status="${item.status}">${item.status === 1 ? '禁用' : '启用'}</button>
                </td>
            </tr>`).join('') : '<tr><td colspan="8" style="text-align:center;color:#9aa3b2;padding:28px">暂无数据</td></tr>';

        renderPager($('#employeePager'), data.total, state.employee.page, state.employee.pageSize, page => {
            state.employee.page = page;
            run(loadEmployees);
        });
    }

    async function openEmployeeModal(item) {
        const isEdit = !!item;
        const body = `
            <label>姓名<input name="name" required value="${escapeHtml(item ? item.name : '')}"></label>
            <label>用户名<input name="username" required value="${escapeHtml(item ? item.username : '')}"></label>
            <label>密码
                <input name="password" type="password" ${isEdit ? '' : 'required'}
                       placeholder="${isEdit ? '不填则不修改密码' : '请输入密码'}">
            </label>
            <div class="form-row">
                <label>手机号<input name="phone" required value="${escapeHtml(item ? item.phone : '')}"></label>
                <label>性别
                    <select name="sex">
                        ${option('男', item ? item.sex : '男', '男')}
                        ${option('女', item ? item.sex : '男', '女')}
                    </select>
                </label>
            </div>
            <label>身份证号<input name="idNumber" required value="${escapeHtml(item ? item.idNumber : '')}"></label>
            <label>状态
                <select name="status">
                    ${option(1, item ? item.status : 1, '启用')}
                    ${option(0, item ? item.status : 1, '禁用')}
                </select>
            </label>`;

        openModal(isEdit ? '编辑员工' : '新增员工', body, async form => {
            const data = formToObject(form);
            const payload = {
                name: data.name.trim(),
                username: data.username.trim(),
                password: data.password,
                phone: data.phone.trim(),
                sex: data.sex,
                idNumber: data.idNumber.trim(),
                status: Number(data.status)
            };
            if (isEdit) {
                payload.id = item.id;
                await API.put('/admin/employee', payload);
            } else {
                await API.post('/admin/employee', payload);
            }
            toast('保存成功');
            await loadEmployees();
        });
    }

    /* ---------------- 菜品 ---------------- */
    function bindDish() {
        $('#dishSearch').onclick = () => {
            state.dish.page = 1;
            state.dish.name = $('#dishName').value.trim();
            state.dish.categoryId = $('#dishCategory').value;
            state.dish.status = $('#dishStatus').value;
            run(loadDishes);
        };
        $('#dishName').addEventListener('keydown', event => {
            if (event.key === 'Enter') $('#dishSearch').click();
        });
        $('#dishAdd').onclick = () => run(() => openDishModal(null));
        $('#dishCategory').onchange = () => $('#dishSearch').click();
        $('#dishStatus').onchange = () => $('#dishSearch').click();

        $('#dishBody').addEventListener('click', async event => {
            const button = event.target.closest('button[data-act]');
            if (!button) return;
            const id = Number(button.dataset.id);
            try {
                if (button.dataset.act === 'edit') {
                    await openDishModal(await API.get('/admin/dish/' + id));
                } else if (button.dataset.act === 'status') {
                    await API.post('/admin/dish/status/' + (button.dataset.status === '1' ? 0 : 1) + '?id=' + id);
                    toast('状态已更新');
                    await loadDishes();
                } else if (button.dataset.act === 'del') {
                    if (!confirm('确定删除这个菜品？')) return;
                    await API.del('/admin/dish?ids=' + id);
                    toast('删除成功');
                    await loadDishes();
                    state.dishes = [];
                }
            } catch (err) {
                toast(err.message);
            }
        });
    }

    async function loadDishes() {
        const params = new URLSearchParams({
            page: state.dish.page,
            pageSize: state.dish.pageSize
        });
        if (state.dish.name) params.set('name', state.dish.name);
        if (state.dish.categoryId) params.set('categoryId', state.dish.categoryId);
        if (state.dish.status) params.set('status', state.dish.status);

        const data = await API.get('/admin/dish/page?' + params.toString());
        const rows = data.records || [];
        $('#dishBody').innerHTML = rows.length ? rows.map(item => `
            <tr>
                <td>${item.id}</td>
                <td><img class="thumb" src="${escapeHtml(item.image || '/images/placeholder.png')}" alt=""></td>
                <td>${escapeHtml(item.name)}</td>
                <td>${escapeHtml(item.categoryName || '-')}</td>
                <td>¥${money(item.price)}</td>
                <td>${badge(item.status, '起售', '停售')}</td>
                <td>${formatTime(item.updateTime)}</td>
                <td>
                    <button class="link-btn" data-act="edit" data-id="${item.id}">编辑</button>
                    <button class="link-btn" data-act="status" data-id="${item.id}" data-status="${item.status}">${item.status === 1 ? '停售' : '起售'}</button>
                    <button class="link-btn danger" data-act="del" data-id="${item.id}">删除</button>
                </td>
            </tr>`).join('') : '<tr><td colspan="8" style="text-align:center;color:#9aa3b2;padding:28px">暂无数据</td></tr>';

        renderPager($('#dishPager'), data.total, state.dish.page, state.dish.pageSize, page => {
            state.dish.page = page;
            run(loadDishes);
        });
    }

    function flavorRowHtml(flavor) {
        let values = '';
        if (flavor && flavor.value) {
            try {
                const parsed = JSON.parse(flavor.value);
                values = Array.isArray(parsed) ? parsed.join(', ') : flavor.value;
            } catch (e) {
                values = flavor.value;
            }
        }
        return `
            <div class="flavor-row">
                <input class="flavor-name" placeholder="口味名，如辣度" value="${escapeHtml(flavor ? flavor.name : '')}">
                <input class="flavor-value" placeholder="多个值用逗号分隔，如 不辣,微辣" value="${escapeHtml(values)}">
                <button type="button" class="btn ghost flavor-del">删除</button>
            </div>`;
    }

    async function openDishModal(item) {
        const isEdit = !!item;
        const detail = item ? await API.get('/admin/dish/' + item.id) : null;
        const flavors = (detail && detail.flavors) || [];
        const image = detail ? detail.image : '';

        const body = `
            <label>菜品名称<input name="name" required value="${escapeHtml(detail ? detail.name : '')}"></label>
            <div class="form-row">
                <label>分类
                    <select name="categoryId" required>
                        ${categoryOptions(detail ? detail.categoryId : '', 1)}
                    </select>
                </label>
                <label>价格<input name="price" type="number" step="0.01" min="0.01" required value="${detail ? detail.price : ''}"></label>
            </div>
            <label>图片路径<input name="image" value="${escapeHtml(image)}"></label>
            <div class="hint">也可以选择本地图片上传</div>
            <input type="file" class="image-file" accept="image/*">
            <img class="image-preview" src="${escapeHtml(image || '/images/placeholder.png')}" alt="">
            <label>描述<textarea name="description">${escapeHtml(detail ? detail.description : '')}</textarea></label>
            <label>状态
                <select name="status">
                    ${option(1, detail ? detail.status : 1, '起售')}
                    ${option(0, detail ? detail.status : 1, '停售')}
                </select>
            </label>
            <div class="hint">口味：名称和值分开填，多个值用逗号分隔</div>
            <div id="flavorRows">${flavors.map(flavorRowHtml).join('')}</div>
            <button type="button" class="btn" id="flavorAdd">添加口味</button>`;

        openModal(isEdit ? '编辑菜品' : '新增菜品', body, async form => {
            const data = formToObject(form);
            const flavorList = $$('.flavor-row', form).map(row => {
                const name = row.querySelector('.flavor-name').value.trim();
                const raw = row.querySelector('.flavor-value').value.trim();
                if (!name || !raw) return null;
                const values = raw.split(',').map(value => value.trim()).filter(Boolean);
                if (!values.length) return null;
                return {name: name, value: JSON.stringify(values)};
            }).filter(Boolean);

            const payload = {
                name: data.name.trim(),
                categoryId: Number(data.categoryId),
                price: Number(data.price),
                image: data.image.trim(),
                description: data.description.trim(),
                status: Number(data.status),
                flavors: flavorList
            };

            if (isEdit) {
                payload.id = item.id;
                await API.put('/admin/dish', payload);
            } else {
                await API.post('/admin/dish', payload);
            }
            toast('保存成功');
            await loadDishes();
            // 菜品名称/价格可能变了，下次打开套餐弹窗时重新拉取
            state.dishes = [];
        }, form => {
            bindImageUpload(form);
            $('#flavorAdd', form).onclick = () => {
                $('#flavorRows', form).insertAdjacentHTML('beforeend', flavorRowHtml(null));
            };
            $('#flavorRows', form).addEventListener('click', event => {
                if (event.target.classList.contains('flavor-del')) {
                    event.target.closest('.flavor-row').remove();
                }
            });
        });
    }

    /* ---------------- 套餐 ---------------- */
    function bindSetmeal() {
        $('#setmealSearch').onclick = () => {
            state.setmeal.page = 1;
            state.setmeal.name = $('#setmealName').value.trim();
            state.setmeal.categoryId = $('#setmealCategory').value;
            state.setmeal.status = $('#setmealStatus').value;
            run(loadSetmeals);
        };
        $('#setmealName').addEventListener('keydown', event => {
            if (event.key === 'Enter') $('#setmealSearch').click();
        });
        $('#setmealAdd').onclick = () => run(() => openSetmealModal(null));
        $('#setmealCategory').onchange = () => $('#setmealSearch').click();
        $('#setmealStatus').onchange = () => $('#setmealSearch').click();

        $('#setmealBody').addEventListener('click', async event => {
            const button = event.target.closest('button[data-act]');
            if (!button) return;
            const id = Number(button.dataset.id);
            try {
                if (button.dataset.act === 'edit') {
                    await openSetmealModal(await API.get('/admin/setmeal/' + id));
                } else if (button.dataset.act === 'status') {
                    await API.post('/admin/setmeal/status/' + (button.dataset.status === '1' ? 0 : 1) + '?id=' + id);
                    toast('状态已更新');
                    await loadSetmeals();
                } else if (button.dataset.act === 'del') {
                    if (!confirm('确定删除这个套餐？')) return;
                    await API.del('/admin/setmeal?ids=' + id);
                    toast('删除成功');
                    await loadSetmeals();
                }
            } catch (err) {
                toast(err.message);
            }
        });
    }

    async function loadSetmeals() {
        const params = new URLSearchParams({
            page: state.setmeal.page,
            pageSize: state.setmeal.pageSize
        });
        if (state.setmeal.name) params.set('name', state.setmeal.name);
        if (state.setmeal.categoryId) params.set('categoryId', state.setmeal.categoryId);
        if (state.setmeal.status) params.set('status', state.setmeal.status);

        const data = await API.get('/admin/setmeal/page?' + params.toString());
        const rows = data.records || [];
        $('#setmealBody').innerHTML = rows.length ? rows.map(item => `
            <tr>
                <td>${item.id}</td>
                <td><img class="thumb" src="${escapeHtml(item.image || '/images/placeholder.png')}" alt=""></td>
                <td>${escapeHtml(item.name)}</td>
                <td>${escapeHtml(item.categoryName || '-')}</td>
                <td>¥${money(item.price)}</td>
                <td>${badge(item.status, '起售', '停售')}</td>
                <td>${formatTime(item.updateTime)}</td>
                <td>
                    <button class="link-btn" data-act="edit" data-id="${item.id}">编辑</button>
                    <button class="link-btn" data-act="status" data-id="${item.id}" data-status="${item.status}">${item.status === 1 ? '停售' : '起售'}</button>
                    <button class="link-btn danger" data-act="del" data-id="${item.id}">删除</button>
                </td>
            </tr>`).join('') : '<tr><td colspan="8" style="text-align:center;color:#9aa3b2;padding:28px">暂无数据</td></tr>';

        renderPager($('#setmealPager'), data.total, state.setmeal.page, state.setmeal.pageSize, page => {
            state.setmeal.page = page;
            run(loadSetmeals);
        });
    }

    async function openSetmealModal(item) {
        const isEdit = !!item;
        const detail = item ? await API.get('/admin/setmeal/' + item.id) : null;

        if (!state.dishes.length) {
            state.dishes = await API.get('/admin/dish/list') || [];
        }

        const selected = new Map(((detail && detail.setmealDishes) || []).map(rel => [rel.dishId, rel.copies]));
        const dishHtml = state.dishes.length ? state.dishes.map(dish => {
            const copies = selected.get(dish.id) || 1;
            return `
                <label class="setmeal-dish-item">
                    <input type="checkbox" class="setmeal-dish-check" value="${dish.id}"
                           data-name="${escapeHtml(dish.name)}" data-price="${dish.price}"
                           ${selected.has(dish.id) ? 'checked' : ''}>
                    <span>${escapeHtml(dish.name)}（¥${money(dish.price)}）</span>
                    <input type="number" class="setmeal-dish-copies" min="1" value="${copies}">
                </label>`;
        }).join('') : '<div class="hint">暂无菜品，请先添加菜品</div>';

        const body = `
            <label>套餐名称<input name="name" required value="${escapeHtml(detail ? detail.name : '')}"></label>
            <div class="form-row">
                <label>分类
                    <select name="categoryId" required>
                        ${categoryOptions(detail ? detail.categoryId : '', 2)}
                    </select>
                </label>
                <label>价格<input name="price" type="number" step="0.01" min="0.01" required value="${detail ? detail.price : ''}"></label>
            </div>
            <label>图片路径<input name="image" value="${escapeHtml(detail ? detail.image : '')}"></label>
            <div class="hint">也可以选择本地图片上传</div>
            <input type="file" class="image-file" accept="image/*">
            <img class="image-preview" src="${escapeHtml((detail && detail.image) || '/images/placeholder.png')}" alt="">
            <label>描述<textarea name="description">${escapeHtml(detail ? detail.description : '')}</textarea></label>
            <label>状态
                <select name="status">
                    ${option(1, detail ? detail.status : 1, '起售')}
                    ${option(0, detail ? detail.status : 1, '停售')}
                </select>
            </label>
            <div class="hint">套餐菜品：勾选菜品并填写份数</div>
            <div class="setmeal-dish-list">${dishHtml}</div>`;

        openModal(isEdit ? '编辑套餐' : '新增套餐', body, async form => {
            const data = formToObject(form);
            const setmealDishes = $$('.setmeal-dish-check:checked', form).map(check => {
                const row = check.closest('.setmeal-dish-item');
                return {
                    dishId: Number(check.value),
                    name: check.dataset.name,
                    price: Number(check.dataset.price),
                    copies: Number(row.querySelector('.setmeal-dish-copies').value || 1)
                };
            });

            const payload = {
                name: data.name.trim(),
                categoryId: Number(data.categoryId),
                price: Number(data.price),
                image: data.image.trim(),
                description: data.description.trim(),
                status: Number(data.status),
                setmealDishes: setmealDishes
            };

            if (isEdit) {
                payload.id = item.id;
                await API.put('/admin/setmeal', payload);
            } else {
                await API.post('/admin/setmeal', payload);
            }
            toast('保存成功');
            await loadSetmeals();
        }, bindImageUpload);
    }

    /* ---------------- 订单 ---------------- */
    const ORDER_STATUS = {
        1: '待付款', 2: '待接单', 3: '已接单',
        4: '派送中', 5: '已完成', 6: '已取消'
    };

    function orderStatusBadge(status) {
        const cls = status === 5 ? 'on' : (status === 6 ? 'off' : '');
        return `<span class="badge ${cls}">${ORDER_STATUS[status] || status}</span>`;
    }

    function bindOrders() {
        $('#orderSearch').onclick = () => {
            state.order.page = 1;
            state.order.number = $('#orderNumber').value.trim();
            state.order.status = $('#orderStatus').value;
            run(loadOrders);
        };
        $('#orderNumber').addEventListener('keydown', event => {
            if (event.key === 'Enter') $('#orderSearch').click();
        });
        $('#orderStatus').onchange = () => $('#orderSearch').click();

        $('#orderBody').addEventListener('click', async event => {
            const button = event.target.closest('button[data-act]');
            if (!button) return;
            const id = Number(button.dataset.id);
            try {
                if (button.dataset.act === 'detail') {
                    await openOrderDetail(id);
                } else if (button.dataset.act === 'confirm') {
                    await API.put('/admin/order/confirm/' + id, {});
                    toast('已接单');
                    await loadOrders();
                } else if (button.dataset.act === 'delivery') {
                    await API.put('/admin/order/delivery/' + id, {});
                    toast('已派送');
                    await loadOrders();
                } else if (button.dataset.act === 'complete') {
                    await API.put('/admin/order/complete/' + id, {});
                    toast('订单已完成');
                    await loadOrders();
                } else if (button.dataset.act === 'reject') {
                    const reason = prompt('拒单原因', '商家拒单');
                    if (reason === null) return;
                    await API.put('/admin/order/reject/' + id
                        + '?rejectionReason=' + encodeURIComponent(reason), {});
                    toast('已拒单');
                    await loadOrders();
                }
            } catch (err) {
                toast(err.message);
            }
        });
    }

    async function loadOrders() {
        const params = new URLSearchParams({
            page: state.order.page,
            pageSize: state.order.pageSize
        });
        if (state.order.number) params.set('number', state.order.number);
        if (state.order.status) params.set('status', state.order.status);

        const data = await API.get('/admin/order/page?' + params.toString());
        const rows = data.records || [];
        $('#orderBody').innerHTML = rows.length ? rows.map(order => {
            const actions = [`<button class="link-btn" data-act="detail" data-id="${order.id}">详情</button>`];
            if (order.status === 2) {
                actions.push(`<button class="link-btn" data-act="confirm" data-id="${order.id}">接单</button>`);
                actions.push(`<button class="link-btn danger" data-act="reject" data-id="${order.id}">拒单</button>`);
            }
            if (order.status === 3) {
                actions.push(`<button class="link-btn" data-act="delivery" data-id="${order.id}">派送</button>`);
            }
            if (order.status === 4) {
                actions.push(`<button class="link-btn" data-act="complete" data-id="${order.id}">完成</button>`);
            }
            return `
                <tr>
                    <td>${order.id}</td>
                    <td>${escapeHtml(order.number)}</td>
                    <td>${orderStatusBadge(order.status)}</td>
                    <td>¥${money(order.amount)}</td>
                    <td>${escapeHtml(order.consignee)}</td>
                    <td>${escapeHtml(order.phone)}</td>
                    <td>${formatTime(order.orderTime)}</td>
                    <td>${actions.join('')}</td>
                </tr>`;
        }).join('') : '<tr><td colspan="8" style="text-align:center;color:#9aa3b2;padding:28px">暂无订单</td></tr>';

        renderPager($('#orderPager'), data.total, state.order.page, state.order.pageSize, page => {
            state.order.page = page;
            run(loadOrders);
        });
    }

    async function openOrderDetail(id) {
        const order = await API.get('/admin/order/' + id);
        const details = (order.details || []).map(item => `
            <div class="order-detail-row">
                <span>${escapeHtml(item.name)} x${item.number}</span>
                <span>¥${money(item.amount)}</span>
            </div>`).join('') || '<div class="hint">无明细</div>';

        const body = `
            <p><strong>订单号：</strong>${escapeHtml(order.number)}</p>
            <p><strong>状态：</strong>${orderStatusBadge(order.status)}</p>
            <p><strong>金额：</strong>¥${money(order.amount)}</p>
            <p><strong>收货人：</strong>${escapeHtml(order.consignee)} ${escapeHtml(order.phone)}</p>
            <p><strong>地址：</strong>${escapeHtml(order.address)}</p>
            <p><strong>备注：</strong>${escapeHtml(order.remark || '-')}</p>
            <p><strong>下单时间：</strong>${formatTime(order.orderTime)}</p>
            ${order.rejectionReason ? `<p><strong>拒单原因：</strong>${escapeHtml(order.rejectionReason)}</p>` : ''}
            <div class="hint">订单明细</div>
            ${details}`;
        openInfoModal('订单详情', body);
    }

    /* ---------------- 初始化 ---------------- */
    function renderShopStatus(status) {
        const badge = $('#shopStatus');
        const button = $('#shopToggle');
        badge.textContent = status === 1 ? '营业中' : '已打烊';
        badge.className = 'badge ' + (status === 1 ? 'on' : 'off');
        button.textContent = status === 1 ? '打烊' : '开始营业';
        button.dataset.status = String(status);
    }

    async function loadShopStatus() {
        const status = await API.get('/admin/shop/status');
        renderShopStatus(status);
    }

    function bindShopStatus() {
        $('#shopToggle').onclick = async () => {
            const current = Number($('#shopToggle').dataset.status);
            const target = current === 1 ? 0 : 1;
            try {
                await API.put('/admin/shop/' + target, {});
                toast(target === 1 ? '已开始营业' : '已打烊');
                await loadShopStatus();
            } catch (err) {
                toast(err.message);
            }
        };
    }

    function bindNav() {
        $$('.nav-item').forEach(button => {
            button.onclick = () => showTab(button.dataset.tab);
        });
    }

    function connectWs() {
        const protocol = location.protocol === 'https:' ? 'wss' : 'ws';
        const ws = new WebSocket(protocol + '://' + location.host + '/ws/admin');
        ws.onmessage = event => {
            try {
                const message = JSON.parse(event.data);
                if (message.type === 'newOrder') {
                    toast('有新订单，请到订单管理查看');
                    if (state.tab === 'order') run(loadOrders);
                }
            } catch (e) {
                // 忽略非 JSON 消息
            }
        };
        ws.onclose = () => setTimeout(connectWs, 5000);
    }

    function showTab(tab) {
        state.tab = tab;
        $$('.nav-item').forEach(button => button.classList.toggle('active', button.dataset.tab === tab));
        $$('.panel').forEach(panel => panel.classList.add('hidden'));
        $('#panel-' + tab).classList.remove('hidden');

        const titles = {category: '分类管理', employee: '员工管理', dish: '菜品管理', setmeal: '套餐管理', order: '订单管理'};
        $('#pageTitle').textContent = titles[tab];

        if (tab === 'category') run(loadCategories);
        if (tab === 'employee') run(loadEmployees);
        if (tab === 'dish') run(loadDishes);
        if (tab === 'setmeal') run(loadSetmeals);
        if (tab === 'order') run(loadOrders);
    }

    async function init() {
        $('#userName').textContent = API.getName() || '管理员';
        $('#logoutBtn').onclick = () => {
            API.clearSession();
            location.href = 'login.html';
        };

        bindNav();
        bindCategory();
        bindEmployee();
        bindDish();
        bindSetmeal();
        bindOrders();
        bindShopStatus();
        connectWs();

        try {
            await loadShopStatus();
        } catch (err) {
            toast(err.message);
        }

        try {
            await loadCategoryOptions();
            await loadCategories();
        } catch (err) {
            toast(err.message);
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
