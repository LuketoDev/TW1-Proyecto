package com.tallerwebi.dominio.entidades;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Procesador extends Componente {

    /*Caracteristicas generales*/
    @Column(length = 100)
    private String modelo;
    @ManyToOne
    @JoinColumn(name = "socket_id")
    private Socket socket;
    @Column(length = 100)
    private String procesoDeFabricacion;
    private Boolean incluyeGraficosIntegrados;
    @Column(length = 100)
    private String chipsetGPU;
    @Column(length = 100)
    private String familia;

    /*Especificaciones CPU*/
    private Integer nucleos;
    private Integer hilos;
    @Column(length = 100)
    private String frecuencia;
    @Column(length = 100)
    private String frecuenciaTurbo;

    /*Coolers y Disipadores*/
    private Boolean incluyeCooler;
    @Column(length = 100)
    private String consumo;

    /*Memoria*/
    @Column(length = 100)
    private String l1Cache;
    @Column(length = 100)
    private String l2Cache;
    @Column(length = 100)
    private String l3Cache;

    public Procesador() {}

    public Procesador(Long id, String nombre, Double precio, Integer stock, String marca,
                      String modelo, Socket socket, String procesoDeFabricacion, Boolean incluyeGraficosIntegrados, String chipsetGPU, String familia,
                      Integer nucleos, Integer hilos, String frecuencia, String frecuenciaTurbo,
                      Boolean incluyeCooler, String consumo,
                      String l1Cache, String l2Cache, String l3Cache) {
        super(id, nombre, precio, stock, marca);

        this.modelo = modelo;
        this.socket = socket;
        this.procesoDeFabricacion = procesoDeFabricacion;
        this.incluyeGraficosIntegrados = incluyeGraficosIntegrados;
        this.chipsetGPU = chipsetGPU;
        this.familia = familia;

        this.nucleos = nucleos;
        this.hilos = hilos;
        this.frecuencia = frecuencia;
        this.frecuenciaTurbo = frecuenciaTurbo;

        this.incluyeCooler = incluyeCooler;
        this.consumo = consumo;

        this.l1Cache = l1Cache;
        this.l2Cache = l2Cache;
        this.l3Cache = l3Cache;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getProcesoDeFabricacion() {
        return procesoDeFabricacion;
    }

    public void setProcesoDeFabricacion(String procesoDeFabricacion) {
        this.procesoDeFabricacion = procesoDeFabricacion;
    }

    public Boolean getIncluyeGraficosIntegrados() {
        return incluyeGraficosIntegrados;
    }

    public void setIncluyeGraficosIntegrados(Boolean incluyeGraficosIntegrados) {
        this.incluyeGraficosIntegrados = incluyeGraficosIntegrados;
    }

    public String getChipsetGPU() {
        return chipsetGPU;
    }

    public void setChipsetGPU(String chipsetGPU) {
        this.chipsetGPU = chipsetGPU;
    }

    public String getFamilia() {
        return familia;
    }

    public void setFamilia(String familia) {
        this.familia = familia;
    }

    public Integer getNucleos() {
        return nucleos;
    }

    public void setNucleos(Integer nucleos) {
        this.nucleos = nucleos;
    }

    public Integer getHilos() {
        return hilos;
    }

    public void setHilos(Integer hilos) {
        this.hilos = hilos;
    }

    public String getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(String frecuencia) {
        this.frecuencia = frecuencia;
    }

    public String getFrecuenciaTurbo() {
        return frecuenciaTurbo;
    }

    public void setFrecuenciaTurbo(String frecuenciaTurbo) {
        this.frecuenciaTurbo = frecuenciaTurbo;
    }

    public Boolean getIncluyeCooler() {
        return incluyeCooler;
    }

    public void setIncluyeCooler(Boolean incluyeCooler) {
        this.incluyeCooler = incluyeCooler;
    }

    public String getConsumo() {
        return consumo;
    }

    public void setConsumo(String consumo) {
        this.consumo = consumo;
    }

    public String getL1Cache() {
        return l1Cache;
    }

    public void setL1Cache(String l1Cache) {
        this.l1Cache = l1Cache;
    }

    public String getL2Cache() {
        return l2Cache;
    }

    public void setL2Cache(String l2Cache) {
        this.l2Cache = l2Cache;
    }

    public String getL3Cache() {
        return l3Cache;
    }

    public void setL3Cache(String l3Cache) {
        this.l3Cache = l3Cache;
    }

}
