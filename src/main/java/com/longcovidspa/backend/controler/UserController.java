package com.longcovidspa.backend.controler;

import com.longcovidspa.backend.model.User;
import com.longcovidspa.backend.payload.request.UserRequest;
import com.longcovidspa.backend.repositories.UserRepositories;
import com.longcovidspa.backend.services.UsersService;
import com.longcovidspa.backend.services.impl.UsersServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/user")
public class UserController {

    @Autowired
    private UserRepositories repositories;

    @Autowired
    private UsersServiceImpl service;

    @PutMapping()
    public ResponseEntity<?> editUser(@RequestBody UserRequest userRequest) {
        return ResponseEntity.ok(service.updateUser(userRequest));
    }

    @GetMapping()
    public ResponseEntity<?> getUserInformationByUsername(@RequestParam(name = "username") String username) {
        System.out.println("USERNAME" + username);
        return ResponseEntity.ok(service.getUserInformationByUsername(username));
    }

}
