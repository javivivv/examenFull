package cl.gamego.despacho.dto;

import java.time.LocalDateTime;

public record PedidoResponse(Long id, Long clienteId, Integer total, String estado, LocalDateTime fechaCreacion) {}
