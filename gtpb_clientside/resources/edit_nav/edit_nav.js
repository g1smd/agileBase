var levelsList = [];

var currentLevel = 0;

$(document).ready(function() {
  alert('ready');
  $(".block").click(function() {
	alert('.block click');
    var href = $(this).attr("href");
    alert(href);
    moveDownTo(href);
  });
  // Initialise home screen for user
  createLevel("AppController.servlet?return=gui/edit_nav/report");
});

function createLevel(levelId) {
  var newLevel = {};
  newLevel.levelId = levelId;
  levelsList[currentLevel] = newLevel;
  $("#levels").append("<div class='level invisible'></div>").load(levelId, function() {
	var jqLevel = $(this);
	var title = jqLevel.find(".title").text();
	levelsList[currentLevel].title = title;
	levelsList[currentLevel].levelContent = this;
	updateBreadcrumb();
  });
  $("#levels:last-child").removeClass("invisible");
}

function showCurrentLevel() {
  var jqLevelContent = $(levelsList[currentLevel].levelContent);
  jqLevelContent.removeClass("flyDown").removeClass("flyUp").removeClass("invisible");
}

function moveUp() {
  if (currentLevel == 0) {
	return;
  }
  var jqLevelContent = $(levelsList[currentLevel].levelContent);
  jqLevelContent.addClass("flyDown").addClass("invisible");
  currentLevel--;
  showCurrentLevel();
}

function moveDown() {
  if ((currentLevel + 1) >= levelsList.length) {
	return;
  }
  var jqLevelContent = $(levelsList[currentLevel].levelContent);
  jqLevelContent.addClass("flyUp").addClass("invisible");
  currentLevel++;
  showCurrentLevel();
}

function moveUpTo(levelId) {
  var jqLevelContent = $(levelsList[currentLevel].levelContent);
  // search for the levelId somewhere above the current level
  for (var level = 0; level < currentLevel; level++) {
	if (levelsList[level].levelId == levelId) {
	  jqLevelContent.addClass("flyDown").addClass("invisible");
	  currentLevel = level;
	  showCurrentLevel();
	  return;
	}
  }
  // levelId not found, start from scratch creating it as the top element
  jqLevelContent.addClass("invisible"); // no flyDown in this case
  $(".level").addClass("oldLevel");
  setTimeout(function() {
	$(".oldLevel").remove();
  }, 2000);
  levelsList = [];
  currentLevel = 0;
  createLevel(levelId);
}

function moveDownTo(levelId) {
  var jqLevelContent = $(levelsList[currentLevel].levelContent);
  jqLevelContent.addClass("flyUp").addClass("invisible");
  currentLevel++;
  //Check if there is a level below this one
  if (currentLevel < levelsList.length) {
    // Check if the level below is actually the one who'se ID we've been passed
    if (levelsList[currentLevel].levelId == levelId) {
      showCurrentLevel();
      return;
    }
  }
  // levelId not found, create a new child of the level above
  // First remove, the existing child and all sub-levels
  for (var level = currentLevel; level < levelsList.length; level++) {
	$("#" + levelsList[level].levelId).addClass("oldLevel");
  }
  levelsList.splice(currentLevel, levelsList.length - currentLevel); // remove
  setTimeout(function() {
	$(".oldLevel").remove();
  }, 2000);
  createLevel(levelId);
}

function updateBreadcrumb() {
  var jqBreadcrumb = $("#breadcrumb");
  jqBreadcrumb.children().remove();
  for (var level = 0; level < levelsList.length; level++) {
	var title = levelsList[level].title;
	var url = levelsList[level].levelId;
	jqBreadcrumb.append("<a href='" + url + "'>" + title + "</a> ");
  }
}

/* Utilities */
function cleanString(dirtyString) {
  return dirtyString.replace(/\W/g,"_");
}