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
<jsp:include page="header.jsp">
	<jsp:param name="step" value="4" />
</jsp:include>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:include page="../common/logotitle.jsp">
	<jsp:param name="title" value="Finishing Installation" />
	<jsp:param name="leadParagraph" value="All configuration options are set. Click on 'Install' to finish the installation." />
</jsp:include>

<script type="text/javascript">
    if (document.referrer) {
        if (document.referrer.matches(/install\/settings/)) {
            showSuccess("Settings successfully tested.")
        }
    }
</script>

<p>Please enter credentials to login into the administrator panel below. You can reset your admin password by executing the file <code>sql/reset_admin.sql</code> (located inside the SOS installation directory in the webapps folder of your application server) on your database.</p>

<hr />

<form action="<c:url value="/install/finish" />" method="POST" class="form-horizontal">

	<div class="control-group">
		<label class="control-label" for="sos_website">Username</label>
		<div class="controls">
			<input class="input-xlarge" type="text" name="admin_username" autocomplete="off" placeholder="admin" value="${admin_username}"/>
			<span class="help-block"><span class="label label-warning">required</span> The username to login into the admin backend.</span>
		</div>
	</div>

	<div class="control-group">
		<label class="control-label" for="password">Password</label>
		<div class="controls">
			<input type="hidden" name="admin_password" value="${admin_password}"/>
			<input id="password" class="input-xlarge" type="text" autocomplete="off" placeholder="password" value="${admin_password}"/>
			<span class="help-block"><span class="label label-warning">required</span> The password to login into the admin backend.</span>
		</div>
	</div>

	<hr/>
	
	 <p>Clicking 'Install' will persist all settings in the database and (depending on your configuration) create or delete tables and insert test data.</p>
	 
	<div>
		<a href="<c:url value="/install/settings" />" class="btn">Back</a>
		<button class="btn btn-info pull-right" type="submit">Install</button>
	</div>
	<br/>
	<script type="text/javascript">
		$(function(){
            var $pwFacade = $("input#password"),
                $pwHidden = $("input[name=admin_password]"),
                $submit = $('button[type=submit]'),
                $inputs = $("input[type=text]");
			
            $inputs.bind("keyup input", function() {
				var empty = false;
				$inputs.each(function() {
					if ($(this).val() === "") { 
                        empty = true;
                    }
				});
				$submit.attr("disabled", empty);	
			}).trigger("input");
            $pwFacade.bind('focus', function() {
                $pwFacade.val($pwHidden.val());
            }).bind('blur', function() {
                $pwFacade.val($pwFacade.val().replace(/./g, String.fromCharCode(8226)));
            }).bind("keyup input", function() {
                $pwHidden.val($pwFacade.val());
            }).trigger("blur");
			$submit.click(function() {
				$submit.attr("disabled", true);
				$submit.parents("form").submit();
			});
		});
	</script>
</form>
<jsp:include page="../common/footer.jsp" />
