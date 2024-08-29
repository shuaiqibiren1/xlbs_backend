package com.itheima.service.impl;

import com.itheima.mapper.UploadedFilesMapper;
import com.itheima.pojo.UploadedFile;
import com.itheima.service.UploadedFilesService;
import com.itheima.utils.ThreadLocalUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Service
public class UploadedFilesServiceImpl implements UploadedFilesService {
    @Autowired
    private UploadedFilesMapper uploadedFilesMapper;

    @Override
    public void addFile(String fileName, String filePath) {
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer associatedId = (Integer) map.get("id");
        uploadedFilesMapper.insertFile(associatedId, fileName, filePath);
    }

    @Override
    public UploadedFile getFileById(Integer fileId) {
        return uploadedFilesMapper.findFileById(fileId);
    }

    @Override
    public void deleteFile(Integer fileId) {
        uploadedFilesMapper.deleteFile(fileId);
    }

    @Override
    public List<UploadedFile> getFilesById() {
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer Id = (Integer) map.get("id");
        return uploadedFilesMapper.getFilesById(Id);
    }

    @Override
    public List<UploadedFile> getPatientFilesById(Integer id) {
        return uploadedFilesMapper.getFilesById(id);
    }

    @Override
    public void addFileById(Integer associatedId, String fileName, String filePath) {
        uploadedFilesMapper.insertFile(associatedId, fileName, filePath);
    }

    @Override
    public void addFileAndUrl(String fileName, String filePath, String uploadedUrl) {
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer associatedId = (Integer) map.get("id");
        uploadedFilesMapper.insertFileAndUrl(associatedId, fileName, filePath, uploadedUrl);
    }

    @Override
    public void addFileAndUrlById(Integer associatedId, String fileName, String filePath, String uploadedUrl) {
        uploadedFilesMapper.insertFileAndUrl(associatedId, fileName, filePath, uploadedUrl);
    }

    @Override
    public String getniipathById(Integer id) {
        return uploadedFilesMapper.getniipathById(id);
    }
}

