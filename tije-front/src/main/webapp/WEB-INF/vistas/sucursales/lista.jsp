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
                    <c:set var="gestionaModulo" value="${not empty usuarioActual and usuarioActual.tienePermiso('ADMINISTRAR_SUCURSALES')}" />
                    <c:set var="rutaModulo" value="sucursales" />
                    <c:set var="estadoListado" value="activos" />
                    <c:set var="verBajas" value="${gestionaModulo}" />
                    <c:set var="textoAgregar" value="${gestionaModulo ? 'Agregar sucursal' : ''}" />
                    <c:set var="urlAgregar" value="/sucursales/nuevo" />
                    <%@ include file="../fragmentos/barra-listado.jspf" %>
                    <form class="filter-form" method="get" action="<c:url value='/sucursales'/>">
                        <div class="filter-field"><label for="ciudad">Ciudad</label>
                        <select id="ciudad" name="ciudad"><option value="">Todas</option><c:forEach items="${ciudades}" var="opcion"><option value="<c:out value='${opcion}'/>" ${ciudadBusqueda == opcion ? 'selected' : ''}><c:out value="${opcion}"/></option></c:forEach></select></div>
                        <button type="submit">Filtrar</button>
                    </form>
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
                                        <c:if test="${not empty usuarioActual and usuarioActual.tienePermiso('ADMINISTRAR_SUCURSALES')}">
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
                                            <c:if test="${not empty usuarioActual and usuarioActual.tienePermiso('ADMINISTRAR_SUCURSALES')}">
                                                <td>
                                                    <a
                                                        href="<c:url value='/sucursales/${sucursal.codigo}/editar'/>">Editar</a>
                                                    <a
                                                        href="<c:url value='/sucursales/${sucursal.codigo}/eliminar'/>">Dar de baja</a>
                                                </td>
                                            </c:if>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                            <%@ include file="../fragmentos/paginacion.jspf" %>
                        </c:otherwise>
                    </c:choose>
                </main>
        </body>

        </html>
