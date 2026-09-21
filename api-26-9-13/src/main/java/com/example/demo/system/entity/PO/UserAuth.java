package com.example.demo.system.entity.PO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import com.example.demo.common.CurrentUser;
import lombok.Data;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * <p>
 * 验证用户表
 * </p>
 *
 * @author czh
 * @since 2024-04-22
 */
//TODO 这里是需要修改的
@Data
@TableName("_user")
public class UserAuth implements Serializable {

    private static final long serialVersionUID = 1L;

//    const privList = ['所有数据','一般报警','严重严重报警','报警核准']

    public static final String ALL_DATA = "0";
    public static final String COMMON_ISSUE = "1";
    public static final String SERIOUS_ISSUE = "2";
    public static final String ALARM_RATIFY = "3";
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 密码 默认root
     */
    @TableField("psw")
    private String password;

    /**
     * 邮箱/ name this
     */
    @TableField("_name")
    private String name;

    @TableField("sex")
    private String sex;
    /**
     * 更新事件
     */
    @TableField("companyID")
    private String companyID;

    @TableField("_position")
    private String position;

    @TableField("phone")
    private String phone;


    @TableField("priv")
    private String priv;

    public String getSafePassword () {
        return new BCryptPasswordEncoder().encode(this.password);
    }

    //TODO 获取 当前用户
    public CurrentUser toCurrentUser() {
        CurrentUser currentUser = new CurrentUser();
        currentUser.setId(id);
        currentUser.setUsername(username);
        currentUser.setEmail("email");
        currentUser.setName(name);
        currentUser.setSex(sex);
        currentUser.setCompanyID(companyID);
        currentUser.setPosition(position);
        currentUser.setPhone(phone);
        currentUser.setPriv(priv);

        return currentUser;
    }

}
