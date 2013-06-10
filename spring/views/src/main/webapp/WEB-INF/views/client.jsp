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
<link rel="stylesheet" href="<c:url value="/static/lib/prettify.css" />" type="text/css" />
<link rel="stylesheet" href="<c:url value="/static/lib/codemirror-2.34.css" />" type="text/css" />
<link rel="stylesheet" href="<c:url value="/static/css/codemirror.custom.css" />" type="text/css" />
<script type="text/javascript" src="<c:url value="/static/lib/codemirror-2.34.js" />"></script>
<script type="text/javascript" src="<c:url value="/static/lib/codemirror-2.34-xml.js" />"></script>
<script type="text/javascript" src="<c:url value="/static/lib/prettify.min.js" />"></script>
<script type="text/javascript" src="<c:url value="/static/lib/vkbeautify-0.99.00.beta.js" />"></script>
<jsp:include page="common/logotitle.jsp">
    <jsp:param name="title" value="52&deg;North SOS Test Client" />
    <jsp:param name="leadParagraph" value="Choose a request from the examples or write your own to test the SOS." />
</jsp:include>

<script type="text/javascript">
    /* redirect from "client/"" to "client" */
    if (window.location.pathname.slice(-1) === "/") {
        window.location.href = window.location.href.slice(0, -1);
    }
</script>

<form id="form" action="">
    <h3 id="top">Service URL</h3>
    <input id="input-url" class="span12" type="text" placeholder="Service URL" value=""/>
    <h3>Request</h3>
    <div class="controls-row">
        <select id="input-service" class="span2"></select>
        <select id="input-version" class="span2"></select>
        <select id="input-binding" class="span2"></select>
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
    var availableOperations = {};
    // Map<String, Map<Stirng, Set<String>>
    <c:forEach items="${operations}" var="e1">
    availableOperations["${e1.key}"] = {};
        <c:forEach items="${e1.value}" var="e2">
    availableOperations["${e1.key}"]["${e2.key}"] = [];
            <c:forEach items="${e2.value}" var="e3">
    availableOperations["${e1.key}"]["${e2.key}"].push("${e3}");
            </c:forEach>
        </c:forEach>
    </c:forEach>

    <c:forEach items="${bindings}" var="b">
        availableBindings.push("${b}");
    </c:forEach>
</script>

