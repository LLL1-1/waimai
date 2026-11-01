package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.exception.BusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 地址簿实现。 */
@Service
@RequiredArgsConstructor
public class AddressBookServiceImpl implements AddressBookService {

    private final AddressBookMapper addressBookMapper;

    @Override
    public List<AddressBook> list() {
        return addressBookMapper.selectList(new LambdaQueryWrapper<AddressBook>()
                .eq(AddressBook::getUserId, currentUserId())
                .orderByDesc(AddressBook::getIsDefault)
                .orderByDesc(AddressBook::getId));
    }

    @Override
    public AddressBook getDefault() {
        AddressBook address = addressBookMapper.selectOne(new LambdaQueryWrapper<AddressBook>()
                .eq(AddressBook::getUserId, currentUserId())
                .eq(AddressBook::getIsDefault, 1)
                .last("limit 1"));
        if (address != null) {
            return address;
        }
        List<AddressBook> list = list();
        if (list.isEmpty()) {
            throw new BusinessException("还没有收货地址");
        }
        return list.get(0);
    }

    @Override
    public AddressBook getById(Long id) {
        AddressBook address = addressBookMapper.selectOne(new LambdaQueryWrapper<AddressBook>()
                .eq(AddressBook::getId, id)
                .eq(AddressBook::getUserId, currentUserId()));
        if (address == null) {
            throw new BusinessException("地址不存在");
        }
        return address;
    }

    @Override
    @Transactional
    public void save(AddressBook addressBook) {
        addressBook.setId(null);
        addressBook.setUserId(currentUserId());
        addressBook.setIsDefault(addressBook.getIsDefault() == null ? 0 : addressBook.getIsDefault());
        if (addressBook.getIsDefault() == 1) {
            clearDefault();
        }
        addressBookMapper.insert(addressBook);
    }

    @Override
    @Transactional
    public void update(AddressBook addressBook) {
        if (addressBook.getId() == null) {
            throw new BusinessException("地址id不能为空");
        }
        AddressBook existing = getById(addressBook.getId());
        addressBook.setUserId(currentUserId());
        if (addressBook.getIsDefault() != null && addressBook.getIsDefault() == 1) {
            clearDefault();
        } else {
            addressBook.setIsDefault(existing.getIsDefault());
        }
        addressBookMapper.updateById(addressBook);
    }

    @Override
    public void delete(Long id) {
        getById(id);
        addressBookMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void setDefault(Long id) {
        getById(id);
        clearDefault();
        AddressBook address = new AddressBook();
        address.setId(id);
        address.setIsDefault(1);
        addressBookMapper.updateById(address);
    }

    private void clearDefault() {
        AddressBook update = new AddressBook();
        update.setIsDefault(0);
        addressBookMapper.update(update, new LambdaUpdateWrapper<AddressBook>()
                .eq(AddressBook::getUserId, currentUserId()));
    }

    private Long currentUserId() {
        Long id = BaseContext.getCurrentId();
        if (id == null) {
            throw new BusinessException("未登录");
        }
        return id;
    }
}
