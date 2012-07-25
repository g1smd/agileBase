<!DOCTYPE html>
<!--  
##  Copyright 2011 GT webMarque Ltd
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
String customLogin = request.getParameter("customlogin");
if (customLogin != null) {
  customLogin = customLogin.replaceAll("\\W","") + ".jsp";
  @include file=%>"<%customLogin%>"<%
} else {
  String queryString = request.getQueryString();
  if (queryString == null) {
    queryString = "";
  }
  if (queryString.contains("boot_mobile")) {
    @include file="mobile.jsp"
  } else if(queryString.contains("/common/a3/")) {
    @include file="a3/a3.jsp"
  } else if(queryString.contains("set_password")) {
    @include file="password_reset.jsp"
  } else {
    @include file="agilebase.jsp"
  }
}
%>