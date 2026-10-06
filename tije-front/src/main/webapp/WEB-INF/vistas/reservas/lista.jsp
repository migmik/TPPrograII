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
                        <c:if test="${not empty mensajeExito}">
                            <p role="status">
                                <c:out value="${mensajeExito}" />
                            </p>
                        </c:if>
                        <c:if test="${!puedeGestionar}">
                            <p>Estas son las reservas de tu grupo familiar.</p>
                        </c:if>
                        <c:if test="${puedeCrear}">
                            <p><a href="<c:url value='/reservas/nueva'/>">Crear reserva</a></p>
                        </c:if>
                        <form method="get" action="<c:url value='/reservas'/>">
                            <label for="codigoTurista">Código de turista</label>
                            <input id="codigoTurista" name="codigoTurista" type="number" min="1"
                                value="<c:out value='${codigoTuristaBusqueda}'/>" />
                            <label for="numeroVuelo">Número de vuelo</label>
                            <input id="numeroVuelo" name="numeroVuelo" type="number" min="1"
                                value="<c:out value='${numeroVueloBusqueda}'/>" />
                            <label for="codigoHotel">Código de hotel</label>
                            <input id="codigoHotel" name="codigoHotel" type="number" min="1"
                                value="<c:out value='${codigoHotelBusqueda}'/>" />
                            <label for="fechaDesde">Llegada desde</label>
                            <input id="fechaDesde" name="fechaDesde" type="date"
                                value="<c:out value='${fechaDesdeBusqueda}'/>" />
                            <label for="fechaHasta">Llegada hasta</label>
                            <input id="fechaHasta" name="fechaHasta" type="date"
                                value="<c:out value='${fechaHastaBusqueda}'/>" />
                            <button type="submit">Filtrar</button>
                            <a href="<c:url value='/reservas'/>">Ver todas</a>
                        </form>
                        <c:choose>
                            <c:when test="${empty reservas}">
                                <p>No hay reservas para mostrar.</p>
                            </c:when>
                            <c:otherwise>
                                <table>
                                    <caption>Reservas disponibles para tu cuenta</caption>
                                    <thead>
                                        <tr>
                                            <th scope="col">Código</th>
                                            <th scope="col">Turista y DNI</th>
                                            <th scope="col">Vuelo</th>
                                            <th scope="col">Hotel</th>
                                            <th scope="col">Llegada</th>
                                            <th scope="col">Partida</th>
                                            <th scope="col">Acciones</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach items="${reservas}" var="reserva">
                                            <c:set var="turista" value="${turistasPorCodigo[reserva.codigoTurista]}" />
                                            <tr>
                                                <td>
                                                    <c:out value="${reserva.codigo}" />
                                                </td>
                                                <td>
                                                    <c:out value="${reserva.codigoTurista}" /> -
                                                    <c:out value="${turista.nombre}" />
                                                    <c:out value="${turista.apellido}" /> (DNI:
                                                    <c:out value="${turista.dni}" default="pendiente" />)
                                                </td>
                                                <td><a href="<c:url value='/vuelos/${reserva.numeroVuelo}'/>">
                                                        <c:out value="${reserva.numeroVuelo}" />
                                                    </a></td>
                                                <td><a href="<c:url value='/hoteles/${reserva.codigoHotel}'/>">
                                                        <c:out value="${reserva.codigoHotel}" />
                                                    </a></td>
                                                <td>
                                                    <c:out value="${reserva.fechaLlegadaFormateada}" />
                                                </td>
                                                <td>
                                                    <c:out value="${reserva.fechaPartidaFormateada}" />
                                                </td>
                                                <td>
                                                    <a href="<c:url value='/reservas/${reserva.codigo}'/>">Ver
                                                        detalle</a>
                                                    <c:if test="${puedeGestionar}">
                                                        | <a
                                                            href="<c:url value='/reservas/${reserva.codigo}/editar'/>">Editar</a>
                                                        | <a
                                                            href="<c:url value='/reservas/${reserva.codigo}/eliminar'/>">Eliminar</a>
                                                    </c:if>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </c:otherwise>
                        </c:choose>
                        <form method="get" action="<c:url value='/reservas'/>">
                            <input type="hidden" name="codigoTurista" value="<c:out value='${codigoTuristaBusqueda}'/>" />
                            <input type="hidden" name="numeroVuelo" value="<c:out value='${numeroVueloBusqueda}'/>" />
                            <input type="hidden" name="codigoHotel" value="<c:out value='${codigoHotelBusqueda}'/>" />
                            <input type="hidden" name="fechaDesde" value="<c:out value='${fechaDesdeBusqueda}'/>" />
                            <input type="hidden" name="fechaHasta" value="<c:out value='${fechaHastaBusqueda}'/>" />
                            <c:if test="${hayAnterior}">
                                <button type="submit" name="pagina" value="${pagina - 1}">Anterior</button>
                            </c:if>
                            <span>Página <c:out value="${pagina + 1}" /> de <c:out value="${totalPaginas}" /></span>
                            <c:if test="${haySiguiente}">
                                <button type="submit" name="pagina" value="${pagina + 1}">Siguiente</button>
                            </c:if>
                        </form>
                    </main>
            </body>

            </html>
