<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
            <!DOCTYPE html>
            <html lang="es">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>Turistas - Tije Travel</title>
                <link rel="stylesheet" href="<c:url value='/css/base.css'/>">
            </head>

            <body>
                <%@ include file="../fragmentos/navegacion.jspf" %>
                    <main>
                        <h1>${empty actual ? 'Crear turista' : 'Editar turista'}</h1>
                        <c:if test="${not empty errorOperacion}">
                            <p role="alert" class="error">
                                <c:out value="${errorOperacion}" />
                            </p>
                        </c:if>
                        <c:choose>
                            <c:when test="${empty actual}">
                                <c:url var="urlGuardar" value="/turistas" />
                            </c:when>
                            <c:otherwise>
                                <c:url var="urlGuardar" value="/turistas/${actual.codigo}/editar" />
                            </c:otherwise>
                        </c:choose>
                        <form:form method="post" action="${urlGuardar}" modelAttribute="turista" htmlEscape="true">
                            <p>
                                <form:label path="dni">DNI</form:label>
                                <form:input path="dni" required="required" inputmode="numeric" pattern="[0-9]{7,8}"
                                    minlength="7" maxlength="8" aria-describedby="ayudaDni" />
                                <form:errors path="dni" cssClass="error" />
                            </p>
                            <p id="ayudaDni">Ingresá 7 u 8 dígitos, sin puntos ni espacios. Cada turista tiene su propio
                                DNI.</p>
                            <c:if test="${not empty actual && empty actual.dni}">
                                <p>Este turista se registró sin DNI. Completá el dato real para guardar.</p>
                            </c:if>
                            <p>
                                <form:label path="nombre">Nombre</form:label>
                                <form:input path="nombre" type="text" required="required" maxlength="255" />
                                <form:errors path="nombre" cssClass="error" />
                            </p>
                            <p>
                                <form:label path="apellido">Apellido</form:label>
                                <form:input path="apellido" type="text" required="required" maxlength="255" />
                                <form:errors path="apellido" cssClass="error" />
                            </p>
                            <p>
                                <form:label path="direccion">Dirección</form:label>
                                <form:input path="direccion" type="text" required="required" maxlength="255" />
                                <form:errors path="direccion" cssClass="error" />
                            </p>
                            <p>
                                <form:label path="email">Email</form:label>
                                <form:input path="email" type="email" required="required" maxlength="255" />
                                <form:errors path="email" cssClass="error" />
                            </p>
                            <p>
                                <form:label path="telefonoFijo">Teléfono fijo</form:label>
                                <form:input path="telefonoFijo" type="text" required="required" maxlength="255" />
                                <form:errors path="telefonoFijo" cssClass="error" />
                            </p>
                            <p>
                                <form:label path="telefonoCelular">Teléfono celular</form:label>
                                <form:input path="telefonoCelular" type="text" required="required" maxlength="255" />
                                <form:errors path="telefonoCelular" cssClass="error" />
                            </p>
                            <c:choose>
                                <c:when test="${empty actual}">
                                    <p>Para crear un titular, seleccioná una sucursal y dejá el titular sin seleccionar.
                                        Para crear un familiar, seleccioná su titular y dejá la sucursal sin
                                        seleccionar.</p>
                                    <p>
                                        <form:label path="codigoTitular">Titular del familiar</form:label>
                                        <form:select path="codigoTitular">
                                            <form:option value="" label="Sin seleccionar (nuevo titular)" />
                                            <c:forEach items="${titulares}" var="titular">
                                                <form:option value="${titular.codigo}">
                                                    <c:out value="${titular.codigo}" /> -
                                                    <c:out value="${titular.nombre}" />
                                                    <c:out value="${titular.apellido}" />
                                                    - DNI:
                                                    <c:out value="${titular.dni}" default="pendiente" />
                                                </form:option>
                                            </c:forEach>
                                        </form:select>
                                        <form:errors path="codigoTitular" cssClass="error" />
                                    </p>
                                </c:when>
                                <c:otherwise>
                                    <p>El tipo de turista y su titular no se cambian al editar.</p>
                                </c:otherwise>
                            </c:choose>
                            <c:choose>
                                <c:when test="${not empty actual && !actual.titular}">
                                    <p>Este familiar hereda la sucursal de su titular. Código actual:
                                        <c:out value="${actual.codigoSucursal}" />.
                                    </p>
                                    <form:errors path="codigoSucursal" cssClass="error" />
                                </c:when>
                                <c:otherwise>
                                    <p>
                                        <form:label path="codigoSucursal">Sucursal</form:label>
                                        <form:select path="codigoSucursal">
                                            <form:option value="" label="Sin seleccionar" />
                                            <c:forEach items="${sucursales}" var="sucursal">
                                                <form:option value="${sucursal.codigo}">
                                                    <c:out value="${sucursal.codigo}" /> -
                                                    <c:out value="${sucursal.direccion}" />
                                                </form:option>
                                            </c:forEach>
                                        </form:select>
                                        <form:errors path="codigoSucursal" cssClass="error" />
                                    </p>
                                    <c:if test="${empty sucursales}">
                                        <p>No hay sucursales disponibles. Para crear un titular debe existir una
                                            sucursal.</p>
                                    </c:if>
                                    <c:if test="${not empty actual}">
                                        <p>Cambiar la sucursal del titular también actualiza la de sus familiares.</p>
                                    </c:if>
                                </c:otherwise>
                            </c:choose>
                            <button type="submit">Guardar turista</button>
                        </form:form>
                        <p><a href="<c:url value='/turistas'/>">Cancelar y volver al listado</a></p>
                    </main>
            </body>

            </html>