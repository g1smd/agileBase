/*
 *  Copyright 2009 GT webMarque Ltd
 * 
 *  This file is part of GT portalBase.
 *
 *  GT portalBase is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  GT portalBase is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with GT portalBase.  If not, see <http://www.gnu.org/licenses/>.
 */

// This function is never called - deletion candidate
function fCallback(sResponseText,sResponseXML){  			 

}

function fNothing() {
	return;
}

function fRequest(sURL, aPostVars, fCallback, iShowWait){   
  // common functions
  function fCheckRequest(oReqObj){  
    try {
      if (oReqObj.readyState == 4){ 
        if (oReqObj.status != 200){  
          alert('Warning - network connection problem!\n'+oReqObj.statusText);
		  return false;
	    }
	    return true;
	  }
	} 
	catch(e) {
	  // this is probably that the request couldn't be sent e.g. the host isn't available
	  // should we do some kind of alert?
	  return false; 
	} 
  }

  // the please wait object
  function fWait(){  
    function fInsertWaitHTML(){  
      //if (fCheckRequest(oReq)) oHTML.insertAdjacentHTML('beforeEnd',oReq.responseText);
	  if (fCheckRequest(oReq)){  
	    oHTML.innerHTML=oReq.responseText;
	    //if (sMessage) oHTML.item('oMessage').innerHTML=sMessage;
	  }
	}	
			 
	function fClose(){  
	  //oHTML.removeNode(true);
	  oHTML.parentNode.removeChild(oHTML);
	}
			 
    // expose this function to the parent object
	this.fClose=fClose;
			 
	// private variables
	if (window.XMLHttpRequest)
	  var oReq = new XMLHttpRequest(); // the download object for Moz
	else
      var oReq = new ActiveXObject("Microsoft.XMLHTTP");	 // the download object for IE
	var oHTML = top.document.createElement('DIV');	// wrapper to show "please wait"
	oHTML.id='oWait';
			  
	oReq.onreadystatechange=fInsertWaitHTML;			 
	top.document.body.appendChild(oHTML);
	oReq.open('GET','resources/wait/wait.htm',true);
	oReq.send('');
  }
		
  function fHTTPRequest(sURL, aPostVars, fCallback){  	 
    function fComplete(){  
      if (fCheckRequest(oReq)){
        if (oWait) oWait.fClose(); // a wait window may not have been requested  
        fCallback(oReq.responseText,oReq.responseXML);
      }
	}
			 
	function fPostString(){  
	  if (!aPostVars) return null;
	  var sPostString='';
	  for (var sKey in aPostVars){
	    if(sKey=='containsValue') continue; // this is a special case.  containsValue is a prototype method.  I can't think of another way to exclude it from the array :( 	
		if(sKey!='return') aPostVars[sKey]=encodeURIComponent(aPostVars[sKey]); // return is a special case.  Otherwise URI encode non-word chars
		sPostString+=sKey+(aPostVars[sKey]?'='+aPostVars[sKey]:'')+'&'; 
      }
	  // remove the trailing &
	  return sPostString.slice(0,-1);
	}
		
	// jQuery syntax
	//var data = fPostString();
	//var oReq = $.ajax({
	//  type: "POST",
	//  url: sURL,
	//  data: data,
	//  success: fComplete
	//});
	
	if (window.XMLHttpRequest)
	  var oReq = new XMLHttpRequest(); // the download object for Moz
	else
	  var oReq = new ActiveXObject("Microsoft.XMLHTTP");
	oReq.onreadystatechange=fComplete;
	oReq.open('POST',sURL,true);
			 
	if (aPostVars) oReq.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
	oReq.send(fPostString());
    this.xml_http_request=oReq;
  }
  			
  if (iShowWait>=0) var oWait=new fWait();
  var oHTTPRequest=new fHTTPRequest(sURL, aPostVars, fCallback);
  
  this.xml_http_request=oHTTPRequest.xml_http_request;
}	