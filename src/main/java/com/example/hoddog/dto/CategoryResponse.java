package com.example.hoddog.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;
@Data
@Builder
public class CategoryResponse {
    private UUID id;
    private String name;
}
