var abCardScale = 1.0;

$(document).ready(function() {
	init();
  $("#filters td.leading").children("div").append("<a href='AppController.servlet?return=boot'><img id='home' src='resources/toolbar/agilebase.png' /></a>");
	$("#fieldFilters").show();
	$("#fieldFilters input").removeAttr("onkeyup");
	$("#fieldFilters input").attr("gtpb_return","gui/preview/argument");
	$("#fieldFilters input").keyup(function(event) {
		function fReqCompleteOverride(data) {
			$("#fieldFilters input").removeAttr("changed").removeClass("changed");
			$("#argument").children().remove();
			$("#argument").append(data);
			init();
		}
		new fSetFilter(event, this, fReqCompleteOverride);
	});
	// For report chooser
	$("button.report_choice").click(function() {
		$("button.report_choice").fadeOut();
		var internalReportName = $(this).val();
		var internalTableName = $(this).attr("data-internaltablename");
		var assetManager = $(this).attr("data-assetmanager");
		if(assetManager != "true") {
			assetManager = "false";
		}
		document.location = "AppController.servlet?return=gui/preview/argument_presenter_standalone&set_report=" + internalReportName + "&set_table=" + internalTableName + "&set_custom_boolean=true&booleankey=assetmanager&custombooleanvalue=" + assetManager;
	});
	if($("button.report_choice").size() == 1) {
		$("#sets").append("<h1>loading...</h1>");
		$("button.report_choice").click();
		$("button.report_choice").attr("disabled","true");
	}
	$("#upload_toggle").click(function() {
		$("#upload").hide();
		$("#upload").load("AppController.servlet", {
			"return": "gui/preview/uploader",
			"save_new_record": true
		}, function() {
			editTabFunctions();
			$("#upload").show("normal");
		});
	});
});

function unzoom() {
  $(".container").removeClass("zoomed unzoomed").removeAttr("style").find(".card").removeClass("flipped");
}

function init() {
	if($("#argument").hasClass("asset_manager")) {
		$(".card").click(function(event) {
			if ($(this).hasClass("flipped")) {
				$(this).find(".front").children().fadeIn("normal");
			} else {
				$(this).find(".front").children().fadeOut("normal");
			}
	    $(this).toggleClass("flipped");
		});
		$(".mailto").click(function() {
			var subject = $(this).attr("data-filename").replace(/\s/g,"%20").replace(/&/g,"%26");
			var targetUrl = $(this).attr("data-targeturl");
			targetUrl = "https://appserver.gtportalbase.com" + targetUrl.replace(/&/g,"%26").replace(/\s/g,"%20");
			var body = subject + " has been uploaded to%0A%0A";
			body += "www.chfoods.co.uk/digitalassets%0A%0A";
			body += "Direct link:%0A%0A";
			body += targetUrl + "%0A";
			var loc = "mailto:?subject=" + subject + "%20uploaded" + "&body=" + body;
			document.location=loc;
		});
		$(".delete").click(function() {
			$(this).closest(".container").addClass("poof");
			var rowId = $(this).attr("data-rowid");
			setTimeout(function() {
				$("#argument").load("AppController.servlet", {
					"return": "gui/preview/assets",
					remove_record: true,
					rowid: rowId
				}, function(data) {
					init();
					// Just in case the delete didn't work, revert to normal
					$(".container").removeClass("poof");
				});
			}, 1000);
		});
		return;
	}
	$(".card").click(function(event) {
	  var container = $(this).closest(".container");
	  var left = container.position().left;
	  var top = container.position().top;
	  //console.log(top + ", " + left);
	  if(container.hasClass("zoomed")) {
	    $(this).toggleClass("flipped");
	    container.addClass("semi");
	    setTimeout(function() {
	      container.removeClass("semi");
	    }, 500);
	  } else {
			unzoom(); // un-zoom other cards
	    if (left < 450) {
	      container.css("left","400px");
	    } else if (left < 800) {
	      container.css("left","100px");
	    }
	    if (top < 700) {
	    	container.css("top","600px");
	    }
	    var img = $(this).find("img.image");
	    img.attr("src", img.attr("data-bigsrc"));
	    container.addClass("zoomed");
	    abCardScale = 1.0;
	    setTimeout(function() {
		    $(".container").not(container).addClass("unzoomed");
	    }, 1000);
	  }
	  event.stopPropagation();
	}); // end of card click
	$(document).keypress(function(event) {
		var key = event.which;
		if (key !== 0) {
		  var charPressed = String.fromCharCode(key);
		  if ((charPressed == "-") || (charPressed == "_")) {
				abCardScale -= 0.1; // zoom out
		  } else if ((charPressed == "+") || (charPressed == "=")) {
				abCardScale += 0.1; // zoom in
		  }
		}
		$(".container.zoomed").css("-moz-transform","scale(" + abCardScale + ")");
		$(".container.zoomed").css("-webkit-transform","scale(" + abCardScale + ")");
		$(".container.zoomed").css("transform","scale(" + abCardScale + ")");
	});
	$("#argument").click(function() {
		unzoom();
	});
}