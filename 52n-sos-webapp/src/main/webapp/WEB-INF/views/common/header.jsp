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
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="sos" uri="/WEB-INF/tags/functions.tld" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta http-equiv="Content-Language" content="en" />
		<meta name="author" content="c.autermann@52north.org" />
		<meta name="Date-Creation-yyyymmdd" content="20120306" />
		<meta name="Date-Revision-yyyymmdd" content="20120307" />
		<link href="<c:url value="/static/images/favicon.ico" />" rel="shortcut icon" type="image/x-icon" />		
		<link rel="stylesheet" href="<c:url value="/static/css/52n.css" />" type="text/css" />
		<link rel="stylesheet" href="<c:url value="/static/css/52n.cssmenu.css" />" type="text/css"/>
		<link rel="stylesheet" href="<c:url value="/static/css/bootstrap-2.1.1.min.css" />" type="text/css" />
		<link rel="stylesheet" href="<c:url value="/static/css/application.css" />" type="text/css" />
		<script type="text/javascript" src="<c:url value="/static/js/arrays.js" />"></script>
		<script type="text/javascript" src="<c:url value="/static/js/jquery-1.8.2.min.js" />"></script>
		<script type="text/javascript" src="<c:url value="/static/js/bootstrap-2.1.1.min.js" />"></script>
		<script type="text/javascript" src="<c:url value="/static/js/application.js" />"></script>
		<title>52&deg;North Sensor Observation Service</title>
		
		<c:if test="${not sos:configurated(pageContext.servletContext)}">
			<script type="text/javascript">
				$(function() { 
					showMessage('You first have to complete the installation process! Click <a href="install/index"><strong>here</strong></a> to start it.', "error"); 
				});
			</script>
		</c:if>
	</head>
	<body>
		<div id="wrap">
			<div id="main" class="clearfix">
				<div id="navigation_h">
					<div id="wopper" class="wopper">
						<div id="ja-mainnavwrap">
							<div id="ja-mainnav">
								<ul id="ja-cssmenu" class="clearfix">
									<li>
										<a id="home-menuitem" class="menu-item0" href="<c:url value="/index" />">
											<span class="menu-title">Home</span>
										</a>
									</li>
									<li>
										<a id="client-menuitem" class="menu-item1" href="<c:url value="/client" />">
											<span class="menu-title">Test Client</span>
										</a>
									</li>
									<li>
										<a id="admin-menuitem" class="menu-item2" href="<c:url value="/admin/index" />">
											<span class="menu-title">Admin</span>
										</a>
										<sec:authorize access="hasRole('ROLE_ADMIN')">
											<script type="text/javascript">
												$("#admin-menuitem").addClass("havechild")
											</script>
											<ul>
												<li>
													<a class="first-item"  href="<c:url value="/admin/settings" />">
														<span class="menu-title">Settings</span>
													</a>
												</li>
                                                <li>
													<a href="<c:url value="/admin/logging" />">
														<span class="menu-title">Logging Settings</span>
													</a>
												</li>
												<li>
													<a href="<c:url value="/admin/database" />">
														<span class="menu-title">Database</span>
													</a>
												</li>
												<li>
													<a href="<c:url value="/admin/database/settings" />">
														<span class="menu-title">Database Settings</span>
													</a>
												</li>
												<li>
													<a href="<c:url value="/admin/reset" />">
														<span class="menu-title">Reset</span>
													</a>
												</li>
											</ul>
										</sec:authorize>
									</li>
									<sec:authorize access="hasRole('ROLE_ADMIN')">
										<li style="float: right;">
											<a id="logout-menuitem" class="menu-item3" href="<c:url value="/j_spring_security_logout" />">
												<span class="menu-title">Logout</span>
											</a>
										</li>
									</sec:authorize>
								</ul>                                	
							</div>
						</div>
					</div>
				</div>
				<script type="text/javascript">
					$("#ja-cssmenu li a#${param.activeMenu}-menuitem").addClass("active");
				</script>
				<div class="container">
					<div id="content" class="span12">
