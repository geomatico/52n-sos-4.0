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
<jsp:include page="../common/header.jsp">
    <jsp:param name="activeMenu" value="admin" />
</jsp:include>

<link rel="stylesheet" href="<c:url value="/static/lib/prettify.css" />" type="text/css" />
<link rel="stylesheet" href="<c:url value="/static/lib/codemirror-2.34.css" />" type="text/css" />  
<link rel="stylesheet" href="<c:url value="/static/css/codemirror.custom.css" />" type="text/css" />
<script type="text/javascript" src="<c:url value="/static/lib/codemirror-2.34.js" />"></script>
<script type="text/javascript" src="<c:url value="/static/lib/codemirror-2.34-plsql.js" />"></script>
<script type="text/javascript" src="<c:url value="/static/lib/prettify.min.js" />"></script>
<script type="text/javascript" src="<c:url value="/static/lib/vkbeautify-0.99.00.beta.js" />"></script>

<jsp:include page="../common/logotitle.jsp">
    <jsp:param name="title" value="Database Panel" />
    <jsp:param name="leadParagraph" value="Here you can query the database directly." />
</jsp:include>

<div class="pull-right">
    <ul class="inline">
        <li><a href="#confirmDialogClear" data-toggle="modal" role="button" title="Clear Database" class="btn btn-danger">Clear Database</a></li>
        <li><a href="#confirmDialogDelete" data-toggle="modal" role="button" title="Delete deleted Observations" class="btn btn-danger">Delete deleted Observations</a></li>
        <li><button id="testdata" type="button" class="btn btn-danger"></button></li>
    </ul>
</div>

<form id="form" action="" method="POST">
    <h3>Query</h3>
    <div class="controls-row">
        <select id="input-query" class="span12 pull-right">
            <option value="" disabled selected style="display: none;">Select a example query &hellip;</option>
        </select>
    </div>
    <div class="controls-row">
        <textarea id="editor" class="span12" rows="15"></textarea>
    </div>
    <br />
    <div class="controls-row">
        <div class="pull-right">
            <button id="send-button" type="button" class="btn btn-info inline">Send</button>
        </div>
    </div>
</form>
<div id="result"></div>


<div class="modal hide fade in" id="confirmDialogClear">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h3>Are you really sure?</h3>
    </div>
    <div class="modal-body">
        <p><span class="label label-important">Warning!</span> This will remove all contents (except settings) from the database!</p>
    </div>
    <div class="modal-footer">
        <button type="button" class="btn" data-dismiss="modal" aria-hidden="true">Cancel</button>
        <button type="button" id="clear" class="btn btn-danger">Do it!</button>
    </div>
</div>

<div class="modal hide fade in" id="confirmDialogDelete">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h3>Are you really sure?</h3>
    </div>
    <div class="modal-body">
        <p><span class="label label-important">Warning!</span> This will remove all deleted observations from the database!</p>
    </div>
    <div class="modal-footer">
        <button type="button" class="btn" data-dismiss="modal" aria-hidden="true">Cancel</button>
        <button type="button" id="delete" class="btn btn-danger">Do it!</button>
    </div>
</div>

