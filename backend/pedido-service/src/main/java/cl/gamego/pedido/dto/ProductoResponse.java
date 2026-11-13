package cl.gamego.pedido.dto;

public record ProductoResponse(Long id, String codigo, String nombre, Integer stock, Integer precio) {}
