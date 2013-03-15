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

<jsp:include page="../common/logotitle.jsp">
    <jsp:param name="title" value="Configure Observation Encodings" />
    <jsp:param name="leadParagraph" value="Disable or enable observation encodings" />
</jsp:include>

<link rel="stylesheet" href="<c:url value='/static/lib/jquery.tablesorter-bootstrap-2.712.min.css' />">
<script type="text/javascript" src="<c:url value='/static/lib/jquery.tablesorter-2.7.12.min.js'/>"></script>
<script type="text/javascript" src="<c:url value='/static/lib/jquery.tablesorter.widgets-2.7.12.min.js'/>"></script>

<table id="encodingsTable" class="table table-striped table-bordered">
    <thead>
        <tr>
            <th>Service</th>
            <th>Version</th>
            <th>Encoding</th>
            <th>Status</th>
        </tr>
    </thead>
    <tbody></tbody>
</table>

<script type="text/javascript">
jQuery(document).ready(function($) {
    $.getJSON("<c:url value='/admin/encodings/json'/>", function(j) {
        var $tbody = $("#encodingsTable tbody"),
        encodings = j.encodings, i, o, $row, $button;
        for (i = 0; i < encodings.length; ++i) {
            o = encodings[i];
            $row = $("<tr>");
            $("<td>").addClass("service").text(o.service).appendTo($row);
            $("<td>").addClass("version").text(o.version).appendTo($row);
            $("<td>").addClass("encoding").text(o.responseFormat).appendTo($row);
            $button = $("<button>").attr("type", "button")
                    .addClass("btn btn-small btn-block").on("click", function() {
                var $b = $(this),
                    $tr = $b.parents("tr"),
                    active = !$b.hasClass("btn-success"),
                    j = {
                        service: $tr.find(".service").text(),
                        version: $tr.find(".version").text(),
                        responseFormat: $tr.find(".encoding").text(),
                        active: active
                    };
                $b.prop("disabled", true);
                $.ajax("<c:url value='/admin/encodings/json'/>", {
                    type: "POST",
                    contentType: "application/json",
                    data: JSON.stringify(j)
                }).fail(function(e) {
                    showError("Failed to save settings: " + e.status + " " + e.statusText);
                    $b.prop("disabled", false);
                }).done(function() {
                    $b.toggleClass("btn-danger btn-success")
                      .text(active ? "active" : "inactive")
                      .prop("disabled", false);
                    
                });
            });
            if (o.active) { 
                $button.addClass("btn-success").text("active"); 
            } else {
                $button.addClass("btn-danger").text("inactive"); 
                
            }
            $("<td>").addClass("status").append($button).appendTo($row);
            
            $tbody.append($row);    
        }
        $.extend($.tablesorter.themes.bootstrap, {
            table: "table table-bordered",
            header: "bootstrap-header",
            sortNone: "bootstrap-icon-unsorted",
            sortAsc: "icon-chevron-up",
            sortDesc: "icon-chevron-down",
        });
        $("#encodingsTable").tablesorter({
            theme : "bootstrap",
            widgets : [ "uitheme", "zebra" ],
            headerTemplate: "{content} {icon}",
            widthFixed: true,
            headers: { 
                0: { sorter: "text" },
                1: { sorter: "text" },
                2: { sorter: "text" },
                3: { sorter: false } 
            },
            sortList: [ [0,0], [1,1], [2,0] ]
        });
    });
});
</script>




<jsp:include page="../common/footer.jsp" />
