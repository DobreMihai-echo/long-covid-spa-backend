package com.longcovidspa.backend.services.impl;

import com.longcovidspa.backend.model.User;
import com.longcovidspa.backend.payload.request.UserRequest;
import com.longcovidspa.backend.payload.response.UserResponse;
import com.longcovidspa.backend.repositories.UserRepositories;
import com.longcovidspa.backend.services.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

    private final UserRepositories userRepository;

    public Boolean save(User user) {

        try {
            userRepository.save(user);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        return true;
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).get();
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).get();
    }

    @Override
    public String updateUser(UserRequest userRequest) {
        User userToUpdate = userRepository.findByUsername(userRequest.getUsername()).get();
        userToUpdate.setFirstName(userRequest.getFirstName());
        userToUpdate.setLastName(userRequest.getLastName());
        userToUpdate.setHeight(userToUpdate.getHeight());
        userToUpdate.setWeight(userToUpdate.getWeight());
        userToUpdate.setDateOfBirth(userRequest.getDateOfBirth());

        try {
            userRepository.save(userToUpdate);
            return "User saved successfully";
        } catch (Exception ex) {
            return "There was an error updating user";
        }
    }

    @Override
    public UserResponse getUserInformationByUsername(String username) {
        User user = userRepository.findByUsername(username).get();
        System.out.println("USER USER:" + user);
        return UserResponse.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .weight(user.getWeight())
                .height(user.getHeight())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .build();
    }
}
