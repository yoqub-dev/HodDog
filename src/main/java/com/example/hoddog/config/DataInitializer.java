package com.example.hoddog.config;

import com.example.hoddog.entity.*;
import com.example.hoddog.enums.Role;
import com.example.hoddog.enums.SoldBy;
import com.example.hoddog.repository.*;
import com.example.hoddog.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SupplierRepository supplierRepository;
    private final CategoryRepository categoryRepository;
    private final ModifierRepository modifierRepository;
    private final ModifierOptionRepository modifierOptionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ProductRepository productRepository;


    @Override
    public void run(String... args) {


        User admin = userRepository.findByEmail("admin@system.com").orElse(null);

        if (admin == null) {
            admin = User.builder()
                    .firstName("Admin")
                    .lastName("System")
                    .email("admin")
                    .password(passwordEncoder.encode("123"))
                    .role(Role.ADMIN)
                    .build();

            admin = userRepository.save(admin);
            System.out.println("✅ ADMIN user yaratildi.");
        } else {
            System.out.println("ℹ️ ADMIN avvaldan mavjud.");
        }

        System.out.println("Bearer " + jwtService.generateToken(admin));


        // ---------- DEFAULT SUPPLIER ----------
        if (supplierRepository.count() == 0) {
            Supplier supplier = Supplier.builder()
                    .name("Default Supplier")
                    .phone("+998900000000")
                    .build();
            supplierRepository.save(supplier);
        }

        // ---------- DEFAULT CATEGORY ----------
        if (categoryRepository.count() == 0) {
            Category hotdog = Category.builder().name("Hot-Dog").build();
            Category drinks = Category.builder().name("Drinks").build();
            Category sauces = Category.builder().name("Sauces").build();

            categoryRepository.save(hotdog);
            categoryRepository.save(drinks);
            categoryRepository.save(sauces);
        }

        // ---------- DEFAULT MODIFIER ----------
        if (modifierRepository.count() == 0) {
            Modifier modifier = Modifier.builder()
                    .name("Sauce Options")
                    .active(true)
                    .build();
            modifierRepository.save(modifier);

            // OPTIONS (modifierga bog‘langan)
            ModifierOption ketchup = ModifierOption.builder()
                    .modifier(modifier)
                    .name("Ketchup")
                    .price(2000.0)
                    .build();

            ModifierOption mayo = ModifierOption.builder()
                    .modifier(modifier)
                    .name("Mayonnaise")
                    .price(2000.0)
                    .build();

            ModifierOption cheese = ModifierOption.builder()
                    .modifier(modifier)
                    .name("Cheese Sauce")
                    .price(3000.0)
                    .build();

            modifierOptionRepository.save(ketchup);
            modifierOptionRepository.save(mayo);
            modifierOptionRepository.save(cheese);

        }

//        Category ingredientCat = categoryRepository.save(
//                Category.builder().name("Ingredientlar").build()
//        );
//        Product non = Product.builder()
//                .name("non")
//                .description("")
//                .availableForSale(false)
//                .soldBy(SoldBy.EACH)
//                .price(0.0)
//                .cost(0.0)
//                .sku("")   // yoki bo'sh qoldir, service auto-generate qiladi
//                .composite(false)
//                .trackStock(true)
//                .quantity(0.0)
//                .lowQuantity(5.0)
//                .category(ingredientCat)
//                .build();
//
//        productRepository.save(non);


    }
}
