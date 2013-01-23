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
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
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
		<link rel="stylesheet" href="<c:url value="/static/css/52n.cssmenu.no-hover.css" />" type="text/css"/>
		<link rel="stylesheet" href="<c:url value="/static/css/bootstrap-2.1.1.min.css" />" type="text/css" />
		<link rel="stylesheet" href="<c:url value="/static/css/application.css" />" type="text/css" />
		<style type="text/css">
			#ja-cssmenu li { 
				cursor: default !important; 
			}
		</style>
		<script type="text/javascript" src="<c:url value="/static/js/jquery-1.8.2.min.js" />"></script>
		<script type="text/javascript" src="<c:url value="/static/js/bootstrap-2.1.1.min.js" />"></script>
		<script type="text/javascript" src="<c:url value="/static/js/application.js" />"></script>
		<title>52&deg;North SOS installer</title>
	</head>
	<body>
		<div id="wrap">
			<div id="main" class="cleafix">
			<div id="navigation_h">
				<div id="wopper" class="wopper">
					<div id="ja-mainnavwrap">
						<div id="ja-mainnav">
							<ul id="ja-cssmenu" class="clearfix">
								<li><a><span class="menu-title">Welcome</span></a></li>
								<li><a><span class="menu-title">Database Configuration</span></a></li> 
								<li><a><span class="menu-title">Optional Settings</span></a></li>
								<li><a><span class="menu-title">Finish</span></a></li>
							</ul>                                	
						</div>
					</div>
				</div>
			</div>
			<script type="text/javascript">
				$("#ja-cssmenu li:nth-child(${param.step}) a")
					.addClass("active");
				$("#ja-cssmenu li,#ja-cssmenu a,#ja-cssmenu span").hover(function(e){
					e.preventDefault();
				});
				$(function(){
					/* put warnings on empty fields */
					$("input[type=text], input[type=password], textarea").bind("keyup input", function() {
						var $this = $(this); 
						if ($this.val() === "") {
							$this.parents(".control-group").addClass("warning")
						} else {
							$this.parents(".control-group").removeClass("warning")
						}
					}).trigger("input");
				})
			</script>
			<div class="container">  
				<div id="content" class="span12">
					<c:if test="${not empty sessionScope.error}">
						<div class="alert alert-error">
							<strong>Error!</strong> ${fn:escapeXml(sessionScope.error)}
						</div>
						<c:remove var="error" scope="session" />
					</c:if>
