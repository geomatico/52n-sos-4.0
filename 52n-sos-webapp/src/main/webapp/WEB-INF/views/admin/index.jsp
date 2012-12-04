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
<jsp:include page="../common/header.jsp">
	<jsp:param name="activeMenu" value="admin" />
</jsp:include>
<jsp:include page="../common/logotitle.jsp">
	<jsp:param name="title" value="Administration Panel" />
	<jsp:param name="leadParagraph" value="Use the admin menu above to select different administrative tasks." />
</jsp:include>
<p class="pull-right">
<button type="button" id="reloadCapsCache" class="btn">Reload Capabilities Cache</button>
</p>
    
<script type="text/javascript">
    $("#reloadCapsCache").click(function() {
        var $b = $(this);
        $b.attr("disabled", true);
        $.ajax({
            url: "<c:url value="/admin/cache/reload"/>",
            type: "POST"
        }).done(function(e) {
            showSuccess("Capabilties cache reloaded.");
            $b.removeAttr("disabled");
        }).fail(function(error){
            showError("Capabilites cache reload failed: " + error.responseText);
            $b.removeAttr("disabled");
        });
    });
</script>


<div class="row">
    <div class="span12">
        <c:if test="${not empty VERSION}">
            <p><strong>Version:</strong> ${VERSION}</p>
        </c:if>
        <c:if test="${not empty SVN_VERSION}">
            <p><strong>Revision:</strong> ${SVN_VERSION}</p>
        </c:if>
        <c:if test="${not empty BUILD_DATE}">
            <p><strong>Build date:</strong> ${BUILD_DATE}</p>
        </c:if>
        <c:if test="${not empty INSTALL_DATE}">
            <p><strong>Installation date:</strong> ${INSTALL_DATE}</p>
        </c:if>
    </div>
</div>
    
<jsp:include page="../common/footer.jsp" />
