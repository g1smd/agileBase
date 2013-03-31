/**
 * JS used for the simple interface
 */
$(document).ready(function() {
	if ($("#infovis").size() > 0) {
		loadTreemap();
	} else if ($("#tile_suggestions").size() > 0) {
		tileSuggestions();
	} else {
		tileEvents();
		// Focus on first record
		$(".tile.large .report_data_row:first-child").mouseenter();
		// If only one tile, expand it
		if ($(".tile").not(".large").not(".adder").not(".focus").size() == 1) {
			$(".tile").not(".large").not(".adder").not(".focus").click();
		}
	}
});

var abTileColours = [ "blue", "yellow", "green", "purple", "pink", "turquoise" ];

function tileEvents() {
	commonTileEvents();
	dataStreamEvents();
	focusEvents();
}

function tileSuggestions() {
	var tileCount = 0;
	$("#tile_suggestions span").each(function() {
		var tileType = "data_link"; // The default tile type
		if (tileCount == 0) {
			// The most popular report will have a large tile
			tileType = "data_stream";
		}
		var internalReportName = $(this).attr("data-internalreportname");
		var internalTableName = $(this).attr("data-internaltablename");
		var reportName = $(this).text();
		$.ajaxq("tile_suggestions", {
			url : "AppController.servlet",
			type : "POST",
			data : {
				"return" : "blank",
				add_tile : true,
				colour : abTileColours[tileCount % abTileColours.length],
				internaltablename : internalTableName,
				internalreportname : internalReportName,
				tiletype : tileType
			}
		});
		tileCount++;
	});
	$.ajaxq("tile_suggestions", {
		url : "AppController.servlet",
		type : "POST",
		data : {
			"return" : "s/tiles/tiles",
		},
		success : function(data) {
			$("#tiles").html(data);
			tileEvents();
		}
	});
}

function focusEvents() {
	/** http://tweet.seaofclouds.com/ */
	// Fetch 20 tweets, but filter out @replies, and display only 1:
	$(".twitter").each(function() {
		if ($(this).hasClass("tweetRegistered")) {
			return;
		} else {
			$(this).addClass("tweetRegistered");
		}
		var username = $(this).attr("data-username");
		$(this).tweet({
			avatar_size : 48,
			count : 1,
			fetch : 20,
			filter : function(t) {
				return !/^@\w+/.test(t["tweet_raw_text"]);
			},
			username : username
		}).bind("loaded", function() {
			$(this).find("a").attr("target", "_blank");
		});
	});
}

function commonTileEvents() {
	$(".backHome").click(function() {
		backHome();
	});
	$(".removeTile").click(function() {
		removeTile();
	});
	$("#fieldFilters input").click(function(event) {
		event.stopPropagation();
	});
	$(".tile").click(function() {
		var tile = $(this);
		if (tile.hasClass("expanded") || tile.hasClass("focus")) {
			return;
		}
		expandTile(tile);
		if (tile.hasClass("calendar")) {
			$("#calendar").removeClass("notfocus");
			$("#agenda").addClass("notfocus");
			$("#report_selection_header").removeClass("notfocus");
			if($("#report_selection input:checked").size() == 0) {
				// Show calendar report chooser if no reports chosen
			  $("#report_selection").removeClass("notfocus");
			}
			loadCalendar($("#calendar"));
			tileLoaded(tile, false);
		} else {
			var template = "s/tiles/" + tile.attr("data-type");
			var internalTileName = tile.attr("data-internaltilename");
			tile.find(".content").load("AppController.servlet", {
				"return" : template,
				set_tile : internalTileName
			}, function() {
				tileLoaded(tile, false);
			});
		}
		tile.find(".content").removeClass("notfocus");
	});
	$(".sideAction.backToView").click(function() {
		tileLoaded($(".tile.expanded"), false);
	});
	$(".sideAction.newRecord").click(function() {
		var internalTableName = $(".tile.expanded").attr("data-internaltablename");
		newRecord(internalTableName);
	});
	$(".sideAction.cloneRecord").click(function() {
		var internalTableName = $(".tile.expanded").attr("data-internaltablename");
		cloneRecord(internalTableName);
	});
	$(".tile.large .add").click(function(event) {
		event.stopPropagation();
		var tile = $(this).closest(".tile");
		var internalTableName = tile.attr("data-internaltablename");
		expandTile(tile);
		newRecord(internalTableName);
	});
	$(".sideAction.removeRecord").click(function() {
		if (confirm("Delete this record?")) {
			fDeleteObj("remove_record", "rowid");
		}
	});
}

