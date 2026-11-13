package cl.gamego.pedido.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemPedidoRequest {
    @NotNull(message = "El producto es obligatorio") private Long productoId;
    @NotNull(message = "La cantidad es obligatoria") @Min(value = 1, message = "La cantidad debe ser mayor a cero") private Integer cantidad;
}
