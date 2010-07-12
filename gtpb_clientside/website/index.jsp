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

Google key is <%= requestURL %>

<% } %> <%-- End of non-mobile login page --%>