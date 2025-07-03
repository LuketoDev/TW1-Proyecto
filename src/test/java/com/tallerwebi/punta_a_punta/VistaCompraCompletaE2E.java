package com.tallerwebi.punta_a_punta;

import com.microsoft.playwright.*;
import com.tallerwebi.punta_a_punta.vistas.VistaTarjetaDeCredito;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public class VistaCompraCompletaE2E {
    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    VistaTarjetaDeCredito vistaTarjetaDeCredito;

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
        vistaTarjetaDeCredito = new VistaTarjetaDeCredito(page);
    }

    @AfterEach
    void cerrarContexto() {
        context.close();
    }
}
