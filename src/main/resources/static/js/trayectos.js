// Íconos SVG inline (estilo Lucide) usados en el contenido generado dinámicamente
const ICON_USER = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>';
const ICON_TRUCK = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 3h15v13H1z"></path><path d="M16 8h4l3 3v5h-7V8z"></path><circle cx="5.5" cy="18.5" r="2.5"></circle><circle cx="18.5" cy="18.5" r="2.5"></circle></svg>';
const ICON_PIN = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg>';

// Variables globales
let mapa;
let markers = [];
let polyline;
let trayectosActuales = [];

// Inicializar el mapa cuando la página carga
document.addEventListener('DOMContentLoaded', function () {
    initMapa();
    loadTrayectosIniciales();
});

/**
 * Inicializa el mapa de Leaflet
 */
function initMapa() {
    // Centro inicial aproximado
    const centerCoords = [5.3193, -75.5156];

    mapa = L.map('mapa').setView(centerCoords, 13);

    // Capa base OpenStreetMap
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors',
        maxZoom: 19
    }).addTo(mapa);

    // Escala inferior izquierda
    L.control.scale({
        imperial: true,
        metric: true
    }).addTo(mapa);
}

/**
 * Carga todos los trayectos automáticamente al iniciar la página
 */
function loadTrayectosIniciales() {
    const messageContainer = document.getElementById('messageContainer');

    messageContainer.innerHTML = '<div class="loading">Cargando trayectos...</div>';

    fetch('/api/trayectos/public')
        .then(response => {
            if (!response.ok) {
                throw new Error('No se pudieron cargar los trayectos');
            }

            return response.json();
        })
        .then(data => {
            messageContainer.innerHTML = '';

            displayTrayectos(data);
            displayOnMap(data);
            updateRouteSummary(data);
        })
        .catch(error => {
            console.error('Error:', error);

            messageContainer.innerHTML = '';
            showMessage(`Error: ${error.message}`, 'error');

            clearMap();
            resetRouteSummary();
        });
}

/**
 * Busca los trayectos de una ruta
 */
