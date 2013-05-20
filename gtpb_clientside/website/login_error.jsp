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
Thread.sleep(3000);
%>
<html>
  <head>
  	<title>Login error</title>
    <link href="/agileBase/website/bootstrap/css/bootstrap.css" rel="stylesheet">
    <link href="/agileBase/website/fontawesome/css/font-awesome.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css?family=Open+Sans:400,300" rel="stylesheet" type="text/css">
    <link href="/agileBase/website/styles.css" rel="stylesheet">
    <link rel="shortcut icon" href="/agileBase/website/gtpb.ico">
  </head>
<body>
  <div class="container big">
    <div class="row abNav">
      <div class="span2 home">
        <a href="http://www.agilebase.co.uk"><img id="nav_home" src="/agileBase/website/images/agilebase.png" /></a>
        <div class="arrow"></div>
      </div>
    </div>
    <div id="content">
    <div class="alert alert-block">
      <i class="icon-large icon-exclamation-sign"></i>
      Login failed, please <a href="/agileBase/AppController.servlet?return=boot">return to the login page</a>
    </div>
    <p>If you've forgotten your password, please use the 'Can't log in' link in the login page to reset it
  </div>
  </div>
</body>
</html>