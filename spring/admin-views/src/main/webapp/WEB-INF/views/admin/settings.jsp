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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="activeMenu" value="admin" />
</jsp:include>

<div class="row">
    <div class="span9">
        <h2>Change SOS Configuration</h2>
        <p class="lead">You can change the current SOS settings or export the settings to back them up and use them in another installation.</p>
    </div>
    <div class="span3 header-img-span">
        <div class="row">
            <div class="span3">
                <img src="<c:url value="/static/images/52n-logo-220x80.png"/>" />    
            </div>
        </div>
        <div class="row">
            <div class="span3">
                <a id="export" class="btn btn-block btn-info" href="settings.json" target="_blank">Export Settings</a>    
            </div>
        </div>
    </div>
</div>

<form id="settings" class="form-horizontal"></form>
<script type="text/javascript">
    function overwriteDefaultSettings(settings) {
    <c:if test="${not empty settings}">
        <c:set var="search" value='"' />
        <c:set var="replace" value='\\"' />
        <c:forEach items="${settings}" var="entry">
            <c:if test="${not empty entry.value}">
                setSetting("${entry.key}","${fn:replace(entry.value, search, replace)}", settings);
            </c:if>
        </c:forEach>
    </c:if>
    }
</script>

<script type="text/javascript">
    $(function(){
        $.getJSON('<c:url value="/settingDefinitions.json" />', function(settings) {
            var $container = $("#settings"),
                $button = $("<button>").attr("type", "button").addClass("btn btn-info").text("Save");
            
            $button.click(function() {
                $.post('<c:url value="/admin/settings" />', $container.serializeArray())
                .fail(function(e) {
                    showError("Failed to save settings: " + e.status + " " + e.statusText);
                    $("input#admin_password_facade,input[name=admin_password],input[name=current_password]").val("");
                })
                .done(function() {
                    $("html,body").animate({ "scrollTop": 0}, "fast");
                    showSuccess("Settings saved!");
                    $("input#admin_password_facade,input[name=admin_password],input[name=current_password]").val("");
                });
            });

            settings.sections.push({
                "id": "credentials",
                "title": "Credentials",
                "settings": {
                    "admin_username": {
                        "type": "string",
                        "title": "Admin name",
                        "description": "The new administrator user name.",
                        "required": true,
                        "default": "${admin_username}"
                    },
					"current_password": {
						"type": "password",
						"title": "Current Password",
						"description": "The current administrator password."
					},
                    "admin_password_facade": {
                        "type": "string",
                        "title": "New Password",
                        "description": "The new administrator password."        
                    }
                }
            });
            generateSettings(settings, $container, true);
            $("#service_identification .control-group:first").before("<legend>Standard Settings</legend>");
            $("#service_provider .control-group:first").before("<legend>Standard Settings</legend>");
            $("#service_identification .control-group:last").before("<legend>Extended Settings</legend>");
            $("#service_provider .control-group:last").before("<legend>Extended Settings</legend>");
            $("<div>").addClass("form-actions").append($button).appendTo($container);
            
            function setSosUrl() {
                $("input[name='service.sosUrl']").val(window.location.toString()
                    .replace(/admin\/settings.*/, "sos")).trigger("input");    
            }
            setSosUrl();
            

            $(".required").bind("keyup input change", function() {
                var valid = true;
                $(".required").each(function(){ 
                    var val = $(this).val();
                    return valid = (val !== null && val !== undefined && val !== "");
                });
                if (valid) {
                    $button.removeAttr("disabled");
                } else {
                    $button.attr("disabled", true);
                }
            });
            
            overwriteDefaultSettings(settings);

            $(".required:first").trigger("change");
			
			$("input[name=admin_password_facade]").removeAttr("name").attr("id","admin_password_facade");
			$("form#settings").append($("<input>").attr({ "type":"hidden", "name": "admin_password" }));
			$("input#admin_password_facade").bind('focus', function() {
				$(this).val($("input[name=admin_password]").val());
			}).bind('blur', function() {
				$(this).val($(this).val().replace(/./g, String.fromCharCode(8226)));
			}).bind("keyup input", function() {
				$("input[name=admin_password]").val($(this).val());
			});

            var $defaultButton = $("<button>").attr("type", "button")
                .attr("disabled", true).css("margin-left", "5px").addClass("btn")
                .text("Defaults").click(function() {
                function getSettings(section) {
                    for (var i = 0; i < settings.sections.length; ++i) {
                        if (settings.sections[i].title === section) {
                            return settings.sections[i].settings;
                        }
                    }
                }
                var activeId = $(".tab-pane.active").attr("id");
                var section = $(".nav.nav-tabs li a[href=#" + activeId + "]").text();
                var s = getSettings(section);
                for (var key in s) {
                    if (key === "service.sosUrl") {
                        setSosUrl();
                    } else {
                        setSetting(key, (s[key]["default"] !== undefined 
                            && s[key]["default"] !== null) ? s[key]["default"] : "", settings);    
                    }
                }
                $(".required").trigger("input").trigger("change");
            });
            $("div.form-actions").append($defaultButton);

            $('a[data-toggle=tab]').on('shown', function (e) {
                var id = $(e.target).attr("href");
                if (id === "#service_settings" || id === "#miscellaneous_settings") {
                    $defaultButton.removeAttr("disabled");
                } else {
                    $defaultButton.attr("disabled", true);
                }
            });

            parsehash();
        });
    });
</script>
<br/>
<jsp:include page="../common/footer.jsp" />
