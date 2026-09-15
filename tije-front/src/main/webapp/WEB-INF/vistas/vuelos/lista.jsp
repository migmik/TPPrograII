<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Vuelos - Tije Travel</title>
    <link rel="stylesheet" href="<c:url value='/css/base.css'/>">
</head>
<body>
    <%@ include file="../fragmentos/navegacion.jspf" %>
    <main>
        <h1>Vuelos</h1>
        <c:choose>
            <c:when test="${empty vuelos}">
                <p>Todavía no hay vuelos registrados.</p>
            </c:when>
            <c:otherwise>
                <table>
                    <caption>Catálogo de vuelos</caption>
                    <thead>
                        <tr><th scope="col">Número</th><th scope="col">Origen</th>
                            <th scope="col">Destino</th><th scope="col">Fecha y hora</th>
                            <th scope="col">Consulta</th></tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${vuelos}" var="vuelo">
                            <tr>
                                <td><c:out value="${vuelo.numero}"/></td>
                                <td><c:out value="${vuelo.origen}"/></td>
                                <td><c:out value="${vuelo.destino}"/></td>
                                <td><c:out value="${vuelo.fechaYHoraFormateada}"/></td>
                                <td><a href="<c:url value='/vuelos/${vuelo.numero}'/>">Ver detalle y disponibilidad</a></td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </main>
</body>
</html>
