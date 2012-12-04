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
<jsp:include page="../common/header.jsp">
    <jsp:param name="activeMenu" value="admin" />
</jsp:include>
<jsp:include page="../common/logotitle.jsp">
	<jsp:param name="title" value="Change SOS Configuration" />
	<jsp:param name="leadParagraph" value="You can download the current configuration <a href=\"settings.json\" target=\"_blank\">here</a> to backup or use it for a new SOS installation." />
</jsp:include>

<form id="settings" class="form-horizontal"></form>
<script type="text/javascript">
    function overwriteDefaultSettings(settings) {
    <c:forEach items="<%=org.n52.sos.service.Setting.getNames()%>" var="setting">
        <c:if test="${not empty requestScope[setting]}">
            setSetting("${setting}","${requestScope[setting]}", settings);
        </c:if>
    </c:forEach>
    }
</script>
<script type="text/javascript">
    $(function(){
        $.getJSON('<c:url value="/static/conf/sos-settings.json" />', function(settings) {
            var $container = $("#settings");
			
			
            var $button = $("<button>").attr("type", "button")
                .addClass("btn btn-info").text("Save").click(function() {
                    $.post("<c:url value="/admin/settings" />", $container.serializeArray())
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
                        "default": "admin"
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

            $("<div>").addClass("form-actions").append($button).appendTo($container);
            function setSosUrl() {
                $("input[name=SOS_URL]").val(window.location.toString()
                    .replace(/admin\/settings.*/, "sos")).trigger("input");    
            }
            setSosUrl();
            

            $(".required").bind("keyup input change", function() {
                var valid = true;
                $(".required").each(function(){ 
                    var val = $(this).val();
                    return valid = (val != null && val != undefined && val != "");
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
                        if (settings.sections[i].title == section) {
                            return settings.sections[i].settings;
                        }
                    }
                }
                var activeId = $(".tab-pane.active").attr("id")
                var section = $(".nav.nav-tabs li a[href=#" + activeId + "]").text();
                var s = getSettings(section);
                for (var key in s) {
                    if (key === "SOS_URL") {
                        setSosUrl();
                    } else {
                        setSetting(key, (s[key]["default"] !== undefined) ? s[key]["default"] : "", settings);    
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
            })
        });
    });
</script>
<br/>
<jsp:include page="../common/footer.jsp" />
