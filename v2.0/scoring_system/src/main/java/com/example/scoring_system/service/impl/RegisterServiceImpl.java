package com.example.scoring_system.service.impl;

import com.example.scoring_system.bean.User;
import com.example.scoring_system.mapper.UserMapper;
import com.example.scoring_system.service.RegisterService;
import com.example.scoring_system.utils.SaltUtils;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegisterServiceImpl implements RegisterService {
    final int SALT_SIZE = 8;

    @Autowired
    UserMapper userMapper;

    /**
     * @Description: return值：-1:用户名已经存在，0：插入数据库失败，
     * @Author: 曹鑫
     * @Date: 2021/4/14
     */
    @Override
    public Integer register(User user) {
        if (userMapper.selUserByUserName(user) != null) {
            System.out.println("用户名已经存在");
            //用户名已经存在
            return -1;
        }
        //生成随机盐并保存
        String salt = SaltUtils.getSalt(SALT_SIZE);
        user.setSalt(salt);
        //对明文密码进行md5+salt+hash散列
        Md5Hash md5Hash = new Md5Hash(user.getPassword(), salt, 1024);
        user.setPassword(md5Hash.toHex());
        return userMapper.insUser(user);
    }
}
