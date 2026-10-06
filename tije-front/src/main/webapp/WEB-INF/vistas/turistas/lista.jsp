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
                            <form method="get" action="<c:url value='/turistas'/>">
                                <label for="dni">Buscar por DNI</label>
                                <input type="search" id="dni" name="dni" inputmode="numeric" minlength="7"
                                    maxlength="8" pattern="[0-9]{7,8}" value="<c:out value='${dniBusqueda}'/>"
                                    aria-describedby="dni-ayuda">
                                <span id="dni-ayuda">Ingresá 7 u 8 dígitos.</span>
                                <label for="titular">Tipo</label>
                                <select id="titular" name="titular">
                                    <option value="" <c:if test="${empty titularBusqueda}">selected</c:if>>Todos</option>
                                    <option value="true" <c:if test="${titularBusqueda == 'true'}">selected</c:if>>Titulares</option>
                                    <option value="false" <c:if test="${titularBusqueda == 'false'}">selected</c:if>>Familiares</option>
                                </select>
                                <button type="submit">Buscar</button>
                                <a href="<c:url value='/turistas'/>">Ver todos</a>
                            </form>
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
                                                <td>
                                                    <c:out value="${turista.dni}" default="DNI pendiente" />
                                                </td>
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