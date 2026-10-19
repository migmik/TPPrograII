<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="referrer" content="no-referrer">
    <c:url var="urlConfirmar" value="/reservas/${reserva.codigo}/pago/confirmar"><c:param name="token" value="${tokenPago}"/></c:url>
    <meta http-equiv="refresh" content="4;url=${urlConfirmar}">
    <title>Procesando pago - Tije Travel</title>
    <link rel="stylesheet" href="<c:url value='/css/base.css'/>">
</head>
<body>
    <%@ include file="../fragmentos/navegacion.jspf" %>
    <main class="payment-panel">
        <h1>Procesando tu pago</h1>
        <p>Reserva #<c:out value="${reserva.codigo}" />. Importe: <strong>$<c:out value="${reserva.total}" /></strong></p>
        <p>Simulamos el pago. En 4 segundos verás el detalle de la reserva pagada.</p>
        <div class="payment-progress" aria-hidden="true"></div>
        <p>No ingreses datos de tarjeta: esta operación es solo una simulación para el TP.</p>
        <p><a href="${urlConfirmar}">Continuar si la página no avanza sola</a></p>
    </main>
</body>
</html>
