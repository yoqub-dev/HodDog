package com.example.hoddog.controller;

import com.example.hoddog.dto.ModifierDto;
import com.example.hoddog.dto.ModifierOptionDto;
import com.example.hoddog.entity.Modifier;
import com.example.hoddog.entity.ModifierOption;
import com.example.hoddog.service.ModifierService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/modifiers")
@RequiredArgsConstructor
public class ModifierController {

    private final ModifierService service;

    // CREATE GROUP + OPTIONS
    @PostMapping
    public Modifier create(@RequestBody ModifierDto dto) {
        return service.create(dto);
    }

    // GET ALL
    @GetMapping
    public List<Modifier> getAll() {
        return service.getAll();
    }

    // GET BY ID
    @GetMapping("/{id}")
    public Modifier getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    // ADD OPTION TO GROUP
    @PostMapping("/{id}/options")
    public ModifierOption addOption(
            @PathVariable UUID id,
            @RequestBody ModifierOptionDto dto
    ) {
        return service.addOption(id, dto);
    }

    // DELETE GROUP
    @DeleteMapping("/{id}")
    public String delete(@PathVariable UUID id) {
        service.delete(id);
        return "Modifier group deleted";
    }
}
