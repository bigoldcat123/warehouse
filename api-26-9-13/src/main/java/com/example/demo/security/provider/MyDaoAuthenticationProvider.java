package com.example.demo.security.provider;

import com.example.demo.system.entity.PO.UserAuth;
import com.example.demo.system.service.IUserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class MyDaoAuthenticationProvider extends DaoAuthenticationProvider{

    IUserAuthService userAuthService;

    static class DaoUserDetailService implements UserDetailsService{
        IUserAuthService userAuthService;

        public DaoUserDetailService(IUserAuthService userAuthService) {
            this.userAuthService = userAuthService;
        }

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            // TODO Auto-generated method stub
            UserAuth userAuthByUsername = userAuthService.getUserAuthByUsername(username);
            return User.withUsername(username).password(userAuthByUsername.getPassword()).build(); // root
        }
    }
    @Autowired
    public MyDaoAuthenticationProvider(IUserAuthService userAuthService) {
        super(new BCryptPasswordEncoder());
        super.setUserDetailsService(new DaoUserDetailService(userAuthService));
        this.userAuthService = userAuthService;
    }
    public static void main(String[] args) {
        //$2a$10$/XCbduwg1WE41qeql6oOB.aVuY0d8xGZDgxF3UwKFwR1.sBhdxoaG
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        System.out.println(bCryptPasswordEncoder.encode("root"));
    }
}
