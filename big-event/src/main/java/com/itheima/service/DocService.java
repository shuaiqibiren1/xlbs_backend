package com.itheima.service;

import com.itheima.pojo.UploadedFile;

public interface DocService {
    UploadedFile getheartParametersById(Integer id);

    void uploadheartParameters(UploadedFile file);
}
