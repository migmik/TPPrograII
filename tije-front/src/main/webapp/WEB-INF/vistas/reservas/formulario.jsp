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
        <p>La llegada debe coincidir con la fecha del vuelo y el hotel debe estar en su ciudad de destino. La partida debe ser posterior.</p>
        <p>La disponibilidad se confirma al guardar. Podés consultarla desde <a href="<c:url value='/hoteles'/>">Hoteles</a> y <a href="<c:url value='/vuelos'/>">Vuelos</a>.</p>
        <c:if test="${not empty actual}"><p>Se conserva la sucursal de contratación original: <c:out value="${actual.codigoSucursalContratacion}"/>.</p></c:if>
        <c:if test="${not empty errorOperacion}"><p class="error" role="alert"><c:out value="${errorOperacion}"/></p></c:if>
        <c:choose>
            <c:when test="${empty actual}"><c:url var="urlGuardar" value="/reservas"/></c:when>
            <c:otherwise><c:url var="urlGuardar" value="/reservas/${actual.codigo}/editar"/></c:otherwise>
        </c:choose>
        <c:if test="${empty turistas || empty hoteles || empty vuelos}"><p>Para guardar una reserva debe haber al menos un turista, un hotel y un vuelo disponibles.</p></c:if>
        <form:form method="post" action="${urlGuardar}" modelAttribute="reserva" htmlEscape="true">
            <p>
                <form:label path="codigoTurista">Turista</form:label>
                <form:select path="codigoTurista" required="required">
                    <form:option value="" label="Seleccioná un turista"/>
                    <c:forEach items="${turistas}" var="turista">
                        <form:option value="${turista.codigo}"><c:out value="${turista.codigo}"/> - <c:out value="${turista.nombre}"/> <c:out value="${turista.apellido}"/> - DNI: <c:out value="${turista.dni}" default="pendiente"/></form:option>
                    </c:forEach>
                </form:select>
                <form:errors path="codigoTurista" cssClass="error"/>
            </p>
            <p>
                <form:label path="numeroVuelo">Vuelo</form:label>
                <form:select path="numeroVuelo" required="required">
                    <form:option value="" label="Seleccioná un vuelo"/>
                    <c:forEach items="${vuelos}" var="vuelo">
                        <form:option value="${vuelo.numero}"><c:out value="${vuelo.numero}"/> - <c:out value="${vuelo.origen}"/> a <c:out value="${vuelo.destino}"/> - <c:out value="${vuelo.fechaYHoraFormateada}"/></form:option>
                    </c:forEach>
                </form:select>
                <form:errors path="numeroVuelo" cssClass="error"/>
            </p>
            <p>
                <form:label path="codigoHotel">Hotel</form:label>
                <form:select path="codigoHotel" required="required">
                    <form:option value="" label="Seleccioná un hotel"/>
                    <c:forEach items="${hoteles}" var="hotel">
                        <form:option value="${hotel.codigo}"><c:out value="${hotel.nombre}"/> - <c:out value="${hotel.ciudad}"/></form:option>
                    </c:forEach>
                </form:select>
                <form:errors path="codigoHotel" cssClass="error"/>
            </p>
            <p>
                <form:label path="claseVuelo">Clase de vuelo</form:label>
                <form:select path="claseVuelo" required="required">
                    <form:option value="" label="Seleccioná una clase"/>
                    <form:option value="TURISTA" label="Turista"/>
                    <form:option value="PRIMERA" label="Primera"/>
                </form:select>
                <form:errors path="claseVuelo" cssClass="error"/>
            </p>
            <p>
                <form:label path="tipoHospedaje">Hospedaje</form:label>
                <form:select path="tipoHospedaje" required="required">
                    <form:option value="" label="Seleccioná un hospedaje"/>
                    <form:option value="MEDIA_PENSION" label="Media pensión"/>
                    <form:option value="PENSION_COMPLETA" label="Pensión completa"/>
                </form:select>
                <form:errors path="tipoHospedaje" cssClass="error"/>
            </p>
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
            <button type="submit">Guardar reserva</button>
        </form:form>
        <p><a href="<c:url value='/reservas'/>">Cancelar y volver al listado</a></p>
    </main>
</body>
</html>
