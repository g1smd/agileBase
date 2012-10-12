var abCardScale = 1.0;

$(document).ready(function() {
	init();
  $("#filters td.leading").children("div").append("<a href='AppController.servlet?return=boot'><img id='home' src='resources/toolbar/agilebase.png' /></a>");
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
  $(".container").removeClass("zoomed unzoomed").removeAttr("style").find(".card").removeClass("flipped");
}

function init() {
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