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
<jsp:include page="common/header.jsp">
    <jsp:param name="active-menu" value="client" />
</jsp:include>
<script type="text/javascript">
	/* redirect from "client/"" to "client" */
	if (window.location.pathname.slice(-1) === "/") {
		window.location.href = window.location.href.slice(0,-1);
	}
</script>
<link rel="stylesheet" href="<c:url value="/static/css/prettify.css" />" type="text/css" />
<link rel="stylesheet" href="<c:url value="/static/css/codemirror-2.34.css" />" type="text/css" />
<link rel="stylesheet" href="<c:url value="/static/css/codemirror.custom.css" />" type="text/css" />
<script type="text/javascript" src="<c:url value="/static/js/codemirror-2.34.js" />"></script>
<script type="text/javascript" src="<c:url value="/static/js/codemirror-2.34-xml.js" />"></script>
<script type="text/javascript" src="<c:url value="/static/js/prettify.min.js" />"></script>
<script type="text/javascript" src="<c:url value="/static/js/vkbeautify-0.99.00.beta.js" />"></script>
<jsp:include page="common/logotitle.jsp">
	<jsp:param name="title" value="52&deg;North SOS Test Client" />
	<jsp:param name="lead-paragraph" value="Choose a request from the examples or write your own to test the SOS." />
</jsp:include>
<form id="form" action="" method="POST">
	<h3 id="top">Service URL</h3>
	<input id="input-url" class="span12" type="text" placeholder="Service URL" value=""/>
	<h3>Request</h3>
	<div class="controls-row">
		<select id="input-version" class="span4">
			<option value="" disabled selected style="display: none;">Select the SOS version &hellip;</option>
			<%-- TODO reenable if v1.0.0 is supported
			<option value="1.0.0">Version 1.0.0</option>-->
			--%>
			<option value="2.0.0">Version 2.0.0</option>
		</select>
		<select id="input-request" class="span8 pull-right">
			<option value="" disabled selected style="display: none;">Select a example request &hellip;</option>
		</select>
	</div>
	<textarea id="editor" name="request" class="span12"></textarea>
	<div class="pull-right control-group" style="margin-bottom: 15px; margin-top: 15px;">
		<label class="checkbox inline" style="margin-right: 30px;">
			<input id="input-send-inline" type="checkbox" checked> show response inline
		</label>
		<button id="send-button" type="button" class="btn btn-info inline">Send</button>
	</div>
	<div id="response" class="span12" style="margin-left: 0;"></div>
</form>
<script type="text/javascript">
	$(function() {
		var version;
		var sosUrl = "<c:url value="/sos" />";

		var editor = CodeMirror.fromTextArea($("#editor").get(0), { 
            "mode": "xml", 
            "lineNumbers": true, 
            "lineWrapping": true
        });

		$("#form").attr("action", sosUrl);
		$("#input-request").addClass("disable").attr("disabled", true);
		$("#input-url").val(sosUrl).change(function() {
			var val = $(this).val();
			$("#form").attr("action", val);
			if (val) {
				$("#send-button").removeClass("disable").attr("disabled", false);
			} else {
				$("#send-button").addClass("disable").attr("disabled", true);
			}
		});
		$("#send-button").click(function() {
			var request = $.trim(editor.getValue());
			if (!$("#input-send-inline").attr("checked")) {
				if (request) {
					$("#form").submit();	
				} else {
					window.location.href = $("#input-url").val();
				}
			} else {
				$.ajax($("#input-url").val(), {
					"type": (request) ? "POST" : "GET",
					"contentType": "application/xml",
					"accepts": "application/xml",
					"data": request
				}).fail(function(error) {
					showError("Request failed: " + error.status + " " + error.statusText);
				}).done(function(data) {
					var xml = data.xml ? data.xml : new XMLSerializer().serializeToString(data);
					var $response = $("#response");
					$response.fadeOut("fast");
					$response.children().remove();
					$("<h3>")
						.text("Response")
						.appendTo($response);
					$("<pre>")
						.addClass("prettyprint")
						.addClass("linenums")
						.text(vkbeautify.xml(xml, 2))
						.appendTo($response);
					prettyPrint();
					$response.fadeIn("fast");
					$("html, body").animate({
						scrollTop: $("#response").offset().top
					}, "slow"); 
				});
			}
		});

		$.get("<c:url value="/static/conf/client-requests.json"/>", function(settings) {
			if ((typeof settings) === "string") {
				settings = JSON.parse(settings);
			}

			$("#input-version").change(function() {
				var oldVersion = version;
				version = $(this).val();
				if (version && version != oldVersion) {
					$("#response").fadeOut("fast").children().remove();
					$("#input-request option").remove()
					$("#input-request")
						.append($("<option>")
							.val("")
							.attr("disabled", true)
							.attr("selected", true)
							.css("display", "none")
							.html("Select a example request &hellip;"));
					editor.setValue("");
					var requests = settings.versions[$(this).val()].requests;
					var $dropdown = $("#input-request");
					$.each(requests, function(i, e) {
						$dropdown.append($("<option>").val(i).html(e.name));
					});
					$dropdown.removeClass("disable").attr("disabled", false);
				}
			}).trigger("change");

			$("#input-request").change(function() {
				var url = settings.versions[$("#input-version").val()].requests[$(this).val()].request;

				var serviceUrl = settings.versions[$("#input-version").val()].requests[$(this).val()].url;
				if (serviceUrl) {
					$("#input-url").val(sosUrl + serviceUrl).trigger("change");
				} else {
					$("#input-url").val(sosUrl).trigger("change");
				}
					
				$.get(url, function(data) {
					var xml = data.xml ? data.xml : new XMLSerializer().serializeToString(data);
					editor.setValue(vkbeautify.xml(xml, 2));
				});
			});
		});
	});
</script>
<jsp:include page="common/footer.jsp" />
