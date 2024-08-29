package com.itheima.controller;

import com.itheima.pojo.Result;
import com.itheima.pojo.UploadedFile;
import com.itheima.service.UploadedFilesService;
import com.itheima.utils.AliOssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class FileUploadController {

    @Autowired
    private UploadedFilesService uploadedFilesService;


    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${python.interpreter}")
    private String pythonInterpreter;

    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) throws Exception {
        //把文件的内容存储到本地磁盘上
        String originalFilename = file.getOriginalFilename();
        //保证文件的名字是唯一的,从而防止文件覆盖
        String filename = UUID.randomUUID().toString()+originalFilename.substring(originalFilename.lastIndexOf("."));
        //file.transferTo(new File("D:\\spring-boot-projects\\datastore\\"+filename));
        String url = AliOssUtil.uploadFile(filename,file.getInputStream());
        return Result.success(url);
    }

    @PostMapping("/uploadnii")
    public Result<String> uploadNii(MultipartFile file) throws Exception {
        // 获取文件名
        String originalFilename = file.getOriginalFilename();

        // 文件保存路径
        // String filePath = "D:\\spring-boot-projects\\datastore\\" + originalFilename;
        String filePath = uploadDir + originalFilename;

        try {
            // 将文件保存到本地磁盘
            file.transferTo(new File(filePath));

            // 将文件信息插入到数据库中
            uploadedFilesService.addFile(originalFilename, filePath);

            // 返回成功的结果
            return Result.success("File uploaded successfully. URL: " + filePath);
        } catch (IOException e) {
            // 处理文件保存异常
            return Result.error("Failed to upload file: " + e.getMessage());
        }
    }

    @PostMapping("/uploadniibyid")
    public Result<String> uploadNiiById(MultipartFile file,@RequestParam Integer id) throws Exception {
        // 获取文件名
        String originalFilename = file.getOriginalFilename();

        // 文件保存路径
        // String filePath = "D:\\spring-boot-projects\\datastore\\" + originalFilename;
        String filePath = uploadDir + originalFilename;

        try {
            // 将文件保存到本地磁盘
            file.transferTo(new File(filePath));

            // 将文件信息插入到数据库中
            uploadedFilesService.addFileById(id,originalFilename, filePath);

            // 返回成功的结果
            return Result.success("File uploaded successfully. URL: " + filePath);
        } catch (IOException e) {
            // 处理文件保存异常
            return Result.error("Failed to upload file: " + e.getMessage());
        }
    }


    @DeleteMapping("/delete")
    public Result<String> deleteFile(@RequestBody Map<String, Integer> params) {
        Integer fileId = params.get("fileId");

        try {
            // 获取文件信息
            UploadedFile file = uploadedFilesService.getFileById(fileId);
            if (file == null) {
                return Result.error("File not found");
            }

            // 删除数据库中的文件记录
            uploadedFilesService.deleteFile(fileId);

            // 调用 Python 脚本删除文件
            boolean isDeleted = deleteFileWithPython(file.getFilePath());
            if (!isDeleted) {
                return Result.error("Failed to delete file from disk");
            }

            return Result.success("File deleted successfully");
        } catch (Exception e) {
            return Result.error("Failed to delete file: " + e.getMessage());
        }
    }

    private boolean deleteFileWithPython(String filePath) {
        try {
            ProcessBuilder pb = new ProcessBuilder("python", "script/delete_file.py", filePath);
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    @GetMapping("/files")
    public Result<List<UploadedFile>> getFilesById() {
        return Result.success(uploadedFilesService.getFilesById());
    }

    @GetMapping("/patientfiles")
    public Result<List<UploadedFile>> getPatientFilesById(@RequestParam Integer id) {
        if (id == null) {
            return Result.error("ID cannot be null or empty");
        }

        try {
            return Result.success(uploadedFilesService.getPatientFilesById(id));
        } catch (Exception e) {
            return Result.error("Failed to get patients files: " + e.getMessage());
        }
    }

    @PostMapping("/uploadurl")
    public Result<String> uploadUrl(MultipartFile file) throws Exception {
        // 获取文件名
        String originalFilename = file.getOriginalFilename();

        // 检查文件名是否以 .nii 结尾
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".nii")) {
            return Result.error("Invalid file type. Please upload a file with .nii extension.");
        }

        // 文件路径
        String filePath = uploadDir + originalFilename;

        String url;

        try {
            // 保存上传的NII文件
            file.transferTo(new File(filePath));

            // 调用Python脚本进行图像处理
            String pythonScriptPath = "script/extract_slice.py";
            ProcessBuilder processBuilder = new ProcessBuilder(pythonInterpreter, pythonScriptPath, filePath);

            processBuilder.redirectErrorStream(true); // 将错误流合并到输入流中
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line); // 打印输出
            }


            // 等待脚本执行完成
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Python script error: " + exitCode);
            }

            // 提取的 JPG 文件路径
            String sliceFilePath = filePath.replace(".nii", "_middle_slice.jpg");

            // 将 JPG 文件以流的形式上传到云端
            try (FileInputStream fis = new FileInputStream(sliceFilePath)) {
                // 使用文件名作为对象名
                String objectName = sliceFilePath.substring(sliceFilePath.lastIndexOf("\\") + 1);
                String uploadedUrl = AliOssUtil.uploadFile(objectName, fis);
                url = uploadedUrl;
                // 保存上传链接到数据库或做其他处理
                uploadedFilesService.addFileAndUrl(originalFilename, filePath, uploadedUrl);
            }

            return Result.success(url);
        } catch (IOException | InterruptedException e) {
            return Result.error("Failed to upload file: " + e.getMessage());
        }
    }

    @PostMapping("/uploadurlbyid")
    public Result<String> uploadUrlById(MultipartFile file,@RequestParam Integer id) throws Exception {
        // 获取文件名
        String originalFilename = file.getOriginalFilename();

        // 检查文件名是否以 .nii 结尾
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".nii")) {
            return Result.error("Invalid file type. Please upload a file with .nii extension.");
        }

        // 文件路径
        String filePath = uploadDir + originalFilename;

        String url;

        try {
            // 保存上传的NII文件
            file.transferTo(new File(filePath));

            // 调用Python脚本进行图像处理
            String pythonScriptPath = "script/extract_slice.py"; // 替换为你的脚本路径
            ProcessBuilder processBuilder = new ProcessBuilder(pythonInterpreter, pythonScriptPath, filePath);

            processBuilder.redirectErrorStream(true); // 将错误流合并到输入流中
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line); // 打印输出
            }


            // 等待脚本执行完成
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Python script error: " + exitCode);
            }

            // 提取的 JPG 文件路径
            String sliceFilePath = filePath.replace(".nii", "_middle_slice.jpg");

            // 将 JPG 文件以流的形式上传到云端
            try (FileInputStream fis = new FileInputStream(sliceFilePath)) {
                // 使用文件名作为对象名
                String objectName = sliceFilePath.substring(sliceFilePath.lastIndexOf("\\") + 1);
                String uploadedUrl = AliOssUtil.uploadFile(objectName, fis);
                url = uploadedUrl;
                // 保存上传链接到数据库或做其他处理
                uploadedFilesService.addFileAndUrlById(id,originalFilename, filePath, uploadedUrl);
            }

            return Result.success(url);
        } catch (IOException | InterruptedException e) {
            return Result.error("Failed to upload file: " + e.getMessage());
        }
    }

    @GetMapping("/getImage")
    public Result<List<String>> getImageUrls(@RequestParam Integer id) throws Exception {
        String filePath = uploadedFilesService.getniipathById(id);
        List<String> urls = new ArrayList<>();

        try {
            // 调用Python脚本
            String pythonScriptPath = "script/segimage.py"; // 替换为你的脚本路径
            ProcessBuilder processBuilder = new ProcessBuilder(pythonInterpreter, pythonScriptPath, filePath);

            processBuilder.redirectErrorStream(true); // 将错误流合并到输入流中
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line); // 打印输出
            }

            // 等待脚本执行完成
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Python script error: " + exitCode);
            }

            // 处理生成的JPG文件
            String sliceFilePath = filePath.replace(".nii", "_slice.jpg");
            for (int i = 0; i < 10; i++) {
                String newsliceFilePath = sliceFilePath.replace(".jpg", "_" + String.valueOf(i) + ".jpg");

                // 将 JPG 文件以流的形式上传到云端
                try (FileInputStream fis = new FileInputStream(newsliceFilePath)) {
                    // 使用文件名作为对象名
                    String objectName = newsliceFilePath.substring(newsliceFilePath.lastIndexOf("\\") + 1);
                    String uploadedUrl = AliOssUtil.uploadFile(objectName, fis);
                    urls.add(uploadedUrl);
                }
            }
            return Result.success(urls);
        } catch (IOException | InterruptedException e) {
            return Result.error("Failed to upload file: " + e.getMessage());
        }
    }



    @PostMapping("/uploadtestbyid")
    public Result<String> uploadtest(@RequestParam("file") MultipartFile file, @RequestParam Integer id) {
        // 检查文件是否为空
        if (file.isEmpty()) {
            return Result.error("File is empty. Please upload a valid file.");
        }

        // 获取并检查文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".nii")) {
            return Result.error("Invalid file type. Please upload a file with .nii extension.");
        }

        // 安全文件名生成
        String safeFileName = UUID.randomUUID().toString() + ".nii"; // 生成唯一文件名
        String filePath = uploadDir + safeFileName;

        try {
            // 确保上传目录存在
            Files.createDirectories(Paths.get(uploadDir));

            // 保存上传的NII文件
            file.transferTo(new File(filePath));

            // 执行 Python 脚本并处理输出
            String sliceFilePath = executePythonScript(filePath);
            if (sliceFilePath == null) {
                return Result.error("Failed to execute Python script.");
            }

            // 将 JPG 文件以流的形式上传到云端
            String uploadedUrl = uploadSliceFile(sliceFilePath);

            // 保存上传链接到数据库或做其他处理
            uploadedFilesService.addFileAndUrlById(id, originalFilename, filePath, uploadedUrl);

            return Result.success(uploadedUrl);
        } catch (IOException e) {
            return Result.error("Failed to upload file: " + e.getMessage());
        }
    }

    private String executePythonScript(String filePath) {
        try {
            String pythonScriptPath = "script/extract_slice.py"; // 替换为你的脚本路径
            ProcessBuilder processBuilder = new ProcessBuilder(pythonInterpreter, pythonScriptPath, filePath);
            processBuilder.redirectErrorStream(true); // 合并错误与正常输出流
            Process process = processBuilder.start();

            // 读取脚本输出
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line); // 打印输出日志
                }
            }

            // 等待脚本执行完成并检查退出代码
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Python script error: exit code " + exitCode);
            }

            // 提取的 JPG 文件路径
            return filePath.replace(".nii", "_middle_slice.jpg");
        } catch (IOException | InterruptedException e) {
            System.out.println("Error executing Python script: ");
            return null;
        }
    }

    private String uploadSliceFile(String sliceFilePath) throws IOException {

        // 将 JPG 文件以流的形式上传到云端
        try (FileInputStream fis = new FileInputStream(sliceFilePath)) {
            String objectName = sliceFilePath.substring(sliceFilePath.lastIndexOf("\\") + 1);
            return AliOssUtil.uploadFile(objectName, fis);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
