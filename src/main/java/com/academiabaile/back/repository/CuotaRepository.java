package com.academiabaile.back.repository;

import com.academiabaile.back.entidades.Cuota;
import com.academiabaile.back.entidades.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface CuotaRepository extends JpaRepository<Cuota, Long> {

    /**
     * Busca todas las cuotas que no han sido pagadas.
     * Utilizado por el job de cálculo de intereses.
     */
    List<Cuota> findByPagada(boolean pagada);

    /**
     * Busca las cuotas de un préstamo específico que aún no han sido pagadas.
     * Utilizado para verificar si un préstamo está completamente saldado.
     */
    List<Cuota> findByPrestamoAndPagada(Prestamo prestamo, boolean pagada);

    /**
     * Cuenta la cantidad de cuotas de un préstamo específico que aún no han sido pagadas.
     * Es más eficiente que traer la lista completa.
     */
    long countByPrestamoAndPagada(Prestamo prestamo, boolean pagada);
}
