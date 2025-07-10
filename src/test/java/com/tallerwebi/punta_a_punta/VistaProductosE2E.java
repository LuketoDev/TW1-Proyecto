package com.tallerwebi.punta_a_punta;

import com.microsoft.playwright.*;
import com.tallerwebi.punta_a_punta.vistas.*;
import org.junit.jupiter.api.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class VistaProductosE2E {

    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    VistaCoolers vistaCoolers;

    @BeforeAll
    static void abrirNavegador() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(500));
    }

    @AfterAll
    static void cerrarNavegador() {
        playwright.close();
    }

    @BeforeEach
    void crearContextoYPagina() {
        context = browser.newContext();
        Page page = context.newPage();
        vistaCoolers = new VistaCoolers(page);
        vistaCoolers.ir();
    }

    @AfterEach
    void cerrarContexto() {
        context.close();
    }

    @Test
    public void CuandoLeDoyClickAUnMismoItemOnceVecesSeSumaDiezVecesYObtengoUnMensajeDeError() {
        vistaCoolers.darClickEnAgregarPrimerProductoAlCarrito();
        vistaCoolers.darClickEnAgregarPrimerProductoAlCarrito();
        vistaCoolers.darClickEnAgregarPrimerProductoAlCarrito();
        vistaCoolers.darClickEnAgregarPrimerProductoAlCarrito();
        vistaCoolers.darClickEnAgregarPrimerProductoAlCarrito();
        vistaCoolers.darClickEnAgregarPrimerProductoAlCarrito();
        vistaCoolers.darClickEnAgregarPrimerProductoAlCarrito();
        vistaCoolers.darClickEnAgregarPrimerProductoAlCarrito();
        vistaCoolers.darClickEnAgregarPrimerProductoAlCarrito();
        vistaCoolers.darClickEnAgregarPrimerProductoAlCarrito();
        vistaCoolers.darClickEnAgregarPrimerProductoAlCarrito();
        String mensajeError = vistaCoolers.obtenerMensajeDeErrorCooler();

        assertThat(mensajeError, equalTo("Stock insuficiente"));
    }

    @Test
    public void deberiaAgregarUnProductoAlCarrito() {

        vistaCoolers.darClickEnAgregarPrimerProductoAlCarrito();

        String contador = vistaCoolers.obtenerContadorDelCarrito();

        assertThat(Integer.parseInt(contador), greaterThanOrEqualTo(1));
    }
}
