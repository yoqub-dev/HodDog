package com.example.hoddog.dto;

import lombok.Data;
import java.util.List;

@Data
public class ModifierDto {
    private String name;
    private List<ModifierOptionDto> options; // Meva → [Banana, Olma...]
}
