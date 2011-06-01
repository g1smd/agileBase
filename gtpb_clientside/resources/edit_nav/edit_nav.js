var levelsList = [];

var currentLevel = 0;

$(document).ready(function() {
  $("#breadcrumb a").live('click', function(event) {
	event.preventDefault();
	var href = $(this).attr("href");
	var level = $(this).attr("level");
	if (currentLevel > level) {
	  moveUpTo(href);
	} else {
	  moveDownTo(href);
	}
  });
  // Initialise home screen for user
  createLevel("AppController.servlet?return=gui/edit_nav/report");
});

function initialiseSlides() {
    $(".block").click(function() {
      var href = $(this).attr("href");
      moveDownTo(href);
    });
    $("#searchbox").keyup(function() {
      var filterString = $("#searchbox").val();
      $.get("AppController.servlet?return=gui/edit_nav/report_content&set_global_report_filter_string=" + filterString, function(data) {
        // response has come back from the server, check it isn't out of date
    	if($("#searchbox").val() != filterString) {
    	  return;
    	}
    	$("#homeContent").html(data);
      });
    });
	
	if ($(".slide").size() == 0) {
	  return;
	}
    // Initialize
    var slideshow = new SlideShow($(levelsList[currentLevel].levelContent).find(".slide").toArray());
    var counters = document.querySelectorAll('.counter');
    var slides = document.querySelectorAll('.slide');
    
    fComboComponents();
    fRelationPickers();
    fDatePickers();
    /* fTwitter(); */
    
    $("input").focus(function() {
  	  $(this).addClass("editing");
    });
    $("input").blur(function() {
	  $(this).removeClass("editing");
    });
    $("textarea").focus(function() {
	  $(this).addClass("editing");
    });
    $("textarea").blur(function() {
	  $(this).removeClass("editing");
    });      
    
    $(".dependent_table").click(function() {
      slideshow.go($(this).index() + 2);
      $(".presentation").scrollTop(0);
    });
    // live because only first few slide are created on load?
    $(".rewind").live('click', function() {
      var slideNum = $(this).closest(".slide").find(".counter").text();
  	  //go(1) doesn't seem to work always here
      for(var i =0; i < slideNum; i++) {
        slideshow.prev();
      }
    });
    slideshow.go(1);
    $(".presentation").scrollTop(0);
}

/* For any table (represented by a slide), there can be dependent tables that link to it. 
 * Show these as slides to the right
 * 
 * Return true if there were any dependent slides
 */
function loadDependentSlides() {
  var jqSlides = $(levelsList[currentLevel].levelContent).find(".slides");
  var firstSlide = jqSlides.find(".slide").first();
  if (firstSlide.find(".dependent_tables").children().size() > 0) {
    $.get("AppController.servlet?return=gui/edit_nav/dependent_slides", function(data) {
	  jqSlides.append(data);
	  initialiseSlides();
	  dependentSnippets();
	  return true;
    });
  }
  return false;
}

function createLevel(levelUrl) {
  var jqLevel = $("<div class='level transparent'></div>");
  $("#levels").append(jqLevel);
  jqLevel.load(levelUrl, function() {
	var newLevel = {};
	newLevel.levelUrl = levelUrl;
	newLevel.title = jqLevel.find(".breadcrumb_title").text();
	newLevel.levelContent = jqLevel[0];
	levelsList[currentLevel] = newLevel;
	jqLevel.removeClass("transparent").removeClass("invisible");
	updateBreadcrumb();
	if (!loadDependentSlides()) {
	  initialiseSlides();
	}
	window.scrollTo(0,0);
	firefoxBugWorkaround();
  });
}

function showCurrentLevel() {
  var jqLevelContent = $(levelsList[currentLevel].levelContent);
  jqLevelContent.removeClass("flyDown").removeClass("flyUp").removeClass("invisible").removeClass("transparent");
  updateBreadcrumb();
  window.scrollTo(0,0);
}

function moveUp() {
  if (currentLevel == 0) {
	return;
  }
  var jqLevelContent = $(levelsList[currentLevel].levelContent);
  jqLevelContent.addClass("flyDown").addClass("transparent");
  setTimeout(function() {
	jqLevelContent.addClass("invisible");
  }, 500);
  currentLevel--;
  showCurrentLevel();
}

