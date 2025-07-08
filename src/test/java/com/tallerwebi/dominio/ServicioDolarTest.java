package com.tallerwebi.dominio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServicioDolarTest {

    private ServicioDolar servicioDolar;
    private RestTemplate restTemplate;
    private Clock clock;

    @BeforeEach
    public void init() {
        restTemplate = mock(RestTemplate.class);
        clock = mock(Clock.class);
        servicioDolar = new ServicioDolar(restTemplate, clock);
    }

    @Test
    public void dadoQueExisteUnLlamadoAUnaAPICorroboramosQueDevuelveUnaCotizacionCorrectamente() {
        Map<String, Object> respuestaSimulada = new HashMap<>();
        respuestaSimulada.put("venta", 1200.0);

        when(restTemplate.getForObject("https://dolarapi.com/v1/dolares/blue", Map.class))
                .thenReturn(respuestaSimulada);

        Double resultado = servicioDolar.obtenerCotizacionDolarBlue();
        assertEquals(1200.0, resultado);
    }

    @Test
    public void dadoQueExisteQueElPrimerLlamadoALaAPINoDevuelveUn200DebeRetornar1200ComoCotizacion() {
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        Double cotizacion = servicioDolar.obtenerCotizacionDolarBlue();

        assertEquals(1200.0, cotizacion);
    }

    @Test
    public void dadoQuePasan10MinutosDesdeLaUltimaActualizacionDeLaCotizacionDelDolarSeVuelveARealizarUnLlamadoALaAPIDeFormaAutomatica() {
        Instant ahora = Instant.now();
        when(clock.instant()).thenReturn(ahora);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        Map<String, Object> respuestaSimuladaAntes = new HashMap<>();
        respuestaSimuladaAntes.put("venta", 1200.0);

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(respuestaSimuladaAntes);
        servicioDolar.actualizarCotizacionDolar();

        assertEquals(1200.0, servicioDolar.obtenerCotizacionDolarBlue());

        // Pasan 10 minutos y pide una nueva cotizacion
        Instant despues = ahora.plus(Duration.ofMinutes(10));
        when(clock.instant()).thenReturn(despues);

        Map<String, Object> respuestaSimuladaDespues = new HashMap<>();
        respuestaSimuladaDespues.put("venta", 1500.0);

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(respuestaSimuladaDespues);
        servicioDolar.actualizarCotizacionDolar();

        assertEquals(1500.0, servicioDolar.obtenerCotizacionDolarBlue());
    }

    @Test
    public void dadoQueYaHuboUnLlamadoALaAPISeIntentaHacerUnSegundoLLamadoQueResultaVacioYLaCotizacionDevuelveElValorCacheado() {
        // Primer llamado a la api exitoso
        Map<String, Object> respuestaSimulada = new HashMap<>();
        respuestaSimulada.put("venta", 1200.0);

        // Segundo llamado llega null

        Map<String, Object> respuestaSimuladaVacia = null;

        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(respuestaSimulada)
                .thenReturn(respuestaSimuladaVacia);

        Double cotizacion = servicioDolar.obtenerCotizacionDolarBlue();

        assertEquals(1200.0, cotizacion);
    }

}