<script type="text/javascript">
    $(function() {
        var testDataInstalled = ${IS_TEST_DATA_SET_INSTALLED_MODEL_ATTRIBUTE};
        var $button = $("#testdata");
        
        function create() {
            $button.attr("disabled", true);
            $.ajax({
                "url": "<c:url value="/admin/database/testdata/create" />",
                "type": "POST"
            }).fail(function(error) {
                showError("Request failed: " + error.status + " " + error.statusText);
                $button.removeAttr("disabled");
            }).done(function() {
                showSuccess("Test data set was inserted.");
                testDataInstalled = !testDataInstalled;
                setButtonLabel();
                $button.removeAttr("disabled");
            });
        }

        function remove() {
            $button.attr("disabled", true);
            $.ajax({
                "url": "<c:url value="/admin/database/testdata/remove" />",
                "type": "POST"
            }).fail(function(error) {
                showError("Request failed: " + error.status + " " + error.statusText);
                $button.removeAttr("disabled");
            }).done(function() {
                showSuccess("The test data was removed.");
                testDataInstalled = !testDataInstalled;
                setButtonLabel();
                $button.removeAttr("disabled");
            });
        }

        function setButtonLabel() {
            if (testDataInstalled) {
                $button.text("Remove test data");
            } else {
                $button.text("Insert test data");
            }
        }

        $button.click(function() {
            if (testDataInstalled) {
                remove();
            } else {
                create();
            }
        });
        
        $("#clear").click(function() {
            $("#confirmDialogClear").find("button").add($button).attr("disabled", true);
            $.ajax({
                "url": "<c:url value="/admin/database/clear" />",
                "type": "POST"
            }).fail(function(error) {
                showError("Request failed: " + error.status + " " + error.statusText);
                $("#confirmDialogClear").find("button").add($button).removeAttr("disabled");
                $("#confirmDialogClear").modal("hide");
            }).done(function() {
                showSuccess("The database was cleared");
                testDataInstalled = false;
                setButtonLabel();
                $("#confirmDialogClear").find("button").add($button).removeAttr("disabled");
                $("#confirmDialogClear").modal("hide");
            });
        });

        $("#delete").click(function() {
            $("#confirmDialogDelete").find("button").add($button).attr("disabled", true);
            $.ajax({
                "url": "<c:url value="/admin/database/deleteDeletedObservations" />",
                "type": "POST"
            }).fail(function(error) {
                if (error.responseText) {
                    showError(error.responseText);
                } else {
                    showError("Request failed: " + error.status + " " + error.statusText);
                }

                $("#confirmDialogDelete").find("button").add($button).removeAttr("disabled");
                $("#confirmDialogDelete").modal("hide");
            }).done(function() {
                showSuccess("The deleted observation were deleted.");
                $("#confirmDialogDelete").find("button").add($button).removeAttr("disabled");
                $("#confirmDialogDelete").modal("hide");
            });
        });
        
        setButtonLabel();
    });
</script>

<script type="text/javascript">
$(function() {
    var editor = CodeMirror.fromTextArea($("#editor").get(0), 
        { "mode": "text/x-plsql", "lineNumbers": true, "lineWrapping": true });

    $.get("<c:url value="/static/conf/sql-queries.json"/>", function(settings) {
        if ((typeof settings) === "string") {
            settings = JSON.parse(settings);
        }
        var $select = $("#input-query");

        for (var key in settings.queries) {
            $("<option>").text(key).appendTo($select);
        }
        $select.change(function() {
            var sql = settings.queries[$(this).val()];
            sql = vkbeautify.sql(sql, 2);
            editor.setValue(sql);
        });
        $("#send-button").click(function() {
            var query = editor.getValue();
            if (query == "") {
                showError("No query specified.");
            } else {
                var $result = $("#result")
                $result.slideUp("fast");
                $result.children().remove();
                $.ajax({
                    "url": "<c:url value="/admin/database" />",
                    "type": "POST",
                    "data": encodeURIComponent(query),
                    "contentType": "text/plain",
                    "dataType": "json"
                }).fail(function(error){
                    showError("Request failed: " + error.status + " " + error.statusText);
                }).done(function(response){
                    if (typeof(response) === "string") {
                        response = $.parseJSON(response);
                    }
                    if (response.error) {
                        showError(response.error);
                    } else if (response.message) {
                        showSuccess(response.message);
                    } else {
                        $("<h3>").text("Result").appendTo($result);
                        $("html, body").animate({ 
                            scrollTop: $("#result").offset().top
                         }, "slow");
                        var $table = $("<table>");
                        $table.addClass("table table-striped table-bordered table-condensed");
                        $table.appendTo($result);
                        if (response.names) {
                            var $tr = $("<tr>");
                            for (var i = 0; i < response.names.length; ++i) {
                                $("<th>").text(response.names[i]).appendTo($tr);
                            }
                            $tr.appendTo($table)
                        }
                        if (response.rows) {
                            for (var i = 0; i < response.rows.length; ++i) {
                                var row = response.rows[i];
                                var $row = $("<tr>");
                                for (var j = 0; j < row.length; ++j) {
                                    $("<td>").text(row[j]).appendTo($row);
                                }
                                $row.appendTo($table)
                            }
                        }
                        $result.slideDown();
                    }
                });
            }
        });
    });
});
</script>
<br/>
<jsp:include page="../common/footer.jsp" />
