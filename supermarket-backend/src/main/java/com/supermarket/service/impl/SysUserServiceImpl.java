package com.supermarket.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.dto.UserDTO;
import com.supermarket.entity.SysUser;
import com.supermarket.exception.BusinessException;
import com.supermarket.mapper.SysUserMapper;
import com.supermarket.service.SysUserService;
import com.supermarket.vo.UserVO;
import com.supermarket.vo.UserStatusVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Page<UserVO> getUserList(Integer current, Integer size, String username, String realName, Integer status) {
        Page<UserVO> page = new Page<>(current, size);
        List<UserVO> list = userMapper.selectUserVOList(username, realName, status);
        
        // 计算分页
        int total = list.size();
        int start = (current - 1) * size;
        int end = Math.min(start + size, total);
        
        List<UserVO> pageList = list.subList(start, end);
        page.setRecords(pageList);
        page.setTotal(total);
        
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(UserDTO dto) {
        // 检查用户名是否存在
        Long count = lambdaQuery()
                .eq(SysUser::getUsername, dto.getUsername())
                .count();
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 密码必填
        if (!StringUtils.hasText(dto.getPassword())) {
            throw new BusinessException("密码不能为空");
        }

        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        
        // 默认启用
        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        userMapper.insert(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("用户ID不能为空");
        }

        SysUser user = userMapper.selectById(dto.getId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 检查用户名是否被其他用户占用
        Long count = lambdaQuery()
                .eq(SysUser::getUsername, dto.getUsername())
                .ne(SysUser::getId, dto.getId())
                .count();
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        BeanUtils.copyProperties(dto, user, "password"); // 不复制密码

        // 如果提供了新密码，则更新密码
        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 不能删除admin用户
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException("不能删除管理员账号");
        }

        userMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long id, Integer status) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 不能禁用admin用户
        if ("admin".equals(user.getUsername()) && status == 0) {
            throw new BusinessException("不能禁用管理员账号");
        }

        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long id, String newPassword) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (!StringUtils.hasText(newPassword)) {
            throw new BusinessException("新密码不能为空");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    @Override
    public UserStatusVO getUserStatus(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        String statusDesc = user.getStatus() == 1 ? "启用" : "禁用";

        return UserStatusVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .status(user.getStatus())
                .statusDesc(statusDesc)
                .build();
    }
}



