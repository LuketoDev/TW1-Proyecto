function suma(a, b) {
    return a + b
}

function multiplicar(a, b) {
    return a * b;
}

document.addEventListener("DOMContentLoaded", function () {
    cargarProductos(1);

    const swiperInterval = setInterval(() => {
        if (document.querySelector('.mySwiper .swiper-wrapper')) {
            clearInterval(swiperInterval);
            new Swiper('.mySwiper', {
                slidesPerView: 5,
                spaceBetween: 16,
                navigation: {
                    nextEl: '.swiper-button-next',
                    prevEl: '.swiper-button-prev',
                },
                breakpoints: {
                    0: {slidesPerView: 3},
                    576: {slidesPerView: 4},
                    768: {slidesPerView: 5},
                    992: {slidesPerView: 6},
                }
            });
        }
    }, 100);
});

function cargarProductos(idCategoria) {
    fetch(`/spring/productos/tipoComponente/${idCategoria}`)
        .then(response => response.text())
        .then(html => {
            document.getElementById("productos-container").innerHTML = html;
        });
}

window.agregarAlCarrito = function(componenteId, cantidad = 1) {
    console.log("agregarAlCarrito llamada con:", componenteId, cantidad);

    return fetch("/spring/agregarAlCarrito", {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: `componenteId=${componenteId}&cantidad=${cantidad}`
    })
        .then(response => response.json())
        .then(data => {
            console.log(" Respuesta:", data);
            if(data.success){
                mostrarMensaje(data.mensaje, 'success', componenteId);
                actualizarContadorCarrito(data.cantidadEnCarrito);
            } else {
                mostrarMensaje(data.mensaje, 'error', componenteId);
            }
            return data;
        })
        .catch(error => {
            console.error(' ERROR:', error);
            mostrarMensaje("Error al conectar con el servidor", 'error', componenteId);
            throw error;
        });
}

window.mostrarMensaje = function(mensaje, tipo, productId) {
    console.log("Mostrando mensaje:", mensaje, "para producto:", productId);

    const mensajeNotificacion = document.getElementById(`mensajeNotificacion-${productId}`);

    if(mensajeNotificacion){
        mensajeNotificacion.textContent = mensaje;
        mensajeNotificacion.className = `alert alert-${tipo === 'success' ? 'success' : 'danger'} mt-2`;
        mensajeNotificacion.style.display = 'block';

        setTimeout(() => {
            mensajeNotificacion.style.display = 'none';
        }, 3000);
    } else {
        console.log(" No se encontró elemento mensajeNotificacion-" + productId);
        alert(mensaje);
    }
}

window.actualizarContadorCarrito = function(nuevaCantidad){
    console.log(" Actualizando contador a:", nuevaCantidad);
    const contadorCarrito = document.getElementById("contadorCarrito");
    if(contadorCarrito){
        contadorCarrito.textContent = nuevaCantidad;
    }
}