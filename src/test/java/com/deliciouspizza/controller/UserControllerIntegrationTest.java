package com.deliciouspizza.controller;

import com.deliciouspizza.dto.user.UserInputDto;
import com.deliciouspizza.dto.user.UserUpdateDto;
import com.deliciouspizza.model.user.User;
import com.deliciouspizza.model.user.UserRole;
import com.deliciouspizza.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    private User createUserInDb(String username, String email, String password) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(UserRole.CUSTOMER); // Use the enum here
        user.setActive(true);
        user.setAddress("Default Test Address");
        return userRepository.save(user);
    }

    // --- Test for POST /sign-up ---
    @Test
    void signUp_shouldCreateNewUser_whenValidInput() throws Exception {
        UserInputDto inputDto = new UserInputDto();
        inputDto.setUsername("testuser");
        inputDto.setEmail("test@example.com");
        inputDto.setPassword("password123");
        inputDto.setUserRole(UserRole.CUSTOMER); // Use the UserRole enum

        mockMvc.perform(post("/api/v1/users/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.active").value(false));

        Optional<User> createdUser = userRepository.findByEmail("test@example.com");
        assertTrue(createdUser.isPresent());
        assertTrue(passwordEncoder.matches("password123", createdUser.get().getPasswordHash()));
        assertEquals(UserRole.CUSTOMER, createdUser.get().getRole()); // Verify enum value
    }

    @Test
    void signUp_shouldReturnBadRequest_whenInvalidInput() throws Exception {
        UserInputDto inputDto = new UserInputDto();
        inputDto.setEmail("invalid-email");
        inputDto.setPassword("short"); // Less than 8 chars
        inputDto.setUserRole(UserRole.CUSTOMER);

        mockMvc.perform(post("/api/v1/users/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signUp_shouldReturnConflict_whenEmailAlreadyExists() throws Exception {
        createUserInDb("existinguser", "exist@example.com", "password");

        UserInputDto inputDto = new UserInputDto();
        inputDto.setUsername("newuser");
        inputDto.setEmail("exist@example.com"); // Duplicate email
        inputDto.setPassword("newpassword123");
        inputDto.setUserRole(UserRole.CUSTOMER);

        mockMvc.perform(post("/api/v1/users/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isConflict());
    }

    // --- Test for GET /{id} ---
    @Test
    void getUser_shouldReturnUser_whenIdExists() throws Exception {
        User existingUser = createUserInDb("findme", "find@example.com", "pass12345");

        mockMvc.perform(get("/api/v1/users/{id}", existingUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingUser.getId()))
                .andExpect(jsonPath("$.username").value("findme"))
                .andExpect(jsonPath("$.email").value("find@example.com"));
    }

    @Test
    void getUser_shouldReturnNotFound_whenIdDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    // --- Test for PUT /{id} ---
    @Test
    void updateUser_shouldUpdateUser_whenIdExistsAndValidInput() throws Exception {
        User existingUser = createUserInDb("updateme", "update@example.com", "oldpass12345");

        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setUsername("updatedusername");
        updateDto.setEmail("updated@example.com");

        mockMvc.perform(put("/api/v1/users/{id}", existingUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingUser.getId()))
                .andExpect(jsonPath("$.username").value("updatedusername"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));

        // Verify in DB
        User updatedUserInDb = userRepository.findById(existingUser.getId()).get();
        assertEquals("updatedusername", updatedUserInDb.getUsername());
        assertEquals("updated@example.com", updatedUserInDb.getEmail());
        // Verify other fields remain unchanged as they are not in UserUpdateDto
        assertEquals(UserRole.CUSTOMER, updatedUserInDb.getRole());
        assertEquals("Default Test Address", updatedUserInDb.getAddress());
    }

    @Test
    void updateUser_shouldReturnNotFound_whenIdDoesNotExist() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setUsername("nonexistent");

        mockMvc.perform(put("/api/v1/users/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_shouldReturnBadRequest_whenInvalidEmailFormat() throws Exception {
        User existingUser = createUserInDb("updateme2", "update2@example.com", "pass123456");

        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setEmail("invalid-email-format");
        updateDto.setUsername("");

        mockMvc.perform(put("/api/v1/users/{id}", existingUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest());
    }


    // --- Test for PATCH /{id} (deactivateUser) ---
    @Test
    void deactivateUser_shouldSetUserToInactive_whenIdExists() throws Exception {
        User activeUser = createUserInDb("activeuser", "active@example.com", "pass12345");

        mockMvc.perform(patch("/api/v1/users/{id}", activeUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(activeUser.getId()))
                .andExpect(jsonPath("$.active").value(false));

        User deactivatedUserInDb = userRepository.findById(activeUser.getId()).get();
        assertFalse(deactivatedUserInDb.getActive());
    }

    @Test
    void deactivateUser_shouldReturnNotFound_whenIdDoesNotExist() throws Exception {
        mockMvc.perform(patch("/api/v1/users/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    // --- Test for PATCH /{id}/photo ---
    @Test
    void uploadUserPhoto_shouldStorePhoto_whenValidInput() throws Exception {
        User user = createUserInDb("photouser", "photo@example.com", "pass12345");

        byte[] photoContent = "test image bytes".getBytes();
        MockMultipartFile photo = new MockMultipartFile(
                "photo", // This name must match @RequestParam("photo") or just "photo" if no name specified
                "profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                photoContent
        );

        mockMvc.perform(multipart("/api/v1/users/{id}/photo", user.getId())
                        .file(photo)
                        .with(request -> { // Multipart requests via MockMvc need method explicitly set for PATCH
                            request.setMethod("PATCH");
                            return request;
                        })
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.imageData").exists()); // Check that imageData exists in the response JSON

        User updatedUserInDb = userRepository.findById(user.getId()).get();
        assertNotNull(updatedUserInDb.getImageData());
        assertArrayEquals(photoContent, updatedUserInDb.getImageData());
    }

    @Test
    void uploadUserPhoto_shouldReturnBadRequest_whenPhotoIsEmpty() throws Exception {
        User user = createUserInDb("emptyphotouser", "empty@example.com", "pass12345");

        MockMultipartFile emptyPhoto = new MockMultipartFile(
                "photo",
                "empty.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[0] // Empty bytes
        );

        mockMvc.perform(multipart("/api/v1/users/{id}/photo", user.getId())
                        .file(emptyPhoto)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        })
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadUserPhoto_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        byte[] photoContent = "test image bytes".getBytes();
        MockMultipartFile photo = new MockMultipartFile(
                "photo",
                "profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                photoContent
        );

        mockMvc.perform(multipart("/api/v1/users/{id}/photo", 999L) // Non-existent user
                        .file(photo)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        })
                )
                .andExpect(status().isNotFound());
    }
}