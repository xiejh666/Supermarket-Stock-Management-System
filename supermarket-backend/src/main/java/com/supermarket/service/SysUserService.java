package com.supermarket.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.supermarket.dto.UserDTO;
import com.supermarket.entity.SysUser;
import com.supermarket.vo.UserVO;
import com.supermarket.vo.UserStatusVO;

/**
 * 用户服务接口
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 分页查询用户列表
     */
    Page<UserVO> getUserList(Integer current, Integer size, String username, String realName, Integer status);

    /**
     * 创建用户
     */
    void createUser(UserDTO dto);

    /**
     * 更新用户
     */
    void updateUser(UserDTO dto);

    /**
     * 删除用户
     */
    void deleteUser(Long id);

    /**
     * 修改用户状态
     */
    void updateUserStatus(Long id, Integer status);

    /**
     * 重置密码
     */
    void resetPassword(Long id, String newPassword);

    /**
     * 获取用户状态
     */
    UserStatusVO getUserStatus(Long id);
}



