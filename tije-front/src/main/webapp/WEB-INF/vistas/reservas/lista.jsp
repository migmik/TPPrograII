<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Reservas - Tije Travel</title>
    <link rel="stylesheet" href="<c:url value='/css/base.css'/>">
</head>
<body>
    <%@ include file="../fragmentos/navegacion.jspf" %>
    <main>
        <h1>Reservas</h1>
        <c:if test="${not empty mensajeExito}"><p role="status"><c:out value="${mensajeExito}"/></p></c:if>
        <c:if test="${!puedeGestionar}"><p>Estas son las reservas de tu grupo familiar.</p></c:if>
        <c:if test="${puedeGestionar}"><p><a href="<c:url value='/reservas/nueva'/>">Crear reserva</a></p></c:if>
        <c:choose>
            <c:when test="${empty reservas}"><p>No hay reservas para mostrar.</p></c:when>
            <c:otherwise>
                <table>
                    <caption>Reservas disponibles para tu cuenta</caption>
                    <thead><tr><th scope="col">Código</th><th scope="col">Turista y DNI</th><th scope="col">Vuelo</th><th scope="col">Hotel</th><th scope="col">Llegada</th><th scope="col">Partida</th><th scope="col">Acciones</th></tr></thead>
                    <tbody>
                        <c:forEach items="${reservas}" var="reserva">
                            <c:set var="turista" value="${turistasPorCodigo[reserva.codigoTurista]}"/>
                            <tr>
                                <td><c:out value="${reserva.codigo}"/></td>
                                <td><c:out value="${reserva.codigoTurista}"/> - <c:out value="${turista.nombre}"/> <c:out value="${turista.apellido}"/> (DNI: <c:out value="${turista.dni}" default="pendiente"/>)</td>
                                <td><a href="<c:url value='/vuelos/${reserva.numeroVuelo}'/>"><c:out value="${reserva.numeroVuelo}"/></a></td>
                                <td><a href="<c:url value='/hoteles/${reserva.codigoHotel}'/>"><c:out value="${reserva.codigoHotel}"/></a></td>
                                <td><c:out value="${reserva.fechaLlegadaFormateada}"/></td>
                                <td><c:out value="${reserva.fechaPartidaFormateada}"/></td>
                                <td>
                                    <a href="<c:url value='/reservas/${reserva.codigo}'/>">Ver detalle</a>
                                    <c:if test="${puedeGestionar}">
                                        | <a href="<c:url value='/reservas/${reserva.codigo}/editar'/>">Editar</a>
                                        | <a href="<c:url value='/reservas/${reserva.codigo}/eliminar'/>">Eliminar</a>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </main>
</body>
</html>
