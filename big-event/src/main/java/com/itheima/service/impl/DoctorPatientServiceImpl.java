package com.itheima.service.impl;

import com.itheima.mapper.DoctorPatientMapper;
import com.itheima.mapper.UserMapper;
import com.itheima.pojo.User;
import com.itheima.service.DoctorPatientService;
import com.itheima.utils.ThreadLocalUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Service
public class DoctorPatientServiceImpl implements DoctorPatientService {

    @Autowired
    private DoctorPatientMapper doctorPatientMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<Integer> getPatientIdsByDoctorId() {
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer doctorId = (Integer) map.get("id");
        return doctorPatientMapper.findPatientIdsByDoctorId(doctorId);
    }

    public List<User> getPatientDetailsByDoctorId() {
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer doctorId = (Integer) map.get("id");
        // 获取病人 ID 列表
        List<Integer> patientIds = doctorPatientMapper.findPatientIdsByDoctorId(doctorId);
        // 获取病人详细信息
        return userMapper.findUsersByIds(patientIds);
    }

    @Override
    public String deletePatientById(Integer patientId) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer doctorId = (Integer) map.get("id");
        try {
            doctorPatientMapper.deleteById(doctorId, patientId);
            return "Patient deleted successfully";
        } catch (Exception e) {
            // 记录错误日志（可选）
            e.printStackTrace();
            return "Error deleting patient: " + e.getMessage();
        }
    }

}