function moveDown() {
  if ((currentLevel + 1) >= levelsList.length) {
	return;
  }
  var jqLevelContent = $(levelsList[currentLevel].levelContent);
  jqLevelContent.addClass("flyUp").addClass("transparent");
  setTimeout(function() {
	jqLevelContent.addClass("invisible");
  }, 500);
  currentLevel++;
  showCurrentLevel();
}

function moveUpTo(levelUrl) {
  var jqLevelContent = $(levelsList[currentLevel].levelContent);
  // search for the levelUrl somewhere above the current level
  for (var level = 0; level < currentLevel; level++) {
	if (levelsList[level].levelUrl == levelUrl) {
	  jqLevelContent.addClass("flyDown").addClass("transparent");
	  setTimeout(function() {
		jqLevelContent.addClass("invisible");
	  }, 500);
	  currentLevel = level;
	  showCurrentLevel();
	  return;
	}
  }
  // levelUrl not found, start from scratch creating it as the top element
  jqLevelContent.addClass("flyDown").addClass("transparent");
  $(".level").addClass("oldLevel");
  $(".presentation").remove(); // more than one pres in the DOM can cause problems
  setTimeout(function() {
	$(".oldLevel").remove();
  }, 500);
  levelsList = [];
  currentLevel = 0;
  createLevel(levelUrl);
}

function moveDownTo(levelUrl) {
  var jqLevelContent = $(levelsList[currentLevel].levelContent);
  jqLevelContent.addClass("flyUp").addClass("transparent");
  setTimeout(function() {
	jqLevelContent.addClass("invisible");
  }, 500);
  currentLevel++;
  //Check if there is a level below this one
  if (currentLevel < levelsList.length) {
    // Check if the level below is actually the one whose ID we've been passed
    if (levelsList[currentLevel].levelUrl == levelUrl) {
      showCurrentLevel();
      return;
    }
  }
  // levelUrl not found, create a new child of the level above
  // First remove, the existing child and all sub-levels
  for (var level = currentLevel; level < levelsList.length; level++) {
	$(levelsList[level].levelContent).addClass("oldLevel");
	$(levelsList[level].levelContent).find(".presentation").remove();
  }
  levelsList.splice(currentLevel, levelsList.length - currentLevel); // remove
  setTimeout(function() {
	$(".oldLevel").remove();
  }, 500);
  createLevel(levelUrl);
}

function updateBreadcrumb() {
  var jqBreadcrumb = $("#breadcrumb");
  jqBreadcrumb.html("<span id='you_are_here'>you are here: </span>");
  for (var level = 0; level < currentLevel; level++) {
	var title = levelsList[level].title;
	var url = levelsList[level].levelUrl;
	jqBreadcrumb.append("<a level='" + level + "' href='" + url + "'>" + title + "</a> / ");
  }
  var title = levelsList[currentLevel].title;
  jqBreadcrumb.append("<span class='currentLevel'>" + title + "</span>");
}

function dependentSnippets() {
  var jqLevel = $(levelsList[currentLevel].levelContent);
  var slides = jqLevel.find(".slide");
  var firstSlide = slides.first();
  var remainingSlides = slides.not(":first");
  remainingSlides.each(function() {
	var jqSlide = $(this);
	var tableId = jqSlide.attr("id").replace("slide_","");
	var jqSnippets = $("#dependent_table_" + tableId);
	var numCards = jqSlide.find(".block.current").size();
	if (numCards > 0) {
	  jqSnippets.addClass("active");
	  jqSnippets.find(".count").text(numCards + " ");
	}
	var firstCards = jqSlide.find(".block.current:lt(2)");
	firstCards.each(function() {
	  var snippetText = $(this).text();
	  jqSnippets.append("<div class='snippet'>" + snippetText + "</div>");
	});
  });
}

/**
 * Workaround for https://bugzilla.mozilla.org/show_bug.cgi?id=660699
 */
function firefoxBugWorkaround() {	
  if ($.browser.mozilla) {
	$(".centrebox").css("float","none");
  }
}
