<%
String googleKey = "";
String requestURL = request.getRequestURL().toString();
boolean ssl = false;
boolean live = false;
if (requestURL.startsWith("http://appserver.gtportalbase.com")) {
  googleKey = "http://www.google.com/jsapi?key=ABQIAAAAAmhDcBizb6sHKLYdSFLnLBTkxqGBZcNO6KTJ8OH7el13ZyLEzxT0-wdY7BkQmrNPx8dhLS-syRRsRQ";  
  live = true;
} else if (requestURL.startsWith("https://appserver.gtportalbase.com")) {
  googleKey = "https://www.google.com/jsapi?key=ABQIAAAAAmhDcBizb6sHKLYdSFLnLBSsFD5D7A41QFa4vWfOgDnykADPDxRmS3oyj7HLtk0xVDNhc4xnV0s6sg";
  ssl = true;
  live = true;
} else if (requestURL.startsWith("http://gtwmbackup.dh.bytemark.co.uk")) {
  googleKey = "http://www.google.com/jsapi?key=ABQIAAAAAmhDcBizb6sHKLYdSFLnLBQf1koDrgwv2nVopgtXyNJJGf3wPhSGxzvELTvIoGYjhEXJzrBbXQIbKw";
} else if (requestURL.startsWith("http://mcc.hpl.hp.com:8080")) {
	googleKey = "http://www.google.com/jsapi?key=ABQIAAAAAmhDcBizb6sHKLYdSFLnLBSc_vMwNn3P31Pbn-7BnVhZNCM03BSA2_dWIrkQNs5kAuiaHmwVTAjuYw";
}
%>
<html>
	<head>
		<title>agileBase - a dedicated Lean Back Office platform</title>
		<link rel="icon" href="/agileBase/website/gtpb.ico" type="image/x-icon"> <!-- favicon --> 
    <link rel="apple-touch-icon" href="resources/icons/apple-touch-icon.png"/> 
		<script type="text/javascript" src="/agileBase/website/scripts/jquery.js"></script>
		<script type="text/javascript" src="<%= googleKey %>"></script>
        <% if(ssl) { %>
			<script src="https://www.google.com/uds/solutions/dynamicfeed/gfdynamicfeedcontrol.js" type="text/javascript"></script>
		<% } else { %>
			<script src="http://www.google.com/uds/solutions/dynamicfeed/gfdynamicfeedcontrol.js" type="text/javascript"></script>
	    <% } %>
		<script type="text/javascript">
			google.load("feeds", "1");
		</script>
		<script type="text/javascript" src="/agileBase/website/scripts/ab.js"></script>
		<style>
		/* Google RSS stylesheet */
		@import url("/agileBase/website/styles/gfdynamicfeedcontrol.css");
		@import url("/agileBase/website/styles/styles.css");
		@import url("/agileBase/website/styles/fonts.css");
		</style>
    <meta name="google-site-verification" content="f-uEpO4sFJ0ePStIn6Svsj_wumUtsr153X4VYBA96K8" />
	</head>
	<body>
	<div id="scroller">
		<div id="tl_wrapper">
			<div id="header" class="wrapper">
					<div class="content">
						<a id="home" href="#" alt="agileBase"><img src="/agileBase/website/images/logo-agilebase.png"></a>
						<table cellspacing="0" cellpadding="0" border="0">
              <tr class="row1">
                <td class="col1"><a href="#popupPricingContent" class="colorbox">pricing</a></td>
                <td><a href="#popupDemoContent" class="colorbox">demo</a></td>
              </tr>
              <tr class="row2">
                <td class="col1"><a href="#popupHelpContent" class="colorbox">help</a></td>
                <td><a href="#popupContactContent" class="colorbox">contact</a></td>
              </tr>
						</table>
					</div>
		  </div> <!-- end of header -->
          <div id="popupPricingContent" class="popup">
            <h1>Setup</h1>
            We typically spend the first day with a client prototyping a system in real time. 
            By the end of the day, clients have firstly, a useful working basic system and secondly a good idea of how long further development is likely to take (and cost) based on their ambitions. 
            <h1>Training</h1>
            Alternatively, a popular method is to provide a fixed cost for training existing staff to allow them to develop systems completely on their own. 
            Following this, users are empowered to develop the system based on their demand, at their own pace.
            <h1>Hosting, licensing* and support</h1>
            Hosted pricing is simply &pound;10/month/table.
            Total cost will obviously depend on the ambitions of business users but SMEs using agileBase in some core areas can expect costs of a few tens of pounds per month.<br>
            Add unlimited users for free! There's no additional per user cost, even if you set up logins for clients, suppliers or partners.<br>
            Partner discounts are 50% and volume discounts are also available.<p>
            <small>* agileBase is open source but custom components may be licensed</small>
          </div>
          <div id="popupDemoContent" class="popup">
            Log in to the demo server at <a href="http://www.agilebase.co.uk/test">www.agilebase.co.uk/test</a> with username and password <b>demo</b> for a sample demo.<br>
            Please contact us for custom demos
          </div>
          <div id="popupHelpContent" class="popup">
            Our support centre is<p><a href="http://www.agilebase.co.uk/help">www.agilebase.co.uk/help</a><p>
            In brief, support for agileBase is organised into three types
            <ul>
            <li><a href="http://www.agilebase.co.uk/help">Support Forum (free)</a></li>
            <li>Business specific support (charged)<br>
            For private support issues regarding confidential client data, e.g. data restore requests, please email support@agilebase.co.uk
            </li>
            <li>Open source support (hybrid)<br>For details, see the support centre</li>
            </ul>
            <p>User and administrator documentation is also freely available online at the support centre
          </div>
          <div id="popupContactContent" class="popup">
            Email <a href="mailto:oliver@agilebase.co.uk">oliver@agilebase.co.uk</a><br>
            Phone +44(0)845 4561810<br>
            Skype okohll<br>
            <a href="http://www.gtwm.co.uk">More contacts</a> at GT webMarque
          </div>
			<div id="intro" class="wrapper">      
				<div class="content">
								<div id="login">
					<% if(live) { %>
						<form method="POST" action="https://appserver.gtportalbase.com/agileBase/j_security_check" name="loginform" id="loginform">
					<% } else { %>
					    <form method="POST" action="/agileBase/j_security_check" name="loginform" id="loginform">
					<% } %>
								<div class="input">username<input type="text" name="j_username" id="j_username" autocorrect="off" autocapitalize="off"/>
								</div>  
								<div class="input">password<input type="password" name="j_password" id="j_password" />
								</div>
								<!-- submit element allows us to press enter to log in -->
								<input type="submit" value="login" style="display:none"/>
								<a id="button_login" href="javascript:document.loginform.submit();">login</a>
						</form>
				</div>
				<div id="urls" class="detail">
				  <h1>Log in to agileBase &rarr;</h1>
				  <br clear="left" />
				  Bookmark<p>
				  <a href="https://www.agilebase.co.uk/start">www.agilebase.co.uk/start</a><p>
				  to return to this login page
				</div>
				<div id="announce"><!-- populated by JavaScript RSS feed --></div>
				</div>   <!-- end content -->
			</div> <!-- end wrapper -->
				<div class="wrapper">
					<div class="content"> 
					  <div id="detail_right">
						</div>

						<br clear="both" /> <!-- because of the right floated content -->
					</div>  <!-- end of content -->
				</div>   <!-- end of wrapper -->
				<div class="wrapper">
					<div class="content" id="footer">
						&copy; 2012 GT webMarque Ltd. Company number 03851934. Contact: Bristol office +44(0)845 456 1810, oliver@gtwm.co.uk. Swansea office cliff@gtwm.co.uk
					</div>
				</div>	<!-- end of wrapper -->				
			</div>  <!-- end of tl_wrapper -->
		</div>  <!-- end of scroller -->
	</body>
</html>		
