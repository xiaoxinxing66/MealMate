package com.sivan.oauth2.server.service;

import com.sivan.commons.model.domain.SignInIdentity;
import com.sivan.commons.model.pojo.Diners;
import com.sivan.commons.utils.AssertUtil;
import com.sivan.oauth2.server.mapper.DinersMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Author 不知名网友鑫
 * @Date 2023/9/4
 **/
/*
    登录校验
 */
@Service
public class UserService implements UserDetailsService {
    @Resource
    DinersMapper dinersMapper;
    /**
     *
     * @param s
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        AssertUtil.isNotEmpty(s,"请输入用户名");
        Diners diners = dinersMapper.selectByAccountInfo(s);
        if(diners == null){
            throw new UsernameNotFoundException("用户名或密码错误，请重新输入");
        }
        //优化初始化登录认证对象
        SignInIdentity signInIdentity = new SignInIdentity();
        BeanUtils.copyProperties(diners , signInIdentity);
        return signInIdentity;
    }
}
