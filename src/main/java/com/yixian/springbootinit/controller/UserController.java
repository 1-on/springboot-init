package com.yixian.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yixian.springbootinit.common.DeleteRequest;
import com.yixian.springbootinit.common.Result;
import com.yixian.springbootinit.constant.MessageConstant;
import com.yixian.springbootinit.context.BaseContext;
import com.yixian.springbootinit.exception.BaseException;
import com.yixian.springbootinit.model.dto.user.*;
import com.yixian.springbootinit.model.entity.User;
import com.yixian.springbootinit.model.vo.LoginUserVO;
import com.yixian.springbootinit.model.vo.UserVO;
import com.yixian.springbootinit.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/ok")
    public String ok() {
        return "ok";
    }

    /**
     * 用户注册
     *
     * @param userRegisterDTO 账号、密码、确认密码
     * @return
     */
    @PostMapping("/register")
    public Result<Long> userRegister(@RequestBody UserRegisterDTO userRegisterDTO) {
        if (userRegisterDTO == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        String userAccount = userRegisterDTO.getUserAccount();
        String userPassword = userRegisterDTO.getUserPassword();
        String checkPassword = userRegisterDTO.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return Result.success(result);
    }


    @PostMapping("/login")
    public Result<LoginUserVO> userLogin(@RequestBody UserLoginDTO userLoginDTO) {
        if (userLoginDTO == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        String userAccount = userLoginDTO.getUserAccount();
        String userPassword = userLoginDTO.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword);
        return Result.success(loginUserVO);
    }

    @GetMapping("/get/login")
    public Result<LoginUserVO> getLoginUser() {
        User user = userService.getLoginUser();
        return Result.success(userService.getLoginUserVo(user));
    }

    /**
     * 用户注销 前端移除jwt即可
     *
     * @return
     */
    @PostMapping("/logout")
    public Result<Boolean> userLogout() {
        return Result.success();
    }

    // 增删改查

    /**
     * 添加用户
     *
     * @param userAddDTO 用户信息
     * @return
     */
    @PostMapping("/add")
    public Result<Long> addUser(@RequestBody UserAddDTO userAddDTO) {
        if (userAddDTO == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        Long userId = userService.addUser(userAddDTO);
        return Result.success(userId);
    }

    /**
     * 删除用户
     *
     * @param deleteRequest 用户id
     * @return
     */
    @PostMapping("/delete")
    public Result<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return Result.success(b);
    }

    /**
     * 更新用户信息
     *
     * @param userUpdateDTO 用户信息
     * @return
     */
    @PostMapping("update")
    public Result<Boolean> updateUser(@RequestBody UserUpdateDTO userUpdateDTO) {
        if (userUpdateDTO == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateDTO, user);
        boolean result = userService.updateById(user);
        if (!result) {
            throw new BaseException(MessageConstant.OPERATION_ERROR);
        }
        return Result.success(true);
    }

    /**
     * 根据id获取用户信息（仅管理员）
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public Result<User> getUserById(long id) {
        if (id <= 0) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        User user = userService.getById(id);
        if (user == null) {
            throw new BaseException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        return Result.success(user);
    }

    /**
     * 根据id获取用户信息包装类
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public Result<UserVO> getUserVOById(long id) {
        Result<User> userById = getUserById(id);
        User user = userById.getData();
        return Result.success(userService.getUserVO(user));
    }

    /**
     * 分页获取用户列表（仅管理员）
     *
     * @param userQueryDTO
     * @return
     */
    @PostMapping("/list/page")
    public Result<Page<User>> listUserByPage(@RequestBody UserQueryDTO userQueryDTO) {
        if (userQueryDTO == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        int current = userQueryDTO.getCurrent();
        int size = userQueryDTO.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size), userService.getQueryWrapper(userQueryDTO));
        return Result.success(userPage);
    }

    /**
     * 分页获取用户封装列表
     *
     * @param userQueryDTO
     * @return
     */
    @PostMapping("/list/page/vo")
    public Result<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryDTO userQueryDTO) {
        if (userQueryDTO == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        int current = userQueryDTO.getCurrent();
        int size = userQueryDTO.getPageSize();
        // 限制爬虫
        if (size > 20) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        Page<User> userPage = userService.page(new Page<>(current, size), userService.getQueryWrapper(userQueryDTO));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return Result.success(userVOPage);
    }

    @PostMapping("/update/my")
    public Result<Boolean> updateMyUser(@RequestBody UserUpdateMyDTO userUpdateMyDTO) {
        if (userUpdateMyDTO == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyDTO, user);
        user.setId(BaseContext.getCurrentId());
        boolean result = userService.updateById(user);
        if (!result) {
            throw new BaseException(MessageConstant.OPERATION_ERROR);
        }
        return Result.success(true);
    }

}
