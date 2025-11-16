package com.example.hoddog.service;

import com.example.hoddog.dto.SupplierDto;
import com.example.hoddog.entity.Supplier;
import com.example.hoddog.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    // Create Supplier
    public Supplier create(SupplierDto dto) {

        Supplier supplier = Supplier.builder()
                .name(dto.getName())
                .phone(dto.getPhone())
                .build();

        return supplierRepository.save(supplier);
    }

    // Get all suppliers
    public List<Supplier> getAll() {
        return supplierRepository.findAll();
    }

    // Get one supplier
    public Supplier getById(UUID id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
    }

    // Update Supplier
    public Supplier update(UUID id, SupplierDto dto) {

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        supplier.setName(dto.getName());
        supplier.setPhone(dto.getPhone());

        return supplierRepository.save(supplier);
    }

    // Delete Supplier
    public void delete(UUID id) {
        if (!supplierRepository.existsById(id)) {
            throw new RuntimeException("Supplier not found");
        }
        supplierRepository.deleteById(id);
    }
}

