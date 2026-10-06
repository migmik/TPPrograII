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
                        <h1>Eliminar turista</h1>
        <p>DNI: <c:out value="${turista.dni}" default="DNI pendiente"/>.</p>
                        <p>Vas a eliminar a <strong>
                                <c:out value="${turista.nombre}" />
                                <c:out value="${turista.apellido}" />
                            </strong>,
                            código
                            <c:out value="${turista.codigo}" />.
                        </p>
                        <p>Esta acción no se puede deshacer. No se permite eliminar turistas con reservas,
                            familiares o una cuenta de usuario asociada.</p>
                        <c:if test="${not empty errorOperacion}">
                            <p role="alert" class="error">
                                <c:out value="${errorOperacion}" />
                            </p>
                        </c:if>
                        <form method="post" action="<c:url value='/turistas/${turista.codigo}/eliminar'/>">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                            <button type="submit">Confirmar eliminación</button>
                        </form>
                        <p><a href="<c:url value='/turistas'/>">Cancelar y volver al listado</a></p>
                    </main>
            </body>

            </html>
