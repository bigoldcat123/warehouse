package com.example.demo.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.CurrentUser;
import com.example.demo.common.R;
import com.example.demo.system.entity.PO.UserAuth;
import com.example.demo.system.service.IUserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 验证用户表 前端控制器
 * </p>
 *
 * @author czh
 * @since 2024-04-22
 *
 * 添加、删除、修改用户信息，修改密码
 */
@RestController
@RequestMapping("/user")
public class UserAuthController {
    @Autowired
    IUserAuthService userAuthService;


    @GetMapping
    public R getUsers(Integer current, Integer size) {
        Page<UserAuth> page = userAuthService.page(new Page<>(current, size));
        page.getRecords().forEach(x->{x.setPassword("");});
        return R.ok(page);
    }

    @PostMapping
    public R add (@RequestBody UserAuth userAuth) {
        UserAuth one = userAuthService.getUserAuthByUsername(userAuth.getUsername());
        if (one != null) {
            return R.errorShow("用户名存在");
        }
        userAuth.setPassword(userAuth.getSafePassword());
        boolean save = userAuthService.save(userAuth);
        if (save) {
            return R.ok();
        }else {
            return R.error();
        }
    }

    @PutMapping
    public R update (@RequestBody UserAuth userAuth) {
        if(userAuth.getPassword() != null && !userAuth.getPassword().isEmpty()){
            userAuth.setPassword(userAuth.getSafePassword());
        }else {
            userAuth.setPassword(null);
        }
        boolean update = userAuthService.updateById(userAuth);
        if (update) {
            return R.ok();
        }else {
            return R.error();
        }
    }
    @DeleteMapping("{id}")
    public R delete (@PathVariable Long id) {
        boolean remove = userAuthService.removeById(id);
        if (remove) {
            return R.ok();
        }else {
            return R.error();
        }
    }

    @GetMapping("kv")
    public R kv() {
        Object[] array = userAuthService.list().stream().map(x -> {
            Map<String, Object> map = new HashMap<>();
            map.put("key", x.getId());
            map.put("value", x.getUsername());
            return map;
        }).toArray();
        return R.ok(array);
    }
    @GetMapping("kv/getUsersByWarehouseId/{warehouseId}")
    public R getUsersByWarehouseId(@PathVariable String warehouseId) {
        QueryWrapper<UserAuth> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("companyID", warehouseId);
        Object[] array = userAuthService.list(queryWrapper).stream().map(x -> {
            Map<String, Object> map = new HashMap<>();
            map.put("key", x.getId());
            map.put("value", x.getUsername());
            return map;
        }).toArray();
        return R.ok(array);
    }
    @PostMapping("passwd")
    public R passwd (@RequestBody Map<String, String> map) {
        String newP = map.get("newP");
        String odlP = map.get("oldP");
        if(!newP.isEmpty() && !odlP.isEmpty()){
            UserAuth u = userAuthService.getById(CurrentUser.get().getId());
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            boolean matches = bCryptPasswordEncoder.matches(odlP, u.getPassword());
            if (matches) {
                u.setPassword(bCryptPasswordEncoder.encode(newP));
                userAuthService.updateById(u);
                return R.okShow("成功😄");
            }
        }
        return R.errorShow("失败☹️");
    }
}
