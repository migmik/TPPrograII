<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
            <!DOCTYPE html>
            <html lang="es">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>Iniciar sesión - Tije Travel</title>
                <link rel="stylesheet" href="<c:url value='/css/base.css'/>">
            </head>

            <body>
                <%@ include file="../fragmentos/navegacion.jspf" %>
                    <main>
                        <h1>Iniciar sesión</h1>
                        <c:if test="${param.salida != null}">
                            <p>Sesión cerrada.</p>
                        </c:if>
                        <c:if test="${param.salidaLocal != null}">
                            <p>Cerramos tu sesión en esta página. No pudimos confirmar el cierre en el servidor; su
                                sesión vencerá por inactividad.</p>
                        </c:if>
                        <c:if test="${param.sesionVencida != null}">
                            <p>Tu sesión venció. Ingresá nuevamente.</p>
                        </c:if>
                        <c:if test="${not empty errorIngreso}">
                            <p class="error" role="alert">
                                <c:out value="${errorIngreso}" />
                            </p>
                        </c:if>
                        <c:url var="urlIngreso" value="/login" />
                        <form:form method="post" action="${urlIngreso}" modelAttribute="credenciales" htmlEscape="true">
                            <p>
                                <form:label path="nombreUsuario">Usuario</form:label>
                                <form:input path="nombreUsuario" autocomplete="username" required="required"
                                    maxlength="255" />
                                <form:errors path="nombreUsuario" cssClass="error" />
                            </p>
                            <p>
                                <form:label path="contrasenia">Contraseña</form:label>
                                <form:password path="contrasenia" autocomplete="current-password" required="required"
                                    maxlength="72" showPassword="false" />
                                <form:errors path="contrasenia" cssClass="error" />
                            </p>
                            <button type="submit">Ingresar</button>
                        </form:form>
                    </main>
            </body>

            </html>