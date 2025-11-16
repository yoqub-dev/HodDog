package com.example.hoddog.service;

import com.example.hoddog.dto.DiscountDto;
import com.example.hoddog.entity.Discount;
import com.example.hoddog.repository.DiscountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DiscountService {

    private final DiscountRepository discountRepository;

    public Discount create(DiscountDto dto) {

        Discount discount = Discount.builder()
                .name(dto.getName())
                .type(dto.getType())
                .value(dto.getValue())
                .build();

        return discountRepository.save(discount);
    }


    public List<Discount> getAll() {
        return discountRepository.findAll();
    }

    public Discount getById(UUID id) {
        return discountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount not found"));
    }

    public void delete(UUID id) {
        discountRepository.deleteById(id);
    }

    public Discount update(UUID id, DiscountDto dto) {

        Discount discount = getById(id);

        discount.setName(dto.getName());
        discount.setType(dto.getType());
        discount.setValue(dto.getValue());

        return discountRepository.save(discount);
    }
}
