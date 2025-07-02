package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;

import com.tallerwebi.dominio.entidades.Componente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.*;

import static junit.framework.Assert.*;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ControladorCarritoTest {

    @Mock
    private ProductoCarritoDto productoMock1;
    @Mock
    private ProductoCarritoDto productoMock2;
    @Mock
    private ServicioProductoCarritoImpl servicioProductoCarritoImplMock;
    @Mock
    private ServicioDeEnviosImpl servicioEnviosMock;
    @Mock
    private ServicioPrecios servicioPreciosMock;
    @Mock
    private HttpSession httpSessionMock;

    private CarritoController carritoController;
    private List<ProductoCarritoDto> carritoSesion;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        carritoController = new CarritoController(servicioProductoCarritoImplMock, servicioEnviosMock, servicioPreciosMock);
        carritoSesion = new ArrayList<>();

    }

    // mostrarVistaCarritoDeCompras
    @Test
    public void cuandoQuieroVerElCarritoDeComprasObtengoLaVistaDelCarrito() {
        carritoSesion.add(productoMock1);
        carritoSesion.add(productoMock2);

        when(productoMock1.getPrecio()).thenReturn(100.0);
        when(productoMock2.getPrecio()).thenReturn(130.0);

        when(httpSessionMock.getAttribute("carritoSesion")).thenReturn(carritoSesion);

        when(servicioProductoCarritoImplMock.getProductos()).thenReturn(carritoSesion);
        when(servicioProductoCarritoImplMock.calcularValorTotalDeLosProductos()).thenReturn(230.0);
        when(servicioProductoCarritoImplMock.calcularCantidadTotalDeProductos()).thenReturn(2);

        when(servicioPreciosMock.conversionDolarAPeso(230.0)).thenReturn("$230.0");

        ModelAndView mostrarvista = carritoController.mostrarVistaCarritoDeCompras(httpSessionMock);
        ModelMap model = (ModelMap) mostrarvista.getModel();

        assertThat(mostrarvista.getViewName(), equalTo("carritoDeCompras"));
        assertThat(model.get("cantidadEnCarrito"), equalTo(2));
        assertThat(model.get("productos"), equalTo(carritoSesion));
        assertThat(model.get("valorTotal"), equalTo("$230.0"));

        verify(servicioProductoCarritoImplMock, times(1)).calcularValorTotalDeLosProductos();
        verify(servicioProductoCarritoImplMock, times(1)).calcularCantidadTotalDeProductos();
        verify(servicioProductoCarritoImplMock, times(1)).getProductos();
        verify(httpSessionMock, times(1)).getAttribute("carritoSesion");
    }

    // mostrarResumenCarritoDeCompras
    @Test
    public void cuandoQuieroVerElResumenDelCarritoDeComprasObtengoElResumenCarrito() {
        carritoSesion.add(productoMock1);
        carritoSesion.add(productoMock2);

        when(productoMock1.getPrecio()).thenReturn(100.0);
        when(productoMock2.getPrecio()).thenReturn(130.0);

        when(httpSessionMock.getAttribute("carritoSesion")).thenReturn(carritoSesion);

        when(servicioProductoCarritoImplMock.getProductos()).thenReturn(carritoSesion);
        when(servicioProductoCarritoImplMock.calcularValorTotalDeLosProductos()).thenReturn(230.0);
        when(servicioProductoCarritoImplMock.calcularCantidadTotalDeProductos()).thenReturn(2);

        when(servicioPreciosMock.conversionDolarAPeso(230.0)).thenReturn("$230.0");

        ModelAndView mostrarResumen = carritoController.mostrarResumenCarritoDeCompras(httpSessionMock);
        ModelMap model = (ModelMap) mostrarResumen.getModel();

        assertThat(mostrarResumen.getViewName(), equalTo("fragments/fragments :: resumenCarrito"));
        assertThat(model.get("cantidadEnCarrito"), equalTo(2));
        assertThat(model.get("productos"), equalTo(carritoSesion));
        assertThat(model.get("valorTotal"), equalTo("$230.0"));

        verify(servicioProductoCarritoImplMock, times(1)).calcularValorTotalDeLosProductos();
        verify(servicioProductoCarritoImplMock, times(1)).calcularCantidadTotalDeProductos();
        verify(servicioProductoCarritoImplMock, times(1)).getProductos();
        verify(httpSessionMock, times(1)).getAttribute("carritoSesion");
    }

    // eliminarProductoDelCarrito
    @Test
    public void cuandoQuieroEliminarUnProductoDelCarritoObtengoLaListaDeProductosDelCarritoSinEseProducto() {
        carritoSesion.add(productoMock1);
        carritoSesion.add(productoMock2);

        when(productoMock1.getPrecio()).thenReturn(100.0);
        when(productoMock2.getPrecio()).thenReturn(130.0);

        when(productoMock1.getId()).thenReturn(1L);
        when(productoMock2.getId()).thenReturn(2L);

        when(productoMock1.getCantidad()).thenReturn(1);
        when(productoMock2.getCantidad()).thenReturn(1);

        when(httpSessionMock.getAttribute("carritoSesion")).thenReturn(carritoSesion);

        List<ProductoCarritoDto> productosSinUno = new ArrayList<>();
        productosSinUno.add(productoMock2);

        when(servicioProductoCarritoImplMock.buscarPorId(1L)).thenReturn(productoMock1);
        when(servicioProductoCarritoImplMock.getProductos()).thenReturn(carritoSesion).thenReturn(productosSinUno);
        when(servicioProductoCarritoImplMock.calcularValorTotalDeLosProductos()).thenReturn(130.0);
        when(servicioProductoCarritoImplMock.calcularCantidadTotalDeProductos()).thenReturn(1);

        Map<String, Object> response = carritoController.eliminarProductoDelCarrito(1L, httpSessionMock);

        assertThat(response.get("eliminado"), equalTo(true));
        assertThat(response.get("productos"), equalTo(productosSinUno));
        assertThat(response.get("cantidadEnCarrito"), equalTo(1));
        assertThat(response.get("valorTotal"), equalTo(130.0));

        verify(servicioProductoCarritoImplMock, times(1)).calcularValorTotalDeLosProductos();
        verify(servicioProductoCarritoImplMock, times(1)).calcularCantidadTotalDeProductos();
        verify(servicioProductoCarritoImplMock, times(3)).getProductos();
        verify(servicioProductoCarritoImplMock, times(1)).buscarPorId(1L);
        verify(httpSessionMock, times(1)).getAttribute("carritoSesion");
    }

    // calcularValorTotalDeLosProductosConDescuento
    @Test
    public void cuandoIntroduzcoUnCodigoDeDescuentoDelCincoPorcientoObtengoElPrecioTotalDelCarritoConSuDescuentoAplicado() {
        when(productoMock1.getPrecio()).thenReturn(100.0);
        when(productoMock1.getCantidad()).thenReturn(2);
        when(productoMock2.getPrecio()).thenReturn(130.0);
        when(productoMock1.getCantidad()).thenReturn(1);

        carritoSesion.add(productoMock1);
        carritoSesion.add(productoMock2);

        when(servicioProductoCarritoImplMock.getProductos()).thenReturn(carritoSesion);
        when(servicioProductoCarritoImplMock.calcularDescuento(5)).thenReturn(313.5);

        Map<String, String> inputConDescuento = new HashMap<>();
        inputConDescuento.put("codigoInput", "baratija5");

        Map<String, Object> porcentajeDescuento = carritoController.calcularValorTotalDeLosProductosConDescuento(inputConDescuento);

        assertThat(porcentajeDescuento.get("mensaje"), equalTo("Descuento aplicado! Nuevo total: $313.5"));
        assertThat(porcentajeDescuento.get("valorTotal"), equalTo(313.5));

        verify(servicioProductoCarritoImplMock, times(1)).calcularDescuento(5);
    }

    @Test
    public void cuandoIntroduzcoUnCodigoDeDescuentoDelDiezPorcientoObtengoElPrecioTotalDelCarritoConSuDescuentoAplicado() {
        when(productoMock1.getPrecio()).thenReturn(100.0);
        when(productoMock1.getCantidad()).thenReturn(2);
        when(productoMock2.getPrecio()).thenReturn(130.0);
        when(productoMock1.getCantidad()).thenReturn(1);

        carritoSesion.add(productoMock1);
        carritoSesion.add(productoMock2);

        when(servicioProductoCarritoImplMock.getProductos()).thenReturn(carritoSesion);
        when(servicioProductoCarritoImplMock.calcularDescuento(10)).thenReturn(297.0);

        Map<String, String> inputConDescuento = new HashMap<>();
        inputConDescuento.put("codigoInput", "baratija10");

        Map<String, Object> porcentajeDescuento = carritoController.calcularValorTotalDeLosProductosConDescuento(inputConDescuento);

        assertThat(porcentajeDescuento.get("mensaje"), equalTo("Descuento aplicado! Nuevo total: $297.0"));
        assertThat(porcentajeDescuento.get("valorTotal"), equalTo(297.0));

        verify(servicioProductoCarritoImplMock, times(1)).calcularDescuento(10);
    }

    @Test
    public void cuandoIntroduzcoUnCodigoDeDescuentoDelQuincePorcientoObtengoElPrecioTotalDelCarritoConSuDescuentoAplicado() {
        when(productoMock1.getPrecio()).thenReturn(100.0);
        when(productoMock1.getCantidad()).thenReturn(2);
        when(productoMock2.getPrecio()).thenReturn(130.0);
        when(productoMock1.getCantidad()).thenReturn(1);

        carritoSesion.add(productoMock1);
        carritoSesion.add(productoMock2);

        when(servicioProductoCarritoImplMock.getProductos()).thenReturn(carritoSesion);
        when(servicioProductoCarritoImplMock.calcularDescuento(15)).thenReturn(280.5);

        Map<String, String> inputConDescuento = new HashMap<>();
        inputConDescuento.put("codigoInput", "baratija15");

        Map<String, Object> porcentajeDescuento = carritoController.calcularValorTotalDeLosProductosConDescuento(inputConDescuento);

        assertThat(porcentajeDescuento.get("mensaje"), equalTo("Descuento aplicado! Nuevo total: $280.5"));
        assertThat(porcentajeDescuento.get("valorTotal"), equalTo(280.5));

        verify(servicioProductoCarritoImplMock, times(1)).calcularDescuento(15);
    }

    @Test
    public void cuandoIntroduzcoUnCodigoDeDescuentoInvalidoObtengoMensajeDeError() {
        Map<String, String> input = new HashMap<>();
        input.put("codigoInput", "descuento99");

        Map<String, Object> response = carritoController.calcularValorTotalDeLosProductosConDescuento(input);

        assertThat(response.get("mensajeDescuento"), equalTo("Codigo de descuento invalido!"));
        assertTrue(response.containsKey("mensajeDescuento"));
    }

    // agregarMasCantidadDeUnProducto
    @Test
    public void cuandoQuieroAgregarUnaUnidadDeUnMismoProductoAlCarritoObtengoEseProductoConLaCantidadSumadaYElValorTotalDelCarritoActualizado() {
        ProductoCarritoDto productoMock = mock(ProductoCarritoDto.class);

        Long productoId = 1L;
        Integer cantidadInicial = 2;
        Integer cantidadFinal = 3;
        Integer stockInicial = 3;
        Double precioUnitario = 8000.0;
        Double precioTotalEsperado = 24000.0;
        Double precioTotalDelProducto = 24000.0;

        when(httpSessionMock.getAttribute("carritoSesion")).thenReturn(carritoSesion);

        servicioProductoCarritoImplMock.descontarStockAlComponente(productoId, 1);
        productoMock.setStock(productoMock.getStock() - 1);

        when(productoMock.getId()).thenReturn(1L);
        when(productoMock.getCantidad()).thenReturn(cantidadInicial, cantidadFinal);
        when(productoMock.getPrecio()).thenReturn(precioUnitario);
        when(productoMock.getStock()).thenReturn(stockInicial);

        when(servicioProductoCarritoImplMock.buscarPorId(productoId)).thenReturn(productoMock);
        when(servicioProductoCarritoImplMock.verificarStock(productoId)).thenReturn(true);
        when(servicioProductoCarritoImplMock.calcularValorTotalDeLosProductos()).thenReturn(precioTotalEsperado);

        Map<String, Object> response = carritoController.agregarMasCantidadDeUnProducto(productoId, httpSessionMock);

        assertNotNull(response.get("cantidadEnCarrito"));
        assertThat(response.get("cantidad"), equalTo(cantidadFinal));
        assertThat(response.get("valorTotal"), equalTo(precioTotalEsperado));
        assertThat(response.get("precioTotalDelProducto"), equalTo(precioTotalDelProducto));

        verify(servicioProductoCarritoImplMock, times(1)).buscarPorId(1L);
        verify(servicioProductoCarritoImplMock, times(1)).calcularValorTotalDeLosProductos();
    }

    // restarCantidadDeUnProducto
    @Test
    public void cuandoQuieroRestarUnaUnidadDeUnMismoProductoAlCarritoObtengoEseProductoConLaCantidadSumadaYElValorTotalDelCarritoActualizado() {
        ProductoCarritoDto productoMock = mock(ProductoCarritoDto.class);

        Long productoId = 1L;
        Integer cantidadInicial = 2;
        Integer cantidadFinal = 1;
        Integer stockInicial = 3;
        Double precioUnitario = 8000.0;
        Double precioTotalEsperado = 8000.0;
        Double precioTotalDelProducto = 8000.0;

        when(httpSessionMock.getAttribute("carritoSesion")).thenReturn(carritoSesion);

        servicioProductoCarritoImplMock.devolverStockAlComponente(productoId, 1);
        productoMock.setStock(productoMock.getStock() - 1);

        when(productoMock.getId()).thenReturn(productoId);
        when(productoMock.getCantidad()).thenReturn(cantidadInicial, cantidadFinal);
        when(productoMock.getPrecio()).thenReturn(precioUnitario);
        when(productoMock.getStock()).thenReturn(stockInicial);

        when(servicioProductoCarritoImplMock.buscarPorId(productoId)).thenReturn(productoMock);

        when(servicioProductoCarritoImplMock.verificarStock(productoId)).thenReturn(true);
        when(servicioProductoCarritoImplMock.calcularValorTotalDeLosProductos()).thenReturn(precioTotalEsperado);
        when(servicioProductoCarritoImplMock.calcularCantidadTotalDeProductos()).thenReturn(cantidadFinal);

        Map<String, Object> response = carritoController.restarCantidadDeUnProducto(productoId, httpSessionMock);

        assertNotNull(response.get("cantidadEnCarrito"));
        assertThat(response.get("cantidad"), equalTo(cantidadFinal));
        assertThat(response.get("valorTotal"), equalTo(precioTotalEsperado));
        assertThat(response.get("precioTotalDelProducto"), equalTo(precioTotalDelProducto));

        verify(servicioProductoCarritoImplMock, times(1)).buscarPorId(1L);
        verify(servicioProductoCarritoImplMock, times(1)).calcularValorTotalDeLosProductos();
    }

    // procesarCompra
    @Test
    public void cuandoSeleccionoElMetodoDePagoValidoSinEnvioObtengoUnMensajeDeError() {
        carritoController.envioActual = null;
        carritoController.codigoPostalActual = null;

        String metodoPagoValido = "mercadoPago";

        Map<String, Object> response = carritoController.procesarCompra(metodoPagoValido);

        assertFalse((Boolean) response.get("success"));
        assertEquals("Debes agregar un codigo postal", response.get("error"));
        assertNull(response.get("costoEnvio"));
    }

    @Test
    public void cuandoSeleccionoElMetodoDePagoValidoConEnvioIncluyeCostoDeEnvio() {
        String metodoPagoValido = "mercadoPago";

        EnvioDto envioDto = new EnvioDto();
        envioDto.setCosto(1500.0);
        envioDto.setDestino("Ramos Mejia");

        carritoController.envioActual = envioDto;
        carritoController.codigoPostalActual = "1704";

        Map<String, Object> response = carritoController.procesarCompra(metodoPagoValido);

        assertTrue((Boolean) response.get("success"));
        assertEquals("mercadoPago", response.get("metodoPago"));
        assertEquals(1500.0, (Double) response.get("costoEnvio"));
    }

    @Test
    public void cuandoElMetodoDePagoEsNullDebeRetornarError() {
        String metodoPagoNull = null;

        Map<String, Object> response = carritoController.procesarCompra(metodoPagoNull);

        assertEquals(false, response.get("success"));
        assertEquals("Debes seleccionar un metodo de pago", response.get("error"));
        assertNull(response.get("metodoPago"));
        assertNull(response.get("costoEnvio"));
    }

    @Test
    public void cuandoElMetodoDePagoEsVacioDebeRetornarError() {
        String metodoPagoVacio = "";

        Map<String, Object> response = carritoController.procesarCompra(metodoPagoVacio);

        assertEquals(false, response.get("success"));
        assertEquals("Debes seleccionar un metodo de pago", response.get("error"));
        assertNull(response.get("metodoPago"));
    }

    // calcularEnvio
    @Test
    public void cuandoIngresoUnCodigoPostalValidoObtengoLaVistaDelCarritoConSusValores() {
        String codigoPostalValido = "1704";
        Double costoEnvio = 1500.0;
        Double totalCarrito = 10000.0;

        EnvioDto envioDto = new EnvioDto();
        envioDto.setCosto(costoEnvio);
        envioDto.setDestino("Ramos Mejia");
        envioDto.setTiempo("1-2 dias habiles");

        when(httpSessionMock.getAttribute("carritoSesion")).thenReturn(carritoSesion);

        when(servicioEnviosMock.calcularEnvio(codigoPostalValido)).thenReturn(envioDto);
        when(servicioProductoCarritoImplMock.getProductos()).thenReturn(List.of());
        when(servicioProductoCarritoImplMock.calcularValorTotalDeLosProductos()).thenReturn(totalCarrito);

        ModelAndView modelAndView = carritoController.calcularEnvio(codigoPostalValido, httpSessionMock);
        ModelMap modelMap = (ModelMap) modelAndView.getModel();

        assertThat(modelAndView.getViewName(), equalTo("carritoDeCompras"));
        assertThat(modelMap.get("envioCalculado"), equalTo(true));
        assertThat(modelMap.get("sinCobertura"), equalTo(false));
        assertThat(modelMap.get("totalConEnvio"), equalTo(totalCarrito + costoEnvio));
        assertThat(modelMap.get("envio"), equalTo(envioDto));

        verify(servicioProductoCarritoImplMock, times(1)).getProductos();
        verify(servicioProductoCarritoImplMock, times(1)).calcularValorTotalDeLosProductos();
        verify(servicioEnviosMock, times(1)).calcularEnvio(codigoPostalValido);
    }

    @Test
    public void cuandoIngresoUnCodigoPostalNoValidoObtengoErrorEnElEnvio() {
        String codigoPostalInvalido = "12";

        ModelAndView modelAndView = carritoController.calcularEnvio(codigoPostalInvalido, httpSessionMock);

        ModelMap modelMap = (ModelMap) modelAndView.getModel();

        assertThat(modelAndView.getViewName(), equalTo("carritoDeCompras"));
        assertThat(modelMap.get("envioCalculado"), equalTo(false));
        assertThat(modelMap.get("sinCobertura"), equalTo(false));
        assertThat(modelMap.get("codigoPostal"), equalTo(codigoPostalInvalido));
        assertThat(modelMap.get("errorEnvio"), equalTo("El código postal debe tener 4 dígitos"));
    }

    @Test
    public void cuandoElCodigoPostalTieneMasDeCuatroNumerosDevuelveError() {
        String codigoPostalInvalido = "12345";

        when(servicioProductoCarritoImplMock.getProductos()).thenReturn(List.of());
        when(servicioProductoCarritoImplMock.calcularValorTotalDeLosProductos()).thenReturn(0.0);

        ModelAndView modelAndView = carritoController.calcularEnvio(codigoPostalInvalido, httpSessionMock);
        ModelMap modelMap = modelAndView.getModelMap();

        assertThat(modelAndView.getViewName(), equalTo("carritoDeCompras"));
        assertThat(modelMap.get("envioCalculado"), equalTo(false));
        assertThat(modelMap.get("sinCobertura"), equalTo(false));
        assertThat(modelMap.get("errorEnvio"), equalTo("El código postal debe tener 4 dígitos"));

        verify(servicioProductoCarritoImplMock, times(1)).getProductos();
        verify(servicioProductoCarritoImplMock, times(1)).calcularValorTotalDeLosProductos();
    }

    @Test
    public void cuandoElCodigoPostalTieneLetrasDevuelveError() {
        String codigoPostalInvalido = "aa15";
        when(servicioProductoCarritoImplMock.getProductos()).thenReturn(List.of());
        when(servicioProductoCarritoImplMock.calcularValorTotalDeLosProductos()).thenReturn(0.0);

        ModelAndView modelAndView = carritoController.calcularEnvio(codigoPostalInvalido, httpSessionMock);
        ModelMap modelMap = modelAndView.getModelMap();

        assertThat(modelAndView.getViewName(), equalTo("carritoDeCompras"));
        assertThat(modelMap.get("envioCalculado"), equalTo(false));
        assertThat(modelMap.get("sinCobertura"), equalTo(false));
        assertThat(modelMap.get("errorEnvio"), equalTo("El código postal debe tener 4 dígitos"));

        verify(servicioProductoCarritoImplMock, times(1)).getProductos();
        verify(servicioProductoCarritoImplMock, times(1)).calcularValorTotalDeLosProductos();
    }

    @Test
    public void cuandoNoHayCoberturaParaElCodigoPostalMuestraMensaje() {
        String codigoPostalSinCobertura = "9999";
        when(servicioEnviosMock.calcularEnvio(codigoPostalSinCobertura)).thenReturn(null);
        when(servicioProductoCarritoImplMock.getProductos()).thenReturn(List.of());
        when(servicioProductoCarritoImplMock.calcularValorTotalDeLosProductos()).thenReturn(0.0);

        ModelAndView modelAndView = carritoController.calcularEnvio(codigoPostalSinCobertura, httpSessionMock);
        ModelMap modelMap = modelAndView.getModelMap();

        assertThat(modelAndView.getViewName(), equalTo("carritoDeCompras"));
        assertThat(modelMap.get("envioCalculado"), equalTo(false));
        assertThat(modelMap.get("sinCobertura"), equalTo(true));
        assertThat(modelMap.get("codigoPostal"), equalTo(codigoPostalSinCobertura));
        assertThat(modelMap.get("mensajeEnvio"), equalTo("No disponemos de envío Andreani para este código postal"));

        verify(servicioProductoCarritoImplMock, times(1)).getProductos();
        verify(servicioProductoCarritoImplMock, times(1)).calcularValorTotalDeLosProductos();
        verify(servicioEnviosMock,  times(1)).calcularEnvio(codigoPostalSinCobertura);
    }

    @Test
    public void cuandoElCodigoPostalEsNuloNoCalculaEnvio() {
        when(servicioProductoCarritoImplMock.getProductos()).thenReturn(List.of());
        when(servicioProductoCarritoImplMock.calcularValorTotalDeLosProductos()).thenReturn(0.0);

        ModelAndView modelAndView = carritoController.calcularEnvio("", httpSessionMock);
        ModelMap modelMap = modelAndView.getModelMap();

        assertThat(modelAndView.getViewName(), equalTo("carritoDeCompras"));
        assertThat(modelMap.get("envioCalculado"), equalTo(null));
        assertThat(modelMap.get("codigoPostal"), equalTo(""));

        verify(servicioProductoCarritoImplMock, times(1)).getProductos();
        verify(servicioProductoCarritoImplMock, times(1)).calcularValorTotalDeLosProductos();
    }

    @Test
    public void cuandoElCodigoPostalSoloTieneEspaciosNoSeCalculaElEnvio() {
        when(servicioProductoCarritoImplMock.getProductos()).thenReturn(List.of());
        when(servicioProductoCarritoImplMock.calcularValorTotalDeLosProductos()).thenReturn(0.0);

        ModelAndView modelAndView = carritoController.calcularEnvio(" ", httpSessionMock);
        ModelMap modelMap = modelAndView.getModelMap();

        assertThat(modelAndView.getViewName(), equalTo("carritoDeCompras"));
        assertThat(modelMap.get("envioCalculado"), equalTo(null));
        assertThat(modelMap.get("codigoPostal"), equalTo(" "));

        verify(servicioProductoCarritoImplMock, times(1)).getProductos();
        verify(servicioProductoCarritoImplMock, times(1)).calcularValorTotalDeLosProductos();
    }

    @Test
    public void cuandoElServicioDeEnviosLanzaExcepcionDevuelveError() {
        String codigoPostal = "1704";

        when(servicioEnviosMock.calcularEnvio(codigoPostal)).thenThrow(new RuntimeException("Error"));
        when(servicioProductoCarritoImplMock.getProductos()).thenReturn(List.of());
        when(servicioProductoCarritoImplMock.calcularValorTotalDeLosProductos()).thenReturn(0.0);

        ModelAndView modelAndView = carritoController.calcularEnvio(codigoPostal, httpSessionMock);
        ModelMap modelMap = modelAndView.getModelMap();

        assertThat(modelAndView.getViewName(), equalTo("carritoDeCompras"));
        assertThat(modelMap.get("envioCalculado"), equalTo(false));
        assertThat(modelMap.get("sinCobertura"), equalTo(false));
        assertThat(modelMap.get("errorEnvio"), equalTo("Error al calcular envío. Intenta nuevamente."));

        verify(servicioProductoCarritoImplMock, times(1)).getProductos();
        verify(servicioProductoCarritoImplMock, times(1)).calcularValorTotalDeLosProductos();
        verify(servicioEnviosMock, times(1)).calcularEnvio(codigoPostal);
    }

    @Test
    public void siempreDevuelveLaVistaCorrectaConProductosYTotal() {
        String codigoPostal = "1704";
        List<ProductoCarritoDto> productos = Arrays.asList(new ProductoCarritoDto(1L, "Motherboard", 50000.0, 1));
        Double total = productos.get(0).getPrecio();

        when(servicioProductoCarritoImplMock.getProductos()).thenReturn(productos);
        when(servicioProductoCarritoImplMock.calcularValorTotalDeLosProductos()).thenReturn(total);

        ModelAndView modelAndView = carritoController.calcularEnvio(codigoPostal, httpSessionMock);
        ModelMap modelMap = modelAndView.getModelMap();

        assertThat(modelAndView.getViewName(), equalTo("carritoDeCompras"));
        assertThat(modelMap.get("valorTotal"), equalTo(total));
        assertThat(modelMap.get("productos"), equalTo(productos));
        assertThat(modelMap.get("codigoPostal"), equalTo(codigoPostal));

        verify(servicioProductoCarritoImplMock, times(1)).getProductos();
        verify(servicioProductoCarritoImplMock, times(1)).calcularValorTotalDeLosProductos();
    }

    // calcularEnvioAjax
    @Test
    public void cuandoCalculoEnvioConCodigoValidoDevuelveEnvioExitoso() {
        String codigoPostal = "1704";
        Double valorTotalCarrito = 50000.0;

        EnvioDto envioDto = new EnvioDto();
        envioDto.setCosto(1500.0);
        envioDto.setDestino("Ramos Mejia");
        envioDto.setTiempo("2-3 dias habiles");

        when(httpSessionMock.getAttribute("carritoSesion")).thenReturn(carritoSesion);

        when(servicioEnviosMock.calcularEnvio(codigoPostal)).thenReturn(envioDto);
        servicioProductoCarritoImplMock.valorTotal = valorTotalCarrito;

        Map<String, Object> response = carritoController.calcularEnvioAjax(codigoPostal, httpSessionMock);

        assertThat(response.get("costo"), equalTo(envioDto.getCosto()));
        assertThat(response.get("success"), equalTo(true));
        assertThat(response.get("tiempo"), equalTo(envioDto.getTiempo()));
        assertThat(response.get("destino"), equalTo(envioDto.getDestino()));
        assertThat(response.get("valorTotal"), equalTo(valorTotalCarrito));

        verify(servicioEnviosMock, times(1)).calcularEnvio(codigoPostal);
    }

    @Test
    public void cuandoCalculoEnvioSinCoberturaDevuelveError() {
        String codigoPostal = "9999";

        when(servicioEnviosMock.calcularEnvio(codigoPostal)).thenReturn(null);

        Map<String, Object> response = carritoController.calcularEnvioAjax(codigoPostal, httpSessionMock);

        assertThat(response.get("success"), equalTo(false));
        assertThat(response.get("mensaje"), equalTo("Sin cobertura para este código postal"));

        verify(servicioEnviosMock, times(1)).calcularEnvio(codigoPostal);
    }

    @Test
    public void cuandoElServicioLanzaUnaExepcionDevuelveError() {
        String codigoPostal = "1704";

        when(servicioEnviosMock.calcularEnvio(codigoPostal)).thenThrow(new RuntimeException("Error"));

        Map<String, Object> response = carritoController.calcularEnvioAjax(codigoPostal, httpSessionMock);

        assertThat(response.get("success"), equalTo(false));
        assertThat(response.get("mensaje"), equalTo("Error al calcular envío"));

        verify(servicioEnviosMock, times(1)).calcularEnvio(codigoPostal);
    }

    // agregarProductoAlCarrito
    @Test
    public void cuandoAgregoUnProductoAlCarritoObtengoUnMensajeDeExitoYSeActualizaLaCantidadEnELMismo() {
        Integer cantidadAAgregar = 1;
        Long componenteId = 1L;
        Integer cantidadTotalEsperada = 2;

        Componente componenteMock = mock(Componente.class);
        when(componenteMock.getId()).thenReturn(componenteId);

        when(httpSessionMock.getAttribute("carritoSesion")).thenReturn(carritoSesion);

        when(servicioProductoCarritoImplMock.verificarStock(componenteId)).thenReturn(true);
        when(servicioProductoCarritoImplMock.calcularCantidadTotalDeProductos()).thenReturn(cantidadTotalEsperada);

        Map<String, Object> response = carritoController.agregarProductoAlCarrito(componenteMock.getId(), 1, httpSessionMock);

        assertThat(response.get("success"), equalTo(true));
        assertThat(response.get("mensaje"), equalTo("Producto agregado al carrito!"));
        assertThat(response.get("cantidadEnCarrito"), equalTo(cantidadTotalEsperada));

        verify(servicioProductoCarritoImplMock).verificarStock(componenteId);
        verify(servicioProductoCarritoImplMock).agregarProducto(componenteMock.getId(), cantidadAAgregar);
        verify(servicioProductoCarritoImplMock).calcularCantidadTotalDeProductos();
    }

    @Test
    public void cuandoAgregoUnProductoAlCarritoMasDeUnaVezObtengoUnMensajeDeExitoCuantasVecesLoAgregueYSeActualizaLaCantidadEnELMismo() {
        Integer cantidadAAgregar = 1;
        Long componenteId = productoMock1.getId();
        Integer cantidadFinal = 2;

        when(productoMock1.getCantidad()).thenReturn(cantidadFinal);

        when(httpSessionMock.getAttribute("carritoSesion")).thenReturn(carritoSesion);

        Componente componenteMock = mock(Componente.class);

        when(servicioProductoCarritoImplMock.buscarPorId(componenteId)).thenReturn(productoMock1);
        when(servicioProductoCarritoImplMock.verificarStock(componenteId)).thenReturn(true);
        when(servicioProductoCarritoImplMock.calcularCantidadTotalDeProductos()).thenReturn(cantidadFinal);

        Map<String, Object> response = carritoController.agregarProductoAlCarrito(componenteMock.getId(), 1, httpSessionMock);

        assertThat(response.get("success"), equalTo(true));
        assertThat(response.get("mensaje"), equalTo("Producto actualizado! Ahora tiene " + cantidadFinal + " unidades en el carrito."));
        assertThat(response.get("cantidadEnCarrito"), equalTo(cantidadFinal));

        verify(servicioProductoCarritoImplMock).verificarStock(componenteId);
        verify(servicioProductoCarritoImplMock).buscarPorId(componenteId);
        verify(servicioProductoCarritoImplMock).agregarProducto(componenteMock.getId(), cantidadAAgregar);
        verify(servicioProductoCarritoImplMock).calcularCantidadTotalDeProductos();
    }

    @Test
    public void cuandoAgregoUnProductoAlCarritoQueNoTieneStockObtengoUnMensajeDeError() {
        Integer cantidadAAgregar = 1;
        Long componenteId = 1L;
        Integer cantidadTotalEsperada = 1;

        Componente componenteMock = mock(Componente.class);
        when(componenteMock.getId()).thenReturn(componenteId);

        when(httpSessionMock.getAttribute("carritoSesion")).thenReturn(carritoSesion);


        when(servicioProductoCarritoImplMock.verificarStock(componenteId)).thenReturn(false);
        when(servicioProductoCarritoImplMock.calcularCantidadTotalDeProductos()).thenReturn(cantidadTotalEsperada);

        Map<String, Object> response = carritoController.agregarProductoAlCarrito(componenteMock.getId(), cantidadAAgregar, httpSessionMock);

        assertThat(response.get("success"), equalTo(false));
        assertThat(response.get("mensaje"), equalTo("Stock insuficiente"));
        assertThat(response.get("cantidadEnCarrito"), equalTo(cantidadTotalEsperada));

        verify(servicioProductoCarritoImplMock).verificarStock(componenteId);
        verify(servicioProductoCarritoImplMock).calcularCantidadTotalDeProductos();
    }

    @Test
    public void cuandoAgregoUnProductoYOcurreAlgoInesperadoObtengoUnMensajeDeError() {
        Integer cantidadAAgregar = 1;
        Long componenteId = 1L;

        Componente componenteMock = mock(Componente.class);
        when(componenteMock.getId()).thenReturn(componenteId);

        when(servicioProductoCarritoImplMock.verificarStock(componenteId)).thenReturn(true);

        doThrow(new RuntimeException("Error simulado"))
                .when(servicioProductoCarritoImplMock)
                .agregarProducto(componenteId, cantidadAAgregar);

        Map<String, Object> response = carritoController.agregarProductoAlCarrito(componenteMock.getId(), 1,  httpSessionMock);

        assertThat(response.get("success"), equalTo(false));
        assertThat(response.get("mensaje"), equalTo("Error al agregar producto al carrito!"));
        assertThat(response.get("cantidadEnCarrito"), equalTo(0));

        verify(servicioProductoCarritoImplMock).verificarStock(componenteId);
    }
}
