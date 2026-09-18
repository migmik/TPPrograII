<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Detalle del vuelo - Tije Travel</title>
    <link rel="stylesheet" href="<c:url value='/css/base.css'/>">
</head>
<body>
    <%@ include file="../fragmentos/navegacion.jspf" %>
    <main>
        <h1>Vuelo <c:out value="${vuelo.numero}"/></h1>
        <dl>
            <dt>Origen</dt><dd><c:out value="${vuelo.origen}"/></dd>
            <dt>Destino</dt><dd><c:out value="${vuelo.destino}"/></dd>
            <dt>Fecha y hora</dt><dd><c:out value="${vuelo.fechaYHoraFormateada}"/></dd>
            <dt>Capacidad total</dt><dd><c:out value="${vuelo.totalPlazas}"/> plazas</dd>
        </dl>
        <h2>Disponibilidad por clase</h2>
        <table>
            <caption>Plazas del vuelo</caption>
            <thead>
                <tr><th scope="col">Clase</th><th scope="col">Capacidad</th><th scope="col">Plazas libres</th></tr>
            </thead>
            <tbody>
                <tr>
                    <th scope="row">Turista</th>
                    <td><c:out value="${vuelo.plazasTurista}"/></td>
                    <td><c:out value="${disponibilidadTurista.plazasDisponibles}"/></td>
                </tr>
                <tr>
                    <th scope="row">Primera</th>
                    <td><c:out value="${vuelo.plazasPrimera}"/></td>
                    <td><c:out value="${disponibilidadPrimera.plazasDisponibles}"/></td>
                </tr>
            </tbody>
        </table>
        <c:if test="${disponibilidadTurista.plazasDisponibles == 0}">
            <p>No hay plazas disponibles en clase turista.</p>
        </c:if>
        <c:if test="${disponibilidadPrimera.plazasDisponibles == 0}">
            <p>No hay plazas disponibles en primera clase.</p>
        </c:if>
        <p>Las plazas libres pueden cambiar si se realizan nuevas reservas.</p>
        <p>Esta consulta no realiza una reserva.</p>
        <p><a href="<c:url value='/vuelos'/>">Volver al listado</a></p>
    </main>
</body>
</html>
