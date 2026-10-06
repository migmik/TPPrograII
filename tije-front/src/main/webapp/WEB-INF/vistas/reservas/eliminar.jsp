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
    <main class="compact-page">
        <h1>Eliminar reserva</h1>
        <%@ include file="resumen.jspf" %>
        <p>Esta acción elimina la reserva y libera sus plazas. No elimina al turista, el vuelo ni el hotel. No se puede deshacer.</p>
        <c:if test="${not empty errorOperacion}"><p class="error" role="alert"><c:out value="${errorOperacion}"/></p></c:if>
        <form method="post" action="<c:url value='/reservas/${reserva.codigo}/eliminar'/>">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <button type="submit">Confirmar eliminación</button>
        </form>
        <p><a href="<c:url value='/reservas'/>">Cancelar y volver al listado</a></p>
    </main>
</body>
</html>
