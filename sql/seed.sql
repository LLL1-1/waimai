-- 外卖点餐平台最小种子数据
-- 依赖：先执行 sky_take_out.sql 建库建表

USE sky_take_out;
SET NAMES utf8mb4;

-- 分类：4 个菜品分类 + 1 个套餐分类
INSERT IGNORE INTO category
    (id, type, name, sort, status, create_time, update_time, create_user, update_user)
VALUES
    (1, 1, '荤菜', 1, 1, NOW(), NOW(), 1, 1),
    (2, 1, '素菜', 2, 1, NOW(), NOW(), 1, 1),
    (3, 1, '主食', 3, 1, NOW(), NOW(), 1, 1),
    (4, 1, '饮品', 4, 1, NOW(), NOW(), 1, 1),
    (5, 2, '套餐', 5, 1, NOW(), NOW(), 1, 1);

-- 菜品
INSERT IGNORE INTO dish
    (id, name, category_id, price, image, description, status,
     create_time, update_time, create_user, update_user)
VALUES
    (1, '红烧肉', 1, 28.00, '/images/dish/hongshaorou.png', '肥而不腻，经典下饭', 1, NOW(), NOW(), 1, 1),
    (2, '宫保鸡丁', 1, 26.00, '/images/dish/gongbaojiding.png', '酸甜微辣，鸡肉鲜嫩', 1, NOW(), NOW(), 1, 1),
    (3, '清炒时蔬', 2, 12.00, '/images/dish/qingchaoshishu.png', '清爽解腻，时令蔬菜', 1, NOW(), NOW(), 1, 1),
    (4, '麻婆豆腐', 2, 14.00, '/images/dish/mapodoufu.png', '麻辣鲜香，下饭神器', 1, NOW(), NOW(), 1, 1),
    (5, '米饭', 3, 2.00, '/images/dish/mifan.png', '香软可口', 1, NOW(), NOW(), 1, 1),
    (6, '面条', 3, 8.00, '/images/dish/miantiao.png', '劲道爽滑', 1, NOW(), NOW(), 1, 1),
    (7, '可乐', 4, 4.00, '/images/dish/kele.png', '冰爽可乐', 1, NOW(), NOW(), 1, 1),
    (8, '鲜榨果汁', 4, 6.00, '/images/dish/xianzhaguozhi.png', '新鲜现榨', 1, NOW(), NOW(), 1, 1);

-- 套餐
INSERT IGNORE INTO setmeal
    (id, category_id, name, price, status, description, image,
     create_time, update_time, create_user, update_user)
VALUES
    (1, 5, '单人套餐A', 38.00, 1, '红烧肉 + 清炒时蔬 + 米饭',
     '/images/setmeal/danren-taocan-a.png', NOW(), NOW(), 1, 1),
    (2, 5, '单人套餐B', 32.00, 1, '宫保鸡丁 + 米饭 + 可乐',
     '/images/setmeal/danren-taocan-b.png', NOW(), NOW(), 1, 1),
    (3, 5, '双人套餐', 68.00, 1, '红烧肉 + 宫保鸡丁 + 清炒时蔬 + 米饭×2',
     '/images/setmeal/shuangren-taocan.png', NOW(), NOW(), 1, 1);

-- 套餐菜品关系
INSERT IGNORE INTO setmeal_dish
    (id, setmeal_id, dish_id, name, price, copies)
VALUES
    (1, 1, 1, '红烧肉', 28.00, 1),
    (2, 1, 3, '清炒时蔬', 12.00, 1),
    (3, 1, 5, '米饭', 2.00, 1),
    (4, 2, 2, '宫保鸡丁', 26.00, 1),
    (5, 2, 5, '米饭', 2.00, 1),
    (6, 2, 7, '可乐', 4.00, 1),
    (7, 3, 1, '红烧肉', 28.00, 1),
    (8, 3, 2, '宫保鸡丁', 26.00, 1),
    (9, 3, 3, '清炒时蔬', 12.00, 1),
    (10, 3, 5, '米饭', 2.00, 2);

-- 管理员：admin / 123456（BCrypt）
INSERT IGNORE INTO employee
    (id, name, username, password, phone, sex, id_number, status,
     create_time, update_time, create_user, update_user)
VALUES
    (1, '管理员', 'admin',
     '$2a$10$xlhj5kW0GfF4ywWOUPP1a.gnq.Yc6V1DQexurdluVjL.PiI9NJFQi',
     '13800000000', '男', '110101199001011234', 1, NOW(), NOW(), 1, 1);
