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
    <link rel="icon" href="/agileBase/website/gtpb.ico" type="image/x-icon"> <!-- favicon --> 
    <style>
      h1 {
        font-family: Verdana,Arial,sans-serif;
        font-size-adjust: 0.58;
        text-align: center;
      }
      
      img {
      	display: block;
      	margin-top: 20px;
    	margin-left: auto;
    	margin-right: auto;
      }
    </style>
  </head>
<body>
<a href="/agileBase/AppController.servlet?return=boot"><img border="0" src="/agileBase/website/images/disallowed.png"></a><br>
<h1>Login failed, please <a href="/agileBase/AppController.servlet?return=boot">try again</a></h1>

</body>
</html>