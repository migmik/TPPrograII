<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Roles y permisos - Tije Travel</title>
            <link rel="stylesheet" href="<c:url value='/css/base.css'/>">
        </head>

        <body>
            <%@ include file="../fragmentos/navegacion.jspf" %>
                <main>
                    <h1>Roles y permisos</h1>
                    <c:if test="${not empty mensajeExito}">
                        <p role="status">
                            <c:out value="${mensajeExito}" />
                        </p>
                    </c:if>
                    <p>Cada usuario tiene un rol. Al cambiar los permisos de un rol, cambian para todos los
                        usuarios que lo tienen.</p>
                    <p><a href="<c:url value='/roles/nuevo'/>">Crear rol</a> |
                        <a href="<c:url value='/usuarios'/>">Ver usuarios</a> | <a href="<c:url value='/bajas/roles'/>">Ver dados de baja</a></p>
                    <table>
                        <caption>Roles activos</caption>
                        <thead>
                            <tr>
                                <th scope="col">Rol</th>
                                <th scope="col">Para</th>
                                <th scope="col">Permisos</th>
                                <th scope="col">Acciones</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${roles}" var="rol">
                                <tr>
                                    <td><strong>
                                            <c:out value="${rol.nombre}" />
                                        </strong></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${rol.paraClientes}">Clientes</c:when>
                                            <c:otherwise>Empleados</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <ul class="permisos-lista">
                                            <c:forEach items="${permisos}" var="permiso">
                                                <c:if test="${rol.tienePermiso(permiso.nombre)}">
                                                    <li>
                                                        <c:out value="${permiso.descripcion}" />
                                                    </li>
                                                </c:if>
                                            </c:forEach>
                                        </ul>
                                        <c:if test="${empty rol.permisos}">Sin permisos</c:if>
                                    </td>
                                    <td>
                                        <a href="<c:url value='/roles/${rol.codigo}/editar'/>">Editar permisos</a> |
                                        <a href="<c:url value='/roles/${rol.codigo}/eliminar'/>">Dar de baja</a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </main>
        </body>

        </html>