function loadCalendar(calendarElement) {
	calendarElement
			.fullCalendar({
				header : {
					left : 'title',
					center : 'month,agendaWeek,agendaDay',
					right : 'today prev,next'
				},
				editable : true,
				eventRender : function(event, jqElement, view) {
					if ((view.name == 'month')
							|| ((view.name == 'agendaWeek') && event.allDay)) {
						jqElement.height(15);
					}
				},
				eventClick : function(calEvent, jsEvent, view) {
					var eventId = calEvent.id;
					if (fMobileDevice()) {
						document.location = "AppController.servlet?return=gui/mobile/calendar_wizard&set_custom_string=true&key=calendar_wizard_template&value=edit_event&set_table="
								+ calEvent.internalTableName + '&set_row_id=' + calEvent.rowId;
					} else {
						fShowModalDialog('gui/calendar/edit_event&set_table='
								+ calEvent.internalTableName + '&set_row_id=' + calEvent.rowId,
								'edit event', fEditEventOK, 'ok', 'width=90%; height=95%');
					}
				},
				eventDrop : function(event, dayDelta, minuteDelta, allDay, revertFunc,
						jsEvent, ui, view) {
					var eventDate = event.start;
					var options = {
						'return' : 'blank',
						'update_record' : 'true',
						'set_table' : event.internalTableName,
						'set_row_id' : event.rowId,
						abCache : new Date().getTime()
					}
					// the new event date
					options[event.dateFieldInternalName + '_years'] = eventDate
							.getFullYear();
					options[event.dateFieldInternalName + '_months'] = eventDate
							.getMonth() + 1;
					options[event.dateFieldInternalName + '_days'] = eventDate.getDate();
					if (event.allDay) {
						options[event.dateFieldInternalName + '_hours'] = 0;
						options[event.dateFieldInternalName + '_minutes'] = 0;
					} else {
						options[event.dateFieldInternalName + '_hours'] = eventDate
								.getHours();
						options[event.dateFieldInternalName + '_minutes'] = eventDate
								.getMinutes();
					}
					// TODO: visually change the event element while saving: add then
					// remove a CSS class
					$.post("AppController.servlet", options);
				},
				dayClick: function(date, allDay, jsEvent, view) {
					alert("day click");
					var dayElement = $(this);
					var addEventElement = dayElement.append("<div class='addEvents' class='transition notfocus'></div>");
					var addEventElement = dayElement.find(".addEvents");
					$("#report_selection_header span").each(function() {
						addEventElement.append("<span class='addEvent'>add " + $(this).attr("data-singulartablename") + "</span>");
					});
					addEventElement.removeClass("notfocus");
				},
				minTime : 6
			});
	// Show initial calendars
	$("#report_selection input:checked").each(function() {
		addRemoveCalendar(this);
	});
	// Re-render calendar once expand animation has completed
	setTimeout(function() {
		$(window).resize()
	}, 500);
}

// Add remove a JSON calendar feed
// checkboxElement is the checkbox to select/deselect a report
function addRemoveCalendar(checkboxElement) {
	var jqCheckbox = $(checkboxElement);
	var internalTableName = jqCheckbox.attr("internaltablename");
	var internalReportName = jqCheckbox.attr("internalreportname");
	var reportName = jqCheckbox.parent().text();
	var reportTooltip = jqCheckbox.parent().attr("title");
	var singularTableName = jqCheckbox.parent().attr("data-singulartablename");
	var feedUrl = "AppController.servlet?return=gui/calendar/feed&internaltablename="
			+ internalTableName + "&internalreportname=" + internalReportName;
	var eventColour = jqCheckbox.siblings("span").css('background-color');
	var textColour = jqCheckbox.siblings("span").css('color');
	if (jqCheckbox.is(":checked")) {
		var eventSource = {
			url : feedUrl,
			color : eventColour,
			textColor : textColour,
		}
		$("#calendar").fullCalendar('addEventSource', eventSource);
		var legendElement = $("<span class='legend_report report_"
				+ internalReportName + "' id='legend_" + internalReportName
				+ "' title='" + reportTooltip + "' data-singulartablename='" + singularTableName + "'>" + reportName + "</span>");
		$("#report_selection_header").append(legendElement);
	} else {
		$("#calendar").fullCalendar('removeEventSource', feedUrl);
		var legendId = "legend_" + internalReportName;
		$("#" + legendId).remove();
	}
}

