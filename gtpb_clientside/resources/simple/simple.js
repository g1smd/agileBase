/**
 * JS used for the simple interface
 */
$(document).ready(function() {
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
	        style.border = '1px solid #9FD4FF';  
	      };  
	      domElement.onmouseout = function() {  
	        style.border = '1px solid transparent';  
	      };  
	  }  
	});  
	var treemapJson = $.getJson("AppController.servlet?return=s/treemap_json&returntype=json");
	tm.loadJSON(treemapJson);  
	tm.refresh();  
});