package cl.gamego.cliente.repository;

import cl.gamego.cliente.model.MensajeContacto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MensajeContactoRepository extends JpaRepository<MensajeContacto, Long> {
}
