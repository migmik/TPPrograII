<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Dar de baja rol - Tije Travel</title>
            <link rel="stylesheet" href="<c:url value='/css/base.css'/>">
        </head>

        <body>
            <%@ include file="../fragmentos/navegacion.jspf" %>
                <main class="compact-page">
                    <h1>Dar de baja rol</h1>
                    <p>Rol: <strong>
                            <c:out value="${actual.nombre}" />
                        </strong>.</p>
                    <p>Solo se puede dar de baja un rol que no tenga usuarios activos. Si hace falta, primero
                        cambiales el rol a esos usuarios.</p>
                    <c:if test="${not empty errorGestion}">
                        <p role="alert" class="error">
                            <c:out value="${errorGestion}" />
                        </p>
                    </c:if>
                    <form method="post" action="<c:url value='/roles/${actual.codigo}/eliminar'/>">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                        <button type="submit">Confirmar baja</button>
                    </form>
                    <p><a href="<c:url value='/roles'/>">Cancelar y volver a los roles</a></p>
                </main>
        </body>

        </html>
