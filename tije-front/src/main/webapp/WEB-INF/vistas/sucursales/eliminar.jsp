<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Eliminar sucursal - Tije Travel</title>
    <link rel="stylesheet" href="<c:url value='/css/base.css'/>">
</head>
<body>
    <%@ include file="../fragmentos/navegacion.jspf" %>
    <main>
        <h1>Eliminar sucursal</h1>
        <p>Sucursal <c:out value="${sucursal.codigo}"/>: <strong><c:out value="${sucursal.direccion}"/></strong>.</p>
        <p>Esta acción no se puede deshacer. Una sucursal vinculada a turistas o reservas no puede eliminarse.</p>
        <c:if test="${not empty errorGestion}"><p role="alert" class="error"><c:out value="${errorGestion}"/></p></c:if>
        <form method="post" action="<c:url value='/sucursales/${sucursal.codigo}/eliminar'/>">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <button type="submit">Confirmar eliminación</button>
        </form>
        <p><a href="<c:url value='/sucursales'/>">Cancelar y volver al listado</a></p>
    </main>
</body>
</html>
