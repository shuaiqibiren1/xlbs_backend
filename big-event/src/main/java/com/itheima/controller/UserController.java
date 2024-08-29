package com.itheima.controller;

import com.itheima.pojo.BindRequest;
import com.itheima.pojo.Result;
import com.itheima.pojo.User;
import com.itheima.service.UserService;
import com.itheima.utils.JwtUtil;
import com.itheima.utils.Md5Util;
import com.itheima.utils.ThreadLocalUtil;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Validated
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @PostMapping("/register")
    public Result register(@Pattern(regexp = "^\\S{5,16}$") String username, @Pattern(regexp = "^\\S{5,16}$") String password) {

        //查询用户
        User u = userService.findByUserName(username);
        if (u == null) {
            //没有占用
            //注册
            userService.register(username, password);
            return Result.success();
        } else {
            //占用
            return Result.error("用户名已被占用");
        }
    }

    @PostMapping("/login")
    public Result<String> login(@Pattern(regexp = "^\\S{5,16}$") String username, @Pattern(regexp = "^\\S{5,16}$") String password) {
        //根据用户名查询用户
        User loginUser = userService.findByUserName(username);
        //判断该用户是否存在
        if (loginUser == null) {
            return Result.error("用户名错误");
        }

        //判断密码是否正确  loginUser对象中的password是密文
        if (Md5Util.getMD5String(password).equals(loginUser.getPassword())) {
            //登录成功
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", loginUser.getId());
            claims.put("username", loginUser.getUsername());
            String token = JwtUtil.genToken(claims);
            //把token存储到redis中
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            operations.set(token,token,1, TimeUnit.HOURS);
            return Result.success(token);
        }
        return Result.error("密码错误");
    }

    @GetMapping("/userInfo")
    public Result<User> userInfo(/*@RequestHeader(name = "Authorization") String token*/) {
        //根据用户名查询用户
       /* Map<String, Object> map = JwtUtil.parseToken(token);
        String username = (String) map.get("username");*/
        Map<String, Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");
        User user = userService.findByUserName(username);
        return Result.success(user);
    }

    @PutMapping("/update")
    public Result update(@RequestBody @Validated User user) {
        userService.update(user);
        return Result.success();
    }
//    1. JSON 解析和数据绑定
//    Spring MVC 使用 HttpMessageConverters 将 JSON 数据转换为 Java 对象。默认情况下，Spring Boot 会自动配置 Jackson 作为 JSON 解析库。转换过程如下：
//    - 读取请求体：Spring MVC 读取请求体中的 JSON 数据。
//    - JSON 解析：使用 Jackson 将 JSON 字符串解析为一个 Map 或直接解析为 User 对象。
//    - 数据绑定：将解析后的数据绑定到 User 对象的对应字段上。Jackson 会根据字段名称进行匹配，并调用 User 类的 setter 方法（或者直接访问字段）进行赋值。
//    2. 验证
//    - 如果在 User 类的字段上有任何验证注解（例如 @NotEmpty, @Email, @Pattern 等），Spring MVC 会在数据绑定后自动进行验证。验证失败时，会抛出 MethodArgumentNotValidException 或类似的异常。
//    3. 控制器方法执行
//    - 数据成功绑定并验证通过后，Spring MVC 会将 User 对象作为参数传递给控制器方法，控制器方法可以直接使用这个对象进行后续操作。

    @PatchMapping("updateAvatar")
    public Result updateAvatar(@RequestParam @URL String avatarUrl) {
        userService.updateAvatar(avatarUrl);
        return Result.success();
    }

    @PatchMapping("/updatePwd")
    public Result updatePwd(@RequestBody Map<String, String> params,@RequestHeader("Authorization") String token) {
        //1.校验参数
        String oldPwd = params.get("old_pwd");
        String newPwd = params.get("new_pwd");
        String rePwd = params.get("re_pwd");
        // 使用Map<string,string>的方法代替新建一个类
        if (!StringUtils.hasLength(oldPwd) || !StringUtils.hasLength(newPwd) || !StringUtils.hasLength(rePwd)) {
            return Result.error("缺少必要的参数");
        }

        //原密码是否正确
        //调用userService根据用户名拿到原密码,再和old_pwd比对
        Map<String,Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");
        User loginUser = userService.findByUserName(username);
        if (!loginUser.getPassword().equals(Md5Util.getMD5String(oldPwd))){
            return Result.error("原密码填写不正确");
        }

        //newPwd和rePwd是否一样
        if (!rePwd.equals(newPwd)){
            return Result.error("两次填写的新密码不一样");
        }

        //2.调用service完成密码更新
        userService.updatePwd(newPwd);
        //删除redis中对应的token
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        operations.getOperations().delete(token);
        return Result.success();
    }

    @PutMapping("/bind")
    public Result bindPatientToDoctor(@RequestBody Map<String, String> params) {
        String PatientName = (String) params.get("patientName");
        try {
            userService.bindPatientToDoctor(PatientName);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/comment")
    public Result addComment(@RequestBody Map<String, String> params) {
        try {
            // 从请求参数中获取 ID 和评论内容，并进行转换
            Integer id = Integer.valueOf(params.get("id")); // 将 String 转换为 Integer
            String comment = params.get("comment");

            // 调用服务层方法添加评论
            userService.addComment(id, comment);

            return Result.success();
        } catch (NumberFormatException e) {
            // 处理 ID 转换失败的情况
            return Result.error("Invalid ID format");
        } catch (IllegalArgumentException e) {
            // 处理业务逻辑中的异常
            return Result.error(e.getMessage());
        } catch (Exception e) {
            // 处理其他可能的异常
            return Result.error("An unexpected error occurred");
        }
    }
}
