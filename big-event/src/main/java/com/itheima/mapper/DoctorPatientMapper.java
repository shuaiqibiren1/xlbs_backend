package com.itheima.mapper;

import com.itheima.pojo.DoctorPatient;
import com.itheima.pojo.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DoctorPatientMapper {
    // 插入医生与病人的关联记录
    @Insert("insert into doctor_patient(doctor_id, patient_id) values(#{doctorId}, #{patientId})")
    void bindDoctorAndPatient(DoctorPatient doctorPatient);

    @Select("SELECT patient_id FROM doctor_patient WHERE doctor_id = #{doctorId}")
    List<Integer> findPatientIdsByDoctorId(@Param("doctorId") Integer doctorId);

    @Delete("DELETE FROM doctor_patient WHERE doctor_id = #{doctorId} AND patient_id = #{patientId}")
    void deleteById(@Param("doctorId") Integer doctorId, @Param("patientId") Integer patientId);

}