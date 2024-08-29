-- 创建数据库
create database big_event;

-- 使用数据库
use big_event;

-- 用户表
create table user (
                      id int unsigned primary key auto_increment comment 'ID',
                      username varchar(20) not null unique comment '用户名',
                      password varchar(32)  comment '密码',
                      nickname varchar(10)  default '' comment '昵称',
                      email varchar(128) default '' comment '邮箱',
                      user_pic varchar(128) default '' comment '头像',
                      create_time datetime not null comment '创建时间',
                      update_time datetime not null comment '修改时间'
) comment '用户表';


ALTER TABLE user
    ADD COLUMN identity varchar(50) default '' comment '身份' AFTER username;

-- 创建 doctor_patient 表
create table doctor_patient (
                                doctor_id int unsigned not null comment '医生ID',
                                patient_id int unsigned not null comment '病人ID',
                                primary key (doctor_id, patient_id),
                                foreign key (doctor_id) references user(id),
                                foreign key (patient_id) references user(id)
) comment '医生与病人关联表';

SELECT * FROM user WHERE username = 'niubi';
ALTER TABLE user
    ADD COLUMN comment varchar(50) default '' comment '评语' AFTER identity;

CREATE TABLE uploaded_files (
                                id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '文件记录的唯一标识符',
                                associated_id INT UNSIGNED NOT NULL COMMENT '与文件相关联的ID',
                                file_name VARCHAR(255) NOT NULL COMMENT '文件名称',
                                file_path VARCHAR(255) COMMENT '文件在服务器上的存储路径',
                                FOREIGN KEY (associated_id) REFERENCES user(id) -- 如果 associated_id 是用户 ID，可以这样设置外键
) COMMENT '上传文件信息表';