function expandTile(tile) {
	$(".tile").not(tile).addClass("notfocus");
	var title = tile.attr("title");
	$("#title").find("h1").text(title);
	var colour = tile.attr("data-colour");
	var allColours = abTileColours.join(" ");
	$("body").removeClass(allColours).addClass(colour);
	tile.addClass("expanded");
	$(".header.row").addClass("expanded");
	tile.find(".tile_icon").addClass("notfocus");
	tile.find(".title").addClass("notfocus");
	/* leave some space for controls on left and right */
	$("#tiles").addClass("padded");
}

/** Data stream tile specific events */
function dataStreamEvents() {
	var searchBox = $(".tile.data_stream input[type=search]");
	searchBox.click(function(event) {
		event.stopPropagation();
	});
	dataStreamFocus();
	searchBox.keyup(function(event) {
		$(this).addClass("changed");
		var filterString = $(this).val();
		var internalTileName = $(this).closest(".tile").attr(
				"data-internaltilename");
		var internalTableName = $(this).closest(".tile").attr(
				"data-internaltablename");
		var internalReportName = $(this).closest(".tile").attr(
				"data-internalreportname");
		$(this).closest(".tile").find(".content").load("AppController.servlet", {
			"return" : "s/tiles/data_stream",
			set_global_report_filter_string : true,
			filterstring : filterString,
			set_tile : internalTileName,
			set_table : internalTableName,
			set_report : internalReportName
		}, function() {
			$(this).removeClass("changed");
			dataStreamFocus();
		});
	});
}

function dataStreamFocus() {
	$(".tile.data_stream .report_data_row").mouseenter(
			function() {
				var row = $(this);
				var focusTile = $(".tile[data-type=focus]");
				var internalTableName = $(this).closest(".tile").attr(
						"data-internaltablename");
				var rowId = $(this).attr("data-rowid");
				focusTile.find(".content").load("AppController.servlet", {
					"return" : "s/tiles/focus/focus",
					set_table : internalTableName,
					set_custom_integer : true,
					integerkey : "focus_row_id",
					customintegervalue : rowId
				}, function() {
					var rowTitle = row.find(".row_title").text();
					focusTile.find(".title").text(rowTitle);
					focusEvents();
				});
			});
	$(".tile.data_stream .report_data_row").click(
			function(event) {
				event.stopPropagation();
				var container = $(this).closest(".content");
				var internalTableName = $(this).closest(".tile").attr(
						"data-internaltablename");
				var rowId = $(this).attr("data-rowid");
				loadEdit(container, internalTableName, rowId);
			});
}

/**
 * This function runs when a tile is clicked to expand it and content has loaded
 */
function tileLoaded(tile, editing) {
	var tileType = tile.attr("data-type");
	$(".sideAction.backHome").addClass("expanded");
	if (editing) {
		showEditControls();
		$(".sideAction.backToView").removeClass("expanded");
	} else {
		$(".sideAction.removeRecord").removeClass("expanded");
		$(".sideAction.cloneRecord").removeClass("expanded");
	}
	if (tileType != "adder") {
		$(".sideAction.removeTile").addClass("expanded");
	}
	// Hide all icons otherwise they can be clicked
	$(".tile_icon i").addClass("notfocus");
	// Tile has a report but is not in the edit screen
	if (tile.attr("data-internalreportname") && (!editing)) {
		$(".sideAction.newRecord").addClass("expanded");
		var internalReportName = tile.attr("data-internalreportname");
		var internalTableName = tile.attr("data-internaltablename");
		var internalTileName = tile.attr("data-internaltilename");
		tile.find(".content").css("opacity", "0.25").load("AppController.servlet",
				{
					"return" : "s/tiles/report_data",
					set_table : internalTableName,
					set_report : internalReportName,
					set_report_row_limit : 50,
					set_tile : internalTileName,
					cache_bust : (new Date()).getTime()
				}, function() {
					// remove opacity
					tile.find(".content").removeAttr("style");
					$(".sideAction.backToView").removeClass("expanded");
					var hoverIntentConfig = {
						over : showTooltip,
						out : hideTooltip,
						interval : 400
					};
					tile.find("#filterhelp").hoverIntent(hoverIntentConfig);
					$(".ab_field_title").hoverIntent(hoverIntentConfig);
					reportRowClicks();
				});
	}
	if ((tileType == "adder")) {
		$("label.tiletype").click(
				function(event) {
					event.stopPropagation(); // stop the .tile click being called
					var selectedApp = $(this).attr("data-tiletype");
					if (selectedApp == "data_stream" || selectedApp == "data_link") {
						$("label.tiletype").not($(this)).addClass("notfocus");
						$(this).find("p").text("Which data would you like to use?");
						$(".adder .reportSelector").show().removeClass("notfocus");
						$(".adder .reportSelector li.module").click(
								function() {
									$(".adder .reportSelector li.module").not($(this)).addClass(
											"notfocus");
									$(this).find("ul.reports").show().removeClass("notfocus");
								});
					} else if (selectedApp == "chat" || selectedApp == "comment_stream"
							|| selectedApp == "calendar") {
						// These types add a tile immediately without further configuration
						backHome();
						var colour = nextColour();
						$.post("AppController.servlet", {
							"return" : "s/tiles/tiles",
							add_tile : true,
							tiletype : selectedApp,
							colour : colour
						}, function(data) {
							$("#tiles").html(data);
							tileEvents();
						});
					}
				}); /* end of label.tiletype.click */
		$(".adder .reportSelector ul.reports li").click(function(event) {
			event.stopPropagation();
			var internalReportName = $(this).attr("data-internalreportname");
			var selectedApp = $("label:visible").attr("data-tiletype");
			var colour = nextColour();
			var icon = $(this).closest("li.module").attr("data-icon");
			if (icon == "") {
				$(".iconChooser").attr("data-internalreportname", internalReportName);
				$(".iconChooser").show().removeClass("notfocus");
			} else {
				addDataTile(selectedApp, colour, internalReportName, icon);
			}
		});
		$(".adder .iconChooser i").click(
				function(event) {
					var internalReportName = $(".iconChooser").attr(
							"attr-internalreportname");
					var selectedApp = $("label:visible").attr("data-tiletype");
					var colour = nextColour();
					var icon = $(this).attr("class");
					addDataTile(selectedApp, colour, internalReportName, icon);
				});
	} // end of if adder
}

