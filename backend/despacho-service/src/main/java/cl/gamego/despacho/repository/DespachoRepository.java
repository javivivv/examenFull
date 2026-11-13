package cl.gamego.despacho.repository;

import cl.gamego.despacho.model.Despacho;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DespachoRepository extends JpaRepository<Despacho, Long> {
}
