package com.sky.controller.user;

import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 用户端地址簿接口。 */
@RestController
@RequestMapping("/user/addressBook")
@RequiredArgsConstructor
@Tag(name = "地址簿")
public class AddressBookController {

    private final AddressBookService addressBookService;

    @GetMapping("/list")
    @Operation(summary = "地址列表")
    public Result<List<AddressBook>> list() {
        return Result.success(addressBookService.list());
    }

    @GetMapping("/default")
    @Operation(summary = "默认地址")
    public Result<AddressBook> getDefault() {
        return Result.success(addressBookService.getDefault());
    }

    @GetMapping("/{id}")
    @Operation(summary = "地址详情")
    public Result<AddressBook> getById(@org.springframework.web.bind.annotation.PathVariable Long id) {
        return Result.success(addressBookService.getById(id));
    }

    @PostMapping
    @Operation(summary = "新增地址")
    public Result<Void> save(@RequestBody AddressBook addressBook) {
        addressBookService.save(addressBook);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改地址")
    public Result<Void> update(@RequestBody AddressBook addressBook) {
        addressBookService.update(addressBook);
        return Result.success();
    }

    @DeleteMapping
    @Operation(summary = "删除地址")
    public Result<Void> delete(@RequestParam Long id) {
        addressBookService.delete(id);
        return Result.success();
    }

    @PutMapping("/default")
    @Operation(summary = "设为默认地址")
    public Result<Void> setDefault(@RequestParam Long id) {
        addressBookService.setDefault(id);
        return Result.success();
    }
}
