package com.itheima.controller;


import com.itheima.pojo.Result;
import com.itheima.pojo.User;
import com.itheima.service.DoctorPatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/doctor-patients")
@Validated
public class DoctorPatientController {
    @Autowired
    private DoctorPatientService doctorPatientService;

    @GetMapping("/patients")
    public Result<List<Integer>> getPatientsByDoctorId() {
        return Result.success(doctorPatientService.getPatientIdsByDoctorId());
    }

    @GetMapping("/patientdetails")
    public Result<List<User>> getPatientDetailsByDoctorId() {
        return Result.success(doctorPatientService.getPatientDetailsByDoctorId());
    }

    @DeleteMapping("/deletepatient")
    public Result<String> deletePatient(@RequestBody Map<String, Integer> params) {
        Integer id = params.get("id");

        if (id == null) {
            return Result.error("ID cannot be null or empty");
        }

        return Result.success(doctorPatientService.deletePatientById(id));
    }
    // RequestBody -> JSON 格式
}
