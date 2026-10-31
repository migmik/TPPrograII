<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Usuarios - Tije Travel</title>
            <link rel="stylesheet" href="<c:url value='/css/base.css'/>">
        </head>

        <body>
            <%@ include file="../fragmentos/navegacion.jspf" %>
                <main>
                    <h1>Usuarios</h1>
                    <c:if test="${not empty mensajeExito}">
                        <p role="status">
                            <c:out value="${mensajeExito}" />
                        </p>
                    </c:if>
                    <c:set var="rutaModulo" value="usuarios" />
                    <c:set var="estadoListado" value="activos" />
                    <c:set var="verBajas" value="true" />
                    <c:set var="textoAgregar" value="Agregar usuario" />
                    <c:set var="urlAgregar" value="/usuarios/nuevo" />
                    <%@ include file="../fragmentos/barra-listado.jspf" %>
                    <form class="filter-form" method="get" action="<c:url value='/usuarios'/>">
                        <div class="filter-field"><label for="codigoRol">Rol</label>
                        <select id="codigoRol" name="codigoRol">
                            <option value="" <c:if test="${empty rolBusqueda}">selected</c:if>>Todos</option>
                            <c:forEach items="${roles}" var="rol">
                                <option value="${rol.codigo}" <c:if test="${rolBusqueda == rol.codigo}">selected</c:if>><c:out value="${rol.nombre}" /></option>
                            </c:forEach>
                        </select></div>
                        <div class="filter-field"><label for="codigo">Código de usuario</label>
                        <input id="codigo" name="codigo" type="number" min="1" value="<c:out value='${codigoBusqueda}'/>"></div>
                        <div class="filter-field"><label for="nombre">Nombre de usuario</label>
                        <input id="nombre" name="nombre" type="search" value="<c:out value='${nombreBusqueda}'/>"></div>
                        <div class="filter-field"><label for="dni">DNI</label>
                        <input id="dni" name="dni" inputmode="numeric" pattern="[0-9]{7,8}" minlength="7" maxlength="8" value="<c:out value='${dniBusqueda}'/>"></div>
                        <button type="submit">Filtrar</button>
                        <a href="<c:url value='/usuarios'/>">Ver todos</a>
                    </form>
                    <c:choose>
                        <c:when test="${empty usuarios}">
                            <p>No hay usuarios registrados.</p>
                        </c:when>
                        <c:otherwise>
                            <table>
                                <caption>Cuentas registradas</caption>
                                <thead>
                                    <tr>
                                        <th scope="col">Código</th>
                                        <th scope="col">Usuario</th>
                                        <th scope="col">DNI</th>
                                        <th scope="col">Rol</th>
                                        <th scope="col">Código de turista</th>
                                        <th scope="col">Acciones</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach items="${usuarios}" var="usuario">
                                        <tr>
                                            <td>
                                                <c:out value="${usuario.codigo}" />
                                            </td>
                                            <td>
                                                <c:out value="${usuario.nombreUsuario}" />
                                            </td>
                                            <td>
                                                <c:out value="${usuario.dni}" default="DNI pendiente" />
                                            </td>
                                            <td>
                                                <c:out value="${usuario.rol}" />
                                            </td>
                                            <td>
                                                <c:out value="${usuario.codigoTurista}" default="No corresponde" />
                                            </td>
                                            <td>
                                                <a href="<c:url value='/usuarios/${usuario.codigo}/editar'/>">Editar
                                                    usuario y rol</a>
                                                <c:if test="${usuario.codigo != usuarioActual.codigo}">
                                                    | <a
                                                        href="<c:url value='/usuarios/${usuario.codigo}/eliminar'/>">Dar de baja</a>
                                                </c:if>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                            <c:if test="${totalPaginas > 1}">
                                <form method="get" action="<c:url value='/usuarios'/>">
                                    <input type="hidden" name="codigoRol" value="<c:out value='${rolBusqueda}'/>">
                                    <input type="hidden" name="codigo" value="<c:out value='${codigoBusqueda}'/>">
                                    <input type="hidden" name="nombre" value="<c:out value='${nombreBusqueda}'/>">
                                    <input type="hidden" name="dni" value="<c:out value='${dniBusqueda}'/>">
                                    <c:if test="${hayAnterior}"><button name="pagina" value="${pagina - 1}">Anterior</button></c:if>
                                    <span>Página <c:out value="${pagina + 1}" /> de <c:out value="${totalPaginas}" /></span>
                                    <c:if test="${haySiguiente}"><button name="pagina" value="${pagina + 1}">Siguiente</button></c:if>
                                </form>
                            </c:if>
                        </c:otherwise>
                    </c:choose>
                </main>
        </body>

        </html>
