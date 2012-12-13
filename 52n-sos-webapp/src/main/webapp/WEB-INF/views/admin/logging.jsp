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
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ page import="org.n52.sos.service.AbstractLoggingConfigurator.Level" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="activeMenu" value="admin" />
</jsp:include>

<jsp:include page="../common/logotitle.jsp">
    <jsp:param name="title" value="Logging Configuration" />
    <jsp:param name="leadParagraph" value="Use this site to adjust the logging configuration." />
</jsp:include>
<hr/>

<style type="text/css">
.icon-plus { padding-bottom: 2px; margin-left: 5px; }
.remove-logger { margin-left: -36px; }
.remove-logger i { vertical-align: text-top !important; }
</style>

<c:if test="${not empty error}">
    <script type="text/javascript">
        showError("${error}");
    </script>
</c:if>

<legend id="messages-header">Latest Log Messages</legend>
<div id="messages-table">
    <table class="table table-striped table-condensed">
        <tbody>
            <c:forEach items="${logMessages}" var="logMessage">
                <tr>
                    <td><small>${fn:escapeXml(logMessage)}<small></td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
    <p class="pull-right"><small>You can download the complete log file <a href="<c:url value="/admin/logging/file"/>" target="_blank">here</a>.</small></p>
</div>
    
<form method="POST" class="form-horizontal">
    <legend>Log Levels</legend>
    <div class="control-group" id="rootLogger">
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
    <div id="level-container">
        <c:forEach var="logger" items="${loggerLevels}">
            <div class="control-group">
                <label class="control-label" for="${logger.key}"><code>${logger.key}</code></label>
                <div class="controls">
                    <select name="${logger.key}" class="input-xlarge logger">
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
                    <a href="#" class="remove-logger" title="Remove Logger"><i class="icon-trash"></i></a>
                </div>
            </div>
        </c:forEach>
    </div>

    <div class="pull-right">
        <a href="#add-logger" data-toggle="modal" role="button" title="Add new Logger" class="btn btn-mini"><i class="icon-plus"></i></a>
    </div>

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
        <p><small>It will take some time till the changes take effect as the logging configuration is read asynchronously.</small></p>
        <button type="submit" class="btn btn-info">Save</button>
    </div>
</form>

<div id="add-logger" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h3>Add new Logger</h3>
    </div>
    <div class="modal-body">
        <div class="form form-horizontal">
            <div class="control-group">
                <label class="control-label" for="new-logger-name">Logger</label>
                <div class="controls">
                    <input type="text" id="new-logger-name" class="input-xlarge" />
                    <span class="help-block">The Logger identification (usually a package name or prefix).</span>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="new-logger-level">Level</label>
                <div class="controls">
                    <select id="new-logger-level" class="input-xlarge">
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
                    <span class="help-block">The Level that is applied to the Logger.</span>
                </div>
            </div>
        </div>
    </div>
    <div class="modal-footer">
        <button type="button" id="add-logger-button" class="btn btn-info">Add</button>
        <a href="#" role="button" data-dismiss="modal" aria-hidden="true" class="btn">Discard</a>
    </div>
</div>

<script type="text/javascript">
    $("#messages-table").hide();
    $("#messages-header").wrapInner($("<a>").attr("href", "#"))
    $("#messages-header").children("a").prepend($("<i>").addClass("icon-chevron-right"));
    $("#messages-header").click(function(e) {
        e.preventDefault();
        $(this).find("i").toggleClass("icon-chevron-right icon-chevron-down")
        $("#messages-table").slideToggle();
    });
    $(".remove-logger").live("click", function(e) {
        e.preventDefault();
        $(this).parents(".control-group").slideUp(function() {
            $(this).remove();
        });
    });
    $("#new-logger-name").on("input keyup", function() {
        var val = $(this).val();
        var loggers = [];
        $("select.logger").each(function(){ 
            loggers.push($(this).attr("name")); 
        });

        if (!val || loggers.contains(val)) {
            $(this).parents(".control-group").addClass("error");
            $("#add-logger-button").attr("disabled", true);
        } else {
            $(this).parents(".control-group").removeClass("error");
            $("#add-logger-button").removeAttr("disabled");
        }
    }).trigger("input");

    $("#add-logger").on("hidden", function() {
        $(this).find("input").val("");
    });

    $("#add-logger-button").on("click", function(e) {
        e.preventDefault();
        var $dialog = $(this).parents(".modal");
        var logger = $("#new-logger-name").val();
        var level = $("#new-logger-level").val();
        var $group = $("<div>").addClass("control-group").hide();
        $("<label>").addClass("control-label").attr("for", logger)
            .append($("<code>").text(logger)).appendTo($group);
        $("<div>").addClass("controls")
            .append($("#rootLogger select").clone().attr("name", logger).addClass("logger").val(level))
            .append($("<a>").addClass("remove-logger").attr({"title": "Remove Logger", "href": "#" })
                .append($("<i>").addClass("icon-trash")))
            .appendTo($group);
        $("#level-container").append($group);
        $group.slideDown();
        $dialog.modal("hide");
    });
</script>
<br/>
<jsp:include page="../common/footer.jsp" />
