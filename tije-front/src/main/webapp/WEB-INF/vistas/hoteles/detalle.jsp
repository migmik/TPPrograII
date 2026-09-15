<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Detalle del hotel - Tije Travel</title>
    <link rel="stylesheet" href="<c:url value='/css/base.css'/>">
</head>
<body>
    <%@ include file="../fragmentos/navegacion.jspf" %>
    <main>
        <h1><c:out value="${hotel.nombre}"/></h1>
        <dl>
            <dt>Ciudad</dt><dd><c:out value="${hotel.ciudad}"/></dd>
            <dt>Dirección</dt><dd><c:out value="${hotel.direccion}"/></dd>
            <dt>Teléfono</dt><dd><c:out value="${hotel.telefono}"/></dd>
            <dt>Capacidad total</dt><dd><c:out value="${hotel.capacidadTotal}"/> plazas</dd>
        </dl>
        <h2>Consultar disponibilidad</h2>
        <c:url var="urlConsulta" value="/hoteles/${hotel.codigo}/disponibilidad"/>
        <form:form method="get" action="${urlConsulta}" modelAttribute="consulta" htmlEscape="true">
            <p>
                <form:label path="fechaLlegada">Fecha de llegada</form:label>
                <form:input path="fechaLlegada" type="date" required="required"/>
                <form:errors path="fechaLlegada" cssClass="error"/>
            </p>
            <p>
                <form:label path="fechaPartida">Fecha de partida</form:label>
                <form:input path="fechaPartida" type="date" required="required"/>
                <form:errors path="fechaPartida" cssClass="error"/>
            </p>
            <button type="submit">Consultar</button>
        </form:form>
        <c:if test="${not empty disponibilidad}">
            <section aria-label="Resultado de disponibilidad">
                <h2>Resultado</h2>
                <p>Del <c:out value="${disponibilidad.fechaLlegada}"/>
                   al <c:out value="${disponibilidad.fechaPartida}"/>.</p>
                <c:choose>
                    <c:when test="${disponibilidad.plazasDisponibles == 0}">
                        <p>No hay plazas disponibles para esas fechas.</p>
                    </c:when>
                    <c:otherwise>
                        <p>Plazas disponibles: <strong><c:out value="${disponibilidad.plazasDisponibles}"/></strong></p>
                    </c:otherwise>
                </c:choose>
                <p>Esta consulta no realiza una reserva.</p>
            </section>
        </c:if>
        <p><a href="<c:url value='/hoteles'/>">Volver al listado</a></p>
    </main>
</body>
</html>
