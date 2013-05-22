<%
String requestURL = request.getRequestURL().toString();
boolean live = false;
if (requestURL.contains("appserver.")) {
  live = true;
}
%>
<html>
<head>
	<title>agileBase</title>
	<link type="image/x-icon" rel="icon" href="/agileBase/website/gtpb.ico" /> <!-- favicon -->
	<script type="text/javascript" src="/agileBase/website/scripts/jquery.js"></script>
	<script type="text/javascript">
	  var _gaq = _gaq || [];
	  _gaq.push(['_setAccount', 'UA-59206-20']);
	  _gaq.push(['_trackPageview']);

	  (function() {
	    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
	    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
	    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
	  })();
	</script>
	<script type="text/javascript">
	  $(document).ready(function() {
	  	$("#loginform").submit();
	  });
	</script>
</head>
<body>
	Please wait a moment...
	<% if(live) { %>
		<form method="POST" action="https://appserver.gtportalbase.com/agileBase/j_security_check" name="loginform" id="loginform">
	<% } else { %>
		<form method="POST" action="http://backup.agilebase.co.uk:8080/agileBase/j_security_check" name="loginform" id="loginform">
	<% } %>
	  <input type="hidden" name="j_username" id="j_username" value="<%= request.getParameter("u") %>" /><br>
	  <input type="hidden" name="j_password" id="j_password" value="<%= request.getParameter("x") %>" /><br><br>
	</form>
</body>
</html>
