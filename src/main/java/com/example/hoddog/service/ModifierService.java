package com.example.hoddog.service;

import com.example.hoddog.dto.ModifierRequest;
import com.example.hoddog.dto.ModifierResponse;
import com.example.hoddog.entity.Modifier;
import com.example.hoddog.exception.ResourceNotFoundException;
import com.example.hoddog.repository.ModifierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ModifierService {

    private final ModifierRepository modifierRepository;

    public ModifierResponse create(ModifierRequest request) {
        Modifier modifier = Modifier.builder()
                .name(request.getName())
                .extraPrice(request.getExtraPrice())
                .build();

        modifierRepository.save(modifier);
        return mapToResponse(modifier);
    }

    public ModifierResponse update(UUID id, ModifierRequest request) {
        Modifier modifier = modifierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Modifier topilmadi"));

        modifier.setName(request.getName());
        modifier.setExtraPrice(request.getExtraPrice());
        modifierRepository.save(modifier);

        return mapToResponse(modifier);
    }

    public void delete(UUID id) {
        if (!modifierRepository.existsById(id)) {
            throw new ResourceNotFoundException("Modifier topilmadi");
        }
        modifierRepository.deleteById(id);
    }

    public ModifierResponse get(UUID id) {
        Modifier modifier = modifierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Modifier topilmadi"));
        return mapToResponse(modifier);
    }

    public List<ModifierResponse> getAll() {
        return modifierRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ModifierResponse mapToResponse(Modifier modifier) {
        return ModifierResponse.builder()
                .id(modifier.getId())
                .name(modifier.getName())
                .extraPrice(modifier.getExtraPrice())
                .build();
    }
}