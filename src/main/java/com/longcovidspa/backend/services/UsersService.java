package com.longcovidspa.backend.services;

import com.longcovidspa.backend.model.User;
import com.longcovidspa.backend.payload.request.UserRequest;
import com.longcovidspa.backend.payload.response.UserResponse;
import org.springframework.stereotype.Service;

@Service
public interface UsersService {

    String updateUser(UserRequest userRequest);

    UserResponse getUserInformationByUsername(String username);

}
