package com.yixian.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.yixian.springbootinit.model.dto.user.UserAddDTO;
import com.yixian.springbootinit.model.dto.user.UserQueryDTO;
import com.yixian.springbootinit.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yixian.springbootinit.model.vo.LoginUserVO;
import com.yixian.springbootinit.model.vo.UserVO;

import java.util.List;

/**
 * @author jiangfei
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2024-02-08 18:44:01
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param checkPassword 确认密码
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);


    /**
     * 用户登录
     *
     * @param userAccount  账号
     * @param userPassword 密码
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword);

    /**
     * 获取完整登录用户信息
     *
     * @return
     */
    User getLoginUser();

    /**
     * 获取脱敏的登录用户信息
     *
     * @param user 需要脱敏的用户
     * @return
     */
    LoginUserVO getLoginUserVo(User user);

    /**
     * 新增用户
     *
     * @param userAddDTO 用户信息
     * @return
     */
    Long addUser(UserAddDTO userAddDTO);

    /**
     * 获取脱敏用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取查询条件
     *
     * @param userQueryDTO
     * @return
     */
    Wrapper<User> getQueryWrapper(UserQueryDTO userQueryDTO);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 判断是否为管理员
     *
     * @param currentId
     * @return
     */
    boolean isAdmin(Long currentId);
}
