<%--

    Copyright (C) 2012
    by 52 North Initiative for Geospatial Open Source Software GmbH

    Contact: Andreas Wytzisk
    52 North Initiative for Geospatial Open Source Software GmbH
    Martin-Luther-King-Weg 24
    48155 Muenster, Germany
    info@52north.org

    This program is free software; you can redistribute and/or modify it under
    the terms of the GNU General Public License version 2 as published by the
    Free Software Foundation.

    This program is distributed WITHOUT ANY WARRANTY; even without the implied
    WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
    General Public License for more details.

    You should have received a copy of the GNU General Public License along with
    this program (see gnu-gpl v2.txt). If not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
    visit the Free Software Foundation web page, http://www.fsf.org.

--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page import="org.n52.sos.service.AbstractLoggingConfigurator.Level" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="activeMenu" value="admin" />
</jsp:include>

<jsp:include page="../common/logotitle.jsp">
    <jsp:param name="title" value="Logging Configuration" />
    <jsp:param name="leadParagraph" value="Use this site to adjust the logging configuration." />
</jsp:include>
<hr/>

<c:if test="${not empty error}">
    <script type="text/javascript">
        showError("${error}");
    </script>
</c:if>

<p>It will take some time till the changes take effect as the logging configuration is read asynchronously.</p>
    
<form method="POST" class="form-horizontal">
    <legend>Log Levels</legend>
    <div class="control-group">
        <label class="control-label" for="rootLogLevel">Root Log Level</label>
        <div class="controls">
            <select name="rootLogLevel" class="input-xlarge">
                <c:forEach var="level" items="<%= Level.values()%>">
                    <c:choose>
                        <c:when test="${rootLogLevel eq level}">
                            <option selected>${level}</option>
                        </c:when>
                        <c:otherwise>
                            <option>${level}</option>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </select>
            <span class="help-block">The log level of the <code>root</code> logger. This level is applied to all not explicitly configured loggers.</span>
        </div>
    </div>
    <c:forEach var="logger" items="${loggerLevels}">
        <div class="control-group">
            <label class="control-label" for="rootLogLevel">Log Level for <code>${logger.key}</code></label>
            <div class="controls">
                <select name="${logger.key}" class="input-xlarge">
                    <c:forEach var="level" items="<%= Level.values()%>">
                        <c:choose>
                            <c:when test="${logger.value eq level}">
                                <option selected>${level}</option>
                            </c:when>
                            <c:otherwise>
                                <option>${level}</option>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                </select>
                <span class="help-block">The log level of the <code>${logger.key}</code> logger.</span>
            </div>
        </div>
    </c:forEach>
    <legend>General Configuration</legend>
    <div class="control-group">
        <div class="controls">
            <label class="checkbox">
                <c:choose>
                    <c:when test="${isConsoleEnabled}" >
                        <input type="checkbox" name="isConsoleEnabled" value="${isConsoleEnabled}" checked />
                    </c:when>
                    <c:otherwise>
                        <input type="checkbox" name="isConsoleEnabled" value="${isConsoleEnabled}" />
                    </c:otherwise>
                </c:choose>
                Shoud messages be logged to the standard output?
            </label>
        </div>
    </div>
    <div class="control-group">
        <div class="controls">
            <label class="checkbox">
                <c:choose>
                    <c:when test="${isFileEnabled}" >
                        <input type="checkbox" name="isFileEnabled" value="${isFileEnabled}" checked />
                    </c:when>
                    <c:otherwise>
                        <input type="checkbox" name="isFileEnabled" value="${isFileEnabled}" />
                    </c:otherwise>
                </c:choose>
                Shoud messages be logged to the log file?
            </label>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label" for="daysToKeep">Days of log files to keep</label>
        <div class="controls">
            <input type="text" class="input-xlarge" name="daysToKeep" value="${daysToKeep}" />
            <span class="help-block">How many days of log files should be kept?</span>
        </div>
    </div>
    <div class="form-actions">
        <button type="submit" class="btn btn-info">Save</button>
    </div>
</form>

<br/>
<jsp:include page="../common/footer.jsp" />
