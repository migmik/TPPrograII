<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
            <!DOCTYPE html>
            <html lang="es">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>Tije Travel</title>
                <link rel="stylesheet" href="<c:url value='/css/base.css'/>">
            </head>

            <body class="pagina-inicio">
                <%@ include file="fragmentos/navegacion.jspf" %>
                    <main class="home-main">
                        <section class="search-hero" aria-labelledby="tituloInicio">
                            <div class="search-hero__intro">
                                <p class="search-hero__eyebrow">Tu viaje empieza acá</p>
                                <h1 id="tituloInicio">Encontrá vuelos y alojamiento para tu próxima escapada</h1>
                                <p>Elegí las ciudades y las fechas. Te mostramos las opciones con plazas disponibles.
                                </p>
                            </div>
                            <c:url var="urlBuscar" value="/buscar" />
                            <form:form method="get" action="${urlBuscar}" modelAttribute="busqueda"
                                cssClass="search-panel" htmlEscape="true">
                                <h2>Buscá tu viaje</h2>
                                <div class="search-grid">
                                    <div class="search-field">
                                        <form:label path="origen">Salgo de</form:label>
                                        <form:select path="origen" required="required">
                                            <form:option value="" label="Elegí una ciudad" />
                                            <form:options items="${ciudades}" />
                                        </form:select>
                                        <form:errors path="origen" cssClass="error" />
                                    </div>
                                    <div class="search-field">
                                        <form:label path="destino">Voy a</form:label>
                                        <form:select path="destino" required="required">
                                            <form:option value="" label="Elegí una ciudad" />
                                            <form:options items="${ciudades}" />
                                        </form:select>
                                        <form:errors path="destino" cssClass="error" />
                                    </div>
                                    <div class="search-field">
                                        <form:label path="fechaLlegada">Llegada</form:label>
                                        <form:input path="fechaLlegada" type="date" required="required" />
                                        <form:errors path="fechaLlegada" cssClass="error" />
                                    </div>
                                    <div class="search-field">
                                        <form:label path="fechaPartida">Partida del hotel</form:label>
                                        <form:input path="fechaPartida" type="date" required="required" />
                                        <form:errors path="fechaPartida" cssClass="error" />
                                    </div>
                                    <div class="search-field">
                                        <form:label path="personas">Personas</form:label>
                                        <form:input path="personas" type="number" min="1" max="20"
                                            required="required" />
                                        <form:errors path="personas" cssClass="error" />
                                    </div>
                                    <div class="search-field search-field--button">
                                        <button type="submit">Buscar opciones</button>
                                    </div>
                                </div>
                                <p class="search-panel__note">La llegada es el día del vuelo de ida. Esta búsqueda
                                    consulta disponibilidad; no realiza una reserva.</p>
                            </form:form>
                        </section>

                        <c:if test="${busquedaRealizada}">
                            <section class="search-results" aria-labelledby="tituloResultados">
                                <h2 id="tituloResultados">Opciones para tu viaje</h2>
                                <p>De <strong>
                                        <c:out value="${busqueda.origen}" />
                                    </strong> a <strong>
                                        <c:out value="${busqueda.destino}" />
                                    </strong>, para
                                    <c:out value="${busqueda.personas}" /> persona(s).
                                </p>
                                <div class="search-results__columns">
                                    <div class="search-results__panel">
                                        <h3>Vuelos de ida</h3>
                                        <c:choose>
                                            <c:when test="${empty vuelosEncontrados}">
                                                <p>No hay vuelos con plazas suficientes para esa fecha.</p>
                                                <c:if test="${not empty vuelosSugeridos}">
                                                    <div class="search-results__suggestions">
                                                        <h4>Otras fechas para este destino</h4>
                                                        <p>Estos vuelos salen en otros d&iacute;as. Si eleg&iacute;s uno,
                                                            volv&eacute; a buscar con esa fecha para consultar el
                                                            alojamiento.</p>
                                                        <ul class="search-results__list">
                                                            <c:forEach items="${vuelosSugeridos}" var="vuelo">
                                                                <li><strong>Vuelo
                                                                        <c:out value="${vuelo.numero}" />
                                                                    </strong>
                                                                    <span>
                                                                        <c:out value="${vuelo.fechaYHoraFormateada}" />
                                                                    </span>
                                                                    <a href="<c:url value='/vuelos/${vuelo.numero}'/>">Ver
                                                                        vuelo</a>
                                                                </li>
                                                            </c:forEach>
                                                        </ul>
                                                    </div>
                                                </c:if>
                                            </c:when>
                                            <c:otherwise>
                                                <ul class="search-results__list">
                                                    <c:forEach items="${vuelosEncontrados}" var="vuelo">
                                                        <li><strong>Vuelo
                                                                <c:out value="${vuelo.numero}" />
                                                            </strong>
                                                            <span>
                                                                <c:out value="${vuelo.fechaYHoraFormateada}" />
                                                            </span>
                                                            <a href="<c:url value='/vuelos/${vuelo.numero}'/>">Ver
                                                                vuelo</a>
                                                        </li>
                                                    </c:forEach>
                                                </ul>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="search-results__panel">
                                        <h3>Alojamientos</h3>
                                        <c:choose>
                                            <c:when test="${empty hotelesEncontrados}">
                                                <p>No hay hoteles con plazas suficientes para esas fechas.</p>
                                            </c:when>
                                            <c:otherwise>
                                                <ul class="search-results__list">
                                                    <c:forEach items="${hotelesEncontrados}" var="hotel">
                                                        <li><strong>
                                                                <c:out value="${hotel.nombre}" />
                                                            </strong>
                                                            <span>
                                                                <c:out value="${hotel.direccion}" />
                                                            </span>
                                                            <a href="<c:url value='/hoteles/${hotel.codigo}'/>">Ver
                                                                hotel</a>
                                                        </li>
                                                    </c:forEach>
                                                </ul>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                                <c:if test="${not empty vuelosEncontrados and not empty hotelesEncontrados}">
                                    <div class="search-results__booking">
                                        <h3>Reservar vuelo y hotel</h3>
                                        <p>Eleg&iacute; una combinaci&oacute;n. Cada reserva corresponde a una persona; las plazas se confirman al guardarla.</p>
                                        <c:choose>
                                        <c:when test="${empty usuarioActual}">
                                            <p><a href="<c:url value='/login'/>">Inici&aacute; sesi&oacute;n para reservar</a></p>
                                        </c:when>
                                        <c:otherwise>
                                            <ul class="search-results__list">
                                                <c:forEach items="${vuelosEncontrados}" var="vuelo">
                                                    <c:forEach items="${hotelesEncontrados}" var="hotel">
                                                        <c:url var="urlReserva" value="/reservas/nueva">
                                                            <c:param name="numeroVuelo" value="${vuelo.numero}"/>
                                                            <c:param name="codigoHotel" value="${hotel.codigo}"/>
                                                            <c:param name="fechaLlegada" value="${busqueda.fechaLlegada}"/>
                                                            <c:param name="fechaPartida" value="${busqueda.fechaPartida}"/>
                                                        </c:url>
                                                        <li><strong>Vuelo <c:out value="${vuelo.numero}"/> + <c:out value="${hotel.nombre}"/></strong>
                                                            <a class="search-results__reserve-link" href="${urlReserva}">Reservar</a></li>
                                                    </c:forEach>
                                                </c:forEach>
                                            </ul>
                                        </c:otherwise>
                                        </c:choose>
                                    </div>
                                </c:if>
                            </section>
                        </c:if>

                        <div class="cards">
                            <section class="card">
                                <h2>Vuelos</h2>
                                <p>Consultá destinos, horarios y plazas disponibles.</p>
                                <a href="<c:url value='/vuelos'/>">Explorar vuelos</a>
                            </section>
                            <section class="card">
                                <h2>Hoteles</h2>
                                <p>Elegí fechas y conocé la disponibilidad de cada hotel.</p>
                                <a href="<c:url value='/hoteles'/>">Explorar hoteles</a>
                            </section>
                            <section class="card">
                                <h2>Sucursales</h2>
                                <p>Encontrá las sucursales registradas de Tije Travel.</p>
                                <a href="<c:url value='/sucursales'/>">Ver sucursales</a>
                            </section>
                        </div>
                    </main>
            </body>

            </html>
