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
@RequestMapping(value = "/resident")
public class ResidentController {

    @Resource
    private UserService userService;

    @PostMapping(value = "/login")
    public Map<String, Object> login(@RequestBody User resident) {
        User userObj = userService.login(resident);
        if (userObj == null) {
            return R.getResultMap(420, "Invalid username, password or role");
        }

        String token = TokenProcessor.getInstance().generateToken();
        userService.saveUser(token, userObj);
        return R.getResultMap(200, "login success", new HashMap<String, String>() {{
            put("token", token);
        }});
    }

    @GetMapping(value = "/info")
    public Map<String, Object> info(String token) {
        User resident = userService.getUser(token);
        if (resident == null) {
            return R.getResultMap(420, "token invalid or expired");
        }
        return R.getResultMap(200, "success", resident);
    }

    @PostMapping(value = "/logout")
    public Map<String, Object> logout(String token) {
        userService.removeUser(token);
        return R.getResultMap(200, "logout success");
    }

    @PostMapping(value = "/register")
    public Integer register(String username, String password) {
        return userService.register(username, password);
    }

    @PostMapping(value = "/updateProfile")
    public Integer updateProfile(@RequestBody User resident) {
        return userService.updateUserProfile(resident);
    }

    @PostMapping(value = "/alterPassword")
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

    @GetMapping(value = "/queryResidents")
    public List<User> queryResidents() {
        return userService.queryUsers();
    }

    @GetMapping(value = "/queryResidentsByPage")
    public Map<String, Object> queryResidentsByPage(@RequestParam Map<String, Object> params) {
        PageUtils.parsePageParams(params);
        int count = userService.getSearchCount(params);
        List<User> residents = userService.searchUsersByPage(params);
        return R.getListResultMap(0, "success", count, residents);
    }

    @PostMapping(value = "/addResident")
    public Integer addResident(@RequestBody User resident) {
        return userService.addUser(resident);
    }

    @DeleteMapping(value = "/deleteResident")
    public Integer deleteResident(@RequestBody User resident) {
        return userService.deleteUser(resident);
    }

    @DeleteMapping(value = "/deleteResidents")
    public Integer deleteResidents(@RequestBody List<User> residents) {
        return userService.deleteUsers(residents);
    }

    @PutMapping(value = "/updateResident")
    public Integer updateResident(@RequestBody User resident) {
        return userService.updateUser(resident);
    }
}
