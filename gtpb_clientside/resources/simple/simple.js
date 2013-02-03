/**
 * JS used for the simple interface
 */
$(document).ready(function() {
  if($("#infovis").size() > 0) {
  	loadTreemap();
  } else {
    tileEvents();
  }
}); // end of document.ready

function tileEvents() {
	$('.tile').click(function() {
		var tile = $(this);
		if (tile.hasClass("expanded")) {
			$(".tile").not(tile).removeClass("notfocus");
			tile.removeClass("expanded");
		} else {
			$(".tile").not(tile).addClass("notfocus");
			tile.addClass("expanded");
			var colour = tile.attr("data-colour");
			$("body").removeClass("blue pink green yellow purple").addClass(colour);
			$(".header.row").addClass("expanded");
		}
	});
}



function loadTreemap() {
	$.getJSON("AppController.servlet?return=s/treemap_json&returntype=json", function(treemapJson) {
		var tm = new $jit.TM.Squarified({  
		  //where to inject the visualization  
		  injectInto: 'infovis',  
		  //parent box title heights  
		  titleHeight: 15,  
		  //enable animations  
		  animate: true,  
		  //box offsets  
		  offset: 1,  
		  //Attach left and right click events  
		  Events: {  
		    enable: true,  
		    onClick: function(node) {  
		      if(node) tm.enter(node);  
		    },  
		    onRightClick: function() {  
		      tm.out();  
		    }  
		  },  
		  duration: 1000,  
		  //Enable tips  
		  Tips: {  
		    enable: true,  
		    //add positioning offsets  
		    offsetX: 20,  
		    offsetY: 20,  
		    //implement the onShow method to  
		    //add content to the tooltip when a node  
		    //is hovered  
		    onShow: function(tip, node, isLeaf, domElement) {  
		      var html = "<div class=\"tip-title\">" + node.name   
		        + "</div><div class=\"tip-text\">";  
		      tip.innerHTML =  html;   
		    }    
		  },  
		  //Add the name of the node in the correponding label  
		  //This method is called once, on label creation.  
		  onCreateLabel: function(domElement, node){  
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