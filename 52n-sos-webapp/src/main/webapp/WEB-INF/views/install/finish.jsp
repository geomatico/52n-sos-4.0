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
	<jsp:param name="step" value="4" />
</jsp:include>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:include page="../common/logotitle.jsp">
	<jsp:param name="title" value="Finishing Installation" />
	<jsp:param name="leadParagraph" value="All configuration options are set. Click on 'Install' to finish the installation." />
</jsp:include>

<p>Please enter credentials to login into the administrator panel below. You can reset your admin password by executing the file <code>sql/reset_admin.sql</code> (located inside the SOS installation directory in the webapps folder of your application server) on your database.</p>

<hr />

<form action="<c:url value="/install/finish" />" method="POST" class="form-horizontal">

	<div class="control-group">
		<label class="control-label" for="sos_website">Username</label>
		<div class="controls">
			<input class="input-xlarge" type="text" name="admin_username" autocomplete="off" placeholder="admin"/>
			<span class="help-block"><span class="label label-warning">required</span> The username to login into the admin backend.</span>
		</div>
	</div>

	<div class="control-group">
		<label class="control-label" for="password">Password</label>
		<div class="controls">
			<input type="hidden" name="admin_password"/>
			<input id="password" class="input-xlarge" type="text" autocomplete="off" placeholder="password"/>
			<span class="help-block"><span class="label label-warning">required</span> The password to login into the admin backend.</span>
		</div>
	</div>

	<hr/>
	
	 <p>Clicking 'Install' will persist all settings in the database and (depending on your configuration) create or delete tables and insert test data. Thanks for using the 52&deg; North SOS!</p>

	 <p>If your SOS instance is publically available please inform the community on <a href="mailto:sensorweb@52north.org">sensorweb@52north.org</a> so that many people can profit from your hard work. If you like your SOS can also be listed in our <a href="https://wiki.52north.org/bin/view/SensorWeb/SosExampleServices">Wiki</a> as an example.</p>

	<div>
		<a href="<c:url value="/install/settings" />" class="btn">Back</a>
		<button class="btn btn-info pull-right" type="submit">Install</button>
	</div>
	<br/>
	<script type="text/javascript">
		$(function(){
			$("input[type=text]").bind("keyup input", function() {
				var empty = false;
				$("input[type=text]").each(function() {
					if ($(this).val() === "") { empty = true; }
				});
				$("button[type=submit]").attr("disabled", empty);	
			}).trigger("input");
			$("input#password")
			  .bind('focus', function() {
				$(this).val($("input[name=admin_password]").val());
			}).bind('blur', function() {
				$(this).val($(this).val().replace(/./g, String.fromCharCode(8226)));
			}).bind("keyup input", function() {
				$("input[name=admin_password]").val($(this).val());
			});
			$('button').click(function() {
				$(this).parents("form").submit();
			});
		});
	</script>
</form>
<jsp:include page="../common/footer.jsp" />
