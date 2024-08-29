package com.itheima.mapper;

import com.itheima.pojo.UploadedFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface DocMapper {

    @Select("SELECT * FROM uploaded_files WHERE id = #{id}")
    UploadedFile getheartParametersById(Integer id);

    @Update("update uploaded_files set age=#{age},sex=#{sex},cp=#{cp},trestbps=#{trestbps}," +
            "chol=#{chol},fbs=#{fbs},restecg=#{restecg},thalach=#{thalach}," +
            "exang=#{exang},oldpeak=#{oldpeak},ca=#{ca},thal=#{thal} where id=#{id}")
    void uploadheartParameters(UploadedFile file);
}
