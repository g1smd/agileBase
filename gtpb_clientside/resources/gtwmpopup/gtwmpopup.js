/*
 *  Copyright 2010 GT webMarque Ltd
 * 
 *  This file is part of agileBase.
 *
 *  agileBase is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  agileBase is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with agileBase.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
  v. 1.02

  dependencies:
  needs jquery.js
  
  suggest that the whole document content is wrapped in a div to controll scrolling so that the
  psudeo modal blanking div covers all scrollbars
  
  in strict mode, this requires:
  
		html, 
		body {
			height:100%;
			overflow:hidden;
		}	
		
	and something along the lines of
	
		#scroller {
			height:100%;
			width:100%;
			overflow:auto;
			overflow-y:scroll;  * stop the page jumping around when the content exceeds the x height, IE only *
			text-align:center;
	  }
	  
	to trigger the popup, give an a tag a class of gtwmpopup
	- to load in ajax content put the url of the content as the href
    e.g. <a href="test_content.htm" class="gtwmpopup">show ajax content</a>
    
  - to show content from an element within the page put the element's id into the href
    e.g <a href="#test_div" class="gtwmpopup">show div content</a>
        <div id="test_div" style="display:none">test content div's content</div>
  
  - callback parameter can be passed as a string (e.g. if calling from an href) in which case the function
  must be in the scope of this script, or a functional reference can be passed (if popup is called
  directly).  The plugin returns success (true|false), a handle to the popup DOM object and whether the call is 
  in response to a load or close event (onload|onclose)
  
  - when parameter can be left null (in which case the callback runs on both the close and load events or one of (onload|onclose|both)
  
*/


$(document).ready(function(){
	$('a.gtwmpopup').click(function(){
		fPopup(this.getAttribute('href'),null,null,this.getAttribute('title'),this.getAttribute('callback'),this.getAttribute('when'));
		this.blur();
		return false;
	});
});

function fPopup(sLocation, iWidth, iHeight, sCaption, vCallback, sWhen) {
	function fShowPopup(vResponseText) {
		function fCallbackPopup(sType, bSuccess) {
			if(vCallback && ( (sWhen==sType)||(sWhen=='both')||(!sWhen) ) ) {
	  		try {
	  		  try {
	  			  vCallback(bSuccess, oPopup, sType);
	  		  } catch(e) {
		  		  var fCallback=eval(vCallback);
		  		  fCallback(bSuccess, oPopup, sType);	  
	  		  }
	  		} catch(e) {
	  			// do nothing
	  		}
	  	}
		}
		
	  function fCentralise() {
	  	try {
  	    // top bottom
  	    var iParentHeight=oPopup.offsetParent.offsetHeight;
  	    var iTop=Math.round((iParentHeight-oPopup.offsetHeight)/2);
  	    if (iTop<0) iTop=0; // position at the top if the dialog is taller than the available screen space  	    
  	    oPopup.style.top=iTop+'px';  // use a margin to vertically position
  	      
  	    // left right
  	    var iParentWidth=oPopup.offsetParent.offsetWidth;
  	    var iLeft=Math.round((iParentWidth-oPopup.offsetWidth)/2);
  	    if (iLeft<0) iLeft=0; // position at the top if the dialog is taller than the available screen space  	    
  	    oPopup.style.left=iLeft+'px';  // use a margin to vertically position
  	       	      
  	    oPopup.style.visibility='visible';
  	  } 
  	  catch(e) {
  	    // don't do anything!
  	  }
  	  finally {
  	    //  maintain the central position by running again if the window is resized
  	    // IE isn't really happy with this and really slows down...
  	    //  if(top.addEventListener) top.addEventListener('resize',fCentralise,false);
  	    //  else top.attachEvent('onresize',fCentralise);
  	  }
	  }
	  
	  function fClose(bSuccess) {
	  	function fDestroy() {
	  		$(oPopup).remove();
		  }
	  	
		  $('[hiddenByPopup]').each(function(){
		  	this.removeAttribute('hiddenByPopup');
		  	$(this).removeClass('obj_hidden')
		  });

	  	fCallbackPopup('onclose',bSuccess);
	  	
	  	$(oPopup).fadeOut('slow',fDestroy);
	  	oBlocker.destroy();
	  }
	  
	  function fCancel() {
		fClose(false);  
	  }
	  
	  function fOK() {
		  fClose(true);
	  }
	  		
		var oPopup=document.createElement('div');
		oPopup.setAttribute('id','popup');
		oPopup.cancel=fCancel;
		oPopup.ok=fOK;
		
		if(sCaption) {
			var oCaption=document.createElement('div');
			oCaption.setAttribute('id','caption');
			oCaption.appendChild(document.createTextNode(sCaption));
			oPopup.appendChild(oCaption);
		}
		
		var oContent=document.createElement('div');
		oContent.setAttribute('id','content');
		
		$(oContent).html(vResponseText);
		
		$(oPopup).append(oContent);
		oPopup.content=oContent;
		
		var oCloser=document.createElement('a');
		oCloser.setAttribute('id','closer');
		oCloser.setAttribute('href','#');
		oCloser.appendChild(document.createTextNode('close x'));
		$(oCloser).click(fCancel);
		oPopup.appendChild(oCloser);
		
		if(iWidth) oPopup.style.width=iWidth+'px';
		if(iHeight) oPopup.style.height=iHeight+'px';
		
		$(oBlocker).unbind('click',oBlocker.destroy); // want to run the function to destroy the whole lot, not just the blocker
		$(oBlocker).click(fCancel);
		
		oBody.appendChild(oPopup);
		fCentralise();
		
		fCallbackPopup('onload',true);
	}
	
	function fDestroyBlocker() {
		$(oBlocker).remove();
	}

  var oBlocker=document.createElement('div');
  oBlocker.setAttribute('id','blocker');
  oBlocker.destroy=fDestroyBlocker;
  $(oBlocker).click(oBlocker.destroy); // allow the blocker to be clicked and destroyed - useful if the load fails...
  
  var oBody=document.getElementsByTagName('body')[0];
  oBody.appendChild(oBlocker);
  
  $('object').each(function(){
  	this.setAttribute('hiddenByPopup',true);
  	$(this).addClass('obj_hidden');
  });
  
  function fLoadError(oXMLHttpRequest, sTextStatus, oErrorThrown) {
  	alert('could not load content\nplease try again');
  	oBlocker.destroy();  	
  }
  
  if($('#'+sLocation.split('#',2)[1]).length>0) fShowPopup($('#'+sLocation.split('#',2)[1]).html()); // the href contains an ID which is found in the document
  else var oReq=$.ajax({url: sLocation, cache: false, success: fShowPopup, dataType: 'html', timeout:5000, error:fLoadError}); // see if the location is a url which we can load over ajax
}