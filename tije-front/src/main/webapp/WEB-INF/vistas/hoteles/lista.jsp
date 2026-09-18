<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Hoteles - Tije Travel</title>
            <link rel="stylesheet" href="<c:url value='/css/base.css'/>">
        </head>

        <body>
            <%@ include file="../fragmentos/navegacion.jspf" %>
                <main>
                    <h1>Hoteles</h1>
                    <c:choose>
                        <c:when test="${empty hoteles}">
                            <p>Todavía no hay hoteles registrados.</p>
                        </c:when>
                        <c:otherwise>
                            <table>
                                <caption>Catálogo de hoteles</caption>
                                <thead>
                                    <tr>
                                        <th scope="col">Nombre</th>
                                        <th scope="col">Ciudad</th>
                                        <th scope="col">Capacidad total</th>
                                        <th scope="col">Consulta</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach items="${hoteles}" var="hotel">
                                        <tr>
                                            <td>
                                                <c:out value="${hotel.nombre}" />
                                            </td>
                                            <td>
                                                <c:out value="${hotel.ciudad}" />
                                            </td>
                                            <td>
                                                <c:out value="${hotel.capacidadTotal}" />
                                            </td>
                                            <td><a href="<c:url value='/hoteles/${hotel.codigo}'/>">Ver detalle y
                                                    disponibilidad</a></td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                            <p>La capacidad total es el tamaño del hotel. Las plazas libres dependen de las fechas.</p>
                        </c:otherwise>
                    </c:choose>
                </main>
        </body>

        </html>