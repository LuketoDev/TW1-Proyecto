package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.entidades.Compra;

import javax.persistence.Column;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class CompraDto {

    private Long id;
    private LocalDate fecha;
    private Double total;
    private String metodoDePago;
    private Long idUsuario;
    private List<CompraComponenteDto> productosComprados;
    private String cp;
    private String formaEntrega;
    private Double costoDeEnvio;

    public CompraDto(Compra compra) {
        this.id = compra.getIdCompra();
        this.fecha = LocalDate.now();
        this.total = compra.getTotal();
        this.metodoDePago = compra.getMetodoDePago();
        this.idUsuario = compra.getIdUsuario().getId();
        this.cp = compra.getCp();
        this.productosComprados = compra.getProductosComprados().stream()
                .map(CompraComponenteDto::new)
                .collect(Collectors.toList());
        this.formaEntrega = compra.getFormaEntrega();
        this.costoDeEnvio = compra.getCostoDeEnvio();
    }

    public CompraDto() {
    }

    public List<CompraComponenteDto> getProductosComprados() {
        return productosComprados;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public String getMetodoDePago() {
        return metodoDePago;
    }

    public Double getTotal() {
        return total;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public void setMetodoDePago(String metodoDePago) {
        this.metodoDePago = metodoDePago;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public void setProductosComprados(List<CompraComponenteDto> productosComprados) {
        this.productosComprados = productosComprados;
    }

    public void setFormaEntrega(String formaEntrega) {
        this.formaEntrega = formaEntrega;
    }

    public String getFormaEntrega() {
        return formaEntrega;
    }

    public String getCp() {
        return this.cp;
    }

    public void setCp(String cp) {
        this.cp = cp;
    }

    public Double getCostoDeEnvio() {
        return costoDeEnvio;
    }

    public void setCostoDeEnvio(Double costoDeEnvio) {
        this.costoDeEnvio = costoDeEnvio;
    }
}
