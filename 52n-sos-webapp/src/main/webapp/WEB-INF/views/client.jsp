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
    <jsp:param name="activeMenu" value="client" />
</jsp:include>
<link rel="stylesheet" href="<c:url value="/static/css/prettify.css" />" type="text/css" />
<link rel="stylesheet" href="<c:url value="/static/css/codemirror-2.34.css" />" type="text/css" />
<link rel="stylesheet" href="<c:url value="/static/css/codemirror.custom.css" />" type="text/css" />
<script type="text/javascript" src="<c:url value="/static/js/codemirror-2.34.js" />"></script>
<script type="text/javascript" src="<c:url value="/static/js/codemirror-2.34-xml.js" />"></script>
<script type="text/javascript" src="<c:url value="/static/js/prettify.min.js" />"></script>
<script type="text/javascript" src="<c:url value="/static/js/vkbeautify-0.99.00.beta.js" />"></script>
<jsp:include page="common/logotitle.jsp">
	<jsp:param name="title" value="52&deg;North SOS Test Client" />
	<jsp:param name="leadParagraph" value="Choose a request from the examples or write your own to test the SOS." />
</jsp:include>

<script type="text/javascript">
	/* redirect from "client/"" to "client" */
	if (window.location.pathname.slice(-1) === "/") {
		window.location.href = window.location.href.slice(0,-1);
	}
</script>

<form id="form" action="" method="POST">
	<h3 id="top">Service URL</h3>
	<input id="input-url" class="span12" type="text" placeholder="Service URL" value=""/>
	<h3>Request</h3>
	<div class="controls-row">
		<select id="input-version" class="span3">
			<option value="" disabled selected style="display: none;">Select the SOS version &hellip;</option>
		</select>
		<select id="input-binding" class="span3">
			<option value="" disabled selected style="display: none;">Select the request binding &hellip;</option>
		</select>
		<select id="input-request" class="span6 pull-right">
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
		var availableBindings = [];
		var availableVersions = [];
<c:forEach items="${bindings}" var="b">
		availableBindings.push("${b}");
</c:forEach>
<c:forEach items="${versions}" var="v">
		availableVersions.push("${v}");
</c:forEach>

		var $version = $("#input-version");
		var $binding = $("#input-binding");
		var $request = $("#input-request");
		var $url = $("#input-url");
		var $send = $("#send-button");
		var version, binding;
		var sosUrl = document.location.protocol +"//"+ document.location.host + "<c:url value="/sos" />";

		function appendDefaultBindingOption() {
			$("<option>")
				.val("")
				.attr("disabled", true)
				.attr("selected", true)
				.css("display", "none")
				.html("Select the request binding &hellip;")
				.appendTo($binding)
					
		}
		function appendDefaultRequestOption() {
			$("<option>")
				.val("")
				.attr("disabled", true)
				.attr("selected", true)
				.css("display", "none")
				.html("Select a example request &hellip;")
				.appendTo($request);
		}

		var editor = CodeMirror.fromTextArea($("#editor").get(0), { 
            "mode": "xml", "lineNumbers": true, "lineWrapping": true
        });

		$("#form").attr("action", sosUrl);
		$("#input-request, #input-binding").attr("disabled", true);

		$url.val(sosUrl).change(function() {
			var url = $url.val();
			$("#form").attr("action", url);
			if (url) {
				$send.removeAttr("disabled");
			} else {
				$send.attr("disabled", true);
			}
		});

		$("#send-button").click(function() {
			var request = $.trim(editor.getValue());
			if (!$("#input-send-inline").attr("checked")) {
				if (request) {
					$("#form").submit();
				} else {
					window.location.href = $url.val();
				}
			} else {
				$send.attr("disabled", true);
				$.ajax($url.val(), {
					"type": (request) ? "POST" : "GET",
					"contentType": "application/xml",
					"accepts": "application/xml",
					"data": request
				}).fail(function(error) {
					showError("Request failed: " + error.status + " " + error.statusText);
					$send.attr("disabled", false);
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
					$send.removeAttr("disabled");
				});
			}
		});

		$.getJSON("<c:url value="/static/conf/client-requests.json"/>", function(requests) {

			for (var v in requests) {
				if (!availableVersions.contains(v)) {
					delete requests[v];
				} else {
					for (var b in requests[v]) {
						if (!availableBindings.contains(b)) {
							delete requests[v][b];
						}
					}
				}

			}
			for (var key in requests) {
				$("<option>").html(key).appendTo($version);
			}

			$version.change(function() {
				var oldVersion = version;
				version = $version.val();
				if (version && version != oldVersion) {
					$binding.children("option").remove();
					appendDefaultBindingOption();
					appendDefaultRequestOption();

					editor.setValue("");
					for (var key in requests[version]) {
						$("<option>").html(key).appendTo($binding);
					}
					$binding.removeAttr("disabled");
					$request.attr("disabled", true);
					binding = null;
				}
			}).trigger("change");

			$binding.change(function() {
				var oldBinding = binding;
				binding = $binding.val();
				if (binding && binding != oldBinding) {
					$request.children("option").remove()
					editor.setValue("");
					appendDefaultRequestOption();
					for (var key in requests[version][binding]) {
						$("<option>").html(key).appendTo($request);
					}
					var url = sosUrl + binding;
					$url.val(url).trigger("change");
					$request.removeAttr("disabled");
				}
			});

			$request.change(function() {
				var def = requests[version][binding][$request.val()];
				var url = sosUrl + binding;
				if (def.url) url += def.url;
				$url.val(url).trigger("change");
				if (def.request) {
					$.get(def.request, function(data) {
						var xml = data.xml ? data.xml : new XMLSerializer().serializeToString(data);
						editor.setValue(xml);
					});
				}
			});

			if (availableVersions.length === 0) {
				editor.setOption("readOnly", true);
				[ $version, $binding, $request, $url, $send ].forEach(function($e) {
					$e.attr("disabled", true);
				});
			}
		});
	});
</script>
<jsp:include page="common/footer.jsp" />
