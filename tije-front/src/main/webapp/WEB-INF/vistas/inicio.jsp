<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
    <%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
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
                        <section class="search-hero${busquedaRealizada ? ' search-hero--compacta' : ''}"
                            aria-label="Buscador de viajes">
                            <c:if test="${not busquedaRealizada}">
                                <div class="search-hero__intro">
                                    <p class="search-hero__eyebrow">Tu viaje empieza acá</p>
                                    <h1 id="tituloInicio">Encontrá vuelos y alojamiento para tu próxima escapada</h1>
                                    <p>Elegí las ciudades y las fechas. Te mostramos las opciones con plazas
                                        disponibles.</p>
                                </div>
                            </c:if>
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
                                        <form:label path="ordenPrecio">Ordenar precios</form:label>
                                        <form:select path="ordenPrecio">
                                            <form:option value="fecha" label="Fecha" />
                                            <form:option value="precioAsc" label="Menor precio" />
                                            <form:option value="precioDesc" label="Mayor precio" />
                                        </form:select>
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
                                <c:if test="${not empty recomendados}">
                                    <div class="search-results__booking">
                                        <h3>Recomendados: vuelo + hotel</h3>
                                        <p>Las combinaciones más baratas para tus fechas. Cada reserva corresponde a
                                            una persona; las plazas se confirman al guardarla.</p>
                                        <ul class="search-results__list combinaciones">
                                            <c:forEach items="${recomendados}" var="combinacion">
                                                <%@ include file="fragmentos/combinacion.jspf" %>
                                            </c:forEach>
                                        </ul>
                                        <c:if test="${not empty otrasCombinaciones}">
                                            <details class="combinaciones-todas">
                                                <summary>Ver todas las combinaciones (<c:out value="${fn:length(otrasCombinaciones)}" /> más)</summary>
                                                <ul class="search-results__list combinaciones">
                                                    <c:forEach items="${otrasCombinaciones}" var="combinacion">
                                                        <%@ include file="fragmentos/combinacion.jspf" %>
                                                    </c:forEach>
                                                </ul>
                                            </details>
                                        </c:if>
                                    </div>
                                </c:if>
                                <div class="search-results__columns">
                                    <div class="search-results__panel">
                                        <h3>Solo vuelos</h3>
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
                                                            <span>$<c:out value="${vuelo.precio}" /> por persona</span>
                                                            <a href="<c:url value='/vuelos/${vuelo.numero}'/>">Ver
                                                                vuelo</a>
                                                        </li>
                                                    </c:forEach>
                                                </ul>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="search-results__panel">
                                        <h3>Solo hoteles</h3>
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
                                                            <span>$<c:out value="${hotel.precioNochePersona}" /> por persona y noche</span>
                                                            <a href="<c:url value='/hoteles/${hotel.codigo}'/>">Ver
                                                                hotel</a>
                                                        </li>
                                                    </c:forEach>
                                                </ul>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </section>
                        </c:if>

                        <c:if test="${not busquedaRealizada}">
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
                        </c:if>
                    </main>
            </body>

            </html>
