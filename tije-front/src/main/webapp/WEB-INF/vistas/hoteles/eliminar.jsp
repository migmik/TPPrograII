<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Eliminar hotel - Tije Travel</title>
    <link rel="stylesheet" href="<c:url value='/css/base.css'/>">
</head>
<body>
    <%@ include file="../fragmentos/navegacion.jspf" %>
    <main class="compact-page">
        <h1>Eliminar hotel</h1>
        <p>Hotel: <strong><c:out value="${hotel.nombre}"/></strong>, <c:out value="${hotel.ciudad}"/>.</p>
        <p>Esta acción no se puede deshacer. Si el hotel tiene reservas, no se podrá eliminar.</p>
        <c:if test="${not empty errorGestion}"><p role="alert" class="error"><c:out value="${errorGestion}"/></p></c:if>
        <form method="post" action="<c:url value='/hoteles/${hotel.codigo}/eliminar'/>">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <button type="submit">Confirmar eliminación</button>
        </form>
        <p><a href="<c:url value='/hoteles'/>">Cancelar y volver al listado</a></p>
    </main>
</body>
</html>
