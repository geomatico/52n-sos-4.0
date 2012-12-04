/*
 * Copyright (C) 2012
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
if (typeof String.prototype.startsWith != "function") {
    String.prototype.startsWith = function(str) {
        "use strict";
        if (this == null) throw new TypeError();
        return this.slice(0, str.length) === str;
    };
}

if (typeof String.prototype.endsWith != "function") {
    String.prototype.endsWith = function(str) {
        "use strict";
        if (this == null) throw new TypeError();
        return this.slice(-str.length) === str;
    };
}

if (!String.prototype.matches) {
    String.prototype.matches = function(regexp) {
        "use strict";
        if (this == null) throw new TypeError();
        return this.match(regexp) ? true : false;
    };
}

(function($) {
    $.queryParam = (function(a) {
        if (a === "") {
            return {};
        }
        var b = {};
        for (var i = 0; i < a.length; ++i) {
            var p = a[i].split('=');
            if (p.length != 2) continue;
            b[p[0]] = decodeURIComponent(p[1].replace(/\+/g, " "));
        }
        return b;
    })(window.location.search.substr(1).split('&'));
})(jQuery);

function showMessage(text, type, autoclose) {
    function closeAlert(a) {
        a.fadeTo(500, 0).slideUp(500, function() {
            a.remove();
        });
    }
    var $alert = $("<div>");
    $alert.addClass("alert alert-" + type).append(text);
    $("<button>").attr("type", "button").addClass("close").click(function() {
        closeAlert($alert);
    }).html("&times;").prependTo($alert);
    $alert.hide().prependTo($("#content")).css("opacity", 0).slideDown(500).animate({
        opacity: 1
    }, {
        queue: false,
        duration: 1000
    });
    if (autoclose) {
        window.setTimeout(function() {
            closeAlert($alert);
        }, (typeof(autoclose) == "number") ? autoclose : 5000);
    }
}

function showError(error, autoclose) {
	if (autoclose === undefined) {
		autoclose = true; 
	}
    showMessage("<strong>Error!</strong> " + error, "error", autoclose);
}

function showSuccess(message) {
    showMessage("<strong>Success!</strong> " + message, "success", true);
}

function generateSettings(settings, container, tabbed) {
    function required() {
        var valid = $(this).val() === "";
        if (valid) {
            $(this).parents(".control-group").addClass("error");
        } else {
            $(this).parents(".control-group").removeClass("error");
        }
    }

    function generateSetting(setting) {
        var $setting = $("<div>").addClass("control-group");
        switch (setting.type) {
        case "integer":
        case "password":
        case "text":
        case "string":
            var $label = $("<label>").addClass("control-label").attr("for", setting.id).html(setting.title);
            var $controls = $("<div>").addClass("controls");
            var $input = null;
            switch (setting.type) {
            case "integer":
                // TODO slider
            case "string":
                $input = $("<input>").attr("type", "text").attr("name", setting.id).addClass("span8");
                break;
            case "password":
                $input = $("<input>").attr("type", "password").attr("name", setting.id).addClass("span8");
                break;
            case "text":
                $input = $("<textarea>").attr("rows", 5) // TODO make this a setting
                .attr("name", setting.id).addClass("span8");
                break;
            }
            if (setting["default"]) {
                $input.val(setting["default"]);
            }
            var $description = $("<span>").addClass("help-block").html(setting.description);
            if (setting.required) {
                var $required = $("<span>").addClass("label label-warning").text("required");
                $description.prepend(" ").prepend($required);
                $input.bind("keyup input", required);
                $input.addClass("required");
            } else {
                var $optional = $("<span>").addClass("label label-info").text("optional");
                $description.prepend(" ").prepend($optional);
            }
            $setting.append($label).append($controls.append($input).append($description));
            break;
        case "choice":
            var $controls = $("<div>").addClass("controls");
            var $label = $("<label>").attr("for", setting.id).addClass("control-label").text(setting.title);
            var $input = $("<select>").attr("name", setting.id).addClass("span8");
            $.each(setting.options, function(val, desc) {
                $("<option>").attr("value", val).text(desc).appendTo($input);
            });
            if (!setting["default"]) {
                var $option = $("<option>").attr("value", "").attr("selected", true);
                $input.prepend($option);
                if (setting.required) {
                    $input.addClass("required");
                    $option.attr("disabled", true).css("display", "none");
                } else {
                    var $optional = $("<span>").addClass("label label-info").text("optional");
                    $description.prepend(" ").prepend($optional);
                }
            } else {
                $input.val(setting["default"]);
            }
            var $description = $("<span>").addClass("help-block").html(setting.description);
            if (setting.required) {
                var $required = $("<span>").addClass("label label-warning").text("required");
                $description.prepend(" ").prepend($required);
                $input.bind("change", required);
            }
            $setting.append($label).append($controls.append($input).append($description));
            break;
        case "boolean":
            var $controls = $("<div>").addClass("controls");
            var $input = $("<input>").attr("type", "checkbox").attr("name", setting.id);
            var $label = $("<label>").attr("for", setting.id).addClass("checkbox").text(setting.title);
            var $description = $("<span>").addClass("help-block").html(setting.description);
            $setting.append($label).append($controls.append($label.prepend($input)).append($description));
            if ((typeof setting["default"]) === "boolean") {
                $input.attr("checked", setting["default"]);
            }
            break;
        }
        return $setting;
    }

    function generateTabbedSection(section, $tabTitles, $tabs) {
        if (!section.title) {
            return;
        } /* generate the tab title */
        section.id = section.title.toLowerCase().replace(/\W/g, "_");
        var $tabHead = $("<li>").append($("<a>").text(section.title).attr("href", "#" + section.id).attr("data-toggle", "tab")); /* generate the tab pane */
        var $tabPane = $("<div>").addClass("tab-pane").attr("id", section.id);
        if (section.description) {
            $("<p>").html(section.description).appendTo($tabPane);
        }
        $.each(section.settings, function(id, setting) {
            setting.id = id;
            $tabPane.append(generateSetting(setting));
        });
        $tabs.append($tabPane);
        $tabTitles.append($tabHead);

    }

    function generateSection(section, $container) {
        if (!section.title) {
            return;
        }
        $("<legend>").text(section.title).appendTo($container);
        if (section.description) {
            $("<p>").html(section.description).appendTo($container);
        }
        $.each(section.settings, function(id, setting) {
            setting.id = id;
            $container.append(generateSetting(setting));
        });
    }
    var $container = $(container);
    if (tabbed) {
        var $tabTitles = $("<ul>").addClass("nav nav-tabs");
        var $tabs = $("<div>").addClass("tab-content");
        $.each(settings.sections, function(_, section) {
            generateTabbedSection(section, $tabTitles, $tabs);
        });
        $tabs.children(":first").addClass("active");
        $tabTitles.children(":first").addClass("active");
        $container.append($tabTitles).append($tabs);
    } else {
        $.each(settings.sections, function(_, section) {
            generateSection(section, $container);
        });
    }
    $container.find("input[type=text],input[type=password],textarea").trigger("input");
    $container.find("select").trigger("change");
}

function setSetting(id, val, settings) {
    for (var section in settings.sections) {
        for (var setting in settings.sections[section].settings) {
            if (setting === id) {
                switch (settings.sections[section].settings[setting].type) {
                case "integer":
                case "string":
                case "password":
                    $("input[name=" + setting + "]").val(val);
                    break;
                case "text":
                    $("textarea[name=" + setting + "]").val(val);
                    break;
                case "choice":
                    $("select[name=" + setting + "]").val(val);
                    break;
                case "boolean":
                    if (val === "true" || val === true) {
                        $("select[name=" + setting + "]").attr("checked", true);
                    } else {
                        $("select[name=" + setting + "]").removeAttr("checked");
                    }
                    
                    break;
                }
                return;
            }
        }
    }
}
