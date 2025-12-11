package com.ahana.botmonitoring.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document(collection = "User")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserModel {

    @Id
    private String id;

    @Field(name="userId")
    private Integer userid;

    private String employeeID;

    @Field(name = "userName")
    private String userName;

    @Field(name = "domainName")
    private String domainName;

    @Field(name = "emailID")
    private String emailID;

    @Field(name = "mobileNo")
    private String mobileNo;

    @Field(name = "password")
    private String password;

    @Field(name="process")
    private List<String> process;

    @Field(name="bots")
    private List<String> bots;

    @Field(name="role")
    private String role;
}
