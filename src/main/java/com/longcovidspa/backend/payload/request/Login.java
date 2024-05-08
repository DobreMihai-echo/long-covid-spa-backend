package com.longcovidspa.backend.payload.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Login {

    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
}
