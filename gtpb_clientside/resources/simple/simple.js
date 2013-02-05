/**
 * JS used for the simple interface
 */
$(document).ready(function() {
	if ($("#infovis").size() > 0) {
		loadTreemap();
	} else {
		tileEvents();
	}
}); // end of document.ready

var abTileColours = ["blue", "yellow", "green", "purple", "pink"];

function tileEvents() {
	$('.tile').not(".expanded").click(function() {
		var tile = $(this);
		$(".tile").not(tile).addClass("notfocus");
		var title = tile.attr("title");
		$("#title").find("h1").text(title);
		tile.addClass("expanded");
		var colour = tile.attr("data-colour");
		var allColours = abTileColours.join(" ");
		$("body").removeClass(allColours).addClass(colour);
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
	$(".backHome").click(function() {
		backHome();
	});
}

/**
 * This function runs when a tile is clicked and content has loaded
 */
function tileLoaded(tile) {
	var tileType = tile.attr("data-type");
	if (tileType == "adder") {
		$("label.tiletype").click(function(event) {
			event.stopPropagation(); // stop the .tile click being called
			$("label.tiletype").not($(this)).addClass("notfocus");
			$(this).find("p").fadeOut();
			var selectedApp = $(this).attr("data-tiletype");
			if (selectedApp == "chat" || selectedApp == "comment_stream") {
				// These types add a tile immediately without further configuration
				backHome();
				// Choose a colour
				var numExistingTiles = $(".tile.notfocus").size();
				var colourIndex = numExistingTiles % abTileColours.length;
				var colour = abTileColours[colourIndex];
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