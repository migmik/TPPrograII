<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Eliminar usuario - Tije Travel</title>
            <link rel="stylesheet" href="<c:url value='/css/base.css'/>">
        </head>

        <body>
            <%@ include file="../fragmentos/navegacion.jspf" %>
                <main class="compact-page">
                    <h1>Eliminar usuario</h1>
                    <p>DNI:
                        <c:out value="${cuenta.dni}" default="DNI pendiente" />.
                    </p>
                    <p>Usuario: <strong>
                            <c:out value="${cuenta.nombreUsuario}" />
                        </strong>.
                        Código:
                        <c:out value="${cuenta.codigo}" />. Rol:
                        <c:out value="${cuenta.rol}" />.
                    </p>
                    <c:if test="${not empty errorEliminacion}">
                        <p role="alert" class="error">
                            <c:out value="${errorEliminacion}" />
                        </p>
                    </c:if>
                    <c:choose>
                        <c:when test="${esMiCuenta}">
                            <p>No podés eliminar tu propia cuenta.</p>
                        </c:when>
                        <c:otherwise>
                            <p>Se eliminará esta cuenta de acceso. El turista asociado y sus reservas se conservan.</p>
                            <p>Esta acción no se puede deshacer. Para recuperar el acceso será necesario crear otra
                                cuenta.</p>
                            <form method="post" action="<c:url value='/usuarios/${cuenta.codigo}/eliminar'/>">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                <button type="submit">Confirmar eliminación</button>
                            </form>
                        </c:otherwise>
                    </c:choose>
                    <p><a href="<c:url value='/usuarios'/>">Cancelar y volver al listado</a></p>
                </main>
        </body>

        </html>