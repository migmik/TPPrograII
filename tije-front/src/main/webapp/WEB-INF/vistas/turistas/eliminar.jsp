<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
            <!DOCTYPE html>
            <html lang="es">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>Turistas - Tije Travel</title>
                <link rel="stylesheet" href="<c:url value='/css/base.css'/>">
            </head>

            <body>
                <%@ include file="../fragmentos/navegacion.jspf" %>
                    <main class="compact-page">
                        <h1>Dar de baja turista</h1>
                        <p>DNI:
                            <c:out value="${turista.dni}" default="DNI pendiente" />.
                        </p>
                        <p>Vas a dar de baja a <strong>
                                <c:out value="${turista.nombre}" />
                                <c:out value="${turista.apellido}" />
                            </strong>,
                            código
                            <c:out value="${turista.codigo}" />.
                        </p>
                        <p>Si es titular, también se dan de baja sus familiares, su cuenta de cliente y las reservas
                            sin pagar. Las reservas pagadas se conservan. Se puede reactivar todo junto desde los
                            dados de baja.</p>
                        <c:if test="${not empty errorOperacion}">
                            <p role="alert" class="error">
                                <c:out value="${errorOperacion}" />
                            </p>
                        </c:if>
                        <form method="post" action="<c:url value='/turistas/${turista.codigo}/eliminar'/>">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                            <button type="submit">Confirmar baja</button>
                        </form>
                        <p><a href="<c:url value='/turistas'/>">Cancelar y volver al listado</a></p>
                    </main>
            </body>

            </html>