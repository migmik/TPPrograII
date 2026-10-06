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
                        <h1>Detalle del turista</h1>
                        <dl>
            <dt>DNI</dt><dd><c:out value="${turista.dni}" default="DNI pendiente"/></dd>
                            <dt>Código</dt>
                            <dd>
                                <c:out value="${turista.codigo}" default="No corresponde" />
                            </dd>
                            <dt>Nombre</dt>
                            <dd>
                                <c:out value="${turista.nombre}" default="No corresponde" />
                            </dd>
                            <dt>Apellido</dt>
                            <dd>
                                <c:out value="${turista.apellido}" default="No corresponde" />
                            </dd>
                            <dt>Dirección</dt>
                            <dd>
                                <c:out value="${turista.direccion}" default="No corresponde" />
                            </dd>
                            <dt>Email</dt>
                            <dd>
                                <c:out value="${turista.email}" default="No corresponde" />
                            </dd>
                            <dt>Teléfono fijo</dt>
                            <dd>
                                <c:out value="${turista.telefonoFijo}" default="No corresponde" />
                            </dd>
                            <dt>Teléfono celular</dt>
                            <dd>
                                <c:out value="${turista.telefonoCelular}" default="No corresponde" />
                            </dd>
                            <dt>Código de sucursal</dt>
                            <dd>
                                <c:out value="${turista.codigoSucursal}" default="No corresponde" />
                            </dd>
                            <dt>Código de titular (solo familiares)</dt>
                            <dd>
                                <c:out value="${turista.codigoTitular}" default="No corresponde" />
                            </dd>
                        </dl>
                        <c:if test="${puedeGestionar}">
                            <p><a href="<c:url value='/turistas/${turista.codigo}/editar'/>">Editar turista</a></p>
                        </c:if>
                        <p><a href="<c:url value='/turistas'/>">Volver al listado</a></p>
                    </main>
            </body>

            </html>