var levelsList = [];

var currentLevel = 0;

var homeUrl = "AppController.servlet?return=gui/edit_nav/report";

var dataChanged = false;

$(document)
		.ready(
				function() {
					$("#breadcrumb a").live('click', function(event) {
						event.preventDefault();
						var href = $(this).attr("href");
						var level = $(this).attr("level");
						if (currentLevel > level) {
							moveUpTo(href, false);
						} else {
							moveDownTo(href);
						}
					});
					$(".block").live('click', function() {
						var href = $(this).attr("href");
						moveUpTo(href, true); // move up only if level already exists above,
						// otherwise move down
					});
					$("a.jumpto_table").live('click', function(event) {
						event.preventDefault();
						var href = $(this).attr("href");
						moveUpTo(href, false); // always move up
					});
					$("a.reference_link").live('click', function(event) {
						event.preventDefault();
						var href = $(this).attr("href");
						moveUpTo(href, true); // move up only if level already exists above,
						// otherwise move down
					});
					$("a.report_tooltip").live('click', function(event) {
						event.preventDefault();
						var jqLink = $(this);
						var href= jqLink.attr("href");
					  var id = jqLink.closest("li").attr("id");
					  var jqReportIncludingContent = $(".report_including_content");
					  jqReportIncludingContent.addClass("transparent");
					  jqReportIncludingContent.load(href, function() {
					  	var rowCount = $(".row_count").html();
							fSetCurrentOption(id, rowCount);
							jqReportIncludingContent.removeClass("transparent");
						});
					});
					// TODO: refactor new and clone into a single function
					$("button#control_new")
							.live(
									'click',
									function(event) {
										var internalTableName = $(this).attr("internaltablename");
										$
												.post(
														"AppController.servlet",
														{
															"return" : "gui/resources/xmlreturn_rowid",
															set_table : internalTableName,
															save_new_record : true
														},
														function(xml) {
															var jqXml = $(xml);
															if (jqXml.find("response").text() == "ok") {
																var rowId = jqXml.find("rowid").text();
																var levelUrl = "AppController.servlet?return=gui/edit_nav/edit&set_table="
																		+ internalTableName
																		+ "&set_row_id="
																		+ rowId;
																moveOverTo(levelUrl);
															} else {
																var errorMessage = jqXml.find("exception")
																		.text();
																alert("Unable to add record: " + errorMessage);
															}
														});
									});
					$("button#control_clone")
							.live(
									'click',
									function(event) {
										var internalTableName = $(this).attr("internaltablename");
										var rowId = $(this).attr("rowid");
										$
												.post(
														"AppController.servlet",
														{
															"return" : "gui/resources/xmlreturn_rowid",
															set_table : internalTableName,
															set_row_id : rowId,
															clone_record : true
														},
														function(xml) {
															var jqXml = $(xml);
															if (jqXml.find("response").text() == "ok") {
																rowId = jqXml.find("rowid").text();
																var levelUrl = "AppController.servlet?return=gui/edit_nav/edit&set_table="
																		+ internalTableName
																		+ "&set_row_id="
																		+ rowId;
																moveOverTo(levelUrl);
															} else {
																var errorMessage = jqXml.find("exception")
																		.text();
																alert("Unable to clone: " + errorMessage);
															}
														});
									});
					$("button#control_delete")
							.live(
									'click',
									function(event) {
										var internalTableName = $(this).attr("internaltablename");
										var rowId = $(this).attr("rowid");
										if (confirm("Delete this record?")) {
											$
													.post(
															"AppController.servlet",
															{
																"return" : "gui/administration/xmlreturn_fieldchange",
																remove_record : true,
																returntype : "xml",
																set_table : internalTableName,
																rowid : rowId
															},
															function(xml) {
																var jqXml = $(xml);
																if (jqXml.find("response").text() == 'ok') {
																	dataChanged = true; // mark level up for
																	// reload
																	moveUp();
																} else {
																	var prompt = jqXml.find("exception").text()
																			+ '.\n\nDelete all of this?';
																	if (confirm(prompt)) {
																		$
																				.post(
																						"AppController.servlet",
																						{
																							"return" : "gui/administration/xmlreturn_fieldchange",
																							remove_record : true,
																							returntype : "xml",
																							set_table : internalTableName,
																							rowid : rowId,
																							cascadedelete : true
																						},
																						function(xml) {
																							jqXml = $(xml);
																							if (jqXml.find("response").text() == 'ok') {
																								dataChanged = true;
																								moveUp();
																							} else {
																								var message = jqXml.find(
																										"exception").text();
																								alert('Deletion failed.\n\n'
																										+ message);
																							}
																						});
																	}
																}
															});
										}
									});
					$("button#control_print")
							.live(
									'click',
									function() {
										var internalTableName = $(this).attr("internaltablename");
										var rowId = $(this).attr("rowid");
										var oPrintWin = window
												.open(
														'AppController.servlet?return=gui/printouts/pane2_printout_wrapper&set_table='
																+ internalTableName + '&set_row_id=' + rowId,
														'print_window',
														'toolbar=no,location=no,directories=no,status=no,copyhistory=no,menubar=no,resizable=yes,dialog=yes');
									});
					// Initialise home screen for user
					createLevel(homeUrl);
					initialiseHeight();
					$(window).resize(function() {
						initialiseHeight();
					});
				});

