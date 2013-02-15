/**
 * JS used for the simple interface
 */
$(document).ready(function() {
	if ($("#infovis").size() > 0) {
		loadTreemap();
	} else {
		tileEvents();
		dataStreamEvents();
	}
});

var abTileColours = [ "blue", "yellow", "green", "purple", "pink" ];

/** Common tile events */
function tileEvents() {
	$(".backHome").click(function() {
		backHome();
	});
	$(".removeTile").click(function() {
		removeTile();
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
		var template = "s/tiles/" + tile.attr("data-type");
		tile.find(".content").load("AppController.servlet", {
			"return" : template
		}, function() {
			tileLoaded(tile);
		});
		tile.find(".content").show();
	});
}

/** Data stream tile specific events */
function dataStreamEvents() {
	var searchBox = $(".tile.data_stream input[type=search]");
	searchBox.click(function(event) {
		event.stopPropagation();
	});
	searchBox.keyup(function(event) {
		$(this).addClass("changed");
		var filterString = $(this).val();
		searchBox.closest(".tile").find(".content").load("AppController.servlet", {
			"return": "s/tiles/data_stream",
			set_global_report_filter_string: true,
			filterstring: filterString
		}, function() {
			$(this).removeClass("changed");
		});
	});
}

/**
 * This function runs when a tile is clicked to expand it and content has loaded
 */
function tileLoaded(tile) {
	var tileType = tile.attr("data-type");
	$(".sideAction.backHome").addClass("expanded");
	if (tileType != "adder") {
		$(".sideAction.removeTile").addClass("expanded");
	}
	// Hide all icons otherwise they can be clicked
	$(".tile_icon i").hide();
	if (tileType == "adder") {
		$("label.tiletype").click(function(event) {
			event.stopPropagation(); // stop the .tile click being called
			$("label.tiletype").not($(this)).addClass("notfocus");
			var selectedApp = $(this).attr("data-tiletype");
			if (selectedApp == "data_stream" || selectedApp == "data_link") {
				$(this).find("p").text("Which data would you like to use?");
				$(".adder .reportSelector").show().removeClass("notfocus");
				$(".adder .reportSelector li.module").click(function() {
					$(".adder .reportSelector li.module").not($(this)).addClass("notfocus");
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
		$(".adder .iconChooser i").click(function(event) {
			var internalReportName = $(".iconChooser").attr("attr-internalreportname");
			var selectedApp = $("label:visible").attr("data-tiletype");
			var colour = nextColour();
			var icon = $(this).attr("class");
			addDataTile(selectedApp, colour, internalReportName, icon);
		});
	} // end of if adder
}

function addDataTile(selectedApp, colour, internalReportName, icon) {
	backHome();
	$.post("AppController.servlet", {
		"return" : "s/tiles/tiles",
		add_tile : true,
		tiletype : selectedApp,
		colour : colour,
		icon: icon,
		internalreportname : internalReportName
	}, function(data) {
		$("#tiles").html(data);
		tileEvents();
	});
}

function nextColour() {
	var numExistingTiles = $(".tile").size() - 1; /* -1 to discount the tile adder */
	var colourIndex = numExistingTiles % abTileColours.length;
	return abTileColours[colourIndex];
}

/**
 * Contract the expanded tile, go back to the main screen
 */
function backHome() {
	$(".tile.expanded").find(".content").empty();
	$(".tile.expanded").removeClass("expanded");
	$(".tile.notfocus").removeClass("notfocus");
	$("body").removeClass("blue pink green yellow purple");
	$(".header.row").removeClass("expanded");
	$(".sideAction").removeClass("expanded");
	$(".tile .title").removeClass("notfocus");
	$(".tile .tile_icon").removeClass("notfocus");
	$('.tile .tile_icon i').show();
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