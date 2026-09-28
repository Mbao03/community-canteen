package com.example.kitchen.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Data
public class User implements Serializable {

//  后端输出统一叫 userid；
//  前端传 userid 或 userId，后端都能接收。
//  @JsonProperty("userid") 指定这个字段在 JSON 序列化和反序列化时的正式字段名是 userid，Java 对象转 JSON
//  @JsonAlias 只影响反序列化，也就是 JSON 转 Java
    @JsonProperty("userid")
    @JsonAlias("userId")
    private Integer userid;

    @JsonProperty("username")
    @JsonAlias("userName")
    private String username;

    @JsonProperty("userpassword")
    @JsonAlias("userPassword")
    private String userpassword;

    @JsonProperty("isadmin")
    @JsonAlias("isAdmin")
    private Byte isadmin;

    private String phone;
    private String gender;
    private Date birthday;
    private String buildingNo;
    private String unitNo;
    private String roomNo;
    private String dietaryTags;
    private String healthNotes;
    private Byte isActive;
    private Date createdAt;
    private Date updatedAt;
    private Date lastLoginAt;

}
