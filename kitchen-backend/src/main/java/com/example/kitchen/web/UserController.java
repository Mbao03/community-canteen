package com.example.kitchen.web;

import com.example.kitchen.model.User;
import com.example.kitchen.service.UserService;
import com.example.kitchen.utils.PageUtils;
import com.example.kitchen.utils.R;
import com.example.kitchen.utils.TokenProcessor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/user")
public class UserController {

    @Resource
    private UserService userService;

    @RequestMapping(value = "/login")
    public Map<String, Object> login(@RequestBody User user) {
        User userObj = userService.login(user);
        if (userObj == null) {
            return R.getResultMap(420, "Invalid username, password or role");
        }

        String token = TokenProcessor.getInstance().generateToken();
        userService.saveUser(token, userObj);
        return R.getResultMap(200, "login success", new HashMap<String, String>() {{
            put("token", token);
        }});
    }

    @RequestMapping(value = "/info")
    public Map<String, Object> info(String token) {
        User user = userService.getUser(token);
        if (user == null) {
            return R.getResultMap(420, "token invalid or expired");
        }
        return R.getResultMap(200, "success", user);
    }

    @RequestMapping(value = "/logout")
    public Map<String, Object> logout(String token) {
        userService.removeUser(token);
        return R.getResultMap(200, "logout success");
    }

    @RequestMapping(value = "/register")
    public Integer register(String username, String password) {
        return userService.register(username, password);
    }

    @RequestMapping(value = {"/updateProfile", "reader/updateProfile", "resident/updateProfile"})
    public Integer updateProfile(@RequestBody User user) {
        // 只允许更新个人信息字段，不能更新密码、id、角色
        return userService.updateUserProfile(user);
    }

    @RequestMapping(value = {"/alterPassword", "reader/alterPassword", "resident/alterPassword"})
    public Integer alterPassword(Integer userid, String username, Byte isadmin, String oldPassword, String newPassword) {
        User verifyUser = new User();
        verifyUser.setUserid(userid);
        verifyUser.setUsername(username);
        verifyUser.setUserpassword(oldPassword);
        verifyUser.setIsadmin(isadmin);

        User user = userService.login(verifyUser);
        if (user == null) {
            return 0;
        }

        userService.setPassword(userid, newPassword);
        return 1;
    }

    @GetMapping(value = "/getCount")
    public Integer getCount() {
        return userService.getCount();
    }

    @GetMapping(value = "/queryUsers")
    public List<User> queryUsers() {
        return userService.queryUsers();
    }

    @GetMapping(value = "/queryUsersByPage")
    public Map<String, Object> queryUsersByPage(@RequestParam Map<String, Object> params) {
        PageUtils.parsePageParams(params);
        int count = userService.getSearchCount(params);
        List<User> users = userService.searchUsersByPage(params);
        return R.getListResultMap(0, "success", count, users);
    }

    @PostMapping(value = "/addUser")
    public Integer addUser(@RequestBody User user) {
        return userService.addUser(user);
    }

    @DeleteMapping(value = "/deleteUser")
    public Integer deleteUser(@RequestBody User user) {
        return userService.deleteUser(user);
    }

    @DeleteMapping(value = "/deleteUsers")
    public Integer deleteUsers(@RequestBody List<User> users) {
        return userService.deleteUsers(users);
    }

    @RequestMapping(value = "/updateUser")
    public Integer updateUser(@RequestBody User user) {
        return userService.updateUser(user);
    }
}