<script type="text/javascript">
    $(function() {
        var $form = $("#form"),
                $service = $("#input-service"),
                $version = $("#input-version"),
                $binding = $("#input-binding"),
                $request = $("#input-request"),
                $url = $("#input-url"),
                $send = $("#send-button"),
                editor = CodeMirror.fromTextArea($("#editor").get(0), {
            "mode": "xml", "lineNumbers": true, "lineWrapping": true
        }),
        version, binding, request, service,
                sosUrl = document.location.protocol + "//" + document.location.host + "<c:url value="/sos" />";


        function appendDefaultOption(text, $select) {
            $("<option>").attr({"disabled": true, "selected": true}).hide()
                    .val("").html(text).appendTo($select);
        }

        function appendDefaultServiceOption() {
            appendDefaultOption("Service &hellip;", $service);
        }
        function appendDefaultBindingOption() {
            appendDefaultOption("Binding &hellip;", $binding);
        }
        function appendDefaultVersionOption() {
            appendDefaultOption("Version &hellip;", $version);
        }
        function appendDefaultRequestOption() {
            appendDefaultOption("Example request &hellip;", $request);
        }

        appendDefaultServiceOption();
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
            for (var s in requests) {
                if (availableOperations[s]) {
                    filteredRequests[s] = {};
                    for (var v in requests[s]) {
                        if (availableOperations[s][v]) {
                            filteredRequests[s][v] = {};
                            for (var b in requests[s][v]) {
                                if (availableBindings.contains(b)) {
                                    filteredRequests[s][v][b] = {};
                                    for (var o in requests[s][v][b]) {
                                        if (availableOperations[s][v].contains(o)) {
                                            for (var r in requests[s][v][b][o]) {
                                                filteredRequests[s][v][b][r]
                                                        = requests[s][v][b][o][r];
                                            }
                                        }
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
                    type: (request) ? "POST" : "GET",
                    contentType: "application/xml",
                    accepts: "application/xml",
                    data: request,
                    complete: function(xhr, status) {
                        function showResponse(xhr) {
                            var xml = xml2string(xhr.responseText);
                            var $response = $("#response");
                            $("<h3>")
                                    .text("Response")
                                    .appendTo($response);
                            $("<pre>")
                                    .text((xhr.status + " " + xhr.statusText + "\n"
                                    + xhr.getAllResponseHeaders()).trim())
                                    .appendTo($response);
                            $("<pre>")
                                    .addClass("prettyprint")
                                    .addClass("linenums")
                                    .text(xml)
                                    .appendTo($response);
                            prettyPrint();
                            $response.fadeIn("fast");
                            $("html, body").animate({
                                scrollTop: $("#response").offset().top
                            }, "slow");
                        }
                        $("#response").fadeOut("fast").children().remove();
                        switch (status) {
                            case "success":
                                showResponse(xhr);
                                break;
                            case "notmodified":
                                showResponse(xhr);
                                break;
                            case "error":
                                showError("Request failed: " + xhr.status + " " + xhr.statusText);
                                if (xhr.responseText && xhr.responseText.indexOf("ExceptionReport") >= 0) {
                                    showResponse(xhr);
                                }
                                break;
                            case "timeout":
                                showError("Request timed out &hellip;");
                                break;
                            case "abort":
                                showError("Request aborted &hellip;");
                                break;
                            case "parsererror":
                                showError("Unparsable response &hellip;");
                                break;
                        }
                        $send.removeAttr("disabled");
                    }
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
        $version.attr("disabled", true);
        $form.attr("action", sosUrl);
        $url.val(sosUrl).on("change", onUrlChange).trigger("change");
        $send.on("click", onSend);
        $.getJSON("<c:url value="/static/conf/client-requests.json"/>", function(requests) {

            requests = filterRequests(requests);

            $version.on("change", function() {
                var oldVersion = version;
                version = $version.val();
                if (version && version !== oldVersion) {
                    $binding.children("option").remove();
                    appendDefaultBindingOption();
                    appendDefaultRequestOption();

                    editor.setValue("");
                    for (var key in requests[service][version]) {
                        $("<option>").html(key).appendTo($binding);
                    }
                    $binding.removeAttr("disabled");
                    $request.attr("disabled", true);
                    binding = null;
                }
            }).trigger("change");

            $binding.on("change", function() {
                var oldBinding = binding;
                binding = $binding.val();
                if (binding && binding !== oldBinding) {
                    $request.children("option").remove();
                    editor.setValue("");
                    appendDefaultRequestOption();
                    for (var key in requests[service][version][binding]) {
                        $("<option>").text(key).appendTo($request);
                    }
                    var url = sosUrl + binding;
                    $url.val(url).trigger("change");
                    $request.removeAttr("disabled");
                }
            }).trigger("change");

            $request.on("change", function() {
                var oldRequest = request;
                request = $request.val();
                if (request && request !== oldRequest) {
                    var def = requests[service][version][binding][request];
                    var url = sosUrl + binding;
                    if (def.param) {
                        if (!url.endsWith("?"))
                            url += "?";
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
            }).trigger("change");

            $service.on("change", function() {
                var oldService = service;
                service = $service.val();
                if (service && service !== oldService) {
                    $version.children("option").remove();
                    appendDefaultVersionOption();
                    appendDefaultBindingOption();
                    appendDefaultRequestOption();
                    editor.setValue("");
                    for (var key in requests[service]) {
                        $("<option>").text(key).appendTo($version);
                    }
                    $version.removeAttr("disabled");
                    $binding.attr("disabled", true);
                    $request.attr("disabled", true);
                }
            }).trigger("change");

            for (var s in requests) {
                $service.append($("<option>").text(s));
            }

            if (availableBindings.length === 0 || !availableOperations) {
                editor.setOption("readOnly", true);
                [$service, $version, $binding, $request, $url, $send].forEach(function($e) {
                    $e.attr("disabled", true);
                });
            } else {
                if (requests["SOS"]) {
                    $service.val("SOS").trigger("change");
                    var v;
                    if (requests["SOS"]["2.0.0"]) {
                        v = "2.0.0";
                    } else if (requests["SOS"]["1.0.0"]) {
                        v = "1.0.0";
                    }
                    if (v) {
                        $version.val(v).trigger("change");
                        if (requests["SOS"][v]["/kvp"]) {
                            $binding.val("/kvp").trigger("change");
                        } else if (requests["SOS"][v]["/soap"]) {
                            $binding.val("/soap").trigger("change");
                        } else if (requests["SOS"][v]["/pox"]) {
                            $binding.val("/pox").trigger("change");
                        }
                    }
                }
                
                $version.val("2.0.0").trigger("change");
                $binding.val("/kvp").trigger("change");
            }

        });
    });
</script>
<jsp:include page="common/footer.jsp" />
