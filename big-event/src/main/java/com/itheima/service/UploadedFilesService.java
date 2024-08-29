package com.itheima.service;

import com.itheima.pojo.UploadedFile;

import java.util.List;

public interface UploadedFilesService {
    void addFile(String originalFilename, String filePath);

    UploadedFile getFileById(Integer fileId);

    void deleteFile(Integer fileId);

    List<UploadedFile> getFilesById();

    List<UploadedFile> getPatientFilesById(Integer id);

    void addFileById(Integer id, String originalFilename, String filePath);

    void addFileAndUrl(String originalFilename, String filePath,String uploadedUrl);

    void addFileAndUrlById(Integer id, String originalFilename, String filePath, String uploadedUrl);

    String getniipathById(Integer id);
}
