<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
            <!DOCTYPE html>
            <html lang="es">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>Crear cuenta - Tije Travel</title>
                <link rel="stylesheet" href="<c:url value='/css/base.css'/>">
            </head>

            <body>
                <%@ include file="../fragmentos/navegacion.jspf" %>
                    <main class="compact-page">
                        <h1>Crear cuenta</h1>
                        <c:if test="${not empty errorRegistro}">
                            <p class="error" role="alert"><c:out value="${errorRegistro}" /></p>
                        </c:if>
                        <c:url var="urlRegistro" value="/registro" />
                        <form:form method="post" action="${urlRegistro}" modelAttribute="registro" htmlEscape="true">
                            <p><form:label path="nombreUsuario">Nombre de usuario</form:label>
                                <form:input path="nombreUsuario" autocomplete="username" maxlength="255" required="required" />
                                <form:errors path="nombreUsuario" cssClass="error" /></p>
                            <p><form:label path="contrasenia">Contraseña</form:label>
                                <form:password path="contrasenia" autocomplete="new-password" maxlength="72" required="required" showPassword="false" />
                                <form:errors path="contrasenia" cssClass="error" /></p>
                            <p><form:label path="dni">DNI</form:label>
                                <form:input path="dni" inputmode="numeric" minlength="7" maxlength="8" pattern="[0-9]{7,8}" required="required" />
                                <form:errors path="dni" cssClass="error" /></p>
                            <p><form:label path="nombre">Nombre</form:label>
                                <form:input path="nombre" maxlength="255" required="required" />
                                <form:errors path="nombre" cssClass="error" /></p>
                            <p><form:label path="apellido">Apellido</form:label>
                                <form:input path="apellido" maxlength="255" required="required" />
                                <form:errors path="apellido" cssClass="error" /></p>
                            <p><form:label path="direccion">Dirección</form:label>
                                <form:input path="direccion" maxlength="255" required="required" />
                                <form:errors path="direccion" cssClass="error" /></p>
                            <p><form:label path="email">Email</form:label>
                                <form:input path="email" type="email" maxlength="255" required="required" />
                                <form:errors path="email" cssClass="error" /></p>
                            <p><form:label path="telefonoFijo">Teléfono fijo</form:label>
                                <form:input path="telefonoFijo" maxlength="255" required="required" />
                                <form:errors path="telefonoFijo" cssClass="error" /></p>
                            <p><form:label path="telefonoCelular">Teléfono celular</form:label>
                                <form:input path="telefonoCelular" maxlength="255" required="required" />
                                <form:errors path="telefonoCelular" cssClass="error" /></p>
                            <p><form:label path="codigoSucursal">Sucursal</form:label>
                                <form:select path="codigoSucursal" required="required">
                                    <form:option value="" label="Elegí una sucursal" />
                                    <form:options items="${sucursales}" itemValue="codigo" itemLabel="direccion" />
                                </form:select>
                                <form:errors path="codigoSucursal" cssClass="error" /></p>
                            <button type="submit">Crear cuenta</button>
                        </form:form>
                        <p><a href="<c:url value='/login'/>">Ya tengo una cuenta</a></p>
                    </main>
            </body>

            </html>