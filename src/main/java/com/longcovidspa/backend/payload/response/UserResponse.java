package com.longcovidspa.backend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private String username;
    private Date dateOfBirth;
    private Integer height;
    private Integer weight;
    private String firstName;
    private String lastName;
    private String gender;
}
