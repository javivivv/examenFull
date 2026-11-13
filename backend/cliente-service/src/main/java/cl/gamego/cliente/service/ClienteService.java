package cl.gamego.cliente.service;

import cl.gamego.cliente.dto.ContactoRequest;
import cl.gamego.cliente.dto.LoginRequest;
import cl.gamego.cliente.dto.RegistroRequest;
import cl.gamego.cliente.exception.ConflictException;
import cl.gamego.cliente.exception.CredencialesInvalidasException;
import cl.gamego.cliente.exception.ResourceNotFoundException;
import cl.gamego.cliente.model.Cliente;
import cl.gamego.cliente.model.MensajeContacto;
import cl.gamego.cliente.repository.ClienteRepository;
import cl.gamego.cliente.repository.MensajeContactoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteService {
    private final ClienteRepository repository;
    private final MensajeContactoRepository mensajeRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Cliente> listar() {
        log.info("Listando clientes");
        return repository.findAll();
    }

    public Cliente buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe cliente con ID " + id));
    }

    public Cliente registrar(RegistroRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Ya existe una cuenta con el correo " + email);
        }
        Cliente entidad = Cliente.builder()
                .nombre(request.getNombre().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .creadoEn(LocalDateTime.now())
                .build();
        log.info("Registrando cliente {}", email);
        return repository.save(entidad);
    }

    public Cliente iniciarSesion(LoginRequest request) {
        Cliente cliente = repository.findByEmailIgnoreCase(request.getEmail().trim())
                .orElseThrow(CredencialesInvalidasException::new);
        if (!passwordEncoder.matches(request.getPassword(), cliente.getPasswordHash())) {
            throw new CredencialesInvalidasException();
        }
        log.info("Inicio de sesión de {}", cliente.getEmail());
        return cliente;
    }

    public MensajeContacto guardarContacto(ContactoRequest request) {
        MensajeContacto mensaje = MensajeContacto.builder()
                .nombre(request.getNombre().trim())
                .email(request.getEmail().trim())
                .comentario(request.getComentario().trim())
                .fecha(LocalDateTime.now())
                .build();
        log.info("Mensaje de contacto recibido de {}", mensaje.getEmail());
        return mensajeRepository.save(mensaje);
    }

    public void eliminar(Long id) {
        Cliente entidad = buscarPorId(id);
        repository.delete(entidad);
        log.info("Eliminado cliente ID {}", id);
    }
}
