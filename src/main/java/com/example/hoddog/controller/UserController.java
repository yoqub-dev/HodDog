package com.example.hoddog.controller;

import com.example.hoddog.dto.UserDto;
import com.example.hoddog.service.UserService;
import com.example.hoddog.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // CREATE USER
    @PostMapping
    public User create(@RequestBody UserDto dto) {
        return userService.create(dto);
    }

    // GET ALL USERS
    @GetMapping
    public List<User> getAll() {
        return userService.getAll();
    }

    // GET ONE USER
    @GetMapping("/{id}")
    public User getById(@PathVariable UUID id) {
        return userService.getById(id);
    }

    // UPDATE USER
    @PutMapping("/{id}")
    public User update(@PathVariable UUID id, @RequestBody UserDto dto) {
        return userService.update(id, dto);
    }

    // DELETE USER
    @DeleteMapping("/{id}")
    public String delete(@PathVariable UUID id) {
        userService.delete(id);
        return "User deleted";
    }

}
