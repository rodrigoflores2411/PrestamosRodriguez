package com.academiabaile.back.entidades;

import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Data
@Entity
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descripcion;

    @ManyToOne
    private Cliente cliente;

    private Date fechaEmision;

    private Double monto;

    private Double interes;

    private Integer meses;

    private boolean pagado;

    @OneToMany(mappedBy = "prestamo", cascade = CascadeType.ALL)
    private List<Cuota> cuotas;
}
