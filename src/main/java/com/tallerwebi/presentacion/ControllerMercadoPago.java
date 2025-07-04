package com.tallerwebi.presentacion;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.common.PhoneRequest;
import com.mercadopago.client.payment.*;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import com.tallerwebi.dominio.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/checkout")
public class ControllerMercadoPago {

    private final ServicioProductoCarritoImpl servicioProductoCarritoImpl;
    private final ServicioPrecios servicioPrecios;

    public ControllerMercadoPago(ServicioProductoCarritoImpl servicioProductoCarritoImpl, ServicioPrecios servicioPrecios) {
        this.servicioProductoCarritoImpl = servicioProductoCarritoImpl;
        this.servicioPrecios = servicioPrecios;
        this.servicioProductoCarritoImpl.init();
        MercadoPagoConfig.setAccessToken("APP_USR-3784718513902185-053117-353d2d4a3d09f6e4ff6bd5750e1b6878-2465514854");
    }

    @PostMapping(value = "/create-payment", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public ModelAndView crearPago(HttpServletResponse response,
                                  @RequestParam(value = "metodoDePago", required = false) String metodoDePago,
                                  @RequestParam(value = "costoEnvio", required = false) Double costoEnvio,
                                  @RequestParam(value = "totalOriginal", required = false) Double totalOriginal,
                                  @RequestParam(value = "totalConDescuento", required = false) Double totalConDescuento)
            throws IOException {

        PagoRequest pagoRequest = new PagoRequest();
        pagoRequest.setMetodoDePago(metodoDePago);
        pagoRequest.setCostoEnvio(costoEnvio);
        pagoRequest.setProductos(servicioProductoCarritoImpl.getProductos());

        List<PreferenceItemRequest> items = new ArrayList<>();
        for (int i = 0; i < pagoRequest.getProductos().size(); i++) {
            Double precioFinal = getPrecioFinal(pagoRequest, i);
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title(pagoRequest.getProductos().get(i).getNombre())
                    .quantity(pagoRequest.getProductos().get(i).getCantidad())
                    .currencyId("ARS")
                    .unitPrice(BigDecimal.valueOf(precioFinal))
                    .build();
            items.add(item);
        }

        if (pagoRequest.getCostoEnvio() != null && pagoRequest.getCostoEnvio() > 0) {
            PreferenceItemRequest itemEnvio = PreferenceItemRequest.builder()
                    .title("Envío")
                    .quantity(1)
                    .currencyId("ARS")
                    .unitPrice(BigDecimal.valueOf(pagoRequest.getCostoEnvio()))
                    .build();
            items.add(itemEnvio);
        }

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success("http://localhost:8080/pagoExitoso")
                .failure("http://localhost:8080/pagoExitoso")
                .pending("http://localhost:8080/pagoExitoso")
                .build();

        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .purpose("wallet_purchase")
                .backUrls(backUrls)
                .externalReference(UUID.randomUUID().toString())
                .build();

        try {
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);
            response.sendRedirect(preference.getSandboxInitPoint());
            return null;
        } catch (MPApiException e) {
            String errorMsg = e.getApiResponse() != null ? e.getApiResponse().getContent() : "Sin contenido de error";
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al crear la preferencia de pago. Detalle: " + errorMsg);
            return null;
        } catch (Exception ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error inesperado al procesar el pago.");
            return null;
        }
    }

    @PostMapping("/api-payment")
    @ResponseBody
    public ResponseEntity<?> procesarPagoApi(@RequestBody Map<String, Object> payload) {
        try {
            PaymentCreateRequest request = PaymentCreateRequest.builder()
                    .transactionAmount(BigDecimal.valueOf(((Number) payload.get("transactionAmount")).floatValue()))
                    .token((String) payload.get("token"))
                    .description((String) payload.get("description"))
                    .installments((Integer) payload.get("installments"))
                    .paymentMethodId((String) payload.get("paymentMethodId"))
                    .issuerId(String.valueOf(Long.valueOf(payload.get("issuerId").toString())))
                    .payer(PaymentPayerRequest.builder()
                            .email((String) ((Map<?, ?>) payload.get("payer")).get("email"))
                            .build())
                    .build();

            PaymentClient client = new PaymentClient();
            Payment payment = client.create(request);

            return ResponseEntity.ok(payment);

        } catch (MPApiException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getApiResponse().getContent()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    private Double getPrecioFinal(PagoRequest pagoRequest, int i) {
        Double precioOriginal = pagoRequest.getProductos().get(i).getPrecio();
        Double factorDescuento = 1.0;

        if (servicioProductoCarritoImpl.valorTotal != null && servicioProductoCarritoImpl.valorTotal > 0) {
            factorDescuento = servicioProductoCarritoImpl.valorTotalConDescuento / servicioProductoCarritoImpl.valorTotal;
        }

        double precioFinal = precioOriginal * factorDescuento;
        if (Double.isNaN(precioFinal) || precioFinal <= 0) {
            precioFinal = precioOriginal;
        }
        return this.servicioPrecios.conversionDolarAPesoDouble(precioFinal);
    }
}
