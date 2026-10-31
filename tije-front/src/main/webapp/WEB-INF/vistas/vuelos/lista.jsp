<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Vuelos - Tije Travel</title>
            <link rel="stylesheet" href="<c:url value='/css/base.css'/>">
        </head>

        <body>
            <%@ include file="../fragmentos/navegacion.jspf" %>
                <main>
                    <h1>Vuelos</h1>
                    <form class="filter-form" method="get" action="<c:url value='/vuelos'/>">
                        <div class="filter-field"><label for="origen">Origen</label>
                        <select id="origen" name="origen"><option value="">Todos</option><c:forEach items="${origenes}" var="opcion"><option value="<c:out value='${opcion}'/>" ${origen == opcion ? 'selected' : ''}><c:out value="${opcion}"/></option></c:forEach></select></div>
                        <div class="filter-field"><label for="destino">Destino</label>
                        <select id="destino" name="destino"><option value="">Todos</option><c:forEach items="${destinos}" var="opcion"><option value="<c:out value='${opcion}'/>" ${destino == opcion ? 'selected' : ''}><c:out value="${opcion}"/></option></c:forEach></select></div>
                        <div class="filter-field"><label for="fechaDesde">Desde</label>
                        <input id="fechaDesde" name="fechaDesde" type="date" value="<c:out value='${fechaDesde}'/>"></div>
                        <div class="filter-field"><label for="fechaHasta">Hasta</label>
                        <input id="fechaHasta" name="fechaHasta" type="date" value="<c:out value='${fechaHasta}'/>"></div>
                        <div class="filter-field"><label for="orden">Ordenar por</label>
                        <select id="orden" name="orden"><option value="numero">Número</option><option value="precioAsc" ${orden == 'precioAsc' ? 'selected' : ''}>Menor precio</option><option value="precioDesc" ${orden == 'precioDesc' ? 'selected' : ''}>Mayor precio</option></select></div>
                        <button type="submit">Buscar</button>
                    </form>
                    <c:if test="${not empty usuarioActual and usuarioActual.tienePermiso('ADMINISTRAR_VUELOS')}">
                        <p><a href="<c:url value='/vuelos/nuevo'/>">Agregar vuelo</a> | <a href="<c:url value='/bajas/vuelos'/>">Ver dados de baja</a></p>
                    </c:if>
                    <c:if test="${not empty mensajeExito}">
                        <p role="status">
                            <c:out value="${mensajeExito}" />
                        </p>
                    </c:if>
                    <c:choose>
                        <c:when test="${not busquedaRealizada}">
                            <p>Indicá un origen, destino o fecha para buscar vuelos.</p>
                        </c:when>
                        <c:when test="${not empty errorBusqueda}">
                            <p class="error" role="alert"><c:out value="${errorBusqueda}" /></p>
                        </c:when>
                        <c:when test="${empty vuelos}">
                            <p>No encontramos vuelos con esos filtros.</p>
                        </c:when>
                        <c:otherwise>
                            <table>
                                <caption>Catálogo de vuelos</caption>
                                <thead>
                                    <tr>
                                        <th scope="col">Número</th>
                                        <th scope="col">Origen</th>
                                        <th scope="col">Destino</th>
                                        <th scope="col">Fecha y hora</th>
                                        <th scope="col">Precio por persona</th>
                                        <th scope="col">Consulta</th>
                                        <c:if test="${not empty usuarioActual and usuarioActual.tienePermiso('ADMINISTRAR_VUELOS')}">
                                            <th scope="col">Administrar</th>
                                        </c:if>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach items="${vuelos}" var="vuelo">
                                        <tr>
                                            <td>
                                                <c:out value="${vuelo.numero}" />
                                            </td>
                                            <td>
                                                <c:out value="${vuelo.origen}" />
                                            </td>
                                            <td>
                                                <c:out value="${vuelo.destino}" />
                                            </td>
                                            <td>
                                                <c:out value="${vuelo.fechaYHoraFormateada}" />
                                            </td>
                                            <td>$<c:out value="${vuelo.precio}" /></td>
                                            <td><a href="<c:url value='/vuelos/${vuelo.numero}'/>">Ver detalle y
                                                    disponibilidad</a></td>
                                            <c:if test="${not empty usuarioActual and usuarioActual.tienePermiso('ADMINISTRAR_VUELOS')}">
                                                <td>
                                                    <a href="<c:url value='/vuelos/${vuelo.numero}/editar'/>">Editar</a>
                                                    <a
                                                        href="<c:url value='/vuelos/${vuelo.numero}/eliminar'/>">Eliminar</a>
                                                </td>
                                            </c:if>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                            <form method="get" action="<c:url value='/vuelos'/>">
                                <input type="hidden" name="origen" value="<c:out value='${origen}'/>">
                                <input type="hidden" name="destino" value="<c:out value='${destino}'/>">
                                <input type="hidden" name="fechaDesde" value="<c:out value='${fechaDesde}'/>">
                                <input type="hidden" name="fechaHasta" value="<c:out value='${fechaHasta}'/>">
                                <input type="hidden" name="orden" value="<c:out value='${orden}'/>">
                                <c:if test="${hayAnterior}"><button name="pagina" value="${pagina - 1}">Anterior</button></c:if>
                                <span>Página <c:out value="${pagina + 1}" /> de <c:out value="${totalPaginas}" /></span>
                                <c:if test="${haySiguiente}"><button name="pagina" value="${pagina + 1}">Siguiente</button></c:if>
                            </form>
                        </c:otherwise>
                    </c:choose>
                </main>
        </body>

        </html>
