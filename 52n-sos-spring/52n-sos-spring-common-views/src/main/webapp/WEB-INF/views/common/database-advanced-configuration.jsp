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
<fieldset id="database-advanced-configuration">
    <legend>Advanced database server configuration</legend>
    <div class="control-group">
        <label class="control-label" for="schema">Schema</label>
        <div class="controls">
            <input type="text" class="span8" id="schema" name="schema">
                <span class="help-block"><span class="label label-info">optional</span> Qualifies unqualified table names with the given schema in generated SQL.</span>
            </input>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label" for="driver">Database Driver</label>
        <div class="controls">
            <input type="text" class="span8" id="driver" name="driver">
                <span class="help-block"><span class="label label-warning">required</span> Set this to the class of the JDBC driver. The default value for PostgreSQL driver is "org.postgresql.Driver".</span>
            </input>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label" for="connection_pool">Connection Pool</label>
        <div class="controls">
            <input type="text" class="span8" id="driver" name="connection_pool">
            <span class="help-block"><span class="label label-warning">required</span> Set this to the class of the connection pool. The default value is the <a href="http://www.mchange.com/projects/c3p0/" title="C3P0">C3P0</a> connection pool.</span>
            </input>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label" for="dialect">Database Dialect</label>
        <div class="controls">
            <input type="text" class="span8" id="driver" name="dialect">
                <span class="help-block"><span class="label label-warning">required</span> Set this to the class of the JDBC dialect. The default value for PostgreSQL is the custom 52&deg;North dialect.</span>
            </input>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label" for="jdbc-input">Connection String</label>
        <div class="controls">
            <div class="input-prepend">
                <span id="jdbc-input-addon"class="add-on">jdbc:postgresql://</span>
                <input id="jdbc-input" type="text" />
            </div>
            <input type="hidden" name="jdbc_uri" />
            <span style="margin-top:10px;"class="help-block"><span class="label label-info">optional</span> The connection string that will be used to connect with your database.</span>
        </div>
    </div>
</fieldset>
