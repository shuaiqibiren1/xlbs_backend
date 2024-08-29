package com.itheima.mapper;

import com.itheima.pojo.UploadedFile;
import com.itheima.pojo.User;
import org.apache.ibatis.annotations.*;
//import com.itheima.pojo.User;

import java.util.List;
//import org.apache.ibatis.annotations.Mapper;
//import org.apache.ibatis.annotations.Select;
//import org.apache.ibatis.annotations.Update;
//import java.util.List;
//import org.apache.ibatis.annotations.Param;

@Mapper
public interface UploadedFilesMapper {

    @Insert("INSERT INTO uploaded_files (associated_id, file_name, file_path) " +
            "VALUES (#{associatedId}, #{fileName}, #{filePath})")
    void insertFile(Integer associatedId, String fileName, String filePath);

    @Select("SELECT * FROM uploaded_files WHERE id = #{fileId}")
    UploadedFile findFileById(Integer fileId);

    @Delete("DELETE FROM uploaded_files WHERE id = #{fileId}")
    void deleteFile(Integer fileId);

    @Select("SELECT * FROM uploaded_files WHERE associated_id = #{id}")
    List<UploadedFile> getFilesById(Integer id);

    @Insert("INSERT INTO uploaded_files (associated_id, file_name, url, file_path) " +
            "VALUES (#{associatedId}, #{fileName}, #{uploadedUrl}, #{filePath})")
    void insertFileAndUrl(Integer associatedId, String fileName, String filePath, String uploadedUrl);

    @Select("SELECT file_path FROM uploaded_files WHERE id = #{id}")
    String getniipathById(@Param("id") Integer id);
}

