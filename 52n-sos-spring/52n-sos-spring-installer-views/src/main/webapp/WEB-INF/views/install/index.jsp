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
<jsp:include page="header.jsp">
	<jsp:param name="step" value="1" />
</jsp:include>
<jsp:include page="../common/logotitle.jsp">
	<jsp:param name="title" value="52&deg;North SOS Installation Wizard" />
	<jsp:param name="leadParagraph" value="Welcome to installation wizard." />
</jsp:include>

<p>This software is licensed under the <a target="_blank" href="http://www.gnu.org/licenses/gpl-2.0.html">GNU General Public License v2</a> and by installing the 52&deg;North SOS you agree to adhere to the license's terms and conditions. This wizard will guide you through the installation, database setup and initial configuration of SOS. Click the "Start" button below to get your SOS up and running.</p>

<h3>Requirements</h3>

<p>This installer requires a running <strong>PostgreSQL 9</strong> installation with an accessible 
	data base. A detailed installation guide for the different platforms can be
	found in the <a target="_blank" href="http://wiki.postgresql.org/wiki/Detailed_installation_guides">PostgreSQL wiki</a>.
	The database also needs the <strong>PostGIS</strong> extension in <strong>version 1.5 or 2.0</strong> enabled (see
	the <a target="_blank" href="http://postgis.refractions.net/documentation/manual-2.0/postgis_installation.html">PostGIS documentation</a>
	for a description how to install and enable it for your database).</p>

<div id="uploadForm">
	<h3>Upload a previous configuration file</h3>
	<p>You can upload the exported configuration of a previous SOS installation:</p>
	<input type="file" id="file" style="display: none;" />
	<div class="input-append">
		<span id="fileCover" class="input-large uneditable-input disabled"></span>
		<button type="button" class="btn" onclick="$('input[id=file]').click();">Browse</button>
		<button type="button" id="upload" class="btn" disabled="disabled">Upload</button>
	</div>
</div>

<script type="text/javascript">
	if (!window.File || !window.FileReader) {
		$("#uploadForm").remove();
	} else {
		$('#file').change(function() {
			$('#fileCover').text($(this).val().replace("C:\\fakepath\\", ""));
			$('#upload').removeAttr("disabled");
		});
		$('#upload').click(function() {
			var file= $('#file').get(0).files[0];
			var reader = new FileReader();
			reader.onload = function(e) {
				var contents = e.target.result;
				try {
					contents = JSON.parse(contents);
				} catch(e) {
					showError("Couldn't load settings... No valid JSON.")
					return;
				}
				$.ajax('<c:url value="/install/load" />',{
					"type": "POST",
					"contentType": "application/json",
					"data": JSON.stringify(contents)
				}).fail(function(e) {
					showError("Couldn't load settings...");
				}).done(function() {
					showSuccess("Settings have been loaded. Please revise them in the next steps.");
				})
			};
			reader.readAsText(file)
		});		
	}
</script>

<hr />

<a href="<c:url value="/install/database" />" class="btn btn-info pull-right">Start</a>

<jsp:include page="../common/footer.jsp" />
