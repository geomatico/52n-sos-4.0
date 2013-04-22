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
	<jsp:param name="title" value="Reset SOS" />
	<jsp:param name="leadParagraph" value="Use this functionality with care!" />
</jsp:include>
<hr/>
<p>If you click on the Reset button, the database access configuration of this SOS instance will be deleted. The database will stay intact and can be used for a new SOS install.</p>

<p>If you merely want to remove test data, use the test data removal function on the database admin page. If you want to overwrite the existing database, please select the option to delete existing tables in the installation wizard.</p>

<form id="reset-form" method="POST" action="reset" />
<div class="modal hide fade in" id="confirmDialog">
  <div class="modal-header">
    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
    <h3>Are you really sure?</h3>
  </div>
  <div class="modal-body">
     <p><span class="label label-important">Warning!</span> This will delete the database configuration of the this SOS instance.</p>
  </div>
  <div class="modal-footer">
    <button type="button" class="btn" data-dismiss="modal" aria-hidden="true">Cancel</button>
    <button type="button" id="reset" class="btn btn-danger">Reset</button>
  </div>
</div>
<div class="pagination-centered">
    <button type="button" id="showDialog" class="btn btn-danger btn-large">Reset</button>
</div>
<script type="text/javascript">
    $("#confirmDialog").modal({
        "keyboard": true,
        "show": false
    });
    $("#showDialog").click(function(){
        $("#confirmDialog").modal("show");
    })
    $(function() {
        $("#reset").click(function() {
            $("#reset-form").submit();
        });
    });
</script>
<br/>
<jsp:include page="../common/footer.jsp" />