// Nasty JS height, can't we get some pure CSS to work?
function initialiseHeight() {
	var levelHeight = $(window).height() - 30;
	$("#levels").height(levelHeight);
}

function initialiseSlides() {
	$(".searchbox").keyup(
			function() {
				var jqSearchBox = $(this);
				jqSearchBox.addClass("changed");
				var filterString = jqSearchBox.val();
				var internalReportName = $("#searchbox").attr("internalreportname");
				$.get(
						"AppController.servlet?return=gui/edit_nav/report_content&set_report="
								+ internalReportName
								+ "&set_global_report_filter_string=true&filterstring="
								+ filterString, function(data) {
							// response has come back from the
							// server, check it isn't out of
							// date
							if (jqSearchBox.val() != filterString) {
								return;
							}
							$("#homeContent").html(data);
							jqSearchBox.removeClass("changed");
						});
				// also filter the table list
				$(".jumpto_table").each(
						function() {
							var jqLink = $(this);
							var tableName = jqLink.text().toLowerCase();
							var title = jqLink.attr("title").toLowerCase();
							if ((title.indexOf(filterString.toLowerCase()) > -1)
									|| (tableName.indexOf(filterString.toLowerCase()) > -1)) {
								jqLink.removeClass("invisible");
							} else {
								jqLink.addClass("invisible");
							}
						});
				// show all rather than none
				if ($(".jumpto_table").size() == $(".jumpto_table.invisible").size()) {
					$(".jumpto_table").removeClass("invisible");
				}
			});

	pane1Setup();
	
	if ($(".slide").size() == 0) {
		return;
	}

	fComboComponents();
	fRelationPickers();
	fDatePickers();
	fSexyUpload();
	fTwitter();

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
}

/**
 * Called by fRelationPickers() in tabs.js when a relation is changed
 * 
 * @param oHidden
 *          The hidden field containing the row ID of the new selection and
 *          other details
 */
function relationChangeActions(oHidden) {
	var jqHidden = $(oHidden);
	var rowId = jqHidden.val();
	var internalFieldName = jqHidden.attr("internalfieldname");
	var internalTableName = jqHidden.attr("internaltablename");
	var relatedTableInternalName = jqHidden.attr("gtpb_rowidinternaltablename");
	var snippetId = "dependent_relation_" + internalTableName + "_"
			+ relatedTableInternalName;
	$
			.post(
					"AppController.servlet",
					{
						"return" : "gui/edit_nav/relation_snippet_direct",
						set_custom_field : true,
						fieldkey : "relation_snippet_field",
						custominternaltablename : internalTableName,
						custominternalfieldname : internalFieldName
					},
					function(data) {
						var jqSnippet = $("#" + snippetId);
						jqSnippet.html(data);
						jqSnippet.addClass("active");
						jqSnippet.attr("rowid", rowId);
						if (!jqSnippet.hasClass("active")) {
							jqSnippet.addClass("active");
							jqSnippet.click(relationClick(jqSnippet));
						}
					});
}

function relationClick(jqDependentTable) {
	var internalTableName = jqDependentTable
	.attr("internaltablename");
	var rowId = jqDependentTable.attr("rowid");
	var levelUrl = "AppController.servlet?return=gui/edit_nav/edit&set_table=" + internalTableName + "&set_row_id=" + rowId;
	moveUpTo(levelUrl, true);
}

