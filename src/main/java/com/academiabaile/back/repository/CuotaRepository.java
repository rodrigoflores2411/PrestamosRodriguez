package com.academiabaile.back.repository;

import com.academiabaile.back.entidades.Cuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface CuotaRepository extends JpaRepository<Cuota, Long> {

    List<Cuota> findByPagadaIsFalseAndFechaPagoBefore(Date fecha);
}
