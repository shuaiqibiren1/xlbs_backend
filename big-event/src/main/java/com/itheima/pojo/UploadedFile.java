package com.itheima.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadedFile {
    private Integer id;
    private Integer associatedId;
    private String fileName;
    @JsonIgnore
    private String filePath;
    private String url;
    private Integer age;
    private String sex;
    private Integer cp;
    private Integer trestbps;
    private Integer chol;
    private Integer fbs;
    private Integer restecg;
    private Integer thalach;
    private Integer exang;
    private Integer oldpeak;
    private Integer ca;
    private Integer thal;
}
