package cl.gamego.cliente.config;

import cl.gamego.cliente.model.Cliente;
import cl.gamego.cliente.repository.ClienteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class DataLoader {
    @Bean
    CommandLineRunner cargarDatos(ClienteRepository repository, PasswordEncoder encoder) {
        return args -> {
            if (repository.count() == 0) {
                // Cuenta de prueba para entrar a la tienda sin registrarse
                repository.save(Cliente.builder()
                        .nombre("Cliente Demo")
                        .email("demo@gmail.com")
                        .passwordHash(encoder.encode("Demo1234"))
                        .creadoEn(LocalDateTime.now())
                        .build());
            }
        };
    }
}
