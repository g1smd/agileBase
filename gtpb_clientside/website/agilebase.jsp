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
}
%>

<html>
	<head>
		<title>agileBase - a dedicated Lean Back Office platform</title>
		<link rel="icon" href="/agileBase/website/gtpb.ico" type="image/x-icon"> <!-- favicon --> 
	    <link rel="apple-touch-icon" href="resources/icons/apple-touch-icon.png"/> 
		<script type="text/javascript" src="/agileBase/website/scripts/jquery.js"></script>
		<script type="text/javascript" src="/agileBase/website/scripts/thickbox.js"></script>
		<script type="text/javascript" src="<%= googleKey %>"></script>
        <% if(ssl) { %>
			<script src="https://www.google.com/uds/solutions/slideshow/gfslideshow.js" type="text/javascript"></script>
			<script src="https://www.google.com/uds/solutions/dynamicfeed/gfdynamicfeedcontrol.js" type="text/javascript"></script>
		<% } else { %>
		    <script src="http://www.google.com/uds/solutions/slideshow/gfslideshow.js" type="text/javascript"></script>
			<script src="http://www.google.com/uds/solutions/dynamicfeed/gfdynamicfeedcontrol.js" type="text/javascript"></script>
	    <% } %>
		<script type="text/javascript">
			google.load("feeds", "1");
		</script>
		<script type="text/javascript" src="/agileBase/website/scripts/ab.js"></script>
		<style>
		/* Google RSS stylesheet */
		@import url("/agileBase/website/styles/gfdynamicfeedcontrol.css");
		@import url("/agileBase/website/styles/thickbox.css");
		@import url("/agileBase/website/styles/thickbox_override.css");
		@import url("/agileBase/website/styles/styles.css");
		@import url("/agileBase/website/styles/fonts.css");
		</style>
	</head>
	<body>
	<div id="scroller">
		<div id="tl_wrapper">
			<div id="header" class="wrapper">
					<div class="content">
						<a id="home" href="#" alt="agileBase"><img src="/agileBase/website/images/logo-agilebase.png"></a>
						<table cellspacing="0" cellpadding="0" border="0">
							<tr class="row1">
								<td class="col1"><a href="#TB_inline&inlineId=popupPricingContent" class="thickbox">pricing</a></td>
								<td><a href="#TB_inline&inlineId=popupDemoContent" class="thickbox">demo</a></td>
							</tr>
							<tr class="row2">
								<td class="col1"><a href="#TB_inline&inlineId=popupHelpContent" class="thickbox">help</a></td>
								<td><a href="#TB_inline&inlineId=popupContactContent" class="thickbox">contact</a></td>
							</tr>
						</table>
					</div>
					<div id="popupPricingContent" style="display:none">
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
						Log in with username and password <b>demo</b> for a basic demo.<br>
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
		  </div> <!-- end of header -->
			<div id="intro" class="wrapper">      
					<div class="content">
						<div id="bubble">
							<h1>The best IT platform for the SME Lean back office</h1>
							agileBase - built from the ground up to support
							continuous improvement, PDCA, A3 reports and other Lean approaches.
						</div>
				<div id="login">
					<% if(live) { %>
						<form method="POST" action="https://appserver.gtportalbase.com/agileBase/j_security_check" name="loginform" id="loginform">
					<% } else { %>
					    <form method="POST" action="http://gtwmbackup.dh.bytemark.co.uk:8080/agileBase/j_security_check" name="loginform" id="loginform">
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
				<ul id="navigation">
					<li><a href="javascript:showSection('main');" id="nav_main" class="current"><span>business</span></a>
					<li><a href="javascript:showSection('technical');" id="nav_technical"><span>technical</span></a>
					<li id="li_analyst"><a href="javascript:showSection('analyst');" id="nav_analyst"><span>analyst</span></a>
				</ul>
				<div id="announce"><!-- populated by JavaScript RSS feed --></div>
				</div>   <!-- end content -->
			</div> <!-- end wrapper -->
				<div class="wrapper">
					<div class="content"> 
					  <div id="detail_right">
							<div id="quotes">
								<span class="bqstart">&#8220;</span>
								I used to use Oracle at my previous Fortune 100. I'm now an agileBase convert - I adapt it to my needs as soon as I learn a better way of doing something.
								<span style="float: right">- John Collins</span>
							</div>
							<div id="screenshots"><!-- populated by JavaScript slideshow --></div>
						</div>
						<div id="main" class="detail">
							<div class="divider">
								<iframe class="youtube-player" type="text/html" width="500" height="232" src="http://www.youtube.com/embed/HH_RRD7oqvQ" frameborder="0"></iframe>
								Here's a brief introduction to our business philosophy by MD Clifford Calcutt
							</div>
							<div class="divider">
								<h1>Why do I use it?</h1>
								agileBase helps build back office software that is
								<div id="just_in">
								<table border="0" width="100%"><tr>
								<td class="just_in_time"><span>
								  <img src="/agileBase/website/images/pix-lightblue.png"></img> <a href="#">just in time </a>
								  <div class="just_tooltip">develop for this week not next year and for $ not $$$$$$</div>
								</span></td>
								<td class="just_enough"><span>
								  <img src="/agileBase/website/images/pix-darkblue.png"></img> <a href="#">just enough </a>
								  <div class="just_tooltip">avoid being swamped by bloatware and complex user interfaces</div>
								</span></td>
								<td class="just_for_us"><span>
								  <img src="/agileBase/website/images/pix-orange.png"></img> <a href="#">just for us </a>
								  <div class="just_tooltip">customisable to business needs and processes</div>
								<span></td>
								</tr></table>
								</div>
								<p>As a cutting edge Lean provider, we believe an <b>agile platform</b> can enable companies to build what they need, 
								at a pace to match a company's evolving learning, knowledge and understanding.</p>
							</div>
							<div>
								<h1>How do I use it?</h1>
								<p>agileBase is perfect for organisations in rapidly changing environments, 
								where scalability goes hand in hand with the need for a rapid Plan/Do/Check/Act cycle.</p>
								<p>Our vision was to make a product that allows business analysts
								to create Lean, agile, Just-In-Time (JIT) back offices.</p>
							</div>
						</div>   <!-- end of main --> 
						<div id="technical" class="detail">
							<div class="divider">
								<h1>On the foundations</h1>
								<p>Building blocks provide the capacity for massive scalability of end applications if necessary 
								but the agileBase aplication itself also has some nice technical features that anyone who's worked 
								on large scale databases will appreciate, for example...</p>
								<table border="0" cellpadding="0" cellspacing="0">
									<tr>
										<td style="width:50%">
											<h2>View cascading</h2>
											If a view/report A has dependent reports B, C and D, they are automatically dropped and 
											recreated when changes to A are made. There's no laborious manual process of dropping and 
											re-creating multiple reports for a simple field addition.               
										</td>
										<td style="width:50%">
											<h2>Auto joins</h2>
											Joins are automatically created (LEFT OUTER by default) where there are relations but 
											you can still create completely custom joins on any field or calculation if you need to.
										</td>
									</tr>
									<tr>
										<td style="width:50%">
											<h2>Deleting</h2>
											Similarly, if you try to delete a table or view field that something else depends on, 
											agileBase will tell you exactly where the dependencies are so you can solve them first.
										</td>
										<td style="width:50%">
											<h2>More</h2>
										Auto indexes (case insensitive where relevant), quick aggregates, advanced filters 
										(e.g. newer than x months), built-in stats/audit trail and more mean that common app 
										building tasks are taken care of leaving you to concentrate on design or quick prototyping.
										</td>
									</tr>
								</table>        
							</div>
							<div class="divider">
								<h1>Technology</h1>
								agileBase is a web-based, usually hosted application. 
								The <a href="http://en.wikipedia.org/wiki/AJAX">AJAX</a> user interface works on Firefox (recommended), Internet Explorer 7+, Google Chrome and Safari browsers.<p>
								The server side software and data is usually run as a hosted service by us. 
								However, if you wish to run your own version, it is platform agnostic, running on Linux or Windows.<p>
								A number of robust and scalable third party products are used by the server as a foundation for our innovations<p>
								<p>The relational database behind the scenes on our server is the enterprise class database <a href="http://www.postgresql.org/">PostgreSQL</a>.<p>
								<a href="http://www.hibernate.org/">Hibernate</a> and <a href="http://velocity.apache.org/">Velocity</a> provide stable frameworks for object storage and display.<p>
								We host at <a href="http://www.ntt.com/">NTT Communications</a>' London datacentre. Backups are made hourly and transferred offsite (to Manchester) daily.</p>
								<table border="0" width="100%" style="margin-top:20px">
									<tr>
										<td style="text-align:center; vertical-align:middle; width:25%"><a href="http://www.postgresql.org/"><img border="0" src="/agileBase/website/content/postgresql.gif"></a></td>
										<td style="text-align:center; vertical-align:middle; width:25%"><a href="http://www.hibernate.org/"><img border="0" src="/agileBase/website/content/hibernate_logo.gif"></a></td>
										<td style="text-align:center; vertical-align:middle; width:25%"><a href="http://velocity.apache.org/"><img border="0" src="/agileBase/website/content/velocity_logo.gif"></a></td>
										<td style="text-align:center; vertical-align:middle; width:25%"><a href="http://tomcat.apache.org/"><img border="0" src="/agileBase/website/content/tomcat.png"></a></td>
									 </tr>
								</table>
							  </div>
							<div>
								<h1>Documentation &amp; Source</h1>
								<p><a href="http://github.com/okohll/agileBase/tree/"><img border="0" src="/agileBase/website/content/github.png" style="float:left;"></a>The source code for agileBase is <a href="http://github.com/okohll/agileBase/tree/">available</a> on GitHub as open source.								
								</p>
								<p style="clear:both">See 
								  <ul>
								    <li><a href="http://www.gtwebmarque.com/wikis/gtwm/index.php/GT_portalBase#End_user_documentation">User documentation</a></li> 
									<li><a href="http://www.gtwebmarque.com/wikis/gtwm/index.php/GT_portalBase#Administrator_documentation">Administrator Documentation</a></li> 
									<li><a href="/agileBase/website/content/gtpb_overview.pdf">Brochure</a></li>
								  </ul>
								</p>
							  <p>agileBase is a <a href="http://www.gtwm.co.uk">GT webMarque</a> product, <a href="http://www.gtwm.co.uk">contact us</a> for more help and advice</p>
							</div>
						</div>  <!-- end of technical -->
						<div id="analyst" class="detail">
							<h1>A business analyst's view</h1>
							<p>Over the last 10 years the role of the Business Analyst has emerged as organisations grapple with the problem of how to 
							ensure software development projects deliver applications that match the needs of their client.</p>
							<p>Traditionally this role has involved defining requirements and recording these in a formal specification document. 
							However there is a growing recognition of the problems inherent in this approach and a more agile approach 
							involving prototyping is often seen as an viable alternative.</p>
							<p>agileBase requires no programming skills and thus allows analysts to convert schemas into fully working 
							web based prototypes in hours. The prototype is immediately available via the web for client review and feedback 
							from various stakeholders can be incorporated into the product often in real time.</p>
							<h1>agileBase as a long term solution</h1>
							<p>For many organisations these "prototypes" may even suffice, as the PostgreSQL database underlying agileBase is highly scalable & robust.</p>
							<p>For small organisations or applications agileBase therefore provides a cost effective, rapid, easy to use and robust web app. development tool.</p>
							<h1>Customisations</h1>
							<p>If custom features are necessary, wizards for complex processes or custom printouts for example, an plugin engine and <a href="http://gtwebmarque.com/wikis/gtwm/index.php/Javadoc">API</a> is available to build these with. 
							Any developer has to be comfortable with using the <a href="http://velocity.apache.org">Apache Velocity</a> templating language. 
							We are happy to take this type of work or support on as a consultancy service if necessary.
						</div> <!-- end of anyalyst -->
						<br clear="both" /> <!-- because of the right floated content -->
					</div>  <!-- end of content -->
				</div>   <!-- end of wrapper -->
				<div class="wrapper">
					<div class="content" id="footer">
						&copy; 2011 GT webMarque Ltd, Furze Bank, 34 Hanover Street, SWANSEA. +44(0)845 456 1810
					</div>
				</div>	<!-- end of wrapper -->				
			</div>  <!-- end of tl_wrapper -->
		</div>  <!-- end of scroller -->
	</body>
</html>		
