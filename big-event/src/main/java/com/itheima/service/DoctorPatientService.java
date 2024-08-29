package com.itheima.service;

import com.itheima.pojo.User;

import java.util.List;

public interface DoctorPatientService {
    List<Integer> getPatientIdsByDoctorId();

    List<User> getPatientDetailsByDoctorId();

    String deletePatientById(Integer id);
}
