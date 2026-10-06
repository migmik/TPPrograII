<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
            <!DOCTYPE html>
            <html lang="es">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>
                    <c:out value="${titulo}" /> - Tije Travel
                </title>
                <link rel="stylesheet" href="<c:url value='/css/base.css'/>">
            </head>

            <body>
                <%@ include file="../fragmentos/navegacion.jspf" %>
                    <main>
                        <h1>
                            <c:out value="${titulo}" />
                        </h1>
                        <c:if test="${not empty errorGestion}">
                            <p role="alert" class="error">
                                <c:out value="${errorGestion}" />
                            </p>
                        </c:if>
                        <c:choose>
                            <c:when test="${empty codigo}">
                                <c:url var="urlGuardar" value="/sucursales" />
                            </c:when>
                            <c:otherwise>
                                <c:url var="urlGuardar" value="/sucursales/${codigo}/editar" />
                            </c:otherwise>
                        </c:choose>
                        <form:form method="post" action="${urlGuardar}" modelAttribute="sucursal" htmlEscape="true">
                            <p>
                                <form:label path="direccion">Dirección</form:label>
                                <form:input path="direccion" required="required" maxlength="255" />
                                <form:errors path="direccion" cssClass="error" />
                            </p>
                            <p>
                                <form:label path="telefono">Teléfono</form:label>
                                <form:input path="telefono" type="tel" required="required" maxlength="255" />
                                <form:errors path="telefono" cssClass="error" />
                            </p>
                            <button type="submit">Guardar sucursal</button>
                        </form:form>
                        <p><a href="<c:url value='/sucursales'/>">Cancelar y volver al listado</a></p>
                    </main>
            </body>

            </html>