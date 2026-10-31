<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
            <!DOCTYPE html>
            <html lang="es">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>
                    <c:choose>
                        <c:when test="${empty actual}">Crear rol</c:when>
                        <c:otherwise>Editar rol</c:otherwise>
                    </c:choose> - Tije Travel
                </title>
                <link rel="stylesheet" href="<c:url value='/css/base.css'/>">
            </head>

            <body>
                <%@ include file="../fragmentos/navegacion.jspf" %>
                    <main class="compact-page">
                        <c:choose>
                            <c:when test="${empty actual}">
                                <h1>Crear rol</h1>
                                <c:url var="urlGuardar" value="/roles" />
                            </c:when>
                            <c:otherwise>
                                <h1>Editar rol</h1>
                                <p>Los cambios se aplican enseguida a todos los usuarios con el rol
                                    <strong>
                                        <c:out value="${actual.nombre}" />
                                    </strong>.
                                </p>
                                <c:url var="urlGuardar" value="/roles/${actual.codigo}/editar" />
                            </c:otherwise>
                        </c:choose>
                        <c:if test="${not empty errorGestion}">
                            <p role="alert" class="error">
                                <c:out value="${errorGestion}" />
                            </p>
                        </c:if>
                        <form:form method="post" action="${urlGuardar}" modelAttribute="rol" htmlEscape="true">
                            <p>
                                <form:label path="nombre">Nombre del rol</form:label>
                                <form:input path="nombre" required="required" maxlength="60"
                                    placeholder="Por ejemplo: Hotelero" />
                                <form:errors path="nombre" cssClass="error" />
                            </p>
                            <c:choose>
                                <c:when test="${empty actual}">
                                    <fieldset>
                                        <legend>¿Para quién es el rol?</legend>
                                        <label><form:radiobutton path="tipoUsuario" value="EMPLEADO" /> Empleados
                                            (tienen DNI propio y pueden recibir cualquier permiso)</label>
                                        <label><form:radiobutton path="tipoUsuario" value="CLIENTE" /> Clientes
                                            (solo pueden consultar y reservar para su grupo familiar)</label>
                                        <form:errors path="tipoUsuario" cssClass="error" />
                                    </fieldset>
                                </c:when>
                                <c:otherwise>
                                    <p>Rol para:
                                        <c:choose>
                                            <c:when test="${actual.paraClientes}">clientes</c:when>
                                            <c:otherwise>empleados</c:otherwise>
                                        </c:choose>. Esto no se puede cambiar porque ya hay cuentas de ese tipo.
                                    </p>
                                </c:otherwise>
                            </c:choose>
                            <fieldset class="permisos-casillas">
                                <legend>Permisos</legend>
                                <c:forEach items="${permisos}" var="permiso">
                                    <label>
                                        <form:checkbox path="permisos" value="${permiso.nombre}" />
                                        <c:out value="${permiso.descripcion}" />
                                        <c:if test="${permiso.soloEmpleados}"><small>(solo empleados)</small></c:if>
                                    </label>
                                </c:forEach>
                            </fieldset>
                            <button type="submit">Guardar rol</button>
                        </form:form>
                        <p><a href="<c:url value='/roles'/>">Cancelar y volver a los roles</a></p>
                    </main>
            </body>

            </html>
