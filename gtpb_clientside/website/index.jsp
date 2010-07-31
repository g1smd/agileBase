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
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN">
<%
String queryString = request.getQueryString();
if (queryString.contains("boot_mobile")) {
%>
  <%@ include file="mobile.jsp" %>
<% } else if(queryString.contains("a3")) { %>
  <%@ include file="a3/a3.jsp" %>
<% } else { %>
  <%@ include file="agilebase.jsp" %>
<% } %> <%-- End of non-mobile login page --%>