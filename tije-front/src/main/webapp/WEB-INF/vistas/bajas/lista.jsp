<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>
                <c:out value="${modulo.titulo}" /> dados de baja - Tije Travel
            </title>
            <link rel="stylesheet" href="<c:url value='/css/base.css'/>">
        </head>

        <body>
            <%@ include file="../fragmentos/navegacion.jspf" %>
                <main>
                    <h1>
                        <c:out value="${modulo.titulo}" /> dados de baja
                    </h1>
                    <nav class="filtro-estado" aria-label="Estado de los registros">
                        <a href="<c:url value='/${modulo.ruta}'/>">Activos</a>
                        <span aria-current="page">Dados de baja</span>
                    </nav>
                    <c:if test="${not empty mensajeExito}">
                        <p role="status">
                            <c:out value="${mensajeExito}" />
                        </p>
                    </c:if>
                    <c:if test="${not empty errorReactivacion}">
                        <p role="alert" class="error">
                            <c:out value="${errorReactivacion}" />
                        </p>
                    </c:if>
                    <c:choose>
                        <c:when test="${empty filas}">
                            <p>No hay registros dados de baja.</p>
                        </c:when>
                        <c:otherwise>
                            <table>
                                <caption>Del más reciente al más antiguo</caption>
                                <thead>
                                    <tr>
                                        <th scope="col">Código</th>
                                        <th scope="col">Registro</th>
                                        <th scope="col">Alta</th>
                                        <th scope="col">Baja</th>
                                        <th scope="col">Acción</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach items="${filas}" var="fila">
                                        <tr>
                                            <td><c:out value="${fila.codigo}" /></td>
                                            <td><c:out value="${fila.descripcion}" /></td>
                                            <td><c:out value="${fila.alta}" /></td>
                                            <td><c:out value="${fila.baja}" /></td>
                                            <td>
                                                <form method="post"
                                                    action="<c:url value='/bajas/${modulo.ruta}/${fila.codigo}/reactivar'/>">
                                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                                    <button type="submit">Reactivar</button>
                                                </form>
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
