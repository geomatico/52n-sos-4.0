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
<%--
    XXX keep in sync with Ann and http://52north.org/about/get-involved/
--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sos" uri="http://52north.org/communities/sensorweb/sos/tags" %>
<jsp:include page="common/header.jsp">
    <jsp:param name="activeMenu" value="home" />
</jsp:include>
<jsp:include page="common/logotitle.jsp">
	<jsp:param name="title" value="Get Involved!" />
	<jsp:param name="leadParagraph" value="There are various ways to get involved with the 52&deg;North Initiative. What are your interests? (If you want to get involved in open source, but don't know how to start, you might want to read this <a href='http://blog.smartbear.com/software-quality/bid/167051/14-Ways-to-Contribute-to-Open-Source-without-Being-a-Programming-Genius-or-a-Rock-Star'>blog entry</a>.)" />
</jsp:include>
<hr/>

<h4>Tell the world!</h4>
<p>If your SOS instance is publically available please consider informing <a class="telltheworld" href="mailto:sos-installation@52north.org">us</a> or the community on <a class="telltheworld" href="mailto:sensorweb@52north.org" target="_blank">sensorweb@52north.org</a> or use our <a href="http://sensorweb.forum.52north.org/">forums</a> so that many people can profit from your hard work. If you like, your SOS can also be listed in our <a href="https://wiki.52north.org/bin/view/SensorWeb/SosExampleServices">Wiki</a> as an example.</p>

<h4>Code development/ bug fixing</h4>
<p>The best way to get to know a software is to use it! Implement features that you yourself need - new software, code snippets, adaptations, extensions, etc.  Help others solve their problems. Fix bugs and enhance the existing software. There are many ways to get started!</p>
<p>Contact the <a href="http://52north.org/communities/community-leaders/">community leader</a> who is responsible for the software to which you would like to contribute. If you are interested in providing code, you will need to sign our <a href="http://52north.org/images/stories/52nadmin/files/52north%20contributor%20license%20agreement.pdf">contributors license agreement (CLA)</a>. If you have questions about this or our software licensing, please check our <a href="http://52north.org/about/licensing/">licensing pages</a>.</p>

<h4>Testing / bug reporting</h4>
<p>The development of high quality software demands a lot of testing. Run the latest development code and provide feedback on changes as they occur. Report errors or possible improvements. The best way to test 52&deg;North's development code is to use the <a href="https://bugzilla.52north.org/">52&deg;North bugzilla bugtracker</a>. This requires an account. Simply sign up with a legitimate email address. We also have various <a href="http://52north.org/resources/mailing-list-and-forums/">mailing lists/forums</a> in which you can also report bugs.</p>

<h4>Documentation</h4>
<p>Help produce official software documentation, i.e. user guides, tutorials, how tos, FAQs, etc. Document a solution to a problem. Check, proof and test documents for accuracy. Contact the <a href="http://52north.org/communities/community-leaders/">community leader</a> who is responsible for the software documentation in question.</p>

<h4>Discuss and Support</h4>
<p>Join or start discussions about new feature ideas, help answer questions in our <a href="http://52north.org/resources/mailing-list-and-forums/">mailings lists and forums</a>.</p>

<h4>Partnership</h4>
<p>Make your commitment official and become a <a href="http://52north.org/about/get-involved/partnership-levels">52&deg;North cooperation partner!</a></p>

<br/>

<h4>Thanks for using the 52&deg;North SOS and thank you for your support of the Sensor Web community!</h4>

<br/>

<script type="text/javascript">
	$(function() {
        var serviceUrl = "${serviceUrl}" ? "${serviceUrl}" 
                : document.location.protocol + "//" 
                + document.location.host + "<c:url value="/sos" />",
            subject = "I've installed the 52N SOS",
            body = "Check out my installed SOS instance at " + serviceUrl;
        $(".telltheworld").each(function() {
            $(this).attr("href", $(this).attr("href") 
                + "?subject=" + encodeURIComponent(subject) 
                + "&body=" + encodeURIComponent(body));
        })
		if ($.queryParam["install"] === "finished")  {
			window.setTimeout(function() {
				showSuccess("Installation completed!<c:if test="${sos:hasClient()}"><a href='<c:url value="/client"/>'>Test it.</a></c:if>");
			}, 1000);
		}	
    });
</script>

<jsp:include page="common/footer.jsp" />
