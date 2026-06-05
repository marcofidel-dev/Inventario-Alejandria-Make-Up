package com.marcofidel_dev.inventario.application.dto;

public record CustomerDTO(
        Long id,
        String name,
        String phone,
        String email,
        String notes
) {}
