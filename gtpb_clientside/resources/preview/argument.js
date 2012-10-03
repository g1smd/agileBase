$(document).ready(function() {
	$(".card").click(function(event) {
	  var container = $(this).closest(".container");
	  var left = container.position().left;
	  console.log(left);
	  if(container.hasClass("zoomed")) {
	    $(this).toggleClass("flipped");
	    container.addClass("semi");
	    setTimeout(function() {
	      container.removeClass("semi");
	    }, 500);
	  } else {
	    if (left < 400) {
	      container.css("left","400px");
	    } else if (left < 800) {
	      container.css("left","100px");
	    }
	    var img = $(this).find("img.image");
	    img.attr("src", img.attr("data-bigsrc"));
	    container.addClass("zoomed");
	    setTimeout(function() {
		    $(".container").not(container).addClass("unzoomed");	    	
	    }, 1000);
	  }
	  event.stopPropagation();
	});
	$("body").click(function() {
		unzoom();
	});
});

function unzoom() {
	$(".container").removeClass("zoomed flipped unzoomed").removeAttr("css");
}