function initialiseDependencies() {
	// Initialize
	var jqLevel = $(levelsList[currentLevel].levelContent);
	var slideshow = new SlideShow(jqLevel.find(".slide").toArray());
	var counters = document.querySelectorAll('.counter');
	var slides = document.querySelectorAll('.slide');
	jqLevel
			.find(".dependent_table")
			.click(
					function() {
						var jqDependentTable = $(this);
						var internalTableName = jqDependentTable.attr("internaltablename");
						if (!jqDependentTable.hasClass("active")) {
							if (jqDependentTable.hasClass("has_new")) {
								var levelUrl = "AppController.servlet?return=gui/edit_nav/edit&set_table="
										+ internalTableName + "&save_new_record=true";
								moveDownTo(levelUrl);
							}
							return;
						}
						if (jqDependentTable.hasClass("related")) {
							relationClick(jqDependentTable);
						} else {
							// find index of slide to go to
							var slideId = jqDependentTable.attr("id").replace("dependent_table_","slide_");
							var slideNum = $("#" + slideId).index();
							slideshow.go(slideNum + 1);
							$(".presentation").scrollTop(0);
						}
					});
	// live because only first few slide are created on load?
	$(".rewind").live('click', function() {
		var slideNum = $(this).closest(".slide").find(".counter").text();
		// go(1) doesn't seem to work always here
		for ( var i = 0; i < slideNum; i++) {
			slideshow.prev();
		}
	});
	$(".slide_icon").live('click', function() {
		moveUp();
	});

	slideshow.go(1);
	$(".presentation").scrollTop(0);
}

/*
 * For any table (represented by a slide), there can be dependent tables that
 * link to it. Show these as slides to the right
 * 
 * Return true if there were any dependent slides
 */
function loadDependentSlides() {
	var jqSlides = $(levelsList[currentLevel].levelContent).find(".slides");
	var firstSlide = jqSlides.find(".slide").first();
	if (firstSlide.find(".dependent_tables").children().size() > 0) {
		$.get("AppController.servlet?return=gui/edit_nav/dependent_slides",
				function(data) {
					jqSlides.append(data);
					initialiseDependencies();
					dependentSnippets();
				});
	}
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
		loadDependentSlides();
		initialiseSlides();
		window.scrollTo(0, 0);
		firefoxBugWorkaround();
		if (levelUrl == homeUrl) {
			var id = $(".pane1_id").text();
	  	var rowCount = $(".row_count").html();
			fSetCurrentOption(id, rowCount);
		}
	});
	if ((levelUrl.indexOf("save_new_record") > -1)
			|| (levelUrl.indexOf("clone_record") > -1)
			|| (levelUrl.indexOf("remove_record") > -1)) {
		dataChanged = true;
	}
}

