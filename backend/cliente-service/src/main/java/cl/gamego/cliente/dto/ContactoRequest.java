package cl.gamego.cliente.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactoRequest {
    @NotBlank(message = "El nombre es obligatorio") @Size(max = 100, message = "El nombre no puede superar los 100 caracteres") private String nombre;
    @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo debe ser válido") @Size(max = 100, message = "El correo no puede superar los 100 caracteres") private String email;
    @NotBlank(message = "El comentario es obligatorio") @Size(max = 500, message = "El comentario no puede superar los 500 caracteres") private String comentario;
}
