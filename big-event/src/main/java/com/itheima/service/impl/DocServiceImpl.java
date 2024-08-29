package com.itheima.service.impl;

import com.itheima.mapper.DocMapper;
import com.itheima.pojo.UploadedFile;
import com.itheima.service.DocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DocServiceImpl implements DocService {

    @Autowired
    private DocMapper docMapper;

    @Override
    public UploadedFile getheartParametersById(Integer id) {
        return docMapper.getheartParametersById(id);
    }

    @Override
    public void uploadheartParameters(UploadedFile file) {
        docMapper.uploadheartParameters(file);
    }
}

