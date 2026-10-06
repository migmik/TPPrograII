<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${titulo}"/> - Tije Travel</title>
    <link rel="stylesheet" href="<c:url value='/css/base.css'/>">
</head>
<body>
    <%@ include file="../fragmentos/navegacion.jspf" %>
    <main>
        <h1><c:out value="${titulo}"/></h1>
        <c:if test="${not empty errorGestion}"><p role="alert" class="error"><c:out value="${errorGestion}"/></p></c:if>
        <c:choose>
            <c:when test="${empty numero}"><c:url var="urlGuardar" value="/vuelos"/></c:when>
            <c:otherwise><c:url var="urlGuardar" value="/vuelos/${numero}/editar"/></c:otherwise>
        </c:choose>
        <form:form method="post" action="${urlGuardar}" modelAttribute="vuelo" htmlEscape="true">
            <c:choose>
                <c:when test="${empty numero}">
                    <p><form:label path="numero">Número de vuelo</form:label>
                        <form:input path="numero" type="number" min="1" required="required"/>
                        <form:errors path="numero" cssClass="error"/></p>
                </c:when>
                <c:otherwise>
                    <p>Número de vuelo: <strong><c:out value="${numero}"/></strong>. No se puede cambiar.</p>
                    <form:hidden path="numero"/>
                </c:otherwise>
            </c:choose>
            <p><form:label path="fechaYHora">Fecha y hora</form:label>
                <form:input path="fechaYHora" type="datetime-local" required="required"/>
                <form:errors path="fechaYHora" cssClass="error"/></p>
            <p><form:label path="origen">Origen</form:label>
                <form:input path="origen" required="required" maxlength="255"/>
                <form:errors path="origen" cssClass="error"/></p>
            <p><form:label path="destino">Destino</form:label>
                <form:input path="destino" required="required" maxlength="255"/>
                <form:errors path="destino" cssClass="error"/></p>
            <p><form:label path="totalPlazas">Total de plazas</form:label>
                <form:input path="totalPlazas" type="number" min="1" required="required"/>
                <form:errors path="totalPlazas" cssClass="error"/></p>
            <p><form:label path="plazasTurista">Plazas turista</form:label>
                <form:input path="plazasTurista" type="number" min="0" required="required"/>
                <form:errors path="plazasTurista" cssClass="error"/></p>
            <p><form:label path="plazasPrimera">Plazas primera</form:label>
                <form:input path="plazasPrimera" type="number" min="0" required="required"/>
                <form:errors path="plazasPrimera" cssClass="error"/></p>
            <p>Las plazas de ambas clases no pueden sumar más que el total.</p>
            <button type="submit">Guardar vuelo</button>
        </form:form>
        <p><a href="<c:url value='/vuelos'/>">Cancelar y volver al listado</a></p>
    </main>
</body>
</html>
