package cl.gamego.cliente.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroRequest {
    @NotBlank(message = "El nombre es obligatorio") @Size(max = 100, message = "El nombre no puede superar los 100 caracteres") private String nombre;
    @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo debe ser válido") @Size(max = 100, message = "El correo no puede superar los 100 caracteres") private String email;
    @NotBlank(message = "La contraseña es obligatoria") @Size(min = 7, max = 72, message = "La contraseña debe tener entre 7 y 72 caracteres") private String password;
}
