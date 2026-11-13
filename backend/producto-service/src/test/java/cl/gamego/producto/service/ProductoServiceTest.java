package cl.gamego.producto.service;

import cl.gamego.producto.exception.ConflictException;
import cl.gamego.producto.exception.ResourceNotFoundException;
import cl.gamego.producto.model.Producto;
import cl.gamego.producto.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {
    @Mock
    private ProductoRepository repository;

    @InjectMocks
    private ProductoService service;

    @Test
    void buscarPorId_debeRetornarRegistroCuandoExiste() {
        Producto entidad = new Producto();
        entidad.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(entidad));

        Producto resultado = service.buscarPorId(1L);

        assertEquals(1L, resultado.getId());
        verify(repository).findById(1L);
    }

    @Test
    void buscarPorId_debeLanzarExcepcionCuandoNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.buscarPorId(99L));
    }

    @Test
    void descontarStock_debeRestarUnidades() {
        Producto juego = Producto.builder().id(1L).nombre("Bioshock").stock(10).build();
        when(repository.findById(1L)).thenReturn(Optional.of(juego));
        when(repository.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        Producto resultado = service.descontarStock(1L, 3);

        assertEquals(7, resultado.getStock());
    }

    @Test
    void descontarStock_debeFallarSiNoHayStockSuficiente() {
        Producto juego = Producto.builder().id(1L).nombre("Bioshock").stock(2).build();
        when(repository.findById(1L)).thenReturn(Optional.of(juego));

        assertThrows(ConflictException.class, () -> service.descontarStock(1L, 5));
        verify(repository, never()).save(any());
    }
}
