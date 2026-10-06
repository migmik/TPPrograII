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
        <h1>Detalle de la reserva</h1>
        <%@ include file="resumen.jspf" %>
        <c:if test="${puedeGestionar}"><p><a href="<c:url value='/reservas/${reserva.codigo}/editar'/>">Editar</a> | <a href="<c:url value='/reservas/${reserva.codigo}/eliminar'/>">Eliminar</a></p></c:if>
        <p><a href="<c:url value='/reservas'/>">Volver al listado</a></p>
    </main>
</body>
</html>
