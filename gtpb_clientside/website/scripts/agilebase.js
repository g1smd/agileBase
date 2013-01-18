$(document).ready(function() {
	$("a#password_reset").click(function(event) {
		event.preventDefault();
		$("form").hide("normal");
		$("form#password_reset_form").show("normal");
	});
	$("#password_reset_form").submit(function(event) {
    event.preventDefault();
		var form = $(this);
    form.find("input").hide();
    form.find("button").hide();
		var formParent = form.parent();
		$.post(form.attr("action"), form.serialize(), function(data) {
			form.remove();
			var response = $(data).find("response").text();
			if (response == "ok") {
				formParent.append("<p>Request successful, a password reset link has been sent to your email. If it doesn't turn up, try checking your spam filter.</p>");
			} else {
				formParent.append("<div class='errormessage'>Unable to request password. Perhaps the login name was incorrect or no email address is associated with the account. Please contact your administrator</div>");
			}
		});
	});
});
