<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
            <!DOCTYPE html>
            <html lang="es">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>Editar usuario - Tije Travel</title>
                <link rel="stylesheet" href="<c:url value='/css/base.css'/>">
            </head>

            <body>
                <%@ include file="../fragmentos/navegacion.jspf" %>
                    <main>
                        <h1>Editar usuario</h1>
                        <p>Cuenta:
                            <c:out value="${cuenta.codigo}" />. Rol actual:
                            <strong><c:out value="${cuenta.rol}" /></strong>.
                        </p>
                        <c:if test="${cuenta.codigo == usuarioActual.codigo}">
                            <p>Estás editando tu cuenta. Al guardar tendrás que iniciar sesión nuevamente.</p>
                        </c:if>
                        <c:if test="${not empty errorEdicion}">
                            <p role="alert" class="error">
                                <c:out value="${errorEdicion}" />
                            </p>
                        </c:if>
                        <c:url var="urlEditar" value="/usuarios/${cuenta.codigo}/editar" />
                        <form:form method="post" action="${urlEditar}" modelAttribute="usuario" htmlEscape="true">
                            <c:choose>
                                <c:when test="${cuenta.cliente}">
                                    <p>DNI del turista:
                                        <c:out value="${cuenta.dni}" default="DNI pendiente" />. Se modifica desde
                                        Turistas.
                                    </p>
                                </c:when>
                                <c:otherwise>
                                    <p>
                                        <form:label path="dni">DNI</form:label>
                                        <form:input path="dni" inputmode="numeric" pattern="[0-9]{7,8}" minlength="7"
                                            maxlength="8" />
                                        <form:errors path="dni" cssClass="error" />
                                    </p>
                                </c:otherwise>
                            </c:choose>

                            <p>
                                <form:label path="nombreUsuario">Nombre de usuario</form:label>
                                <form:input path="nombreUsuario" required="required" maxlength="255"
                                    autocomplete="off" />
                                <form:errors path="nombreUsuario" cssClass="error" />
                            </p>
                            <p>
                                <form:label path="codigoRol">Rol</form:label>
                                <form:select path="codigoRol" required="required">
                                    <c:forEach items="${roles}" var="rol">
                                        <form:option value="${rol.codigo}">
                                            <c:out value="${rol.nombre}" />
                                        </form:option>
                                    </c:forEach>
                                </form:select>
                                <form:errors path="codigoRol" cssClass="error" />
                            </p>
                            <%@ include file="roles.jspf" %>
                            <p>
                                <form:label path="contrasenia">Nueva contraseña</form:label>
                                <form:password path="contrasenia" maxlength="72"
                                    autocomplete="new-password" showPassword="false" />
                                <form:errors path="contrasenia" cssClass="error" />
                            </p>
                            <p>Dejala vacía para conservar la contraseña actual.</p>
                            <button type="submit">Guardar cambios</button>
                        </form:form>
                        <p><a href="<c:url value='/usuarios'/>">Cancelar y volver al listado</a></p>
                    </main>
            </body>

            </html>
