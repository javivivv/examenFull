package cl.gamego.cliente.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "El correo es obligatorio") private String email;
    @NotBlank(message = "La contraseña es obligatoria") private String password;
}
