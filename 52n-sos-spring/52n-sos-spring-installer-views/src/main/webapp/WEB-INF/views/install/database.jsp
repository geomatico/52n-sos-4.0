<%--

    Copyright (C) 2013
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

<jsp:include page="header.jsp">
    <jsp:param name="step" value="2" />
</jsp:include>
<jsp:include page="../common/logotitle.jsp">
    <jsp:param name="title" value="Database configuration" />
    <jsp:param name="leadParagraph" value="Please enter the details of the database you want to use for SOS." />
</jsp:include>
<script type="text/javascript" src="<c:url value="/static/lib/parseuri.js" />"></script>

<form action="<c:url value="/install/database" />" method="POST" class="form-horizontal">
    <jsp:include page="../common/database-configuration.jsp" />
    <jsp:include page="../common/database-server-configuration.jsp" />
    <jsp:include page="../common/database-advanced-configuration.jsp" />
    <fieldset>
        <legend>Actions</legend>
        <div class="control-group">
            <div class="controls">
                <label class="checkbox">
                    <input type="checkbox" name="create_tables" checked="checked" />
                    <strong>Create tables</strong> &mdash; This will create the necessary tables in the database.
                </label>
            </div>
        </div>
        <div class="control-group">
            <div class="controls">
                <label class="checkbox">
                    <input type="checkbox" name="overwrite_tables" />
                    <strong>Delete existing tables</strong> &mdash; This will delete all existing tables in the database.
                </label>
                <span style="display: none;" class="help-block"><span class="label label-important">Warning!</span> 
                    This will erase the entire database.</span>
            </div>
        </div>
        <div class="control-group">
            <div class="controls">
                <label class="checkbox">
                    <input type="checkbox" name="create_test_data" checked="checked" />
                    <strong>Create test data</strong> &mdash; This will insert the dummy data in the tables thus created.
                </label>
            </div>
        </div>
    </fieldset>
    <hr/>
    <div>
        <a href="<c:url value="/install/index" />" class="btn">Back</a>
        <button id="next" type="button" class="btn btn-info pull-right">Next</button>
    </div>
</form>


<script type="text/javascript">
    warnIfNotHttps();
    $.getJSON("<c:url value="/static/conf/default-database-values.json" />", function(settings) {
        var jdbc_uri = settings["jdbc_uri"],
            driver = settings["driver"],
            dialect = settings["dialect"],
            connectionPool = settings["connection_pool"],
            schema = settings["schema"];

        
        <c:if test="${not empty databaseSettings}">
            <c:if test="${not empty databaseSettings['driver']}">
                driver = "${databaseSettings['driver']}";
            </c:if>
            <c:if test="${not empty databaseSettings['jdbc_uri']}">
                jdbc_uri = "${databaseSettings['jdbc_uri']}";
            </c:if>
            
            <c:if test="${not empty databaseSettings['dialect']}">
                dialect = "${databaseSettings['dialect']}";
            </c:if>
            <c:if test="${not empty databaseSettings['connection_pool']}">
                connectionPool = "${databaseSettings['connection_pool']}";
            </c:if>
            <c:if test="${not empty databaseSettings['schema']}">
                schema = "${databaseSettings['schema']}";
            </c:if>
        </c:if>
            
        $("input[name=connection_pool]").val(connectionPool);
        $("input[name=dialect]").val(dialect);
        $("input[name=schema]").val(schema);
                
        jdbc_uri = jdbc_uri.replace("jdbc:postgresql://","");
        $("#driver").val(driver);
        $(".jdbccomponent").bind("keyup input", setJdbcString);
        $("#jdbc-input").val(jdbc_uri).bind("keyup input", setJdbcInputs).trigger("input");

        $("input[type=text],input[type=password],textarea").trigger("input");
        $("#next").click(function() {
            $("input[name=jdbc_uri]").val("jdbc:postgresql://" + $("#jdbc-input").val());
            $(this).parents("form").submit();
        });
        $("input[name=overwrite_tables]").click(function(){
            $(this).parent().next().toggle("fast");
        });

        var selector = "#database-advanced-configuration, #database-server-configuration";
        var $legend = $(selector).children("legend");
        $legend.parent().each(function() {
            $(this).find(".control-group").wrapAll($("<div>").addClass("control-wrap"));
        });
        $("div.control-wrap").hide();
        $legend.wrapInner($("<a>").attr("href", "#"));
        $legend.children("a").prepend($("<i>").addClass("icon-chevron-right"));
        $legend.click(function(e) {
            e.preventDefault();
            $(this).find("i").toggleClass("icon-chevron-right icon-chevron-down");
            $(this).next().slideToggle();
        });

        $("#database-advanced-configuration legend a").data({
            "trigger": "hover",
            "placement": "right",
            "original-title": "These should only be changed if you know what your're doing."
        }).attr("rel", "tooltip").tooltip();

        $("input[name=overwrite_tables]").change(function() {
            var $create_tables = $("input[name=create_tables]");
            if ($(this).attr("checked")) {
                $create_tables.attr({ 
                    "checked": "checked", 
                    "disabled": true })
                .parent("label").addClass("muted");
            } else {
                $create_tables.removeAttr("disabled")
                    .parent("label").removeClass("muted");
            }
        });
    
    });
</script>

<jsp:include page="../common/footer.jsp" />
