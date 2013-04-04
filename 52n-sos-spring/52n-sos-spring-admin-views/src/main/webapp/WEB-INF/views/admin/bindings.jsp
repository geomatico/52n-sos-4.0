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
    <jsp:param name="title" value="Configure Bindings" />
    <jsp:param name="leadParagraph" value="Disable or enable bindings" />
</jsp:include>

<link rel="stylesheet" href="<c:url value='/static/lib/jquery.tablesorter-bootstrap-2.712.min.css' />">
<script type="text/javascript" src="<c:url value='/static/lib/jquery.tablesorter-2.7.12.min.js'/>"></script>
<script type="text/javascript" src="<c:url value='/static/lib/jquery.tablesorter.widgets-2.7.12.min.js'/>"></script>

<table id="bindings" class="table table-striped table-bordered">
    <colgroup>
        <col style="width: 80%;"/>
        <col style="width: 20%;"/>
    </colgroup>
    <thead>
        <tr>
            <th>Path</th>
            <th>Status</th>
        </tr>
    </thead>
    <tbody></tbody>
</table>

<script type="text/javascript">
    jQuery(document).ready(function($) {

        $.extend($.tablesorter.themes.bootstrap, {
            table: "table table-bordered",
            header: "bootstrap-header",
            sortNone: "bootstrap-icon-unsorted",
            sortAsc: "icon-chevron-up",
            sortDesc: "icon-chevron-down"
        });

        function bindings(encodings) {
            var $tbody = $("#bindings tbody"), i, o, $row, $button;
            for (i = 0; i < encodings.length; ++i) {
                o = encodings[i];
                $row = $("<tr>");
                $("<td>").addClass("binding").text(o.binding).appendTo($row);
                $button = $("<button>").attr("type", "button")
                        .addClass("btn btn-small btn-block").on("click", function() {
                    var $b = $(this),
                            $tr = $b.parents("tr"),
                            active = !$b.hasClass("btn-success"),
                            j = {
                        binding: $tr.find(".binding").text(),
                        active: active
                    };
                    $b.prop("disabled", true);
                    $.ajax("<c:url value='/admin/bindings/json'/>", {
                        type: "POST",
                        contentType: "application/json",
                        data: JSON.stringify(j)
                    }).fail(function(e) {
                        showError("Failed to save binding: "
                                + e.status + " " + e.statusText);
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

            $("#bindings").tablesorter({
                theme: "bootstrap",
                widgets: ["uitheme", "zebra"],
                headerTemplate: "{content} {icon}",
                widthFixed: true,
                headers: {
                    0: {sorter: "text"},
                    1: {sorter: false}
                },
                sortList: [[0, 0]]
            });
        }

        $.getJSON("<c:url value='/admin/bindings/json'/>", function(j) {
            bindings(j.bindings);
        });
    });
</script>

<jsp:include page="../common/footer.jsp" />
