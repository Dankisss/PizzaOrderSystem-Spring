package com.deliciouspizza.service;

import com.deliciouspizza.dto.user.UserInputDto;
import com.deliciouspizza.dto.user.UserUpdateDto;
import com.deliciouspizza.exception.UserAlreadyExistsException;
import com.deliciouspizza.exception.UserNotFoundException;
import com.deliciouspizza.model.user.User;
import com.deliciouspizza.model.user.UserRole;
import com.deliciouspizza.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerNewUser(UserInputDto userInputDto) {
        if (userRepository.existsByUsername(userInputDto.getUsername())) {
            throw new UserAlreadyExistsException("Username: " + userInputDto.getUsername());
        }

        if (userRepository.existsByEmail(userInputDto.getEmail())) {
            throw new UserAlreadyExistsException("Email: " + userInputDto.getEmail());
        }

        User newUser = new User();
        UserRole newRole = userInputDto.getUserRole() == null ? UserRole.CUSTOMER : userInputDto.getUserRole();

        newUser.setUsername(userInputDto.getUsername());
        newUser.setPasswordHash(passwordEncoder.encode(userInputDto.getPassword()));
        newUser.setEmail(userInputDto.getEmail());

        newUser.setRole(newRole);

        return userRepository.saveAndFlush(newUser);
    }

    public User findById(long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Id: " + id));
    }

    public User updateUser(long id, UserUpdateDto userUpdateDto) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Id: " + id));

        String newUsername = userUpdateDto.getUsername();
        String newEmail = userUpdateDto.getEmail();

        if (userRepository.existsByUsername(newUsername)) {
            throw new UserAlreadyExistsException("Username: " + newUsername);
        }

        if (userRepository.existsByEmail(newEmail)) {
            throw new UserAlreadyExistsException("Email: " + newEmail);
        }

        user.setUsername(newUsername);
        user.setEmail(newEmail);

        return userRepository.saveAndFlush(user);
    }

    public User deactivateUser(long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Id: " + id));

        user.setActive(false);

        return userRepository.saveAndFlush(user);
    }

    public User uploadUserPhoto(long id, MultipartFile photo) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Id: " + id));

        try {
            user.setImageData(photo.getBytes());

            return userRepository.saveAndFlush(user);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while getting the bytes from the image");
        }

    }
}
