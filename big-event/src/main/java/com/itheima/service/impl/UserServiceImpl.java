package com.itheima.service.impl;

import com.itheima.mapper.DoctorPatientMapper;
import com.itheima.mapper.UserMapper;
import com.itheima.pojo.DoctorPatient;
import com.itheima.pojo.User;
import com.itheima.service.UserService;
import com.itheima.utils.Md5Util;
import com.itheima.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DoctorPatientMapper doctorPatientMapper;

    @Override
    public User findByUserName(String username) {
        User u = userMapper.findByUserName(username);
        return u;
    }

    @Override
    public void register(String username, String password) {
        //加密
        String md5String = Md5Util.getMD5String(password);
        //添加
        userMapper.add(username,md5String);
    }

    @Override
    public void update(User user) {
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
    }

    @Override
    public void updateAvatar(String avatarUrl) {
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        userMapper.updateAvatar(avatarUrl,id);
    }

    @Override
    public void updatePwd(String newPwd) {
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        userMapper.updatePwd(Md5Util.getMD5String(newPwd),id);
    }


    @Override
    @Transactional
    public void bindPatientToDoctor(String username) {
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer doctorId = (Integer) map.get("id");

        User patient = userMapper.findByUserName(username);
        if (patient == null|| !patient.getIdentity().equals("patient")) {
            throw new IllegalArgumentException("病人不存在或身份不正确");
        }

        // 创建 DoctorPatient 关联记录
        DoctorPatient doctorPatient = new DoctorPatient();
        doctorPatient.setDoctorId(doctorId);
        doctorPatient.setPatientId(patient.getId());

        // 保存关联记录
        doctorPatientMapper.bindDoctorAndPatient(doctorPatient);
    }

    @Override
    public void addComment(Integer id, String comment) {
        userMapper.addComment(id,comment);
    }


}
