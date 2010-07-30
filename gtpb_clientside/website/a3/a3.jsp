<%
String requestURL = request.getRequestURL().toString();
boolean live = false;
if (requestURL.contains("appserver.")) {
  live = true;
}
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
<img id="paper" src="/agileBase/website/a3/paper.jpg">
<div id="stickies">
  <div id="why_a3">Why use A3 reports?</div>
  <div id="scenario" style="left: 250px">Scenario</div>
  <div id="tryout" style="left: 500px">Try it out now!</div>
</div>
<div id="a3_report">
</div>

<div class="actions_area left" style="opacity:0.8">
	<img src="/agileBase/website/a3/a3_reports_logo.png" />
	<hr />
<h1>Login</h1>
<% if(live) { %>
	<form method="POST" action="https://appserver.gtportalbase.com/agileBase/j_security_check" name="loginform" id="loginform">
<% } else { %>
    <form method="POST" action="http://gtwmbackup.dh.bytemark.co.uk:8080/agileBase/j_security_check" name="loginform" id="loginform">
<% } %>
<input type="text" name="j_username" title="username"/><br /><br />
<input type="password" name="j_password" title="password"/><br /><br />
<input type="submit" value="Go" />
</form>
</div>

<div class="actions_area right" style="opacity:0.8">
<h1>Signup</h1>
<h1>&pound;5/m</h1>
<hr />
<h2>Free trial</h2>
for 30 days
<hr />
<form method="POST" action="http://a3reports.co.uk/templateMail.php">
<input type="text" name="email" value="your email address" title="your email address"/>
<input type="hidden" name="template_folder" value="/non_gtwp/a3_reports/templates" />
<input type="hidden" name="templ_success" value="ok.txt" />
<input type="hidden" name="templ_fail" value="not_ok.txt" />
<input type="hidden" name="templ_email" value="email_signup.txt" />
<br /><br />
<input type="submit" value="Go" />
</form>
</div>

<div id="delete_dialog" title="Confirm deletion">
Should this report be deleted?
</div>
</body>
</html>
