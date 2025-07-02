function calcularCantidadDesdeDOM() {
    let cantidadTotal = 0;
    const elementos = document.querySelectorAll('.productoCantidad');

    elementos.forEach(elemento => {
        const cantidad = parseInt(elemento.textContent) || 0;
        cantidadTotal += cantidad;
    });

    return cantidadTotal;
}

function obtenerCantidadCarritoAjax() {
    fetch('/carritoDeCompras/cantidad')
        .then(response => response.json())
        .then(data => {
            actualizarContadorCarrito(data.cantidadEnCarrito);
        })
        .catch(error => {
            const cantidad = calcularCantidadDesdeDOM();
            actualizarContadorCarrito(cantidad);
        });
}

function inicializarContadorCarrito() {
    let cantidadEnCarrito = 0;

    if (typeof window.cantidadEnCarrito !== 'undefined') {
        cantidadEnCarrito = window.cantidadEnCarrito;
    } else if (document.querySelector('.productoCantidad')) {
        cantidadEnCarrito = calcularCantidadDesdeDOM();
    } else {
        obtenerCantidadCarritoAjax();
        return;
    }

    actualizarContadorCarrito(cantidadEnCarrito);
}

document.addEventListener("DOMContentLoaded", function () {
    inicializarContadorCarrito();
});

document.addEventListener("DOMContentLoaded", function () {
    const boton = document.getElementById("btnAplicarDescuento");
    const input = document.getElementById("codigoInput");
    const mensajeParaAlert = document.getElementById("mensajeDescuento");
    const contenidoMensaje = document.getElementById("contenidoMensaje");

    if (boton) {
        boton.addEventListener("click", function () {
            const codigo = input.value.trim();
            if (!codigo) return;

            fetch('/carritoDeCompras/aplicarDescuento', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({codigoInput: codigo})
            })
                .then(response => response.json())
                .then(data => {
                    if (data.mensaje || data.mensajeDescuento) {
                        contenidoMensaje.innerText = data.mensaje || data.mensajeDescuento;
                        mensajeParaAlert.classList.remove("d-none");
                    }

                    if (data.valorTotal) {
                        const valorTotalElement = document.querySelector('.valorTotalDelCarrito');
                        if (valorTotalElement) {
                            if (!valorTotalElement.dataset.valorOriginal) {
                                const valorActualTexto = valorTotalElement.textContent || valorTotalElement.innerHTML;
                                const valorLimpio = parseFloat(valorActualTexto.replace(/[^\d.-]/g, ''));
                                valorTotalElement.dataset.valorOriginal = valorLimpio;
                            }

                            valorTotalElement.dataset.valorConDescuento = data.valorTotal;
                            valorTotalElement.innerHTML = `$${data.valorTotal}`;
                        }
                    }
                })
                .catch(error => {
                    contenidoMensaje.textContent = 'Hubo un error al aplicar el descuento.';
                    mensajeParaAlert.classList.remove("d-none");
                });
        });
    }
});

document.addEventListener("DOMContentLoaded", function () {
    const formularioPago = document.getElementById("formulario-pago");

    if (formularioPago) {
        formularioPago.addEventListener("submit", function (e) {
            e.preventDefault();

            let metodoSeleccionado = document.querySelector('input[name="metodoPago"]:checked');
            if (metodoSeleccionado === null) {
                const errorDiv = document.getElementById("errorMetodoPago");
                errorDiv.innerText = "Debes seleccionar un metodo de pago";
                errorDiv.classList.remove("d-none");
                return false;
            }

            const errorDiv = document.getElementById("errorMetodoPago");
            errorDiv.classList.add("d-none");

            const btnComprar = document.getElementById("btnComprar");
            btnComprar.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Redirigiendo a MercadoPago...';
            btnComprar.disabled = true;

            const params = new URLSearchParams();
            params.append('metodoPago', metodoSeleccionado.value);

            fetch('/carritoDeCompras/formularioPago', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: params
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        crearFormularioMercadoPago(data);
                    } else {
                        btnComprar.innerHTML = 'Finalizar compra';
                        btnComprar.disabled = false;
                        errorDiv.innerText = data.error;
                        errorDiv.classList.remove("d-none");
                    }
                })
                .catch(error => {
                    btnComprar.innerHTML = 'Finalizar compra';
                    btnComprar.disabled = false;
                    errorDiv.innerText = "Error al procesar el pago";
                    errorDiv.classList.remove("d-none");
                });
        });
    }
});