function searchRoute() {
    const routeCode = document.getElementById('routeCode').value.trim();

    if (!routeCode) {
        showMessage('Por favor ingrese un código de ruta', 'error');
        return;
    }

    const messageContainer = document.getElementById('messageContainer');
    messageContainer.innerHTML = '<div class="loading">Buscando ruta...</div>';

    fetch(`/api/public/trayectos/rutas/${routeCode}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Ruta no encontrada');
            }

            return response.json();
        })
        .then(data => {
            messageContainer.innerHTML = '';

            displayTrayectos(data);
            displayOnMap(data);
            updateRouteSummary(data);
        })
        .catch(error => {
            console.error('Error:', error);

            messageContainer.innerHTML = '';
            showMessage(`Error: ${error.message}`, 'error');

            clearMap();
            resetRouteSummary();
        });
}

/**
 * Muestra la lista de trayectos en el sidebar
 */
function displayTrayectos(trayectos) {
    const list = document.getElementById('trayectosList');

    if (!trayectos || trayectos.length === 0) {
        trayectosActuales = [];
        list.classList.remove('empty-state');
        list.innerHTML = '<div class="error">No se encontraron trayectos</div>';
        return;
    }

    // Ordenar por routeCode y stopOrder
    const trayectosOrdenados = [...trayectos].sort((a, b) => {
        const routeA = a.routeCode || '';
        const routeB = b.routeCode || '';

        const routeCompare = routeA.localeCompare(routeB);

        if (routeCompare !== 0) {
            return routeCompare;
        }

        return Number(a.stopOrder || 0) - Number(b.stopOrder || 0);
    });

    trayectosActuales = trayectosOrdenados;

    list.classList.remove('empty-state');

    list.innerHTML = trayectosOrdenados.map((trayecto, index) => `
        <div class="trayecto-item" onclick="selectTrayecto(${index})">
            <div class="trayecto-main">
                <span class="trayecto-stop-order">
                    ${trayecto.stopOrder === 0 ? 'S' : trayecto.stopOrder}
                </span>

                <div class="trayecto-info">
                    <div class="trayecto-location">
                        ${safeText(trayecto.location)}
                    </div>

                    <div class="trayecto-conductor">
                        <span>
                            ${ICON_USER} ${safeText(trayecto.conductorNombre)} ${safeText(trayecto.conductorApellido)}
                        </span>
                        <span>
                            ${ICON_TRUCK} ${safeText(trayecto.vehiclePlate)}
                        </span>
                    </div>

                    <div class="trayecto-conductor">
                        <span>
                            Ruta: ${safeText(trayecto.routeCode)}
                        </span>
                    </div>
                </div>
            </div>
        </div>
    `).join('');
}

/**
 * Selecciona un trayecto y muestra su información
 */
function selectTrayecto(index) {
    const trayecto = trayectosActuales[index];

    if (!trayecto) return;

    // Marcar item activo
    document.querySelectorAll('.trayecto-item').forEach((item, i) => {
        item.classList.toggle('active', i === index);
    });

    const infoPanel = document.getElementById('infoPanel');
    const infoContent = document.getElementById('infoContent');

    const coordenadas = hasValidCoords(trayecto)
        ? `${Number(trayecto.latitude).toFixed(4)}, ${Number(trayecto.longitude).toFixed(4)}`
        : 'No disponibles';

    const fechaCreacion = trayecto.createdAt
        ? new Date(trayecto.createdAt).toLocaleString('es-CO')
        : 'No disponible';

    infoContent.innerHTML = `
        <div class="details-card">
            <div class="detail-row">
                <span class="detail-label">Código de Ruta</span>
                <span class="detail-value">${safeText(trayecto.routeCode)}</span>
            </div>

            <div class="detail-row">
                <span class="detail-label">Orden de Parada</span>
                <span class="detail-value">${safeText(trayecto.stopOrder)}</span>
            </div>

            <div class="detail-row">
                <span class="detail-label">Ubicación</span>
                <span class="detail-value">${safeText(trayecto.location)}</span>
            </div>

            <div class="detail-row">
                <span class="detail-label">Conductor</span>
                <span class="detail-value">
                    ${safeText(trayecto.conductorNombre)} ${safeText(trayecto.conductorApellido)}
                </span>
            </div>

            <div class="detail-row">
                <span class="detail-label">Vehículo</span>
                <span class="detail-value">${safeText(trayecto.vehiclePlate)}</span>
            </div>

            <div class="detail-row">
                <span class="detail-label">Coordenadas</span>
                <span class="detail-value">${coordenadas}</span>
            </div>

            <div class="detail-row">
                <span class="detail-label">Registrado por</span>
                <span class="detail-value">${safeText(trayecto.registeredByLogin)}</span>
            </div>

            <div class="detail-row">
                <span class="detail-label">Fecha de Creación</span>
                <span class="detail-value">${fechaCreacion}</span>
            </div>
        </div>
    `;

    infoPanel.classList.add('active');

    // Centrar mapa en el trayecto seleccionado
    if (hasValidCoords(trayecto)) {
        mapa.setView(
            [Number(trayecto.latitude), Number(trayecto.longitude)],
            15
        );
    }
}

/**
 * Muestra los trayectos en el mapa
 */
async function displayOnMap(trayectos) {
    clearMap();

    if (!trayectos || trayectos.length === 0) return;

    // Agrupar trayectos por código de ruta
    const trayectosPorRuta = {};

    trayectos.forEach(trayecto => {
        const routeCode = trayecto.routeCode || 'SIN_RUTA';

        if (!trayectosPorRuta[routeCode]) {
            trayectosPorRuta[routeCode] = [];
        }

        trayectosPorRuta[routeCode].push(trayecto);
    });

    const routeCodes = Object.keys(trayectosPorRuta);

    for (let routeIndex = 0; routeIndex < routeCodes.length; routeIndex++) {
        const routeCode = routeCodes[routeIndex];
        const sortedTrayectos = trayectosPorRuta[routeCode]
            .sort((a, b) => Number(a.stopOrder || 0) - Number(b.stopOrder || 0));
        const routeColor = getRouteColor(routeCode, routeIndex);

        const latlngs = [];

        sortedTrayectos.forEach((trayecto, index) => {
            if (!hasValidCoords(trayecto)) return;

            const coords = [
                Number(trayecto.latitude),
                Number(trayecto.longitude)
            ];

            latlngs.push(coords);

            let markerLabel = trayecto.stopOrder === 0 ? 'S' : trayecto.stopOrder;
            let markerClass = 'marker-icon-intermediate';

            if (trayecto.stopOrder === 0) {
                markerClass = 'marker-icon-start';
            } else if (index === sortedTrayectos.length - 1) {
                markerLabel = 'F';
                markerClass = 'marker-icon-end';
            }

            const customIcon = L.divIcon({
                className: 'custom-marker',
                html: `
                    <div class="${markerClass}" style="background: ${routeColor};">
                        ${markerLabel}
                    </div>
                `,
                iconSize: [40, 40],
                iconAnchor: [20, 20],
                popupAnchor: [0, -20]
            });

            const marker = L.marker(coords, { icon: customIcon })
                .addTo(mapa)
                .bindPopup(`
                    <strong>${safeText(trayecto.location)}</strong><br>
                    Ruta: ${safeText(trayecto.routeCode)}<br>
                    Parada: ${safeText(trayecto.stopOrder)}<br>
                    Conductor: ${safeText(trayecto.conductorNombre)} ${safeText(trayecto.conductorApellido)}<br>
                    Vehículo: ${safeText(trayecto.vehiclePlate)}
                `);

            markers.push(marker);
        });

        // Dibujar línea de cada ruta. Primero intenta ruta por calles desde backend.
        if (latlngs.length > 1) {
            try {
                const resp = await fetch(`/api/trayectos/public/routes/${encodeURIComponent(routeCode)}/path`);

                if (resp.ok) {
                    const path = await resp.json();
                    if (Array.isArray(path) && path.length > 1) {
                        const routeLatLngs = path.map(p => [Number(p.latitude), Number(p.longitude)]);
                        console.log(`Ruta ${routeCode}: usando geometria de calles con ${routeLatLngs.length} puntos`);

                        polyline = L.polyline(routeLatLngs, {
                            color: routeColor,
                            weight: 5,
                            opacity: 0.9,
                            lineJoin: 'round'
                        }).addTo(mapa);

                        continue;
                    }
                }
            } catch (error) {
                console.warn(`Ruta ${routeCode}: error obteniendo geometria de calles`, error);
            }

            // Fallback: linea recta si la geometria por calles no esta disponible
            console.warn(`Ruta ${routeCode}: usando fallback de linea recta`);
            polyline = L.polyline(latlngs, {
                color: routeColor,
                weight: 5,
                opacity: 0.9,
                lineJoin: 'round'
            }).addTo(mapa);
        }
    }

    // Ajustar el mapa a todos los marcadores
    if (markers.length > 1) {
        const group = new L.featureGroup(markers);

        mapa.fitBounds(group.getBounds(), {
            padding: [50, 50]
        });
    } else if (markers.length === 1) {
        mapa.setView(markers[0].getLatLng(), 15);
    }
}

/**
 * Devuelve un color estable por código de ruta para diferenciar rutas visualmente.
 */
function getRouteColor(routeCode, routeIndex) {
    const palette = [
        '#4f5ce5', '#10b981', '#f97316', '#8b5cf6',
        '#ef4444', '#14b8a6', '#eab308', '#0ea5e9'
    ];

    const normalizedCode = (routeCode || '').toString();
    const hash = Array.from(normalizedCode)
        .reduce((acc, char) => acc + char.charCodeAt(0), 0);

    return palette[(hash + routeIndex) % palette.length];
}

/**
 * Actualiza el resumen de la ruta
 */
function updateRouteSummary(trayectos) {
    const estado = document.getElementById('valueEstado');
    const conductor = document.getElementById('valueConductor');
    const vehiculo = document.getElementById('valueVehiculo');
    const paradas = document.getElementById('valueParadas');

    if (!trayectos || trayectos.length === 0) {
        resetRouteSummary();
        return;
    }

    const trayectosOrdenados = [...trayectos].sort((a, b) => {
        const routeA = a.routeCode || '';
        const routeB = b.routeCode || '';

        const routeCompare = routeA.localeCompare(routeB);

        if (routeCompare !== 0) {
            return routeCompare;
        }

        return Number(a.stopOrder || 0) - Number(b.stopOrder || 0);
    });

    const primerTrayecto = trayectosOrdenados[0];

    const rutasUnicas = new Set(
        trayectos
            .map(trayecto => trayecto.routeCode)
            .filter(routeCode => routeCode !== null && routeCode !== undefined && routeCode !== '')
    );

    estado.textContent = 'Activa';
    conductor.textContent = `${safeText(primerTrayecto.conductorNombre)} ${safeText(primerTrayecto.conductorApellido)}`;
    vehiculo.textContent = safeText(primerTrayecto.vehiclePlate);

    if (rutasUnicas.size > 1) {
        paradas.textContent = `${trayectos.length} trayectos`;
    } else {
        paradas.textContent = trayectos.length;
    }
}

/**
 * Reinicia el resumen de la ruta
 */
function resetRouteSummary() {
    document.getElementById('valueEstado').textContent = '—';
    document.getElementById('valueConductor').textContent = '—';
    document.getElementById('valueVehiculo').textContent = '—';
    document.getElementById('valueParadas').textContent = '—';
}

/**
 * Limpia el mapa de marcadores y líneas
 */
function clearMap() {
    markers.forEach(marker => mapa.removeLayer(marker));
    markers = [];

    if (polyline) {
        mapa.removeLayer(polyline);
        polyline = null;
    }

    // Eliminar líneas adicionales si existen
    mapa.eachLayer(layer => {
        if (layer instanceof L.Polyline && !(layer instanceof L.Polygon)) {
            mapa.removeLayer(layer);
        }
    });
}

/**
 * Limpia la búsqueda y vuelve a cargar todos los trayectos
 */
function clearSearch() {
    document.getElementById('routeCode').value = '';

    const trayectosList = document.getElementById('trayectosList');

    trayectosList.classList.add('empty-state');

    trayectosList.innerHTML = `
        <div class="empty-icon">${ICON_PIN}</div>

        <h3>Cargando trayectos...</h3>

        <p>
            Se están mostrando nuevamente todos los trayectos disponibles.
        </p>
    `;

    document.getElementById('infoContent').innerHTML = '';
    document.getElementById('infoPanel').classList.remove('active');
    document.getElementById('messageContainer').innerHTML = '';

    trayectosActuales = [];

    resetRouteSummary();
    loadTrayectosIniciales();
}

/**
 * Muestra un mensaje en la interfaz
 */
function showMessage(message, type) {
    const container = document.getElementById('messageContainer');

    const className =
        type === 'error'
            ? 'error'
            : type === 'success'
                ? 'success'
                : 'loading';

    container.innerHTML = `<div class="${className}">${message}</div>`;

    if (type !== 'loading') {
        setTimeout(() => {
            container.innerHTML = '';
        }, 5000);
    }
}

/**
 * Valida si un trayecto tiene coordenadas correctas
 */
function hasValidCoords(trayecto) {
    return (
        trayecto &&
        trayecto.latitude !== null &&
        trayecto.longitude !== null &&
        trayecto.latitude !== undefined &&
        trayecto.longitude !== undefined &&
        !Number.isNaN(Number(trayecto.latitude)) &&
        !Number.isNaN(Number(trayecto.longitude))
    );
}

/**
 * Evita mostrar valores null, undefined o vacíos
 */
function safeText(value) {
    if (value === null || value === undefined || value === '') {
        return 'No disponible';
    }

    return value;
}