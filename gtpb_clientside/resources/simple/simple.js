/**
 * JS used for the simple interface
 */
$(document).ready(function() {
	if ($("#infovis").size() > 0) {
		loadTreemap();
	} else {
		tileEvents();
		// Focus on first record
		$(".tile.large .report_data_row:first-child").mouseenter();
	}
});

var abTileColours = [ "blue", "yellow", "green", "purple", "pink", "turquoise" ];

function tileEvents() {
	commonTileEvents();
	dataStreamEvents();
	focusEvents();
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

/** Common tile events */
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
	$('.tile').click(function() {
		var tile = $(this);
		if (tile.hasClass("expanded")) {
			return;
		}
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
		if(tile.hasClass("large")) {
			tile.find("input[type=search]").addClass("notfocus");
			tile.find(".foot_fade").addClass("notfocus");
		}
		var template = "s/tiles/" + tile.attr("data-type");
		tile.find(".content").load("AppController.servlet", {
			"return" : template
		}, function() {
			tileLoaded(tile);
		});
		tile.find(".content").removeClass("notfocus");
	});
	$(".sideAction.backToView").click(function() {
		tileLoaded($(".tile.expanded"));
	});
	$(".sideAction.newRecord").click(function() {
		var internalTableName = $(".tile.expanded").attr("data-internaltablename");
				$(".tile.expanded").find(".content").css("opacity", "0.25").load(
						"AppController.servlet", {
							"return" : "gui/reports_and_tables/tabs/edit",
							save_new_record: true,
							set_table : internalTableName
						}, function() {
							// remove opacity
							$(".content").removeAttr("style");
							editTabFunctions();
							$(".sideAction.backToView").addClass("expanded");
						});
			});
	$(".sideAction.removeRecord").click(function() {
		if (confirm("Delete this record?")) {
			fDeleteObj("remove_record", "rowid");
		}
	});
}

/** Data stream tile specific events */
function dataStreamEvents() {
	var searchBox = $(".tile.large input[type=search]");
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
	$(".tile.large .report_data_row").mouseenter(
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
}

/**
 * This function runs when a tile is clicked to expand it and content has loaded
 */
function tileLoaded(tile) {
	var tileType = tile.attr("data-type");
	$(".sideAction.removeRecord").removeClass("expanded");
	$(".sideAction.backHome").addClass("expanded");
	if (tileType != "adder") {
		$(".sideAction.removeTile").addClass("expanded");
	}
	// Hide all icons otherwise they can be clicked
	$(".tile_icon i").addClass("notfocus");
	if (tile.attr("data-internalreportname")) {
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
					$("#filterhelp").hoverIntent(hoverIntentConfig);
					$(".ab_field_title").hoverIntent(hoverIntentConfig);
					reportRowClicks();
				});
	}
	if (tileType == "adder") {
		$("label.tiletype").click(
				function(event) {
					event.stopPropagation(); // stop the .tile click being called
					$("label.tiletype").not($(this)).addClass("notfocus");
					var selectedApp = $(this).attr("data-tiletype");
					if (selectedApp == "data_stream" || selectedApp == "data_link") {
						$(this).find("p").text("Which data would you like to use?");
						$(".adder .reportSelector").show().removeClass("notfocus");
						$(".adder .reportSelector li.module").click(
								function() {
									$(".adder .reportSelector li.module").not($(this)).addClass(
											"notfocus");
									$(this).find("ul.reports").show().removeClass("notfocus");
								});
					}
					if (selectedApp == "chat" || selectedApp == "comment_stream") {
						// These types add a tile immediately without further configuration
						// Choose a colour
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

/**
 * @param container	A jquery object that the content should be loaded into
 * @param internalTableName
 * @param rowId
 */
function loadEdit(container, internalTableName, rowId) {
	var params = {
			"return": "gui/reports_and_tables/tabs/edit",
			cacheBust: (new Date()).getTime()
	}
	if (internalTableName) {
		params["internalTableName"] = internalTableName;
	}
	if (rowId) {
		params["rowId"] = rowId;
	}
	container.css("opacity", "0.25").load(
			"AppController.servlet", params, function() {
				// remove opacity
				container.removeAttr("style");
				editTabFunctions();
				$(".sideAction.backToView").addClass("expanded");
				$(".sideAction.removeRecord").addClass("expanded");
			});
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
	$(".tile.expanded").find(".content").addClass("notfocus");
	$(".tile.expanded").removeClass("expanded");
	var dataStreamTile = $(".tile.data_stream");
	//dataStreamTile.find("input[type=search]").removeClass("notfocus");
	//dataStreamTile.find(".foot_fade").removeClass("notfocus");
	if (dataStreamTile.find("table.reportData").size() > 0) {
		// Remove big view format, load reduced format again
		dataStreamTile.find(".content").load("AppController.servlet", {
			"return": "s/tiles/data_stream"
		}, function() {
			dataStreamTile.find(".content").removeClass("notfocus");
		});
	}
}

/**
 * Remove the currently expanded tile
 */
function removeTile() {
	var internalTileName = $(".tile.expanded").attr("data-internaltilename");
	backHome();
	$.post("AppController.servlet", {
		"return" : "s/tiles/tiles",
		remove_tile : true,
		internaltilename : internalTileName
	}, function(data) {
		$("#tiles").html(data);
		tileEvents();
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