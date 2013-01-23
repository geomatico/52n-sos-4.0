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
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
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

<form id="form" action="">
	<h3 id="top">Service URL</h3>
	<input id="input-url" class="span12" type="text" placeholder="Service URL" value=""/>
	<h3>Request</h3>
	<div class="controls-row">
		<select id="input-version" class="span3"></select>
		<select id="input-binding" class="span3"></select>
		<select id="input-request" class="span6"></select>
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
	var availableBindings = [];
	var availableVersions = [];
	var availableOperations = [];
	<c:forEach items="${bindings}" var="b">
	availableBindings.push("${b}");</c:forEach>
	<c:forEach items="${versions}" var="v">
	availableVersions.push("${v}");</c:forEach>
	<c:forEach items="${operations}" var="o">
	availableOperations.push("${o}");</c:forEach>
</script>

<script type="text/javascript">
	$(function() {
		var $form = $("#form");
		var $version = $("#input-version");
		var $binding = $("#input-binding");
		var $request = $("#input-request");
		var $url = $("#input-url");
		var $send = $("#send-button");

		var editor = CodeMirror.fromTextArea($("#editor").get(0), { 
			"mode": "xml", "lineNumbers": true, "lineWrapping": true
		});

		var version, binding, request;
		
		var sosUrl = document.location.protocol +"//"+ document.location.host + "<c:url value="/sos" />";
		

		function appendDefaultOption(text, $select) {
			$("<option>").attr({ "disabled": true, "selected": true }).hide()
				.val("").html(text).appendTo($select);
		}

		function appendDefaultBindingOption() { appendDefaultOption("Select the request binding &hellip;", $binding); }
		function appendDefaultRequestOption() { appendDefaultOption("Select a example request &hellip;", $request); }
		function appendDefaultVersionOption() { appendDefaultOption("Select the SOS version &hellip;", $version); }

		appendDefaultBindingOption();
		appendDefaultRequestOption();
		appendDefaultVersionOption();
		

		function obj2param(obj) {
			var q = [];
			for (var key in obj)
			   q.push(key + "=" + encodeURIComponent(
				(obj[key] instanceof Array) ? obj[key].join(",") : obj[key]));
			return q.join("&");
		}

		function xml2string(xml) {
			return typeof(xml) === "string" ? xml : xml.xml ? xml.xml : new XMLSerializer().serializeToString(xml); 
		}

		function filterRequests(requests) {
			var filteredRequests = {};
			for (var v in requests) {
				if (availableVersions.contains(v)) {
					filteredRequests[v] = {};
					for (var b in requests[v]) {
						if (availableBindings.contains(b)) {
							filteredRequests[v][b] = {};
							for (var o in requests[v][b]) {
								if (availableOperations.contains(o)) {
									for (var r in requests[v][b][o]) {
										filteredRequests[v][b][r] 
											  = requests[v][b][o][r];
									}
								}
							}
						}
					}
				}
			}
			return filteredRequests;
		}

		function sendInline() {
			return !!$("#input-send-inline").attr("checked");
		}

		function onUrlChange() {
			var url = $url.val();
			$form.attr("action", url);
			if (url) {
				$send.removeAttr("disabled");
			} else {
				$send.attr("disabled", true);
			}
		}

		function onSend() {
			var request = $.trim(editor.getValue());
			if (sendInline()) {
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
					var xml = xml2string(data);
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
			} else {
				if (request) {
					$form.attr("method", "POST").submit();
				} else {
					window.location.href = $url.val();
				}
			}
		}

		
		$binding.attr("disabled", true);
		$request.attr("disabled", true);
		$form.attr("action", sosUrl);
		$url.val(sosUrl).on("change", onUrlChange).trigger("change");
		$send.on("click", onSend);
		$.getJSON("<c:url value="/static/conf/client-requests.json"/>", function(requests) {
			
			requests = filterRequests(requests);

			function onBindingChange() {
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
			}

			function onRequestChange() {
				var oldRequest = request;
				request = $request.val();
				if (request && request != oldRequest) {
					var def = requests[version][binding][request];
					var url = sosUrl + binding;
					if (def.param) {
						if (!url.endsWith("?")) url += "?";
						url += obj2param(def.param);
					}
					$url.val(url).trigger("change");
					if (def.request) {
						$.get(def.request, function(data) {
							var xml = xml2string(data);
							editor.setValue(vkbeautify.xml(xml));
						});
					} else {
						editor.setValue("");
					}
				}
			}

			function onVersionChange() {
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
			}

			for (var key in requests) {
				$("<option>").html(key).appendTo($version);
			}

			$version.on("change", onVersionChange).trigger("change");
			$binding.on("change", onBindingChange).trigger("change");
			$request.on("change", onRequestChange).trigger("change");

			if (availableVersions.length === 0
				|| availableBindings.length === 0
				|| availableOperations.length === 0) {
				editor.setOption("readOnly", true);
				[ $version, $binding, $request, $url, $send ].forEach(function($e) {
					$e.attr("disabled", true);
				});
			}
		});
	});
</script>
<jsp:include page="common/footer.jsp" />
