package cl.gamego.producto.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRequest {
    @NotBlank(message = "El código es obligatorio") private String codigo;
    @NotBlank(message = "El nombre es obligatorio") private String nombre;
    @NotBlank(message = "La sección es obligatoria") private String seccion;
    @NotBlank(message = "La imagen es obligatoria") private String imagen;
    private String trailer;
    @NotNull(message = "El stock es obligatorio") @Min(value = 0, message = "El stock no puede ser negativo") private Integer stock;
    @NotNull(message = "El precio es obligatorio") @Min(value = 1, message = "El precio debe ser mayor a cero") private Integer precio;
}