function crearFormularioMercadoPago(data) {
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/checkout/create-payment';
    form.innerHTML += `<input type="hidden" name="metodoDePago" value="mercadoPago">`;

    let costoEnvio = data.costoEnvio;
    if (!costoEnvio && window.datosEnvio) {
        costoEnvio = window.datosEnvio.costo;
    }

    if (costoEnvio && costoEnvio > 0) {
        form.innerHTML += `<input type="hidden" name="costoEnvio" value="${costoEnvio}">`;
    }

    const valorTotalElement = document.querySelector('.valorTotalDelCarrito');
    const valorOriginal = valorTotalElement.dataset.valorOriginal;
    const valorConDescuento = valorTotalElement.dataset.valorConDescuento;

    if (valorOriginal && valorConDescuento && valorOriginal !== valorConDescuento) {
        form.innerHTML += `<input type="hidden" name="totalOriginal" value="${valorOriginal}">`;
        form.innerHTML += `<input type="hidden" name="totalConDescuento" value="${valorConDescuento}">`;
    }

    document.body.appendChild(form);
    form.submit();
}

document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('formulario-envio');

    if (form) {
        form.addEventListener('submit', function (e) {
            e.preventDefault();
            const codigoPostal = document.getElementById('codigoPostal').value.trim();
            if (window.fetch && codigoPostal) {
                calcularConAjax(codigoPostal);
            }
        });
    }
});

function calcularConAjax(codigoPostal) {
    const loading = document.getElementById('loading');
    if (loading) {
        loading.classList.remove('d-none');
    }

    fetch(`/carritoDeCompras/calcularEnvio?codigoPostal=${codigoPostal}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                document.getElementById('alertaSinCobertura').classList.add('d-none');
                document.getElementById('costo').textContent = '$' + data.costo;
                document.getElementById('tiempo').textContent = data.tiempo;
                document.getElementById('zona').textContent = data.destino;

                const valorTotalElement = document.querySelector('.valorTotalDelCarrito');
                const totalTexto = valorTotalElement.textContent || '$0';

                let totalActual;
                if (totalTexto.includes(',') && totalTexto.lastIndexOf(',') > totalTexto.lastIndexOf('.')) {
                    totalActual = parseFloat(totalTexto.replace('$', '').replace(/\./g, '').replace(',', '.'));
                } else {
                    totalActual = parseFloat(totalTexto.replace(/[^\d.-]/g, ''));
                }

                const costoEnvio = parseFloat(data.costo);
                const totalConEnvio = (!isNaN(totalActual) && !isNaN(costoEnvio)) ? totalActual + costoEnvio : 0;

                let parrafoTotal = document.querySelector('.total-con-envio');
                if (!parrafoTotal) {
                    parrafoTotal = document.createElement('p');
                    parrafoTotal.className = 'total-con-envio';
                    document.getElementById('btnComprar').parentElement.insertBefore(parrafoTotal, document.getElementById('btnComprar').parentElement.firstChild);
                }

                const hayDescuento = !document.getElementById('mensajeDescuento').classList.contains('d-none');
                parrafoTotal.innerHTML = `Total con envio${hayDescuento ? ' y descuento' : ''}: <span class="total-envio-valor">$${totalConEnvio}</span>`;
                parrafoTotal.style.display = 'block';

                window.datosEnvio = {
                    costo: data.costo,
                    destino: data.destino,
                    codigoPostal: codigoPostal
                };
            } else {
                document.getElementById('alertaSinCobertura').classList.remove('d-none');
                const parrafoTotal = document.querySelector('.total-con-envio');
                if (parrafoTotal) {
                    parrafoTotal.style.display = 'none';
                }
                window.datosEnvio = null;
            }
        })
        .catch(error => {
            const form = document.getElementById('formulario-envio');
            if (form) {
                form.submit();
            }
        })
        .finally(() => {
            if (loading) {
                loading.classList.add('d-none');
            }
        });
}