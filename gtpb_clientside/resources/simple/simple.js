/**
 * JS used for the simple interface
 */
$(document).ready(function() {
	if ($("#infovis").size() > 0) {
		loadTreemap();
	} else {
		tileEvents();
		$(".backHome").click(function() {
			backHome();
		});
		$(".removeTile").click(function() {
			removeTile();
		});
	}
}); // end of document.ready

var abTileColours = ["blue", "yellow", "green", "purple", "pink"];

function tileEvents() {
	$('.tile').click(function() {
		var tile = $(this);
		if (tile.hasClass("expanded")) {
			console.log("expanded tile");
			return;
		}
		console.log("not expanded tile");
		$(".tile").not(tile).addClass("notfocus");
		var title = tile.attr("title");
		$("#title").find("h1").text(title);
		var colour = tile.attr("data-colour");
		var allColours = abTileColours.join(" ");
		$("body").removeClass(allColours).addClass(colour);
		tile.addClass("expanded");
		$(".header.row").addClass("expanded");
		tile.find(".icon").fadeOut();
		var template = "s/tiles/" + tile.attr("data-type");
		tile.find(".content").load("AppController.servlet", {
			"return" : template
		}, function() {
			tileLoaded(tile);
		});
		tile.find(".content").show();
	});
}

/**
 * This function runs when a tile is clicked and content has loaded
 */
function tileLoaded(tile) {
	var tileType = tile.attr("data-type");
	$(".sideAction.backHome").addClass("expanded");
	if (tileType != "adder"){
		$(".sideAction.removeTile").addClass("expanded");
	}
	if (tileType == "adder") {
		$("label.tiletype").click(function(event) {
			event.stopPropagation(); // stop the .tile click being called
			$("label.tiletype").not($(this)).addClass("notfocus");
			$(this).find("p").fadeOut();
			var selectedApp = $(this).attr("data-tiletype");
			if (selectedApp == "chat" || selectedApp == "comment_stream") {
				// These types add a tile immediately without further configuration
				// Choose a colour
				var numExistingTiles = $(".tile").size() - 1; /* -1 to discount the tile adder */
				var colourIndex = numExistingTiles % abTileColours.length;
				var colour = abTileColours[colourIndex];
				console.log(abTileColours);
				backHome();
				$.post("AppController.servlet", {
					"return": "s/tiles/tiles",
					add_tile: true,
					tiletype: selectedApp,
					colour: colour
				}, function(data) {
					$("#tiles").html(data);
					tileEvents();
				});
			}
		});
	}
}

/**
 * Contract the expanded tile, go back to the main screen
 */
function backHome() {
	$(".tile.expanded").find(".content").empty();
	$(".tile.expanded").find(".icon").fadeIn();
	$(".tile.expanded").removeClass("expanded");
	$(".tile.notfocus").removeClass("notfocus");
	$("body").removeClass("blue pink green yellow purple");
	$(".header.row").removeClass("expanded");
	$(".sideAction").removeClass("expanded");
}

/**
 * Remove the currently expanded tile
 */
function removeTile() {
	var internalTileName = $(".tile.expanded").attr("data-internaltilename");
	backHome();
	$.post("AppController.servlet", {
		"return": "s/tiles/tiles",
		remove_tile: true,
		internaltilename: internalTileName
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