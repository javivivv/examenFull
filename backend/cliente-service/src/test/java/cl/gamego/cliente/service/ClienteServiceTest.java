package cl.gamego.cliente.service;

import cl.gamego.cliente.dto.LoginRequest;
import cl.gamego.cliente.dto.RegistroRequest;
import cl.gamego.cliente.exception.ConflictException;
import cl.gamego.cliente.exception.CredencialesInvalidasException;
import cl.gamego.cliente.exception.ResourceNotFoundException;
import cl.gamego.cliente.model.Cliente;
import cl.gamego.cliente.repository.ClienteRepository;
import cl.gamego.cliente.repository.MensajeContactoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {
    @Mock
    private ClienteRepository repository;

    @Mock
    private MensajeContactoRepository mensajeRepository;

    // Se usa el codificador real: así se prueba que la contraseña queda cifrada de verdad
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @InjectMocks
    private ClienteService service;

    private ClienteService servicioConEncoder() {
        return new ClienteService(repository, mensajeRepository, encoder);
    }

    @Test
    void buscarPorId_debeRetornarRegistroCuandoExiste() {
        Cliente entidad = new Cliente();
        entidad.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(entidad));

        Cliente resultado = service.buscarPorId(1L);

        assertEquals(1L, resultado.getId());
        verify(repository).findById(1L);
    }

    @Test
    void buscarPorId_debeLanzarExcepcionCuandoNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.buscarPorId(99L));
    }

    @Test
    void registrar_debeGuardarContraseñaCifrada() {
        when(repository.existsByEmailIgnoreCase("ana@gmail.com")).thenReturn(false);
        when(repository.save(any(Cliente.class))).thenAnswer(i -> i.getArgument(0));

        Cliente creado = servicioConEncoder().registrar(new RegistroRequest("Ana", "Ana@Gmail.com", "secreto123"));

        assertEquals("ana@gmail.com", creado.getEmail());
        assertNotEquals("secreto123", creado.getPasswordHash());
        assertTrue(encoder.matches("secreto123", creado.getPasswordHash()));
    }

    @Test
    void registrar_debeRechazarCorreoRepetido() {
        when(repository.existsByEmailIgnoreCase("ana@gmail.com")).thenReturn(true);
        assertThrows(ConflictException.class,
                () -> servicioConEncoder().registrar(new RegistroRequest("Ana", "ana@gmail.com", "secreto123")));
        verify(repository, never()).save(any());
    }

    @Test
    void iniciarSesion_debeFallarConContraseñaIncorrecta() {
        Cliente cliente = Cliente.builder().id(1L).email("ana@gmail.com").passwordHash(encoder.encode("correcta1")).build();
        when(repository.findByEmailIgnoreCase("ana@gmail.com")).thenReturn(Optional.of(cliente));

        assertThrows(CredencialesInvalidasException.class,
                () -> servicioConEncoder().iniciarSesion(new LoginRequest("ana@gmail.com", "incorrecta")));
    }

    @Test
    void iniciarSesion_debeEntregarElClienteConCredencialesCorrectas() {
        Cliente cliente = Cliente.builder().id(1L).email("ana@gmail.com").passwordHash(encoder.encode("correcta1")).build();
        when(repository.findByEmailIgnoreCase("ana@gmail.com")).thenReturn(Optional.of(cliente));

        Cliente resultado = servicioConEncoder().iniciarSesion(new LoginRequest("ana@gmail.com", "correcta1"));

        assertEquals(1L, resultado.getId());
    }
}
