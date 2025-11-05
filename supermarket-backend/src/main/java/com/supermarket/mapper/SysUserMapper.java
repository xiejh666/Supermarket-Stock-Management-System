package com.supermarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.entity.SysUser;
import com.supermarket.vo.UserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户Mapper
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名查询用户（包含角色信息）
     */
    @Select("SELECT u.*, r.role_name, r.role_code " +
            "FROM sys_user u " +
            "LEFT JOIN sys_role r ON u.role_id = r.id " +
            "WHERE u.username = #{username}")
    SysUser selectByUsernameWithRole(@Param("username") String username);

    /**
     * 查询用户列表（包含角色信息）
     */
    List<UserVO> selectUserVOList(@Param("username") String username,
                                   @Param("realName") String realName,
                                   @Param("status") Integer status);
}