function reportRowClicks() {
	$(".reportData tr").click(
			function(event) {
				var row = $(this);
				if (row.hasClass("rowa") || row.hasClass("rowb")) {
					var rowId = row.attr("name");
					var internalTableName = row.closest(".tile").attr(
							"data-internaltablename");
					loadEdit(row.closest(".content"), internalTableName, rowId);
				}
			});
}

function newRecord(internalTableName) {
	$(".tile.expanded").find(".content").css("opacity", "0.25").load(
			"AppController.servlet", {
				"return" : "gui/reports_and_tables/tabs/edit",
				save_new_record : true,
				set_table : internalTableName
			}, function() {
				// remove opacity
				$(".content").removeAttr("style");
				editTabFunctions();
				showEditControls();
			});
}

function cloneRecord(internalTableName) {
	$(".tile.expanded").find(".content").css("opacity", "0.25").load(
			"AppController.servlet", {
				"return" : "gui/reports_and_tables/tabs/edit",
				clone_record : true,
				set_table : internalTableName
			}, function() {
				// remove opacity
				$(".content").removeAttr("style");
				editTabFunctions();
				showEditControls();
			});
}

/**
 * @param container
 *          A jquery object that the content should be loaded into
 * @param internalTableName
 * @param rowId
 */
function loadEdit(container, internalTableName, rowId) {
	var tile = container.closest(".tile");
	var expanded = false;
	if (tile.hasClass("expanded")) {
		expanded = true;
	} else {
		expandTile(tile);
	}
	var internalTileName = tile.attr("data-internaltilename");
	var params = {
		"return" : "gui/reports_and_tables/tabs/edit",
		set_tile : internalTileName,
		cacheBust : (new Date()).getTime()
	}
	if (internalTableName) {
		params["set_table"] = internalTableName;
	}
	if (rowId) {
		params["set_row_id"] = rowId;
	}
	container.css("opacity", "0.25").load("AppController.servlet", params,
			function() {
				// remove opacity
				container.removeAttr("style");
				editTabFunctions();
				showEditControls();
				if (!expanded) {
					tileLoaded(tile, true);
				}
			});
}

function showEditControls() {
	$(".sideAction.backHome").addClass("expanded");
	$(".sideAction.backToView").addClass("expanded");
	$(".sideAction.newRecord").addClass("expanded");
	$(".sideAction.cloneRecord").addClass("expanded");
	$(".sideAction.removeRecord").addClass("expanded");
}

function addDataTile(selectedApp, colour, internalReportName, icon) {
	backHome();
	$.post("AppController.servlet", {
		"return" : "s/tiles/tiles",
		add_tile : true,
		tiletype : selectedApp,
		colour : colour,
		icon : icon,
		internalreportname : internalReportName
	}, function(data) {
		$("#tiles").html(data);
		tileEvents();
	});
}

