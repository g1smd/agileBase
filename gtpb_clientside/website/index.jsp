<!--  
##  Copyright 2010 GT webMarque Ltd
## 
##  This file is part of agileBase.
##
##  agileBase is free software: you can redistribute it and/or modify
##  it under the terms of the GNU General Public License as published by
##  the Free Software Foundation, either version 3 of the License, or
##  (at your option) any later version.
##  agileBase is distributed in the hope that it will be useful,
##  but WITHOUT ANY WARRANTY; without even the implied warranty of
##  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
##  GNU General Public License for more details.
##
##  You should have received a copy of the GNU General Public License
##  along with agileBase.  If not, see <http://www.gnu.org/licenses/>.
##
-->
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN>
<%
String queryString = request.getQueryString();
if (queryString.contains("boot_mobile")) {
%>
  <%@ include file="mobile.jsp" %>
<% } else {
String googleKey = "";
String requestURL = request.getRequestURL().toString();
boolean ssl = false;
boolean live = false;
if (requestURL.startsWith("http://appserver.gtportalbase.com")) {
  googleKey = "http://www.google.com/jsapi?key=ABQIAAAAAmhDcBizb6sHKLYdSFLnLBTkxqGBZcNO6KTJ8OH7el13ZyLEzxT0-wdY7BkQmrNPx8dhLS-syRRsRQ";  
  live = true;
} else if (requestURL.startsWith("https://appserver.gtportalbase.com")) {
  googleKey = "https://www.google.com/jsapi?key=ABQIAAAAAmhDcBizb6sHKLYdSFLnLBSsFD5D7A41QFa4vWfOgDnykADPDxRmS3oyj7HLtk0xVDNhc4xnV0s6sg";
  ssl = true;
  live = true;
} else if (requestURL.startsWith("http://gtwmbackup.dh.bytemark.co.uk")) {
  googleKey = "http://www.google.com/jsapi?key=ABQIAAAAAmhDcBizb6sHKLYdSFLnLBQf1koDrgwv2nVopgtXyNJJGf3wPhSGxzvELTvIoGYjhEXJzrBbXQIbKw";
}

%>

<html>
	<head>
		<title>agileBase</title>
		<link rel="icon" href="/agileBase/website/gtpb.ico" type="image/x-icon"> <!-- favicon --> 
	    <link rel="apple-touch-icon" href="resources/icons/apple-touch-icon.png"/> 
		<script type="text/javascript" src="/agileBase/website/scripts/jquery.js"></script>
		<script type="text/javascript" src="/agileBase/website/scripts/thickbox.js"></script>
		<script type="text/javascript" src="<%= googleKey %>"</script>
		<script type="text/javascript">
			google.load("feeds", "1");
		</script>
        <% if(ssl) { %>
			<script src="https://www.google.com/uds/solutions/slideshow/gfslideshow.js" type="text/javascript"></script>
			<script src="https://www.google.com/uds/solutions/dynamicfeed/gfdynamicfeedcontrol.js" type="text/javascript"></script>
		<% } else { %>
		    <script src="http://www.google.com/uds/solutions/slideshow/gfslideshow.js" type="text/javascript"></script>
			<script src="http://www.google.com/uds/solutions/dynamicfeed/gfdynamicfeedcontrol.js" type="text/javascript"></script>
	    <% } %>
		<script type="text/javascript" src="/agileBase/website/scripts/ab.js"></script>
		<style>
		/* Google RSS stylesheet */
		@import url("/agileBase/website/styles/gfdynamicfeedcontrol.css");
		@import url("/agileBase/website/styles/thickbox.css");
		@import url("/agileBase/website/styles/thickbox_override.css");
		@import url("/agileBase/website/styles/styles.css");
		@import url("/agileBase/website/styles/fonts.css");
		</style>
	</head>
<body>Hello</body></html>

<% } %> <%-- End of non-mobile login page --%>