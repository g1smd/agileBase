var levelsList = [];

var currentLevel = 0;

$(document).ready(function() {
  $(".block").live('click', function() {
    var href = $(this).attr("href");
    moveDownTo(href);
  });
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
	if ($(".slide").size() == 0) {
		return;
	}
    // Initialize
    var slideshow = new SlideShow(query('.slide'));
    document.querySelector('#toggle-counter').addEventListener('click', toggleCounter, false);
    var counters = document.querySelectorAll('.counter');
    var slides = document.querySelectorAll('.slide');
}

function createLevel(levelUrl) {
  var jqLevel = $("<div class='level transparent'></div>");
  $("#levels").append(jqLevel);
  jqLevel.load(levelUrl, function() {
	var newLevel = {};
	newLevel.levelUrl = levelUrl;
	newLevel.title = jqLevel.find(".title").text();
	newLevel.levelContent = jqLevel[0];
	levelsList[currentLevel] = newLevel;
	jqLevel.removeClass("transparent").removeClass("invisible");
	updateBreadcrumb();
	initialiseSlides();
  });
}

function showCurrentLevel() {
  var jqLevelContent = $(levelsList[currentLevel].levelContent);
  jqLevelContent.removeClass("flyDown").removeClass("flyUp").removeClass("invisible").removeClass("transparent");
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
  }
  levelsList.splice(currentLevel, levelsList.length - currentLevel); // remove
  setTimeout(function() {
	$(".oldLevel").remove();
  }, 500);
  createLevel(levelUrl);
}

function updateBreadcrumb() {
  var jqBreadcrumb = $("#breadcrumb");
  jqBreadcrumb.children().remove();
  for (var level = 0; level < currentLevel; level++) {
	var title = levelsList[level].title;
	var url = levelsList[level].levelUrl;
	jqBreadcrumb.append("<a level='" + level + "' href='" + url + "'>" + title + "</a> / ");
  }
  var title = levelsList[currentLevel].title;
  jqBreadcrumb.append("<span class='currentLevel'"> + title + "</span>");
  alert(levelsList);
}

/* Utilities */
function cleanString(dirtyString) {
  return dirtyString.replace(/\W/g,"_");
}