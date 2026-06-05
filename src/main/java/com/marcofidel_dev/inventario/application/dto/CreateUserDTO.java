package com.marcofidel_dev.inventario.application.dto;

import com.marcofidel_dev.inventario.domain.entity.Role;
import lombok.Data;

@Data
public class CreateUserDTO {
    private String username;
    private String fullName;
    private String password;
    private Role role;
}
