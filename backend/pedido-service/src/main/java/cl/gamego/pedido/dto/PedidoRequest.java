package cl.gamego.pedido.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PedidoRequest {
    @NotNull(message = "El cliente es obligatorio") private Long clienteId;
    @NotEmpty(message = "El pedido debe tener al menos un producto") @Valid private List<ItemPedidoRequest> items;
}
