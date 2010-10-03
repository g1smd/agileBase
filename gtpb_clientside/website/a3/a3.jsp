<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN"> 
<%
String requestURL = request.getRequestURL().toString();
boolean live = false;
if (requestURL.contains("appserver.")) {
  live = true;
}
%>
<html> 
	<head> 
		<title>A3 reports</title>
		<style> 
		  @import url('website/a3/styles.css');
		  @import url('website/a3/styles-thickbox.css');
		</style>
		
		<script type="text/javascript" src="jquery.js"></script>
		
        <script language="javascript">
            $(document).ready(function() {
                
                $('.container:first-child').addClass('first');
                $('.container:last-child').addClass('last');
                $('.container:nth-child(even)').addClass('alternate');

				$.localScroll({target:'#scroller'});
				
                $('.video').each(function() {
					$(this).flashembed($(this).attr('url')+'&rel=0&showinfo=0&');
				});
				
				$('#email_input').focus(function() {
					alert('focus');
					if ($('#email_input').val() == "your email address") {
						$('#email_input').val("");
					}
				}
			});
		</script>
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
        <div id="scroller">
            <div id="wrapper">
				<ul id="menu">
  		            <img style="position:absolute; left:40px; top:30px" src="website/a3/images/logo_a3reports.png" />
				</ul>
				
				<div id="content">
				    <div class="container" style="background-color:#74a9cf">
				        <div style="float:left; width:100%; margin-left:80px;">
				        	<div style="float:right; margin-right:160px;" id="signup">
				        	  <h1>Free trial</h1>
							  <form method="POST" action="http://a3reports.co.uk/templateMail.php" id="signup_form" name="signup_form">
							  <input type="hidden" name="template_folder" value="wp/a3reports/templates" />
							  <input type="hidden" name="templ_success" value="ok.txt" />
							  <input type="hidden" name="templ_fail" value="not_ok.txt" />
							  <input type="hidden" name="templ_email" value="email_signup.txt" />
							  <input type="text" id="email_input" name="email" value="your email address" title="your email address" />
							  <br /><br />
							  <input type="submit" value="Go"/>
							  </form>
				        	</div>
				        	<h1>Login</h1>
							<% if(live) { %>
								<form method="POST" action="https://appserver.gtportalbase.com/agileBase/j_security_check" name="loginform" id="loginform">
							<% } else { %>
							    <form method="POST" action="http://gtwmbackup.dh.bytemark.co.uk:8080/agileBase/j_security_check" name="loginform" id="loginform">
							<% } %>
							<table class="formtable">
							    <tr><td>username</td><td> <input type="text" name="j_username" title="username"/></td></tr>
								<tr><td>password</td><td> <input type="password" name="j_password" title="password"/></td></tr>
							    <tr><td>&nbsp;</td><td> <input type="submit" value="Go" /></td></tr>
							</table>
							</form>

				        </div>
				        <br style="clear:both" />
				    </div>
				    
				    <div class="container" id="contact">
						<table cellspacing="0" cellpadding="0" border="0">
							<tr>
								<td style="padding-right:100px">
									<h1><span>{</span> email <span>}</span></h1>
									<a href="mailto:cliff@gtwm.co.uk">cliff@gtwm.co.uk</a>
								</td>
								<td style="padding-right:100px">
									<h1><span>{</span> phone <span>}</span></h1>
									+44 (0) 845 456 1810
								</td>
								<td>
									<h1><span>{</span> visit <span>}</span></h1>
									Come see us in Bristol, Swansea or London!
								</td>
							</tr>
						</table>
						<p style="text-align:right; font-size:8pt; padding-top:40px; color:#999999">
							site design by David Boultbee &copy;2010
						</p>
				    </div>
				    
				</div> <!-- end of content -->
            </div> <!-- end of wrapper -->
        </div> <!-- end of scroller -->
	</body>
</html>
