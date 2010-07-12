<html>
	<head>
		<title>agileBase</title>
		<link rel="icon" href="/agileBase/website/gtpb.ico" type="image/x-icon"> <!-- favicon --> 
	    <meta name="viewport" content="width = device-width, initial-scale = 1.0, user-scalable=yes" />
	    <link rel="apple-touch-icon" href="resources/icons/apple-touch-icon.png"/> 
	    <style>
	    	@import url("/agileBase/website/styles/styles.css");
		</style>
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
    </head>
    <body class="mobile">
		<div id="header" class="wrapper">
			<div class="content">
				<a id="home" href="#" alt="agileBase"><img src="/agileBase/website/images/logo-agilebase.png"></a>
				<div style="position:relative; top:110px">
			<!--#if expr="$SERVER_NAME='gtwmbackup.dh.bytemark.co.uk'" --> 
					<form method="POST" action="http://gtwmbackup.dh.bytemark.co.uk:8080/agileBase/j_security_check" name="loginform" id="loginform">
			<!--#else --> 
					<form method="POST" action="https://appserver.gtportalbase.com/agileBase/j_security_check" name="loginform" id="loginform">
			<!--#endif -->
			<table border="0" class="mobileLogin">
			<tr>
			  <td>username<br /><input style="right:0px" type="text" name="j_username" id="j_username" autocorrect="off" autocapitalize="off"/></td>
			</tr>
			<tr>
			  <td>password<br /><input style="right:0px" type="password" name="j_password" id="j_password" /></td>
			</tr>
			<tr>
			<td><input type="submit" value="login" style="top:200px"></td>
			</tr>
			
			</table>
					</form>
				</div>
			</div>
		</div>  
    </body>
</html> <!-- end of mobile version -->
