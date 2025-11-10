package com.example.hoddog.config;

import com.example.hoddog.entity.Category;
import com.example.hoddog.entity.Modifier;
import com.example.hoddog.enums.Role;
import com.example.hoddog.repository.CategoryRepository;
import com.example.hoddog.repository.ModifierRepository;
import com.example.hoddog.repository.UserRepository;
import com.example.hoddog.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModifierRepository modifierRepository;
    private final JwtService jwtService;

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

            userRepository.save(admin);
            System.out.println("✅ Default ADMIN user yaratildi:");
        } else {
            System.out.println("ℹ️ Default ADMIN avvaldan mavjud.");
        }

        // ✅ Admin uchun JWT Token yaratish
        String adminToken = jwtService.generateToken(admin);
        System.out.println("🔑 ADMIN JWT TOKEN:");
        System.out.println("Bearer " + adminToken);
        System.out.println("----------------------------------------");

        if (categoryRepository.count() == 0) {

            Category category = Category.builder()
                    .name("Hot-doglar")
                    .build();

            categoryRepository.save(category);
            System.out.println("Default Category yaratildi: Hot-doglar");
        } else {
            System.out.println("ℹ️ Categorylar avvaldan mavjud.");
        }

        if (!modifierRepository.existsByName("Cheese")) {
            Modifier cheese = Modifier.builder()
                    .name("Cheese")
                    .extraPrice(4000.0)
                    .build();
            modifierRepository.save(cheese);
            System.out.println("✅ Default Modifier yaratildi: Cheese +4000");
        }
    }
}
