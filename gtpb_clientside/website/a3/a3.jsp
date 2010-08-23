<%
String requestURL = request.getRequestURL().toString();
boolean live = false;
if (requestURL.contains("appserver.")) {
  live = true;
}
%>
<html>
<head>
  <link rel="icon" href="/agilebase/website/gtpb.ico" type="image/x-icon"> <!-- favicon -->
  <title>
    A3 reports in Lean service organisations
  </title>
  <script type="text/javascript" src="/agileBase/website/scripts/jquery.js"></script>
  <!-- <script type="text/javascript" src="resources/wait/editBuffer_editData.js"></script>
  <script type="text/javascript" src="resources/wait/request_setFilter.js"></script> -->
  <script src="/agileBase/website/scripts/jquery-ui/jquery-ui.js" language="Javascript"></script>
  <script src="/agileBase/website/a3/a3.js" language="Javascript"></script>
  <link type="text/css" href="/agileBase/website/a3/a3.css" rel="stylesheet">
<!--[if IE]>
  <link rel="stylesheet" type="text/css" href="/agileBase/website/a3/a3_ie.css" />
<![endif]-->
  <link rel="stylesheet" href="/agileBase/website/a3/print.css" type="text/css" media="print" />
  <link type="text/css" href="/agileBase/website/scripts/jquery-ui/jquery-ui.css" rel="stylesheet">
<script type="text/javascript">

  var _gaq = _gaq || [];
  _gaq.push(['_setAccount', 'UA-59206-21']);
  _gaq.push(['_trackPageview']);

  (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  })();

</script>
</head>
<body>
<img id="paper" src="/agileBase/website/a3/paper.jpg">
<div id="stickies">
  <div id="why_a3"><a href="/agileBase/website/a3/why_a3.htm" class="selected_link">Why use A3 reports?</a></div>
  <div id="scenario" style="left: 250px"><a href="/agileBase/website/a3/scenario.htm">And why use online?</a></div>
  <div id="tryout" style="left: 500px"><a href="/agileBase/website/a3/tryout.htm">Try it out now!</a></div>
</div>
<div id="a3_report">
<%@ include file="why_a3.htm" %>
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
<input type="text" name="j_username" title="username"/><br /><br />
<input type="password" name="j_password" title="password"/><br /><br />
<input type="submit" value="Go" />
</form>
</div>

<div class="actions_area right">
<h1>Signup</h1>
<h1>&pound;5/m</h1>
<hr />
<h2>Free trial</h2>
for 30 days
<hr />
<form method="POST" action="http://a3reports.co.uk/templateMail.php" id="signup_form" name="signup_form">
<input type="hidden" name="template_folder" value="non_gtwp/a3_reports/templates" />
<input type="hidden" name="templ_success" value="ok.txt" />
<input type="hidden" name="templ_fail" value="not_ok.txt" />
<input type="hidden" name="templ_email" value="email_signup.txt" />
<input type="text" id="email_input" name="email" value="your email address" title="your email address" />
<br /><br />
<input type="submit" value="Go" />
</form>
</div>

<div id="delete_dialog" title="Confirm deletion">
Should this report be deleted?
</div>
</body>
</html>
