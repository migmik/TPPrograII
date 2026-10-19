<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Balance - Tije Travel</title><link rel="stylesheet" href="<c:url value='/css/base.css'/>"></head>
<body>
    <%@ include file="../fragmentos/navegacion.jspf" %>
    <main><h1>Balance de ventas</h1>
        <p>Solo se cuentan las reservas con pago confirmado.</p>
        <dl class="balance-grid">
            <dt>Reservas pagadas</dt><dd><c:out value="${balance.reservasPagadas}" /></dd>
            <dt>Ingresos por vuelos</dt><dd>$<c:out value="${balance.ingresosVuelos}" /></dd>
            <dt>Ingresos por hoteles</dt><dd>$<c:out value="${balance.ingresosHoteles}" /></dd>
            <dt>Total vendido</dt><dd><strong>$<c:out value="${balance.ingresosTotales}" /></strong></dd>
        </dl>
        <p><a href="<c:url value='/reservas'/>">Ver reservas</a></p>
    </main>
</body>
</html>
