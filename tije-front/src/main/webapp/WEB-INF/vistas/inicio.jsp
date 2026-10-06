<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Tije Travel</title>
    <link rel="stylesheet" href="<c:url value='/css/base.css'/>">
</head>
<body>
    <%@ include file="fragmentos/navegacion.jspf" %>
    <main>
        <section class="hero" aria-labelledby="tituloInicio">
            <h1 id="tituloInicio">Descubrí tu próximo destino</h1>
            <p>Explorá nuestros vuelos y hoteles, y consultá la disponibilidad para tu viaje.</p>
            <a class="button" href="<c:url value='/vuelos'/>">Ver vuelos</a>
        </section>
        <div class="cards">
            <section class="card">
                <h2>Vuelos</h2>
                <p>Consultá destinos, horarios y plazas disponibles.</p>
                <a href="<c:url value='/vuelos'/>">Explorar vuelos</a>
            </section>
            <section class="card">
                <h2>Hoteles</h2>
                <p>Elegí fechas y conocé la disponibilidad de cada hotel.</p>
                <a href="<c:url value='/hoteles'/>">Explorar hoteles</a>
            </section>
            <section class="card">
                <h2>Sucursales</h2>
                <p>Encontrá las sucursales registradas de Tije Travel.</p>
                <a href="<c:url value='/sucursales'/>">Ver sucursales</a>
            </section>
        </div>
    </main>
</body>
</html>
