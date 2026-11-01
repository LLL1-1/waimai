package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.context.BaseContext;
import com.sky.dto.OrderPageQueryDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.entity.User;
import com.sky.exception.BusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.mapper.UserMapper;
import com.sky.result.PageResult;
import com.sky.service.OrdersService;
import com.sky.vo.OrdersSubmitVO;
import com.sky.vo.OrdersVO;
import com.sky.websocket.WebSocketServer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** 订单实现：下单、用户查询、管理端状态流转。 */
@Service
@RequiredArgsConstructor
public class OrdersServiceImpl implements OrdersService {

    private final OrdersMapper ordersMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final ShoppingCartMapper shoppingCartMapper;
    private final AddressBookMapper addressBookMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public OrdersSubmitVO submit(OrdersSubmitDTO dto) {
        Long userId = currentUserId();
        List<ShoppingCart> cartList = shoppingCartMapper.selectList(
                new LambdaQueryWrapper<ShoppingCart>().eq(ShoppingCart::getUserId, userId));
        if (cartList.isEmpty()) {
            throw new BusinessException("购物车不能为空");
        }

        AddressBook address = addressBookMapper.selectOne(new LambdaQueryWrapper<AddressBook>()
                .eq(AddressBook::getId, dto.getAddressBookId())
                .eq(AddressBook::getUserId, userId));
        if (address == null) {
            throw new BusinessException("收货地址不存在");
        }

        BigDecimal total = cartList.stream()
                .map(item -> item.getAmount().multiply(BigDecimal.valueOf(item.getNumber())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        User user = userMapper.selectById(userId);
        LocalDateTime now = LocalDateTime.now();
        Orders order = new Orders();
        order.setNumber(String.valueOf(System.currentTimeMillis())
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10000)));
        order.setStatus(1);
        order.setUserId(userId);
        order.setAddressBookId(address.getId());
        order.setOrderTime(now);
        order.setPayMethod(dto.getPayMethod() == null ? 1 : dto.getPayMethod());
        order.setPayStatus(0);
        order.setAmount(total);
        order.setRemark(dto.getRemark());
        order.setPhone(address.getPhone());
        order.setAddress(joinAddress(address));
        order.setUserName(user == null ? "用户" : user.getName());
        order.setConsignee(address.getConsignee());
        order.setDeliveryStatus(1);
        order.setTablewareNumber(dto.getTablewareNumber() == null ? 1 : dto.getTablewareNumber());
        order.setTablewareStatus(dto.getTablewareStatus() == null ? 1 : dto.getTablewareStatus());
        order.setPackAmount(0);
        ordersMapper.insert(order);

        for (ShoppingCart cart : cartList) {
            OrderDetail detail = new OrderDetail();
            detail.setName(cart.getName());
            detail.setImage(cart.getImage());
            detail.setOrderId(order.getId());
            detail.setDishId(cart.getDishId());
            detail.setSetmealId(cart.getSetmealId());
            detail.setDishFlavor(cart.getDishFlavor());
            detail.setNumber(cart.getNumber());
            detail.setAmount(cart.getAmount());
            orderDetailMapper.insert(detail);
        }

        shoppingCartMapper.delete(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, userId));
        return new OrdersSubmitVO(order.getId(), order.getNumber(), order.getAmount(), order.getOrderTime());
    }

    @Override
    @Transactional
    public void pay(Long id) {
        Orders order = getUserOrder(id);
        if (order.getStatus() != 1) {
            throw new BusinessException("订单状态不正确");
        }
        Orders update = new Orders();
        update.setId(id);
        update.setStatus(2);
        update.setPayStatus(1);
        update.setCheckoutTime(LocalDateTime.now());
        ordersMapper.updateById(update);

        // 支付成功后再通知管理端，避免未支付订单提前提醒
        WebSocketServer.sendToAll("{\"type\":\"newOrder\",\"orderId\":" + id + "}");
    }

    @Override
    public PageResult<Orders> pageQuery(Integer status, Integer page, Integer pageSize) {
        Page<Orders> result = new Page<>(page == null || page < 1 ? 1 : page,
                pageSize == null || pageSize < 1 ? 10 : pageSize);
        ordersMapper.selectPage(result, new LambdaQueryWrapper<Orders>()
                .eq(Orders::getUserId, currentUserId())
                .eq(status != null, Orders::getStatus, status)
                .orderByDesc(Orders::getOrderTime));
        return new PageResult<>(result.getTotal(), result.getRecords());
    }

    @Override
    public OrdersVO detail(Long id) {
        return buildDetail(id, true);
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        Orders order = getUserOrder(id);
        if (order.getStatus() != 1 && order.getStatus() != 2) {
            throw new BusinessException("当前订单不能取消");
        }
        Orders update = new Orders();
        update.setId(id);
        update.setStatus(6);
        update.setCancelReason("用户取消");
        update.setCancelTime(LocalDateTime.now());
        ordersMapper.updateById(update);
    }

    @Override
    public PageResult<Orders> adminPage(OrderPageQueryDTO dto) {
        Page<Orders> result = new Page<>(dto.getPage() == null || dto.getPage() < 1 ? 1 : dto.getPage(),
                dto.getPageSize() == null || dto.getPageSize() < 1 ? 10 : dto.getPageSize());
        ordersMapper.selectPage(result, new LambdaQueryWrapper<Orders>()
                .ge(Orders::getStatus, 2)
                .eq(dto.getStatus() != null, Orders::getStatus, dto.getStatus())
                .like(StringUtils.hasText(dto.getNumber()), Orders::getNumber, dto.getNumber())
                .orderByDesc(Orders::getOrderTime));
        return new PageResult<>(result.getTotal(), result.getRecords());
    }

    @Override
    public OrdersVO adminDetail(Long id) {
        return buildDetail(id, false);
    }

    @Override
    public void confirm(Long id) {
        updateStatus(id, 2, 3);
    }

    @Override
    public void reject(Long id, String rejectionReason) {
        Orders order = getOrder(id);
        if (order.getStatus() != 2) {
            throw new BusinessException("订单状态不正确");
        }
        Orders update = new Orders();
        update.setId(id);
        update.setStatus(6);
        update.setRejectionReason(StringUtils.hasText(rejectionReason) ? rejectionReason : "商家拒单");
        ordersMapper.updateById(update);
    }

    @Override
    public void delivery(Long id) {
        updateStatus(id, 3, 4);
    }

    @Override
    public void complete(Long id) {
        Orders order = getOrder(id);
        if (order.getStatus() != 4) {
            throw new BusinessException("订单状态不正确");
        }
        Orders update = new Orders();
        update.setId(id);
        update.setStatus(5);
        update.setDeliveryTime(LocalDateTime.now());
        ordersMapper.updateById(update);
    }

    private OrdersVO buildDetail(Long id, boolean checkUser) {
        Orders order = getOrder(id);
        if (checkUser && !currentUserId().equals(order.getUserId())) {
            throw new BusinessException("订单不存在");
        }
        List<OrderDetail> details = orderDetailMapper.selectList(
                new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, id));
        OrdersVO vo = new OrdersVO();
        BeanUtils.copyProperties(order, vo);
        vo.setDetails(details);
        return vo;
    }

    private Orders getUserOrder(Long id) {
        Orders order = getOrder(id);
        if (!currentUserId().equals(order.getUserId())) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    private Orders getOrder(Long id) {
        Orders order = ordersMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    private void updateStatus(Long id, int expected, int target) {
        Orders order = getOrder(id);
        if (order.getStatus() != expected) {
            throw new BusinessException("订单状态不正确");
        }
        Orders update = new Orders();
        update.setId(id);
        update.setStatus(target);
        ordersMapper.updateById(update);
    }

    private String joinAddress(AddressBook address) {
        return Stream.of(address.getProvinceName(), address.getCityName(),
                        address.getDistrictName(), address.getDetail())
                .filter(StringUtils::hasText)
                .collect(Collectors.joining());
    }

    private Long currentUserId() {
        Long id = BaseContext.getCurrentId();
        if (id == null) {
            throw new BusinessException("未登录");
        }
        return id;
    }
}
