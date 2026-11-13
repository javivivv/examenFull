package cl.gamego.pedido.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EstadoRequest {
    @NotBlank(message = "El estado es obligatorio") private String estado;
}
