// Evitar múltiples inicializaciones
if (!window.eventosCarritoAsignados) {
    window.eventosCarritoAsignados = true;

    document.addEventListener("DOMContentLoaded", function () {
        if (typeof inicializarContadorCarrito === 'function') {
            inicializarContadorCarrito();
        }
        actualizarResumenCarrito();
        asignarEventosVistaCarrito();
    });
}

window.agregarAlCarrito = function (componenteId, cantidad = 1) {
    return fetch(`/agregarAlCarrito/${componenteId}/${cantidad}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                mostrarMensaje(data.mensaje, 'success', componenteId);
            } else {
                mostrarMensaje(data.mensaje, 'error', componenteId);
            }

            if (data.cantidadEnCarrito !== undefined) {
                actualizarContadorCarrito(data.cantidadEnCarrito);
            }
            actualizarResumenCarrito();

            return data;
        })
        .catch(error => {
            mostrarMensaje("Error al conectar con el servidor", 'error', componenteId);
            throw error;
        });
}

window.actualizarResumenCarrito = function () {
    fetch('/fragments/fragments')
        .then(res => res.text())
        .then(html => {
            const parser = new DOMParser();
            const doc = parser.parseFromString(html, 'text/html');
            const nuevoResumen = doc.querySelector('#resumenCarrito');
            const resumenActual = document.getElementById('resumenCarrito');

            if (resumenActual && nuevoResumen) {
                resumenActual.innerHTML = nuevoResumen.innerHTML;
                asignarEventosDelResumenCarrito();
            }
        })
        .catch(err => console.error("Error al recargar resumen del carrito", err));
}

window.mostrarMensaje = function (mensaje, tipo, productId) {
    const mensajeNotificacion = document.getElementById(`mensajeNotificacion-${productId}`);

    if (mensajeNotificacion) {
        mensajeNotificacion.textContent = mensaje;
        mensajeNotificacion.className = `alert alert-${tipo === 'success' ? 'success' : 'danger'} mt-2`;
        mensajeNotificacion.style.display = 'block';

        setTimeout(() => {
            mensajeNotificacion.style.display = 'none';
        }, 3000);
    } else {
        alert(mensaje);
    }
}

window.actualizarContadorCarrito = function (nuevaCantidad) {
    const contadorCarrito = document.getElementById("contadorCarrito");
    if (contadorCarrito) {
        contadorCarrito.textContent = nuevaCantidad;
    }
}

function asignarEventosVistaCarrito() {
    document.querySelectorAll(".btnSumarCantidad").forEach(element => {
        if (!element.dataset.eventoAsignado) {
            element.dataset.eventoAsignado = 'true';
            element.addEventListener("click", function (event) {
                event.preventDefault();

                const btn = event.target;
                const fila = btn.closest('tr');

                if (!fila) {
                    console.error("No se pudo encontrar la fila del producto");
                    return;
                }

                const spanCantidad = fila.querySelector(".productoCantidad");
                const precioTotalDelProducto = fila.querySelector(".precioTotalDelProducto");
                const tdConId = fila.querySelector('[data-id]');

                const idProducto = tdConId.dataset.id;

                fetch(`/carritoDeCompras/agregarMasCantidadDeUnProducto/${idProducto}`, {
                    method: 'POST'
                })
                    .then(response => response.json())
                    .then(data => {

                        actualizarResumenCarrito();

                        if (data.cantidad !== undefined && spanCantidad) {
                            spanCantidad.textContent = data.cantidad;
                        }

                        if (data.precioTotalDelProducto !== undefined && precioTotalDelProducto) {
                            precioTotalDelProducto.textContent = `$${data.precioTotalDelProducto}`;
                        }

                        if (data.valorTotal !== undefined) {
                            document.querySelectorAll(".valorTotalDelCarrito").forEach(el => {
                                el.textContent = `$${data.valorTotal}`;
                            });
                        }

                        if (data.cantidadEnCarrito !== undefined) {
                            actualizarContadorCarrito(data.cantidadEnCarrito);
                        }
                    })
                    .catch(error => {
                        console.error("Error:", error);
                    });
            });
        }
    });

    document.querySelectorAll(".btnRestarCantidad").forEach(element => {
        if (!element.dataset.eventoAsignado) {
            element.dataset.eventoAsignado = 'true';
            element.addEventListener("click", function (event) {
                event.preventDefault();

                const btn = event.target;
                const fila = btn.closest('tr');

                if (!fila) {
                    console.error("No se pudo encontrar la fila del producto");
                    return;
                }

                const spanCantidad = fila.querySelector(".productoCantidad");
                const precioTotalDelProducto = fila.querySelector(".precioTotalDelProducto");
                const tdConId = fila.querySelector('[data-id]');

                if (!tdConId) {
                    console.error("No se pudo encontrar el data-id del producto");
                    return;
                }

                const idProducto = tdConId.dataset.id;

                fetch(`/carritoDeCompras/restarCantidadDeUnProducto/${idProducto}`, {
                    method: 'POST'
                })
                    .then(response => response.json())
                    .then(data => {

                        if (data.eliminado) {
                            fila.remove();
                        } else {
                            if (data.cantidad !== undefined && spanCantidad) {
                                spanCantidad.textContent = data.cantidad;
                            }
                            if (data.precioTotalDelProducto !== undefined && precioTotalDelProducto) {
                                precioTotalDelProducto.textContent = `$${data.precioTotalDelProducto}`;
                            }
                        }

                        if (data.valorTotal !== undefined) {
                            document.querySelectorAll(".valorTotalDelCarrito").forEach(el => {
                                el.textContent = `$${data.valorTotal}`;
                            });
                        }

                        if (data.cantidadEnCarrito !== undefined) {
                            actualizarContadorCarrito(data.cantidadEnCarrito);
                        }
                    })
                    .catch(error => {
                        console.error("Error:", error);
                    });
            });
        }
    });

    document.querySelectorAll(".btnEliminarProducto").forEach(element => {
        if (!element.dataset.eventoAsignado) {
            element.dataset.eventoAsignado = 'true';
            element.addEventListener('click', function (event) {
                event.preventDefault();

                const btn = event.target;
                const idProductoAEliminar = btn.dataset.id;
                const fila = btn.closest('tr');

                if (!idProductoAEliminar) {
                    console.error("No se pudo encontrar el ID del producto a eliminar");
                    return;
                }

                fetch(`/carritoDeCompras/eliminarProducto/${idProductoAEliminar}`, {
                    method: 'POST'
                })
                    .then(response => response.json())
                    .then(data => {

                        if (data.eliminado && fila) {
                            fila.remove();
                        }

                        if (data.valorTotal !== undefined) {
                            document.querySelectorAll(".valorTotalDelCarrito").forEach(el => {
                                el.textContent = `$${data.valorTotal}`;
                            });
                        }

                        if (data.cantidadEnCarrito !== undefined) {
                            actualizarContadorCarrito(data.cantidadEnCarrito);
                        }
                    })
                    .catch(error => {
                        console.error("Error:", error);
                    });
            });
        }
    });
}

window.asignarEventosDelResumenCarrito = function () {
    document.querySelectorAll('#resumenCarrito .btnRestarCantidad').forEach(btn => {
        if (!btn.dataset.eventoAsignado) {
            btn.dataset.eventoAsignado = 'true';
            btn.addEventListener('click', (event) => {
                event.preventDefault();

                const fila = btn.closest('tr');
                const tdConId = fila ? fila.querySelector('[data-id]') : null;
                const id = tdConId ? tdConId.dataset.id : null;

                fetch(`/carritoDeCompras/restarCantidadDeUnProducto/${id}`, {method: 'POST'})
                    .then(res => res.json())
                    .then(data => {
                        actualizarResumenCarrito();
                        if (data.cantidadEnCarrito !== undefined) {
                            actualizarContadorCarrito(data.cantidadEnCarrito);
                        }
                    })
                    .catch(error => {
                        console.error("Error:", error);
                    });
            });
        }
    });

    document.querySelectorAll('#resumenCarrito .btnSumarCantidad').forEach(btn => {
        if (!btn.dataset.eventoAsignado) {
            btn.dataset.eventoAsignado = 'true';
            btn.addEventListener('click', (event) => {
                event.preventDefault();

                const fila = btn.closest('tr');
                const tdConId = fila ? fila.querySelector('[data-id]') : null;
                const id = tdConId ? tdConId.dataset.id : null;

                fetch(`/carritoDeCompras/agregarMasCantidadDeUnProducto/${id}`, {method: 'POST'})
                    .then(res => res.json())
                    .then(data => {
                        actualizarResumenCarrito();
                        if (data.cantidadEnCarrito !== undefined) {
                            actualizarContadorCarrito(data.cantidadEnCarrito);
                        }
                    })
                    .catch(error => {
                        console.error("Error:", error);
                    });
            });
        }
    });

    document.querySelectorAll('#resumenCarrito .btnEliminarProducto').forEach(btn => {
        if (!btn.dataset.eventoAsignado) {
            btn.dataset.eventoAsignado = 'true';
            btn.addEventListener('click', (event) => {
                event.preventDefault();

                const id = btn.dataset.id;

                fetch(`/carritoDeCompras/eliminarProducto/${id}`, {method: 'POST'})
                    .then(res => res.json())
                    .then(data => {
                        actualizarResumenCarrito();
                        if (data.cantidadEnCarrito !== undefined) {
                            actualizarContadorCarrito(data.cantidadEnCarrito);
                        }
                    })
                    .catch(error => {
                        console.error("Error:", error);
                    });
            });
        }
    });
}

// Resto del código para abrir/cerrar el resumen
document.addEventListener('DOMContentLoaded', function() {
    const abrirBtn = document.getElementById('abrirResumenCarrito');
    if (abrirBtn) {
        abrirBtn.addEventListener('click', () => {
            document.getElementById('resumenCarrito').classList.add('abierto');
        });
    }
});

document.addEventListener('click', (e) => {
    const resumenCarrito = document.getElementById('resumenCarrito');
    const abrirBtn = document.getElementById('abrirResumenCarrito');

    if (e.target.id === 'cerrarResumenCarrito') {
        resumenCarrito.classList.remove('abierto');
        return;
    }

    if (resumenCarrito && resumenCarrito.classList.contains('abierto')) {
        if (!resumenCarrito.contains(e.target) && !abrirBtn.contains(e.target)) {
            resumenCarrito.classList.remove('abierto');
        }
    }
});