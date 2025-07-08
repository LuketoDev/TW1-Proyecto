package com.tallerwebi.dominio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.util.Map;

@Service
public class ServicioDolar {

    private final RestTemplate restTemplate; //Clase que permite hacer llamdas HTTP a externos
    private final String API_URL = obtenerAPIURL();
    private Clock clock;
    private Double cotizacionCacheada = null;
    //Instant ultimoUpdate = Instant.EPOCH;
    //private final long tiempoEspera = obtenerTiempoEspera();

    @Autowired
    public ServicioDolar(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    };

    public ServicioDolar(RestTemplate restTemplate, Clock clock) {
        this.restTemplate = restTemplate;
        this.clock = clock;
    };

    public synchronized Double obtenerCotizacionDolarBlue() {
        return cotizacionCacheada != null ? cotizacionCacheada : obtenerCotizacionDolarDefault();
    }

    @Scheduled(fixedRateString = "#{@servicioDolar.obtenerTiempoEspera()}")
    public synchronized void actualizarCotizacionDolar() {
        Double ultimaCotizacionCacheada = obtenerCotizacionDolarBlue();
        try {
            Map<String, Object> response = restTemplate.getForObject(API_URL, Map.class);
            if (response != null && response.containsKey("venta")) {
                cotizacionCacheada = Double.parseDouble(response.get("venta").toString());
            }
        } catch (Exception e) {
            cotizacionCacheada = ultimaCotizacionCacheada;
        }
    }

    private String obtenerAPIURL(){
        if(System.getenv("API_DOLAR") != null){
            return System.getenv("API_DOLAR");
        } else if (System.getenv("API_DOLAR_SECUNDARIA") != null) {
            return System.getenv("API_DOLAR_SECUNDARIA");
        } else {
            return "https://dolarapi.com/v1/dolares/blue";
        }
    }

    private Double obtenerCotizacionDolarDefault() {
        if (System.getenv("VALOR_DEFAULT_DOLAR") != null) {
            return Double.parseDouble(System.getenv("VALOR_DEFAULT_DOLAR"));
        } else {
            return 1200.0;
        }
    }

    public String obtenerTiempoEspera() {
        String tiempoEspera = System.getenv("TIEMPO_ESPERA");
        if(tiempoEspera != null){
            return String.valueOf(Long.parseLong(tiempoEspera) * 60 * 1000);
        } else {
            return String.valueOf(10 * 60 * 1000);
        }
    }
}
