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
<fieldset id="database-configuration">
    <legend>Database Configuration</legend>
    <div class="control-group">
        <label class="control-label" for="user-input">Database User Name</label>
        <div class="controls">
            <input class="jdbccomponent span8" type="text" id="user-input">
                <span class="help-block"><span class="label label-warning">required</span> Your database server user name. The default value for PostgreSQL is "postgres".</span>
            </input>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label" for="pass-input">Database Password</label>
        <div class="controls">
            <input class="jdbccomponent span8" type="text" id="pass-input">
                <span class="help-block"><span class="label label-warning">required</span> Your database server password. The default value is "postgres".</span>
            </input>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label" for="db-input">Database Name</label>
        <div class="controls">
            <input class="jdbccomponent span8" type="text" id="db-input">
                <span class="help-block"><span class="label label-warning">required</span> Set this to the name of the database you want to use for SOS.</span>
            </input>
        </div>
    </div>
    <input class="jdbccomponent" type="hidden" id="scheme-input" value="postgresql"/>
</fieldset>
