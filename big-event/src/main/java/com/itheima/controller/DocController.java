package com.itheima.controller;

import com.itheima.pojo.Result;
import com.itheima.pojo.UploadedFile;
import com.itheima.pojo.User;
import com.itheima.service.DocService;
import com.itheima.utils.AliOssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
public class DocController {
    @Autowired
    private DocService docService;

    @GetMapping("/heartParameters")
    public Result<UploadedFile> heartParametersById(@RequestParam Integer id) throws Exception {
        return Result.success(docService.getheartParametersById(id));
    }

    @PutMapping("/uploadheartParameters")
    public Result uploadheartParameters(@RequestBody @Validated UploadedFile file) throws Exception {
        docService.uploadheartParameters(file);
        return Result.success();
    }
//    {
//            "id": 1,
//            "age": 45,
//            ......
//            "thal": 2
//    }
    
}
