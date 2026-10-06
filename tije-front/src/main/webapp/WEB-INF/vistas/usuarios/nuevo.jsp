<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
            <!DOCTYPE html>
            <html lang="es">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>Crear usuario - Tije Travel</title>
                <link rel="stylesheet" href="<c:url value='/css/base.css'/>">
            </head>

            <body>
                <%@ include file="../fragmentos/navegacion.jspf" %>
                    <main>
                        <h1>Crear usuario</h1>
                        <c:if test="${not empty errorCreacion}">
                            <p class="error" role="alert">
                                <c:out value="${errorCreacion}" />
                            </p>
                        </c:if>
                        <c:url var="urlCrear" value="/usuarios" />
                        <form:form method="post" action="${urlCrear}" modelAttribute="usuario" htmlEscape="true">
                            <p>
                                <form:label path="dni">DNI</form:label>
                                <form:input path="dni" inputmode="numeric" pattern="[0-9]{7,8}" minlength="7"
                                    maxlength="8" />
                                <form:errors path="dni" cssClass="error" />
                            </p>
                            <p>Obligatorio para administradores y vendedores. Para clientes dejalo vacío: se usa el DNI
                                del turista elegido, que debe estar completo.</p>

                            <p>
                                <form:label path="nombreUsuario">Nombre de usuario</form:label>
                                <form:input path="nombreUsuario" required="required" maxlength="255"
                                    autocomplete="off" />
                                <form:errors path="nombreUsuario" cssClass="error" />
                            </p>
                            <p>
                                <form:label path="contrasenia">Contraseña</form:label>
                                <form:password path="contrasenia" required="required" maxlength="72"
                                    autocomplete="new-password" showPassword="false" />
                                <form:errors path="contrasenia" cssClass="error" />
                            </p>
                            <p>
                                <form:label path="rol">Rol</form:label>
                                <form:select path="rol" required="required">
                                    <form:option value="" label="Elegí un rol" />
                                    <form:option value="ADMINISTRADOR" label="Administrador" />
                                    <form:option value="VENDEDOR" label="Vendedor" />
                                    <form:option value="CLIENTE" label="Cliente" />
                                </form:select>
                                <form:errors path="rol" cssClass="error" />
                            </p>
                            <p>
                                <form:label path="codigoTurista">Turista asociado</form:label>
                                <form:select path="codigoTurista" aria-describedby="ayudaTurista">
                                    <form:option value="" label="Sin seleccionar" />
                                    <c:forEach items="${turistasDisponibles}" var="turista">
                                        <form:option value="${turista.codigo}">
                                            <c:out value="${turista.codigo}" /> -
                                            <c:out value="${turista.nombre}" />
                                            <c:out value="${turista.apellido}" /> - DNI:
                                            <c:out value="${turista.dni}" default="pendiente" />
                                        </form:option>
                                    </c:forEach>
                                </form:select>
                                <form:errors path="codigoTurista" cssClass="error" />
                            </p>
                            <p id="ayudaTurista">Elegí un turista solamente para el rol Cliente. Para los otros roles
                                dejá «Sin seleccionar».</p>
                            <c:if test="${empty turistasDisponibles}">
                                <p>No hay turistas titulares sin cuenta disponibles. Podés crear administradores y
                                    vendedores.
                                    Para crear un cliente necesitás primero un turista titular sin cuenta.</p>
                            </c:if>
                            <p>Si hay un error, deberás volver a escribir la contraseña.</p>
                            <button type="submit">Crear usuario</button>
                        </form:form>
                        <p><a href="<c:url value='/usuarios'/>">Volver al listado</a></p>
                    </main>
            </body>

            </html>