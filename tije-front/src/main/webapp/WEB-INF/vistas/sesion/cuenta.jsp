<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Mi cuenta - Tije Travel</title>
            <link rel="stylesheet" href="<c:url value='/css/base.css'/>">
        </head>

        <body>
            <%@ include file="../fragmentos/navegacion.jspf" %>
                <main class="compact-page">
                    <h1>Mi cuenta</h1>
                    <dl>
                        <dt>Usuario</dt>
                        <dd>
                            <c:out value="${usuarioActual.nombreUsuario}" />
                        </dd>
                        <dt>DNI</dt>
                        <dd>
                            <c:out value="${usuarioActual.dni}" default="DNI pendiente" />
                        </dd>
                        <dt>Rol</dt>
                        <dd>
                            <c:out value="${usuarioActual.rol}" />
                        </dd>
                    </dl>
                    <p>Podés consultar los hoteles y vuelos desde el menú.</p>
                </main>
        </body>

        </html>