function nextColour() {
	// Find available colours: create a copy of abTileColours, remove existing
	// tile colours
	var availableColours = abTileColours.slice(0);
	$(".tile").not(".adder").each(function() {
		for ( var i in abTileColours) {
			var tileColour = abTileColours[i];
			if ($(this).hasClass(tileColour)) {
				removeItem(availableColours, tileColour);
			}
		}
	});
	if (availableColours.length > 0) {
		// Return the next available colour
		return availableColours[0];
	}
	// All colours used already, cycle
	var numExistingTiles = $(".tile").size() - 1; /* -1 to discount the tile adder */
	var colourIndex = numExistingTiles % abTileColours.length;
	return abTileColours[colourIndex];
}

function removeItem(array, item) {
	for ( var i in array) {
		if (array[i] == item) {
			array.splice(i, 1);
			break;
		}
	}
}

/**
 * Contract the expanded tile, go back to the main screen
 */
function backHome() {
	$(".tile.notfocus").removeClass("notfocus");
	var allColours = abTileColours.join(" ");
	$("body").removeClass(allColours);
	$(".header.row").removeClass("expanded");
	$(".sideAction").removeClass("expanded");
	$(".tile .title").removeClass("notfocus");
	$(".tile .tile_icon").removeClass("notfocus");
	$(".tile .tile_icon i").removeClass("notfocus");
	$(".tile.expanded").not(".calendar").find(".content").addClass("notfocus");
	$(".tile.expanded").removeClass("expanded");
	$("#tiles").removeClass("padded");
	/* calendar */
	$("#calendar").addClass("notfocus");
	$("#report_selection_header").addClass("notfocus");
	$("#report_selection").addClass("notfocus");
	$("#agenda").removeClass("notfocus");
	/* end of calendar */
	var dataStreamTile = $(".tile.data_stream");
	// If contains report or edit screen
	if ((dataStreamTile.find("table.reportData").size() > 0)
			|| (dataStreamTile.find("#reportData").size() > 0)) {
		// Remove big view format, load reduced format again
		dataStreamTile.find(".content").load("AppController.servlet", {
			"return" : "s/tiles/data_stream"
		}, function() {
			dataStreamTile.find(".content").removeClass("notfocus");
			dataStreamEvents();
		});
	}
}

/**
 * Remove the currently expanded tile
 */
function removeTile() {
	var internalTileName = $(".tile.expanded").attr("data-internaltilename");
	backHome();
	$
			.post(
					"AppController.servlet",
					{
						"return" : "s/tiles/tiles",
						remove_tile : true,
						internaltilename : internalTileName
					},
					function(data) {
						$("#tiles").html(data);
						if ($(data).find("#added").size() > 0) {
							$("#added h1").text("All apps removed");
							$("#added")
									.append(
											"<h1><a href='AppController.servlet?return=s/agilebase'>Load most frequently used apps</a></h1>");
						} else {
							tileEvents();
						}
					});
}

function loadTreemap() {
	$.getJSON("AppController.servlet?return=s/treemap_json&returntype=json",
			function(treemapJson) {
				var tm = new $jit.TM.Squarified({
					// where to inject the visualization
					injectInto : 'infovis',
					// parent box title heights
					titleHeight : 15,
					// enable animations
					animate : true,
					// box offsets
					offset : 1,
					// Attach left and right click events
					Events : {
						enable : true,
						onClick : function(node) {
							if (node)
								tm.enter(node);
						},
						onRightClick : function() {
							tm.out();
						}
					},
					duration : 1000,
					// Enable tips
					Tips : {
						enable : true,
						// add positioning offsets
						offsetX : 20,
						offsetY : 20,
						// implement the onShow method to
						// add content to the tooltip when a node
						// is hovered
						onShow : function(tip, node, isLeaf, domElement) {
							var html = "<div class=\"tip-title\">" + node.name
									+ "</div><div class=\"tip-text\">";
							tip.innerHTML = html;
						}
					},
					// Add the name of the node in the correponding label
					// This method is called once, on label creation.
					onCreateLabel : function(domElement, node) {
						domElement.innerHTML = node.name;
						var style = domElement.style;
						style.display = '';
						style.border = '1px solid transparent';
						domElement.onmouseover = function() {
							style.border = '2px solid #EC00BC';
						};
						domElement.onmouseout = function() {
							style.border = '1px solid transparent';
						};
					}
				});
				tm.loadJSON(treemapJson);
				tm.refresh();
			}); // end of treemap getJSON
	fSparkLines();
}