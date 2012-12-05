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
<jsp:include page="header.jsp">
    <jsp:param name="step" value="2" />
</jsp:include>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<script type="text/javascript" src="<c:url value="/static/js/parseuri.js" />"></script>
<jsp:include page="../common/logotitle.jsp">
    <jsp:param name="title" value="Database configuration" />
    <jsp:param name="leadParagraph" value="Please enter the details of the database you want to use for SOS." />
</jsp:include>

<form action="<c:url value="/install/database" />" method="POST" class="form-horizontal">
    <fieldset>
        <legend>Database Configuration</legend>
        <div class="control-group">
            <label class="control-label" for="user-input">Database User Name</label>
            <div class="controls">
                <input class="jdbccomponent span8" type="text" id="user-input">
                    <span class="help-block"><span class="label label-warning">required</span> Your database server user name. The default value for PostgreSQL is "postgres".</span>
                </input>
            </div>
        </div>

        <div class="control-group">
            <label class="control-label" for="pass-input">Database Password</label>
            <div class="controls">
                <input class="jdbccomponent span8" type="text" id="pass-input">
                    <span class="help-block"><span class="label label-warning">required</span> Your database server password. The default value is "postgres".</span>
                </input>
            </div>
        </div>

        <div class="control-group">
            <label class="control-label" for="db-input">Database Name</label>
            <div class="controls">
                <input class="jdbccomponent span8" type="text" id="db-input">
                    <span class="help-block"><span class="label label-warning">required</span> Set this to the name of the database you want to use for SOS.</span>
                </input>
            </div>
        </div>
    </fieldset>
    <fieldset id="server-config">
        <legend><a href="#"><i class="icon-chevron-down"></i>Database Server configuration</a></legend>
        <div class="control-group">
            <label class="control-label" for="host-input">Database Host</label>
            <div class="controls">
                <input class="jdbccomponent span8" type="text" id="host-input">
                    <span class="help-block"><span class="label label-warning">required</span> Set this to the IP/net location of PostgreSQL database server. The default value for PostgreSQL is "localhost".</span>
                </input>
            </div>
        </div>

        <div class="control-group">
            <label class="control-label" for="port-input">Database Port</label>
            <div class="controls">
                <input class="jdbccomponent span8" type="text" id="port-input">
                    <span class="help-block"><span class="label label-warning">required</span> Set this to the port number of your PostgreSQL server. The default value for PostgreSQL is "5432".</span>
                </input>
            </div>
        </div>
    </fieldset>
    <fieldset id="advanced">
        <legend><a href="#" rel="tooltip" data-trigger="hover" data-placement="right" data-original-title="These should only be changed if you know what your're doing."><i class="icon-chevron-down"></i>Advanced database server configuration</a></legend>
        <div>
            <div class="control-group">
                <label class="control-label" for="driver">Database Driver</label>
                <div class="controls">
                    <input type="text" class="span8" id="driver" name="driver">
                        <span class="help-block"><span class="label label-warning">required</span> Set this to the class of the JDBC driver. The default value for PostgreSQL driver is "org.postgresql.Driver".</span>
                    </input>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="connection_pool">Connection Pool</label>
                <div class="controls">
                    <input type="text" class="span8" id="driver" name="connection_pool">
                    <span class="help-block"><span class="label label-warning">required</span> Set this to the class of the connection pool. The default value is the <a href="http://www.mchange.com/projects/c3p0/" title="C3P0">C3P0</a> connection pool.</span>
                    </input>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="jdbc_dialect">Database Dialect</label>
                <div class="controls">
                    <input type="text" class="span8" id="driver" name="jdbc_dialect">
                        <span class="help-block"><span class="label label-warning">required</span> Set this to the class of the JDBC dialect. The default value for PostgreSQL is the custom 52&deg;North dialect.</span>
                    </input>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="jdbc-input">Connection String</label>
                <div class="controls">
                    <div class="input-prepend">
                        <span id="jdbc-input-addon"class="add-on">jdbc:postgresql://</span>
                        <input id="jdbc-input" type="text" />
                    </div>
                    <input type="hidden" name="jdbc_uri" />
                    <span style="margin-top:10px;"class="help-block"><span class="label label-info">optional</span> The connection string that will be used to connect with your database.</span>
                </div>
            </div>
        </div>
    </fieldset>
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
                <span style="display: none;" class="help-block"><span class="label label-important">Warning!</span> This will erase the entire database.</span>
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
    $.getJSON("<c:url value="/static/conf/default-database-values.json" />", function(settings) {
        var jdbc = {};
        
        function buildJdbcString(j) {
            var string = j.host;
            if (j.port != undefined) {
                string += ":" + j.port;
            }
            string += "/" + encodeURIComponent(j.db);
            if (j.user || j.pass) {
                 string += "?";
            }
            if (j.user) {
                string += "user=" + encodeURIComponent(j.user);
            }
            if (j.pass) {
                if (j.user) string += "&";
                string += "password=" + encodeURIComponent(j.pass);
            }
            return encodeURI(string);
        }

        function parseJdbcString(j) {
            var parsed = parseUri("postgresql://" + j.replace("jdbc:postgresql://",""));
            return {
                port: parsed.port,
                user: parsed.queryKey.user,
                pass: parsed.queryKey.password,
                host: parsed.host,
                db: parsed.path.slice(1)
            };
        }

        function setJdbcString() {
            var $this = $(this);
            var id = $this.attr("id").replace(/-input/, "");
            jdbc[id] = $this.val();
            $("#jdbc-input").val(buildJdbcString(jdbc));
        }

        function setInputs() {
            jdbc = parseJdbcString($(this).val());
            for (key in jdbc) {
                $("#" + key + "-input").val(jdbc[key]);
            }
        }
        
        var jdbc_uri = settings["jdbc_uri"];
        var driver = settings["driver"];
        var dialect = settings["jdbc_dialect"];
        var connectionPool = settings["connection_pool"];
        
        <c:if test="${not empty sessionScope.jdbc_uri}">
            jdbc_uri = "${sessionScope.jdbc_uri}";
        </c:if>
        <c:if test="${not empty sessionScope.driver}">
            driver = "${sessionScope.driver}";
        </c:if>
        <c:if test="${not empty sessionScope.jdbc_dialect}">
            dialect = "${sessionScope.jdbc_dialect}";
        </c:if>
        <c:if test="${not empty sessionScope.connection_pool}">
            connectionPool = "${sessionScope.connection_pool}";
        </c:if>
            
        $("input[name=connection_pool]").val(connectionPool);
        $("input[name=jdbc_dialect]").val(dialect);
                
        jdbc_uri = jdbc_uri.replace("jdbc:postgresql://","");
        $("#driver").val(driver);
        $(".jdbccomponent")
            .bind("keyup input", setJdbcString)
        $("#jdbc-input")
            .val(jdbc_uri)
            .bind("keyup input", setInputs)
            .trigger("input");
        $("input[type=text],input[type=password],textarea").trigger("input");
        $("#next").click(function() {
            $("input[name=jdbc_uri]").val("jdbc:postgresql://" + $("#jdbc-input").val())
            $(this).parents("form").submit();
        });
        $("input[name=overwrite_tables]").click(function(){
            $(this).parent().next().toggle("fast");
        });
        $("#advanced, #server-config").children("legend").click(function(e) {
            e.preventDefault();
            $(this).find("i").toggleClass("icon-chevron-right icon-chevron-down")
            $(this).parent().children("div").slideToggle();
        }).parent().children("div").toggle();
        $("#advanced, #server-config").children("legend").find("i")
            .toggleClass("icon-chevron-right icon-chevron-down");
        $("#advanced legend a").tooltip();
        $("input[name=overwrite_tables]").change(function() {
            var $create_tables = $("input[name=create_tables]");
            if ($(this).attr("checked")) {
                $create_tables.attr({ 
                    "checked": "checked", 
                    "disabled": true })
                .parent("label").addClass("muted")
            } else {
                $create_tables.removeAttr("disabled")
                    .parent("label").removeClass("muted");
            }
        });
    
    });
</script>

<jsp:include page="../common/footer.jsp" />
