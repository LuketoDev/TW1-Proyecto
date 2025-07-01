function suma(a, b) {
    return a + b
}

function multiplicar(a, b) {
    return a * b;
}
// const botonIrARegistrarme = document.getElementById("ir-a-registrarme");
const botonRegistrarme = document.getElementById("btn-registrarme");
const divModal = document.getElementById("modal-div");


asignarHandlersLogin();
function asignarHandlersLogin() {
  const botonIrARegistrarme = document.getElementById("ir-a-registrarme");

    if (botonIrARegistrarme) {
        botonIrARegistrarme.onclick = function () {
            fetch(`/nuevo-usuario`)
                .then(response => response.text())
                .then(html => {
                    document.querySelector("#loginModal .modal-content").innerHTML = html;
                    // asignarHandlersRegistro(); // por si también necesitás volver a enganchar botones de registro
                });
        };
    }
}
fetch("/login")
    .then(response => response.text())
    .then(html => {
        document.querySelector("#loginModal .modal-content").innerHTML = html;
    });

document.addEventListener("submit", function (e) {
    const form = e.target;
    if (!form.matches("form[data-ajax]")) return;

    e.preventDefault();

    const formData = new FormData(form);
    const action = form.getAttribute("action") || form.action;

    fetch(action, {
        method: "POST",
        body: formData,
    })
        .then(async (response) => {
            const text = await response.text();

            if (response.status === 202) {
                // Redirección después del login exitoso
                const url = text.replace("REDIRECT:", "");
                window.location.href = url;
            } else if (response.status === 401) {
                // Error de login: renderizamos el fragmento HTML con errores
                document.querySelector("#loginModal .modal-content").innerHTML = text;
                asignarHandlersLogin(); // Vuelve a enganchar el botón "Registrarme"
            } else {
                console.warn("Respuesta inesperada:", response.status);
                console.log(text);
            }
        })
        .catch(err => {
            console.error("Error en la solicitud de login:", err);
        });
});







// Función para obtener cantidad vía AJAX como respaldo
function obtenerCantidadCarritoAjax() {

    fetch('/carritoDeCompras/cantidad')
        .then(response => response.json())
        .then(data => {
            actualizarContadorCarrito(data.cantidadEnCarrito);
        })
        .catch(error => {
            actualizarContadorCarrito(0);
        });
}

window.addEventListener('DOMContentLoaded', () => {
    asignarHandlersLogin();
    const modal = document.getElementById('modalRegistroExitoso');
    const modalLoginExitoso = document.getElementById('modalLoginExitoso');
    if (modal) {
        const bootstrapModal = new bootstrap.Modal(modal);
        bootstrapModal.show();
    }
    if (modalLoginExitoso) {
        const bootstrapModal = new bootstrap.Modal(modalLoginExitoso);
        bootstrapModal.show();
    }
});

document.addEventListener("DOMContentLoaded", function () {
    // Inicializar contador del carrito
    if (typeof window.cantidadEnCarrito !== 'undefined') {
        actualizarContadorCarrito(window.cantidadEnCarrito);
    } else {
        obtenerCantidadCarritoAjax();
    }

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
    fetch(`/productos/tipoComponente/${idCategoria}`)
        .then(response => response.text())
        .then(html => {
            document.getElementById("productos-container").innerHTML = html;
        })

}
