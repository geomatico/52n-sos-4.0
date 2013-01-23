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
	<jsp:param name="title" value="52&deg;SOS Administration Login" />
	<jsp:param name="leadParagraph" value="Please login to view the admin console." />
</jsp:include>
<hr/>
<form action="<c:url value="/j_spring_security_check" />" method="POST" class="form-horizontal">
	<div class="control-group">
		<label class="control-label" for="j_username">Username</label>
		<div class="controls">
			<input class="input-xlarge" type="text" name="j_username" autocomplete="off"/>
		</div>
	</div>
	<div class="control-group">
		<label class="control-label" for="j_password">Password</label>
		<div class="controls">
			<input class="input-xlarge" type="password" name="j_password" autocomplete="off">
				<span id="passwordReset" class="help-block">You can reset your admin password by executing the file <code>sql/reset_admin.sql</code> (located inside the SOS installation directory in the webapps folder of your application server) on your database.</span>
			</input>
		</div>
	</div>
	<div class="form-actions">
		<button class="btn" type="submit">Login</button>
	</div>
</form>
<script type="text/javascript">
	$(function(){
		$("#passwordReset").hide();
		if ($.queryParam["error"]) {
			showError("Incorrect username/password. Please try again!");
			$("#passwordReset").fadeIn();
		}
		$("input[type=text],input[type=password]").bind("keyup input", function() {
			var empty = false;
			$("input[type=text], input[type=password]").each(function(i,e) {
				if ($(e).val() === "") { empty = true; }
			});
			$("button[type=submit]").attr("disabled", empty);	
		}).trigger("input");
		$("input[name=j_username]").focus();
	});
</script>
<jsp:include page="../common/footer.jsp" />
