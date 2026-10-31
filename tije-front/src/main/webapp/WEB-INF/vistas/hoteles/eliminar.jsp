<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Dar de baja hotel - Tije Travel</title>
            <link rel="stylesheet" href="<c:url value='/css/base.css'/>">
        </head>

        <body>
            <%@ include file="../fragmentos/navegacion.jspf" %>
                <main class="compact-page">
                    <h1>Dar de baja hotel</h1>
                    <p>Hotel: <strong>
                            <c:out value="${hotel.nombre}" />
                        </strong>,
                        <c:out value="${hotel.ciudad}" />.
                    </p>
                    <p>Se puede reactivar después desde los dados de baja. Si el hotel tiene reservas que todavía no terminaron, no se puede dar de baja.</p>
                    <c:if test="${not empty errorGestion}">
                        <p role="alert" class="error">
                            <c:out value="${errorGestion}" />
                        </p>
                    </c:if>
                    <form method="post" action="<c:url value='/hoteles/${hotel.codigo}/eliminar'/>">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                        <button type="submit">Confirmar baja</button>
                    </form>
                    <p><a href="<c:url value='/hoteles'/>">Cancelar y volver al listado</a></p>
                </main>
        </body>

        </html>