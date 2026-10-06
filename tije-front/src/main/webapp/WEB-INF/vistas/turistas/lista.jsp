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
                    <main>
                        <h1>Turistas</h1>
                        <c:if test="${not empty mensajeExito}">
                            <p role="status">
                                <c:out value="${mensajeExito}" />
                            </p>
                        </c:if>
                        <c:if test="${!puedeGestionar}">
                            <p>Estos son los turistas de tu grupo familiar.</p>
                        </c:if>
                        <c:if test="${puedeGestionar}">
                            <p><a href="<c:url value='/turistas/nuevo'/>">Crear turista</a></p>
                        </c:if>
                        <c:choose>
                            <c:when test="${empty turistas}">
                                <p>No hay turistas para mostrar.</p>
                            </c:when>
                            <c:otherwise>
                                <table>
                                    <caption>Turistas disponibles para tu cuenta</caption>
                                    <thead>
                                        <tr>
                                            <th>Código</th>
                                            <th>DNI</th>
                                            <th>Nombre</th>
                                            <th>Apellido</th>
                                            <th>Tipo</th>
                                            <th>Acciones</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach items="${turistas}" var="turista">
                                            <tr>
                                                <td>
                                                    <c:out value="${turista.codigo}" />
                                                </td>
                                                <td><c:out value="${turista.dni}" default="DNI pendiente" /></td>
                                                <td>
                                                    <c:out value="${turista.nombre}" />
                                                </td>
                                                <td>
                                                    <c:out value="${turista.apellido}" />
                                                </td>
                                                <td>${turista.titular ? 'Titular' : 'Familiar'}</td>
                                                <td>
                                                    <a href="<c:url value='/turistas/${turista.codigo}'/>">Ver
                                                        detalle</a>
                                                    <c:if test="${puedeGestionar}">
                                                        | <a
                                                            href="<c:url value='/turistas/${turista.codigo}/editar'/>">Editar</a>
                                                        | <a
                                                            href="<c:url value='/turistas/${turista.codigo}/eliminar'/>">Eliminar</a>
                                                    </c:if>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </c:otherwise>
                        </c:choose>
                    </main>
            </body>

            </html>
