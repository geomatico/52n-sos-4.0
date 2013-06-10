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
<jsp:include page="common/header.jsp">
    <jsp:param name="activeMenu" value="home" />
</jsp:include>
<jsp:include page="common/logotitle.jsp">
	<jsp:param name="title" value="52&deg;North SOS" />
	<jsp:param name="leadParagraph" value="Open Source Sensor Observation Service" />
</jsp:include>
<hr/>
<p>The 52&deg;North SOS is an open source software implementation of the Open Geospatial Consortium's Sensor Observation Service (SOS) Standard.</p>

<p>From the menu at the top you can access a basic form-based test client and the administrative backend.</p>

<p>More information about the SOS and further software components, for example clients, can be found on the <a href="http://52north.org/communities/sensorweb/" title="52&deg;North Sensor Web Community Website">Sensor Web community homepage</a>.</p>

<br/>

<jsp:include page="common/footer.jsp" />
