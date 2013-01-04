/**
 * JS used for the simple interface
 */
$(document).ready(function() {
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
			Color: {  
  			//Allow coloring  
  			allow: true,  
  			//Select a value range for the $color  
		    //property. Defaults to -100 and 100.  
			    minValue: -750,  
			    maxValue: 750,  
  			//Set color range. Defaults to reddish and  
  			//greenish. It takes an array of three  
  			//integers as R, G and B values.  
  			//maxColorValue: [0, 255, 50],
  			maxColorValue: [255, 50, 50],
		    minColorValue: [50, 150, 255]
			},
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
		        style.border = '1px solid #9FD4FF';  
		      };  
		      domElement.onmouseout = function() {  
		        style.border = '1px solid transparent';  
		      };  
		  }  
		});
		tm.loadJSON(treemapJson);  
		tm.refresh();		
	}); // end of getJSON
}); // end of document.ready