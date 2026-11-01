package com.sky.service;

import com.sky.entity.AddressBook;

import java.util.List;

/** 地址簿业务接口。 */
public interface AddressBookService {

    List<AddressBook> list();

    AddressBook getDefault();

    AddressBook getById(Long id);

    void save(AddressBook addressBook);

    void update(AddressBook addressBook);

    void delete(Long id);

    void setDefault(Long id);
}
