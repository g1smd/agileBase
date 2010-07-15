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

<div class="actions_area left">
	<img src="/agileBase/website/a3/a3_reports_logo.png" />
	<hr />
<h1>Login</h1>
<% if(live) { %>
	<form method="POST" action="https://appserver.gtportalbase.com/agileBase/j_security_check" name="loginform" id="loginform">
<% } else { %>
    <form method="POST" action="http://gtwmbackup.dh.bytemark.co.uk:8080/agileBase/j_security_check" name="loginform" id="loginform">
<% } %>
<input type="text" name="j_username" /><br /><br />
<input type="password" name="j_password" />
</form>
</div>

<div class="actions_area right">
  <button type="button" id="delete" title="delete this report"></button><p>
  <hr />
  <button type="button" id="manage" title="manage all of your reports, users etc. in agileBase"></button>
</div>

<div id="delete_dialog" title="Confirm deletion">
Should this report be deleted?
</div>
</body>
</html>
