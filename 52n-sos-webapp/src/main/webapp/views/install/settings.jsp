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
<jsp:include page="header.jsp">
	<jsp:param name="step" value="3" />
</jsp:include>
<jsp:include page="../common/logotitle.jsp">
	<jsp:param name="title" value="Settings - change with care!" />
	<jsp:param name="lead-paragraph" value="You can change these settings later in the administrative backend." />
</jsp:include>
<form action="<c:url value="/install/settings" />" method="POST" class="form-horizontal">
	<div id="settings">
		
	</div>
	<script type="text/javascript">
	$.getJSON('<c:url value="/static/conf/sos-settings.json" />', function(settings) {
 		var $container = $("#settings");

		generateSettings(settings, $container, true);
		$("input[name=SOS_URL]").val(window.location.toString()
			.replace(/install\/settings.*/, "sos")).trigger("input");		

		<%
            /* overwrite default values with session variables */
            for (org.n52.sos.service.Setting s : org.n52.sos.service.Setting.values()) {
                if (request.getAttribute(s.name()) != null) {
                    out.println("setSetting('" + s.name() + "', '" 
                        + request.getAttribute(s.name()) + "', settings);");
                }
            }
		%>

		$(".required").bind("keyup input change", function() {
            var valid = true;
            $(".required").each(function(){ 
                var val = $(this).val();
                return valid = (val != null && val != undefined && val != "");
            });
            if (valid) {
                $("button[type=submit]").removeAttr("disabled");
            } else {
                $("button[type=submit]").attr("disabled", true);
            }
        });

        $(".required:first").trigger("change");
	});
	</script>

	<hr/>
	<div>
		<a href="<c:url value="/install/database" />" class="btn">Back</a>
		<button type="submit" class="btn btn-info pull-right">Next</button>
	</div>
</form>

<jsp:include page="../common/footer.jsp" />