function showCurrentLevel() {
	var jqLevel = $(levelsList[currentLevel].levelContent);
	jqLevel.removeClass("flyDown").removeClass("flyUp").removeClass("invisible")
			.removeClass("transparent");
	updateBreadcrumb();
	window.scrollTo(0, 0);
	var levelUrl = levelsList[currentLevel].levelUrl;
	// reload slide and related slides in case related content has changed
	if (dataChanged) {
		jqLevel.load(levelUrl, function() {
			levelsList[currentLevel].levelContent = jqLevel[0];
			loadDependentSlides();
			initialiseSlides();
		});
		dataChanged = false;
	}
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

/**
 * @param fallbackToDown
 *          By default, moveUpTo will create the level specified as the new top
 *          level if it's not found anywhere above the current slide, however if
 *          fallbackToDown=true is specified, it will call moveDownTo(levelUrl)
 *          instead
 */
function moveUpTo(levelUrl, fallbackToDown) {
	var jqLevelContent = $(levelsList[currentLevel].levelContent);
	// Search for the levelUrl somewhere above the current level.
	// If the levelUrl represents a 'report' level, then allow it to be the new
	// root
	// otherwise ensure the current root stays
	var startLevel = 0;
	if (levelUrl.indexOf("gui/edit_nav/report") == -1) {
		startLevel = 1;
	}
	for ( var level = startLevel; level < currentLevel; level++) {
		if (levelsList[level].levelUrl == levelUrl) {
			if (currentLevel > 0) {
				jqLevelContent.addClass("flyDown");
			}
			jqLevelContent.addClass("transparent");
			setTimeout(function() {
				jqLevelContent.addClass("invisible");
			}, 500);
			currentLevel = level;
			if ((levelUrl.indexOf("save_new_record") > -1)
					|| (levelUrl.indexOf("clone_record") > -1)
					|| (levelUrl.indexOf("remove_record") > -1)) {
				dataChanged = true;
			}
			showCurrentLevel();
			return;
		}
	}
	if (fallbackToDown == true) {
		moveDownTo(levelUrl)
	} else {
		// start from scratch creating it as the top element
		if (currentLevel > startLevel) {
			jqLevelContent.addClass("flyDown");
		}
		jqLevelContent.addClass("transparent");
		if (startLevel == 0) {
			$(".level").addClass("oldLevel");
		} else {
			$(".level").not(":first").addClass("oldLevel");
		}
		$(".presentation").remove(); // more than one pres in the DOM can cause
		// problems
		setTimeout(function() {
			$(".oldLevel").remove();
		}, 500);
		var firstLevelObject = levelsList[0];
		levelsList = [];
		if (startLevel == 1) {
			levelsList.push(firstLevelObject);
		}
		currentLevel = startLevel;
		createLevel(levelUrl);
	}
}

/**
 * Replace the existing level with a new one - don't move up or down
 */
function moveOverTo(levelUrl) {
	var jqLevelContent = $(levelsList[currentLevel].levelContent);
	jqLevelContent.addClass("transparent").addClass("oldLevel");
	// First remove, the existing level and all sub-levels
	for ( var level = currentLevel; level < levelsList.length; level++) {
		$(levelsList[level].levelContent).addClass("oldLevel");
		$(levelsList[level].levelContent).find(".presentation").remove();
	}
	levelsList.splice(currentLevel, levelsList.length - currentLevel); // remove
	setTimeout(function() {
		$(".oldLevel").remove();
	}, 500);
	createLevel(levelUrl);
}

function moveDownTo(levelUrl) {
	var jqLevelContent = $(levelsList[currentLevel].levelContent);
	jqLevelContent.addClass("flyUp").addClass("transparent");
	setTimeout(function() {
		jqLevelContent.addClass("invisible");
	}, 500);
	currentLevel++;
	// Check if there is a level below this one
	if (currentLevel < levelsList.length) {
		// Check if the level below is actually the one whose ID we've been
		// passed
		if (levelsList[currentLevel].levelUrl == levelUrl) {
			if ((levelUrl.indexOf("save_new_record") > -1)
					|| (levelUrl.indexOf("clone_record") > -1)
					|| (levelUrl.indexOf("remove_record") > -1)) {
				dataChanged = true;
			}
			showCurrentLevel();
			return;
		}
	}
	// levelUrl not found, create a new child of the level above
	// First remove, the existing child and all sub-levels
	for ( var level = currentLevel; level < levelsList.length; level++) {
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
	for ( var level = 0; level < currentLevel; level++) {
		var title = levelsList[level].title;
		var url = levelsList[level].levelUrl;
		jqBreadcrumb.append("<a level='" + level + "' href='" + url + "'>" + title
				+ "</a> / ");
	}
	var title = levelsList[currentLevel].title;
	if (currentLevel == 0) {
		title = "home";
	}
	jqBreadcrumb.append("<span class='currentLevel'>" + title + "</span>");
}

function dependentSnippets() {
	var jqLevel = $(levelsList[currentLevel].levelContent);
	var slides = jqLevel.find(".slide");
	var firstSlide = slides.first();
	if (firstSlide.find(".dependent_table.active").size() > firstSlide.find(
			".dependent_table.related.active").size()) {
		// has already been initialised
		return;
	}
	var remainingSlides = slides.not(":first");
	remainingSlides.each(function() {
		var jqSlide = $(this);
		var snippetHolderId = jqSlide.attr("id").replace("slide_",
				"dependent_table_");
		var jqSnippets = $("#" + snippetHolderId);
		var numCards = jqSlide.find(".block.current").size();
		if (numCards > 0) {
			jqSnippets.addClass("active");
			if (numCards == 100) {
				numCardsText = "many";
			} else {
				numCardsText = numCards;
			}
			if (numCardsText > 1) {
			  jqSnippets.find(".count").text("(" + numCardsText + ")");
			}
		}
		var firstTwoCards = jqSlide.find(".block.current:lt(2)");
		firstTwoCards.each(function() {
			var snippetText = $(this).text();
			jqSnippets.append("<div class='snippet'>" + snippetText + "</div>");
		});
	});
	// add 'new' buttons to any unactivated links
	firstSlide
			.find(".dependent_table")
			.each(
					function() {
						var jqSnippets = $(this);
						if (!jqSnippets.hasClass("active")) {
							if ((!jqSnippets.hasClass("has_new"))
									&& (!jqSnippets.hasClass("related"))) {
								jqSnippets.addClass("has_new");
								var snippetHtml = "&nbsp;" + jqSnippets.find("h1").text();
								jqSnippets.find("h1").html(snippetHtml);
							}
						}
					});
}

/**
 * Workaround for https://bugzilla.mozilla.org/show_bug.cgi?id=660699
 */
function firefoxBugWorkaround() {
	return; /* actually, the bug isn't that bad in our context */
	if ($.browser.mozilla) {
		$(".centrebox").css("float", "none");
	}
}
