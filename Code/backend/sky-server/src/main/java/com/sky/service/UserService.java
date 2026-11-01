package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.vo.UserLoginVO;

/** 用户业务接口。 */
public interface UserService {

    /** 用户登录，code 先作为 mock openid。 */
    UserLoginVO login(UserLoginDTO userLoginDTO);
}
