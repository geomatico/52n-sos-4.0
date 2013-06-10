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
    <jsp:param name="step" value="2" />
</jsp:include>
<jsp:include page="../common/logotitle.jsp">
    <jsp:param name="title" value="Datasource configuration" />
    <jsp:param name="leadParagraph" value="" />
</jsp:include>

<form action="<c:url value="/install/datasource" />" method="POST" class="form-horizontal">
    <fieldset>
        <div class="control-group">
            <label class="control-label" for="datasource">Datasource</label>
            <div class="controls">
                <select name="datasource" id="datasource">
                    <option disabled="true" selected="true" value="" style="display: none;"></option>
                </select>
                <span class="help-block">Select the datasource you want to use for the SOS.</span>
            </div>
        </div>
    </fieldset>
    <fieldset id="settings"></fieldset>
    <fieldset id="actions" style="display: none;">
        <legend>Actions</legend>
        <div class="control-group" id="create">
            <div class="controls">
                <label class="checkbox">
                    <input type="checkbox" name="create_tables" checked="checked" />
                    <strong>Create tables</strong> &mdash; This will create the necessary tables in the database.
                </label>
            </div>
        </div>
        <div class="control-group" id="overwrite">
            <div class="controls">
                <label class="checkbox">
                    <input type="checkbox" name="overwrite_tables" />
                    <strong>Delete existing tables</strong> &mdash; This will delete all existing tables in the database.
                </label>
                <span style="display: none;" class="help-block"><span class="label label-important">Warning!</span> 
                    This will erase the entire database.</span>
            </div>
        </div>
        <div class="control-group" id="testdata">
            <div class="controls" >
                <label class="checkbox">
                    <input type="checkbox" name="create_test_data" checked="checked" />
                    <strong>Create test data</strong> &mdash; This will insert the dummy data in the tables thus created.
                </label>
            </div>
        </div>
    </fieldset>
    <hr/>
    <div>
        <a href="<c:url value="/install/index" />" class="btn">Back</a>
        <button id="next" type="submit" class="btn btn-info pull-right">Next</button>
    </div>
</form>


<script type="text/javascript">
    warnIfNotHttps();
    $.getJSON("<c:url value="/install/datasource/sources" />", function(datasources) {
        var datasource, selected,
            $datasource = $("#datasource"),
            $settings = $("#settings"),
            $actions = $("#actions"), 
            $create = $("#create"),
            $overwrite = $("#overwrite"),
            $testdata = $("#testdata");

        for (datasource in datasources) {
            if (datasources.hasOwnProperty(datasource)) {
                $datasource.append($("<option>").text(datasource));
                if (datasources[datasource].selected) {
                    selected = datasource;
                }
            }
        }
        datasource = null;
        
        $datasource.change(function() {
            var d = $(this).val();
            if (d !== datasource) {
                datasource = d;
                
                /* create settings */
                $settings.slideUp("fast", function() {
                    $settings.children().remove();
                    generateSettings(datasources[d].settings,
                                     $settings, false);
                    /* save settings as default values
                     * to keep them between switches */
                    $settings.find(":input").on("change", function() {
                        var $this = $(this), name = $this.attr("name"),
                            i, settings = datasources[d]["settings"].sections;
                        for (i = 0; i < settings.length; ++i) {
                            if (settings[i].settings.hasOwnProperty(name)) {
                                settings[i]["settings"][name]["default"] = $this.val();
                            }
                        }
                    });
                    $settings.slideDown("fast");
                });
                
                /* adjust actions */
                $actions.slideUp("fast", function() {
                    var schema = datasources[d].needsSchema;
                    var testdata = datasources[d].supportsTestData;
                    $create.hide(); $overwrite.hide(); $testdata.hide();
                    if (schema) { $create.show(); $overwrite.show(); }
                    if (testdata) { $testdata.show(); }
                    if (testdata || schema) { $actions.slideDown("fast"); }
                });
            }
        });

        $("input[name=overwrite_tables]").click(function(){
            $(this).parent().next().toggle("fast");
        });

        $("input[name=overwrite_tables]").change(function() {
            var $create_tables = $("input[name=create_tables]");
            if ($(this).attr("checked")) {
                $create_tables.attr({ 
                    "checked": "checked", 
                    "disabled": true })
                .parent("label").addClass("muted");
            } else {
                $create_tables.removeAttr("disabled")
                    .parent("label").removeClass("muted");
            }
        });

        if (selected) {
            $datasource.val(selected).trigger("change");
        }
    
    });
</script>

<jsp:include page="../common/footer.jsp" />
