package com.example.hoddog.controller;

import com.example.hoddog.dto.ModifierRequest;
import com.example.hoddog.dto.ModifierResponse;
import com.example.hoddog.service.ModifierService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/modifiers")
@RequiredArgsConstructor
public class ModifierController {

    private final ModifierService modifierService;

    @PostMapping
    public ModifierResponse create(@RequestBody ModifierRequest request) {
        return modifierService.create(request);
    }

    @PutMapping("/{id}")
    public ModifierResponse update(@PathVariable UUID id, @RequestBody ModifierRequest request) {
        return modifierService.update(id, request);
    }

    @GetMapping("/{id}")
    public ModifierResponse get(@PathVariable UUID id) {
        return modifierService.get(id);
    }

    @GetMapping
    public List<ModifierResponse> getAll() {
        return modifierService.getAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        modifierService.delete(id);
    }
}
