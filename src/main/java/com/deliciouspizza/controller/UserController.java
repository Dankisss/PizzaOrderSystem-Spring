package com.deliciouspizza.controller;

import com.deliciouspizza.dto.user.UserInputDto;
import com.deliciouspizza.dto.user.UserUpdateDto;
import com.deliciouspizza.model.user.User;
import com.deliciouspizza.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<User> signUp(@RequestBody @Validated UserInputDto userInputDto) {
        return ResponseEntity.ok(userService.registerNewUser(userInputDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable long id) {
        return ResponseEntity.ok(userService.findById(id));
    }


    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable long id, @RequestBody UserUpdateDto user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<User> deactivateUser(@PathVariable long id) {
        return ResponseEntity.ok(userService.deactivateUser(id));
    }

    /**
     * Endpoint to upload or update a user's profile photo.
     *
     * @param id    The ID of the user to upload the photo for.
     * @param photo The MultipartFile representing the uploaded photo.
     * @return ResponseEntity with the updated User object or an error status.
     */
    @PatchMapping("/{id}/photo") // Specific endpoint for photo upload/update
    public ResponseEntity<User> uploadUserPhoto(
            @PathVariable long id,
            @RequestPart MultipartFile photo) {

        if (photo.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Or a more specific error DTO
        }

        User updatedUser = userService.uploadUserPhoto(id, photo);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);

    }

// TODO: This should be implemented using JWT authorization
//    @PostMapping("/auth/login")
//    public ResponseEntity<User> signIn() {
//
//    }

}
