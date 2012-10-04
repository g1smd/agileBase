$(document).ready(function() {
	init();
	$("#fieldFilters").show();
	$("#fieldFilters input").removeAttr("onkeyup");
	$("#fieldFilters input").keyup(function(event) {
		new fSetFilter(event, this, function() {
			$("#argument").load("AppController.servlet", {
				"return": "gui/preview/argument",
				abCache: (new Date()).getTime()
			}, function() {
				init();
			});
		});
	});
	// For report chooser
	$("button").click(function() {
		$("button").fadeOut();
		var internalReportName = $(this).val();
		var internalTableName = $(this).attr("data-internaltablename");
		document.location = "AppController.servlet?return=gui/preview/argument_presenter_standalone&set_report=" + internalReportName + "&set_table=" + internalTableName;
	});
});

function unzoom() {
	setTimeout(function() {
		$(".container").removeClass("zoomed unzoomed").removeAttr("style").find(".card").removeClass("flipped");
	}, 1000);
}

function init() {
	$(".card").click(function(event) {
	  var container = $(this).closest(".container");
	  var left = container.position().left;
	  var top = container.position().top;
	  console.log(top + ", " + left);
	  if(container.hasClass("zoomed")) {
	    $(this).toggleClass("flipped");
	    container.addClass("semi");
	    setTimeout(function() {
	      container.removeClass("semi");
	    }, 500);
	  } else {
			unzoom(); // un-zoom other cards
	    if (left < 400) {
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
	    //setTimeout(function() {
		    $(".container").not(container).addClass("unzoomed");	    	
	    //}, 1000);
	  }
	  event.stopPropagation();
	}); // end of card click
	$("#argument").click(function() {
		unzoom();
	});
}