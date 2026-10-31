<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Dar de baja usuario - Tije Travel</title>
            <link rel="stylesheet" href="<c:url value='/css/base.css'/>">
        </head>

        <body>
            <%@ include file="../fragmentos/navegacion.jspf" %>
                <main class="compact-page">
                    <h1>Dar de baja usuario</h1>
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
                            <p>No podés darte de baja a vos mismo.</p>
                        </c:when>
                        <c:otherwise>
                            <p>La cuenta ya no podrá iniciar sesión. El turista asociado y sus reservas se conservan.</p>
                            <p>Se puede reactivar después desde los usuarios dados de baja.</p>
                            <form method="post" action="<c:url value='/usuarios/${cuenta.codigo}/eliminar'/>">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                <button type="submit">Confirmar baja</button>
                            </form>
                        </c:otherwise>
                    </c:choose>
                    <p><a href="<c:url value='/usuarios'/>">Cancelar y volver al listado</a></p>
                </main>
        </body>

        </html>