<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Sucursales - Tije Travel</title>
            <link rel="stylesheet" href="<c:url value='/css/base.css'/>">
        </head>

        <body>
            <%@ include file="../fragmentos/navegacion.jspf" %>
                <main>
                    <h1>Sucursales</h1>
                    <form class="filter-form" method="get" action="<c:url value='/sucursales'/>">
                        <div class="filter-field"><label for="ciudad">Ciudad</label>
                        <select id="ciudad" name="ciudad"><option value="">Todas</option><c:forEach items="${ciudades}" var="opcion"><option value="<c:out value='${opcion}'/>" ${ciudadBusqueda == opcion ? 'selected' : ''}><c:out value="${opcion}"/></option></c:forEach></select></div>
                        <button type="submit">Filtrar</button>
                    </form>
                    <c:if test="${usuarioActual.rol == 'ADMINISTRADOR'}">
                        <p><a href="<c:url value='/sucursales/nuevo'/>">Agregar sucursal</a></p>
                    </c:if>
                    <c:if test="${not empty mensajeExito}">
                        <p role="status">
                            <c:out value="${mensajeExito}" />
                        </p>
                    </c:if>
                    <c:choose>
                        <c:when test="${empty sucursales}">
                            <p>Todavía no hay sucursales registradas.</p>
                        </c:when>
                        <c:otherwise>
                            <table>
                                <caption>Sucursales registradas</caption>
                                <thead>
                                    <tr>
                                        <th scope="col">Código</th>
                                        <th scope="col">Dirección</th>
                                        <th scope="col">Ciudad</th>
                                        <th scope="col">Teléfono</th>
                                        <c:if test="${usuarioActual.rol == 'ADMINISTRADOR'}">
                                            <th scope="col">Administrar</th>
                                        </c:if>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach items="${sucursales}" var="sucursal">
                                        <tr>
                                            <td>
                                                <c:out value="${sucursal.codigo}" />
                                            </td>
                                            <td>
                                                <c:out value="${sucursal.direccion}" />
                                            </td>
                                            <td><c:out value="${sucursal.ciudad}" /></td>
                                            <td><c:out value="${sucursal.telefono}" /></td>
                                            <c:if test="${usuarioActual.rol == 'ADMINISTRADOR'}">
                                                <td>
                                                    <a
                                                        href="<c:url value='/sucursales/${sucursal.codigo}/editar'/>">Editar</a>
                                                    <a
                                                        href="<c:url value='/sucursales/${sucursal.codigo}/eliminar'/>">Eliminar</a>
                                                </td>
                                            </c:if>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </c:otherwise>
                    </c:choose>
                </main>
        </body>

        </html>
