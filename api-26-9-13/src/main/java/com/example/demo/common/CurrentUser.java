package com.example.demo.common;

import com.baomidou.mybatisplus.annotation.TableField;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//type UserDetail
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrentUser {
    //TODO add other properties 
    
    Integer id;
    String username;
    String email;

    private String name;
    private String sex;
    private String companyID;
    private String position;
    private String phone;
    private String priv;

    public static CurrentUser get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (CurrentUser) authentication.getDetails();
    } public static boolean isMainCompany() {
        return "-1".equals(get().companyID);
    }

    public static CurrentUser getDevFakeCurrentUser() {
        CurrentUser currentUser = new CurrentUser();
        currentUser.setId(1);
        currentUser.setName("admin");
        currentUser.setUsername("admin");
        currentUser.setEmail("admin@qq.com");
        currentUser.setCompanyID("0");
        currentUser.setPosition("1");
        currentUser.setPhone("1388888888");
        currentUser.setPriv("0,1,2,3");
        return currentUser;
    }

    public static CurrentUser getGuestFakeCurrentUser(String username) {
        CurrentUser currentUser = new CurrentUser();
        currentUser.setId(-1);
        currentUser.setName(username);
        currentUser.setUsername(username);
        currentUser.setCompanyID("-1");
        currentUser.setPriv("0,1,2,3");
        return currentUser;
    }
}
