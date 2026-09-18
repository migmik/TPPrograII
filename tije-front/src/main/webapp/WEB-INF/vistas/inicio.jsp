<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Tije Travel</title>
            <link rel="stylesheet" href="<c:url value='/css/base.css'/>">
        </head>

        <body>
            <%@ include file="fragmentos/navegacion.jspf" %>
                <main>
                    <h1>Tije Travel</h1>
                    <p>Consultá nuestros hoteles, vuelos y las plazas disponibles para tu viaje.</p>
                    <p><a href="<c:url value='/hoteles'/>">Ver hoteles</a></p>
                    <p><a href="<c:url value='/vuelos'/>">Ver vuelos</a></p>
                </main>
        </body>

        </html>