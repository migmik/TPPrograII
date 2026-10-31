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
                        <h1>${empty actual ? 'Crear reserva' : 'Editar reserva'}</h1>
                        <p>La llegada debe coincidir con la fecha del vuelo y el hotel debe estar en su ciudad de
                            destino. La partida debe ser posterior.</p>
                        <p>La disponibilidad se confirma al guardar. Podés consultarla desde <a
                                href="<c:url value='/hoteles'/>">Hoteles</a> y <a
                                href="<c:url value='/vuelos'/>">Vuelos</a>.</p>
                        <c:if test="${not empty actual}">
                            <p>Se conserva la sucursal de contratación original:
                                <c:out value="${actual.codigoSucursalContratacion}" />.
                            </p>
                        </c:if>
                        <c:if test="${not empty errorOperacion}">
                            <p class="error" role="alert">
                                <c:out value="${errorOperacion}" />
                            </p>
                        </c:if>
                        <c:choose>
                            <c:when test="${empty actual}">
                                <c:url var="urlGuardar" value="/reservas" />
                            </c:when>
                            <c:otherwise>
                                <c:url var="urlGuardar" value="/reservas/${actual.codigo}/editar" />
                            </c:otherwise>
                        </c:choose>
                        <c:choose>
                            <c:when test="${empty reserva.numeroVuelo or empty reserva.codigoHotel}">
                                <section class="card">
                                    <h2>Primero elegí tu viaje</h2>
                                    <p>Buscá una fecha y elegí una combinación de vuelo y hotel. Después vas a poder indicar quién viaja y confirmar la reserva.</p>
                                    <a href="<c:url value='/'/>">Buscar vuelos y hoteles</a>
                                </section>
                            </c:when>
                            <c:otherwise>
                        <section class="card" aria-label="Viaje seleccionado">
                            <h2>Tu viaje</h2>
                            <p>Vuelo <strong><c:out value="${reserva.numeroVuelo}" /></strong>
                                <c:if test="${not empty vueloSeleccionado}">:
                                    <c:out value="${vueloSeleccionado.origen}" /> a
                                    <c:out value="${vueloSeleccionado.destino}" />,
                                    <c:out value="${vueloSeleccionado.fechaYHoraFormateada}" />
                                </c:if>
                            </p>
                            <p>Hotel <strong><c:out value="${hotelSeleccionado.nombre}" default="seleccionado" /></strong>
                                <c:if test="${not empty hotelSeleccionado}">en <c:out value="${hotelSeleccionado.ciudad}" /></c:if>
                            </p>
                            <c:if test="${empty actual}"><a href="<c:url value='/'/>">Cambiar viaje</a></c:if>
                        </section>
                        <c:if test="${not usuarioActual.cliente and empty actual}">
                            <form method="get" action="<c:url value='/reservas/nueva'/>">
                                <input type="hidden" name="numeroVuelo" value="<c:out value='${reserva.numeroVuelo}'/>">
                                <input type="hidden" name="codigoHotel" value="<c:out value='${reserva.codigoHotel}'/>">
                                <input type="hidden" name="fechaLlegada" value="<c:out value='${reserva.fechaLlegada}'/>">
                                <input type="hidden" name="fechaPartida" value="<c:out value='${reserva.fechaPartida}'/>">
                                <label for="dniTurista">Buscar pasajero por DNI</label>
                                <input id="dniTurista" name="dniTurista" inputmode="numeric" pattern="[0-9]{7,8}"
                                    value="<c:out value='${dniTuristaBusqueda}'/>">
                                <button type="submit">Buscar pasajero</button>
                            </form>
                        </c:if>
                        <c:if test="${empty turistas}">
                            <p>Elegí un pasajero para continuar. Si sos vendedor o administrador, buscalo por DNI.</p>
                        </c:if>
                        <form:form method="post" action="${urlGuardar}" modelAttribute="reserva" htmlEscape="true">
                            <form:hidden path="seleccionBuscador" />
                            <p>
                                <form:label path="codigoTurista">Turista</form:label>
                                <form:select path="codigoTurista" required="required">
                                    <form:option value="" label="Seleccioná un turista" />
                                    <c:forEach items="${turistas}" var="turista">
                                        <form:option value="${turista.codigo}">
                                            <c:out value="${turista.codigo}" /> -
                                            <c:out value="${turista.nombre}" />
                                            <c:out value="${turista.apellido}" /> - DNI:
                                            <c:out value="${turista.dni}" default="pendiente" />
                                        </form:option>
                                    </c:forEach>
                                </form:select>
                                <form:errors path="codigoTurista" cssClass="error" />
                            </p>
                            <p>
                                <form:hidden path="numeroVuelo" />
                                <form:errors path="numeroVuelo" cssClass="error" />
                            </p>
                            <p>
                                <form:hidden path="codigoHotel" />
                                <form:errors path="codigoHotel" cssClass="error" />
                            </p>
                            <p>
                                <form:label path="claseVuelo">Clase de vuelo</form:label>
                                <form:select path="claseVuelo" required="required">
                                    <form:option value="" label="Seleccioná una clase" />
                                    <form:option value="TURISTA" label="Turista" />
                                    <form:option value="PRIMERA" label="Primera" />
                                </form:select>
                                <form:errors path="claseVuelo" cssClass="error" />
                            </p>
                            <p>
                                <form:label path="tipoHospedaje">Hospedaje</form:label>
                                <form:select path="tipoHospedaje" required="required">
                                    <form:option value="" label="Seleccioná un hospedaje" />
                                    <form:option value="MEDIA_PENSION" label="Media pensión" />
                                    <form:option value="PENSION_COMPLETA" label="Pensión completa" />
                                </form:select>
                                <form:errors path="tipoHospedaje" cssClass="error" />
                            </p>
                            <p>
                                <form:label path="fechaLlegada">Fecha de llegada</form:label>
                                <form:input path="fechaLlegada" type="date" required="required" />
                                <form:errors path="fechaLlegada" cssClass="error" />
                            </p>
                            <p>
                                <form:label path="fechaPartida">Fecha de partida</form:label>
                                <form:input path="fechaPartida" type="date" required="required" />
                                <form:errors path="fechaPartida" cssClass="error" />
                            </p>
                            <p>Primera clase suma 10% al vuelo. Pensión completa suma 5% al hotel.
                                El total exacto se muestra antes del pago simulado.</p>
                            <button type="submit">${empty actual ? 'Continuar al pago' : 'Guardar reserva'}</button>
                        </form:form>
                            </c:otherwise>
                        </c:choose>
                        <p><a href="<c:url value='/reservas'/>">Cancelar y volver al listado</a></p>
                    </main>
            </body>

            </html>
