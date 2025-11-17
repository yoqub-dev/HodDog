package com.example.hoddog.dto;

import com.example.hoddog.enums.Role;
import lombok.Data;

@Data
public class UserDto {
    private String firstName;
    private String lastName;
    private String email;      // username sifatida ishlaydi
    private String password;   // update bo‘lsa optional
    private Role role;
}
