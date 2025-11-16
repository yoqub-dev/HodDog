package com.example.hoddog.service;

import com.example.hoddog.dto.ModifierDto;
import com.example.hoddog.dto.ModifierOptionDto;
import com.example.hoddog.entity.Modifier;
import com.example.hoddog.entity.ModifierOption;
import com.example.hoddog.repository.ModifierRepository;
import com.example.hoddog.repository.ModifierOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ModifierService {

    private final ModifierRepository modifierRepository;
    private final ModifierOptionRepository optionRepository;

    // CREATE GROUP + OPTIONS
    public Modifier create(ModifierDto dto) {

        if (modifierRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new RuntimeException("Modifier group already exists: " + dto.getName());
        }

        Modifier modifier = Modifier.builder()
                .name(dto.getName())
                .active(true)
                .build();

        modifier = modifierRepository.save(modifier);

        // OPTION qo‘shish
        if (dto.getOptions() != null) {
            for (ModifierOptionDto optionDto : dto.getOptions()) {
                ModifierOption option = ModifierOption.builder()
                        .name(optionDto.getName())
                        .price(optionDto.getPrice())
                        .modifier(modifier)
                        .build();

                optionRepository.save(option);
            }
        }

        return modifierRepository.findById(modifier.getId()).get();
    }

    // GET ALL GROUPS
    public List<Modifier> getAll() {
        return modifierRepository.findAll();
    }

    // GET ONE GROUP
    public Modifier getById(UUID id) {
        return modifierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Modifier group not found"));
    }

    // ADD OPTION to existing GROUP
    public ModifierOption addOption(UUID groupId, ModifierOptionDto dto) {

        Modifier group = getById(groupId);

        ModifierOption option = ModifierOption.builder()
                .modifier(group)
                .name(dto.getName())
                .price(dto.getPrice())
                .build();

        return optionRepository.save(option);
    }

    // DELETE GROUP
    public void delete(UUID id) {
        modifierRepository.deleteById(id);
    }
}
