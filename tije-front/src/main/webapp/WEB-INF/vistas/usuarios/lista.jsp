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
                    <p><a href="<c:url value='/usuarios/nuevo'/>">Crear usuario</a></p>
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
                                                    credenciales</a>
                                                <c:if test="${usuario.codigo != usuarioActual.codigo}">
                                                    | <a
                                                        href="<c:url value='/usuarios/${usuario.codigo}/eliminar'/>">Eliminar</a>
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