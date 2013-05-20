/* This file obsolete */

function  loadFeedControl() {
	var feed  = "http://blog.agilebase.co.uk/?feed=rss2";
		var options = {
		numResults : 6,
				displayTime : 15000,
				title : "<a href='http://blog.agilebase.co.uk'>announcements</a>",
				linkTarget : google.feeds.LINK_TARGET_SELF
		};
	var fg = new GFdynamicFeedControl(feed, "announce", options);
}

$(document).ready(function(){
	// frame buster needed as we may be in one of the agileBase application panes after a login timeout
	if(top != self) {
		top.location.replace("AppController.servlet?return=boot");
	}
	//if (($("#oViewPane").length != 0) || (document.location.href.indexOf("logout") > -1)) {
	//	top.location="http://agilebase.co.uk/start";
	//}
	// focus the 'username' box for logging in
	$("#j_username").focus();
	// test for iPhone
	if((navigator.userAgent.match(/iPhone/i)) || (navigator.userAgent.match(/iPod/i)) || (navigator.userAgent.match(/iPad/i))) {
		if(document.location.href.indexOf("mobile") == -1) {
			$('#login').html('<big>Mobile or tablet users log in at<br><a href="http://www.agilebase.co.uk/mobile">www.agilebase.co.uk/mobile</a></big>');
		}
	}
	// test for Konqueror
	if(navigator.userAgent.match(/Konqueror/i)) {
		if(document.location.href.indexOf("mobile") == -1) {
			$('#login').html('<big>Konqueror/LadyLodge users log in at<br><a href="http://www.agilebase.co.uk/ll">www.agilebase.co.uk/ll</a></big>');
		}
	}
	// if enter is pressed in username field with no password, don't submit details
	$("#loginform").submit(function() {
		if($('#j_password').val() == '') {
			return false;
		} else {
			return true;
		}
	});
	// just in time etc. tooltips
	$("#just_in a").each(function(i) {
		$(this).mouseover(function() {
			$(this).parent().find(".just_tooltip").fadeIn('normal');
		});
	});
	$(".just_tooltip").mouseout(function() {
		$(this).fadeOut();
	});
	$(".colorbox").click(function(event) {
	  event.preventDefault();
	  $("tr.row1").find("td").removeClass("selected");
	  $("tr.row2").find("td").removeClass("selected");
	  $(this).closest("td").addClass("selected");
	  $(".popup").slideUp();
	  var id = $(this).attr("href");
	  $(id).slideDown();
	});
});

function showSection(sectionName) {
	$("#navigation").find("a").removeClass("current");
	$("#nav_" + sectionName).addClass("current");
	$(".detail").fadeOut("fast");
	$("#" + sectionName).fadeIn("fast");
}

// outside of jquery document.ready
try {
	// start the RSS headlines from blog
	google.setOnLoadCallback(loadFeedControl);
} catch(err) {
}
