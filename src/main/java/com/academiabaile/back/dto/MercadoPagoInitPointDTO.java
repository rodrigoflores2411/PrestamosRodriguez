package com.academiabaile.back.dto;

public class MercadoPagoInitPointDTO {
    private String initPoint;

    public MercadoPagoInitPointDTO(String initPoint) {
        this.initPoint = initPoint;
    }

    // Getter y Setter
    public String getInitPoint() {
        return initPoint;
    }

    public void setInitPoint(String initPoint) {
        this.initPoint = initPoint;
    }
}
