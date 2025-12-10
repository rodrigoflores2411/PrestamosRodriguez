package com.academiabaile.back.repository;

import com.academiabaile.back.entidades.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    /**
     * Cuenta la cantidad de pagos registrados para un préstamo específico.
     * Spring Data JPA genera automáticamente la consulta a partir del nombre del método.
     * @param prestamoId El ID del préstamo.
     * @return El número de pagos existentes para ese préstamo.
     */
    long countByPrestamoId(Long prestamoId);
}
