<%
String googleKey = "https://www.google.com/jsapi?key=AIzaSyD87nNNZYrfRNIAOSC0ayCB4yj6KkJ9JlI";
String requestURL = request.getRequestURL().toString();
boolean ssl = false;
boolean live = false;
if (requestURL.startsWith("http://appserver.gtportalbase.com")) {
  live = true;
} else if (requestURL.startsWith("https://appserver.gtportalbase.com")) {
  //googleKey = "https://www.google.com/jsapi?key=ABQIAAAAAmhDcBizb6sHKLYdSFLnLBSsFD5D7A41QFa4vWfOgDnykADPDxRmS3oyj7HLtk0xVDNhc4xnV0s6sg";
  ssl = true;
  live = true;
}
%>
<html>
	<head>
    <meta charset="UTF-8">
		<title>agilebase : log in</title>
    <link href="/agileBase/website/bootstrap/css/bootstrap.css" rel="stylesheet">
    <link href="/agileBase/website/fontawesome/css/font-awesome.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css?family=Open+Sans:400,300" rel="stylesheet" type="text/css">
    <link href="/agileBase/website/styles.css" rel="stylesheet">
		<link rel="shortcut icon" href="/agileBase/website/gtpb.ico">
		<script type="text/javascript" src="/agileBase/website/scripts/jquery.js"></script>
    <script type="text/javascript">
      $(document).ready(function() {
      	$("a#password_reset").click(function(event) {
      		event.preventDefault();
      		$("form").hide("normal");
      		$("form#password_reset_form").show("normal");
      	});
      	$("#password_reset_form").submit(function(event) {
          event.preventDefault();
      		var form = $(this);
      		var formParent = form.parent();
      		$.post(form.attr("action"), form.serialize(), function(data) {
      			form.remove();
      			var response = $(data).find("response").text();
      			if (response == "ok") {
      				formParent.append("<p>A password reset link has been sent to your email address</p>");
      			} else {
      				var message = $(data).find("exception").text();
      				formParent.append("<div class='errormessage'>" + message + "</div");
      			}
      		});
      	});
      });
    </script>
    <meta name="google-site-verification" content="f-uEpO4sFJ0ePStIn6Svsj_wumUtsr153X4VYBA96K8" />
    <% if(!live) { %>
      <!-- don't index the test server -->
      <meta name="robots" content="noindex">
    <% } %>
	</head>
	<body>
    <div class="container">
      <div class="row abNav">
        <div class="span2 home">
          <a href="http://www.agilebase.co.uk"><img id="nav_home" src="/agileBase/website/images/agilebase.png" /></a>
          <div class="arrow"></div>
        </div>
      </div>
      <div id="content">
        <h1 class="light"><img src="/agileBase/website/images/brandmark.png" /> : log in</h1>
        <div class="big spaced">
          <% if(live) { %>
            <form method="POST" action="https://appserver.gtportalbase.com/agileBase/j_security_check" name="loginform" id="loginform" class="form-horizontal">
          <% } else { %>
            <form method="POST" action="/agileBase/j_security_check" name="loginform" id="loginform" class="form-horizontal">
          <% } %>
              <div class="control-group">
                <label class="control-label" for="j_username">username</label>
                <div class="controls"><input type="text" name="j_username" id="j_username" autocorrect="off" autocapitalize="off"/></div>
              </div>  
              <div class="control-group">
                <label class="control-label" for="j_password">password</label>
               <div class="controls"><input type="password" name="j_password" id="j_password" /></div>
              </div>
              <div class="control-group">
                <div class="controls">
                  <button type="submit" value="login" class="btn">sign in</button>
                </div>
              </div>
              <div class="control-group">
                <div class="controls">
                  <small><small><a href="#" id="password_reset">Can't log in?</a></small></small>
                </div>
              </div>
            </form>
          <% if(live) { %>
            <form style="display:none" method="POST" action="https://appserver.gtportalbase.com/agileBase/Public.ab" id="password_reset_form" name="password_reset_form" class="form-inline">
          <% } else { %>
            <form style="display:none" method="POST" action="/agileBase/Public.ab" id="password_reset_form" name="password_reset_form" class="form-inline">
          <% } %>
              <input type="hidden" name="send_password_reset" value="true" />
              <p>If you've forgotten your password, please enter your username to be sent a password reset link by email.
              <p>If you don't know your username, please contact your organisation's administrator.
              <div class="spaced">
                <input type="text" name="username" id="username" autocorrect="off" autocapitalize="off" placeholder="username" />
                <button type="submit" class="btn">request password</button>
              </div>
            </form>
          </div>
      </div>
    </div>
		</body>
</html>		
