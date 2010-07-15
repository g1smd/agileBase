<%
String requestURL = request.getRequestURL().toString();
boolean live = false;
// if (requestUrl.contains("appserver.")) {
//  live = true;
// }
%>
<html>
<head>
  <meta http-equiv="X-UA-Compatible" content="chrome=1">
  <link rel="icon" href="/agilebase/website/gtpb.ico" type="image/x-icon"> <!-- favicon -->
  <title>
    A3 reports collaboration
  </title>
  <script type="text/javascript" src="/agileBase/website/scripts/jquery.js"></script>
  <!-- <script type="text/javascript" src="resources/wait/editBuffer_editData.js"></script>
  <script type="text/javascript" src="resources/wait/request_setFilter.js"></script> -->
  <script src="/agileBase/website/scripts/jquery-ui/jquery-ui.js" language="Javascript"></script>
  <script src="/agileBase/website/a3/a3.js" language="Javascript"></script>
  <link type="text/css" href="/agileBase/website/a3/a3.css" rel="stylesheet">
  <link type="text/css" href="/agileBase/website/scripts/jquery-ui/jquery-ui.css" rel="stylesheet">
</head>
<body>
<img src="/agileBase/website/a3/paper.jpg" style="max-width:100%; z-index: 0">
<div id="a3_report">
## Will be loaded over AJAX
</div>

<div class="actions_area left" style="opacity:0.4">
	<img src="/agileBase/website/a3/a3_reports_logo.png" />
	<hr />
<h1>Login</h1>
<% if(live) { %>
	<form method="POST" action="https://appserver.gtportalbase.com/agileBase/j_security_check" name="loginform" id="loginform">
<% } else { %>
    <form method="POST" action="http://gtwmbackup.dh.bytemark.co.uk:8080/agileBase/j_security_check" name="loginform" id="loginform">
<% } %>
<input type="text" name="j_username" /><br /><br />
<input type="password" name="j_password" /><br /><br />
<input type="submit" value="Go" />
</form>
</div>

<div class="actions_area right" style="opacity:0.4">
<h1>Sign<br />up</h1>
<h1>&pound;5/m</h1>
<h2>Free trial</h2>
for 30 days
<hr />
<input type="text" name="email" />
<br /><br />
<input type="submit" value="Start" />
</div>

<div id="delete_dialog" title="Confirm deletion">
Should this report be deleted?
</div>
</body>
</html>
