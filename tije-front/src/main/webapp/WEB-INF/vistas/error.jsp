<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>No se pudo completar la consulta - Tije Travel</title>
    <link rel="stylesheet" href="<c:url value='/css/base.css'/>">
</head>
<body>
    <%@ include file="fragmentos/navegacion.jspf" %>
    <main>
        <h1>No se pudo completar la consulta</h1>
        <p role="alert"><c:out value="${mensaje}" default="La página solicitada no está disponible."/></p>
        <p><a href="<c:url value='/'/>">Volver al inicio</a></p>
    </main>
</body>
</html>
