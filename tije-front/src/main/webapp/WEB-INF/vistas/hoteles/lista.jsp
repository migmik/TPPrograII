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
                    <form class="filter-form" method="get" action="<c:url value='/hoteles'/>">
                        <div class="filter-field"><label for="ciudad">Ciudad</label>
                        <select id="ciudad" name="ciudad"><option value="">Todas</option><c:forEach items="${ciudades}" var="opcion"><option value="<c:out value='${opcion}'/>" ${ciudadBusqueda == opcion ? 'selected' : ''}><c:out value="${opcion}"/></option></c:forEach></select></div>
                        <div class="filter-field"><label for="nombre">Nombre</label>
                        <input id="nombre" name="nombre" list="nombres-hoteles" value="<c:out value='${nombreBusqueda}'/>" placeholder="Escribí o elegí un hotel"></div>
                        <datalist id="nombres-hoteles"><c:forEach items="${nombresHoteles}" var="opcion"><option value="<c:out value='${opcion}'/>"></option></c:forEach></datalist>
                        <div class="filter-field"><label for="orden">Ordenar por</label>
                        <select id="orden" name="orden"><option value="codigo">Código</option><option value="precioAsc" ${orden == 'precioAsc' ? 'selected' : ''}>Menor precio</option><option value="precioDesc" ${orden == 'precioDesc' ? 'selected' : ''}>Mayor precio</option></select></div>
                        <button type="submit">Buscar</button>
                    </form>
                    <c:if test="${usuarioActual.rol == 'ADMINISTRADOR'}">
                        <p><a href="<c:url value='/hoteles/nuevo'/>">Agregar hotel</a></p>
                    </c:if>
                    <c:if test="${not empty mensajeExito}">
                        <p role="status">
                            <c:out value="${mensajeExito}" />
                        </p>
                    </c:if>
                    <c:choose>
                        <c:when test="${not busquedaRealizada}">
                            <p>Indicá una ciudad o un nombre para buscar hoteles.</p>
                        </c:when>
                        <c:when test="${empty hoteles}">
                            <p>No encontramos hoteles con esos filtros.</p>
                        </c:when>
                        <c:otherwise>
                            <table>
                                <caption>Catálogo de hoteles</caption>
                                <thead>
                                    <tr>
                                        <th scope="col">Nombre</th>
                                        <th scope="col">Ciudad</th>
                                        <th scope="col">Capacidad total</th>
                                        <th scope="col">Precio por persona y noche</th>
                                        <th scope="col">Consulta</th>
                                        <c:if test="${usuarioActual.rol == 'ADMINISTRADOR'}">
                                            <th scope="col">Administrar</th>
                                        </c:if>
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
                                            <td>$<c:out value="${hotel.precioNochePersona}" /></td>
                                            <td><a href="<c:url value='/hoteles/${hotel.codigo}'/>">Ver detalle y
                                                    disponibilidad</a></td>
                                            <c:if test="${usuarioActual.rol == 'ADMINISTRADOR'}">
                                                <td>
                                                    <a
                                                        href="<c:url value='/hoteles/${hotel.codigo}/editar'/>">Editar</a>
                                                    <a
                                                        href="<c:url value='/hoteles/${hotel.codigo}/eliminar'/>">Eliminar</a>
                                                </td>
                                            </c:if>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                            <form method="get" action="<c:url value='/hoteles'/>">
                                <input type="hidden" name="ciudad" value="<c:out value='${ciudadBusqueda}'/>">
                                <input type="hidden" name="nombre" value="<c:out value='${nombreBusqueda}'/>">
                                <input type="hidden" name="orden" value="<c:out value='${orden}'/>">
                                <c:if test="${hayAnterior}"><button name="pagina" value="${pagina - 1}">Anterior</button></c:if>
                                <span>Página <c:out value="${pagina + 1}" /> de <c:out value="${totalPaginas}" /></span>
                                <c:if test="${haySiguiente}"><button name="pagina" value="${pagina + 1}">Siguiente</button></c:if>
                            </form>
                            <p>La capacidad total es el tamaño del hotel. Las plazas libres dependen de las fechas.</p>
                        </c:otherwise>
                    </c:choose>
                </main>
        </body>

        </html>
