/*
 *  Copyright 2011 GT webMarque Ltd
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
   Javascript object to handle XML requests triggered by entry into a text box.

   Object operates as follows:

   Keystroke event occurs on element -> Entry for the element in the queue buffer? -yes-> reset the timer for this entry in the queue -> Keystroke is the nth in the element -no-> end
                                                                  |                                                                         /|\
                                                                 no -> create entry in buffer -> set a timer for this entry in the queue    -

   Queue timer expires -> Object is in the sending buffer -no-> clear timer -> add to send queue -> set timer on send queue -> run change event for the element
                                     |
                                    yes -> end (timer is still running)

   XML request returns (from fChange) -> clear element from send queue -> clear timer  (fChange handles the behaviour of the element)

   Send timer expires -> Resend request

   Notes:
   1.  The value in the object isn't cached - the value sent is the value in the object when the request is made
 */
function fBuffer() {
	// object maintains a reference to a DOM element and tracks keypresses and
	// delays between keypresses
	// after a specified number of keypresses or a 2 second delay, fChange() is
	// called on the DOM element
	// to send an XML request to update the server with its new contents

	// wrap a DOM element as a BufferObject and add to the first empty space
	// within the Buffer array
	// (add it to the end if there are no empty slots)
	function fBufferObject(_oElement) {
		var oElement = _oElement;
		var iKeyCount = 0;
		var iTimerId = -1;
		var oChange;
		this.element = oElement;
		this.keyCount = iKeyCount;
		this.timerId = iTimerId;
		this.change = oChange;
	}

	function fAddElementToBuffer(oBufferObject, aBuffer) {

		function fCompressBuffer(aArray) {
			// remove any null elements from the end of the array
			// while the last element is null, remove it

			// NOTE: this is never called!
			while (aArray[aArray.length - 1] == null)
				aArray.pop();
		}

		function fFirstBlankElement(aArray) {
			// call fCompressBuffer here?
			for ( var i = 0; i < aArray.length; i++) {
				if (!aArray[i])
					return i;
			}
			return -1;
		}

		var iElementIndex = fFirstBlankElement(aBuffer);
		if (iElementIndex < 0) {
			aBuffer.push(oBufferObject);
			var iElementIndex = aBuffer.length - 1;
		} else {
			aBuffer[iElementIndex] = oBufferObject;
		}
		return iElementIndex;
	}

	function fWriteBuffer(oElementToBuffer) {
		// locate the index of the calling DOM element within the buffer array
		function fElementIndex() {
			for ( var i = 0; i < aQueueBuffer.length; i++) {
				if (aQueueBuffer[i]
						&& (aQueueBuffer[i].element == oElementToBuffer))
					return i;
			}
			return -1;
		}
		// clear the timer for a BufferObject if one exists
		// set an initial / new timer for the BufferObject
		function fSetTimer() {
			var iSendMS = 2500;
			if (oBufferObject.timerId >= 0)
				window.clearInterval(oBufferObject.timerId);
			var sInterval = 'top.oBuffer.send(' + iElementIndex + ')';
			oBufferObject.timerId = window.setInterval(sInterval, iSendMS);
		}

		// find the DOM element within the buffer array or add it if not already
		// present
		var iElementIndex = fElementIndex();
		if (iElementIndex < 0)
			iElementIndex = fAddElementToBuffer(new fBufferObject(
					oElementToBuffer), aQueueBuffer);
		var oBufferObject = aQueueBuffer[iElementIndex];
		// increment the keycount and reset/set the timer
		oBufferObject.keyCount++;
		fSetTimer();
		fEnableDisable('disable', oElementToBuffer);
		// if the send is also trigger by a count of keystrokes and this is the
		// nth stroke, try a send immediately
		// keep the timer trigger too in case the send queue isn't clear
		if ((oBufferObject.element.getAttribute('maxKeyCount'))
				&& (oBufferObject.keyCount
						% oBufferObject.element.getAttribute('maxKeyCount') == 0))
			fSend(iElementIndex);
	}

	// call fChange for the DOM element
	// clear timer & delete BufferObject wrapper
	// set occupied Buffer space as empty
	function fSend(iQueueElementIndex) {
		function fSetTimer() {
			var iTimeoutMS = 15000;
			if (oBufferObject.timerId >= 0)
				window.clearInterval(oBufferObject.timerId);
			var sInterval = 'top.oBuffer.resend(' + iSendElementIndex + ')';
			oBufferObject.timerId = window.setInterval(sInterval, iTimeoutMS);
		}

		function fQueueElementIndexInSendBuffer() {
			for ( var i = 0; i < aSendBuffer.length; i++) {
				if (aSendBuffer[i]
						&& (aSendBuffer[i].element == oBufferObject.element))
					return i;
			}
			return -1;
		}

		var oBufferObject = aQueueBuffer[iQueueElementIndex];
		// if the element in the send queue, don't do anything
		if (fQueueElementIndexInSendBuffer() >= 0)
			return;
		// clear the interval timer which is seeing whether the request can be
		// passed to the send buffer
		window.clearInterval(oBufferObject.timerId);
		oBufferObject.keyCount = 0;

		// add the queued element into the send buffer and remove from queue
		// buffer
		iSendElementIndex = fAddElementToBuffer(oBufferObject, aSendBuffer);
		aQueueBuffer[iQueueElementIndex] = null;

		// set an interval on the sent item to resend if no response is received
		fSetTimer();
		
		// this runs the request. change property is a handle to the xmlHTTP
		// request which is handling the data
		oBufferObject.change = new fChange(oBufferObject.element);
	}

	function fResend(iSendElementIndex) {
		// check that the number of resends hasn't been exceeded in a future
		// version maybe...

		iNumberOfResends = iNumberOfResends + 1;
		var numActiveQueueElements = fNumActiveElements(aSendBuffer);
		fDisplayResendMessage(numActiveQueueElements, iNumberOfResends);
		oBufferObject = aSendBuffer[iSendElementIndex];

		// abort the current request
		if (oBufferObject.change)
			oBufferObject.change.xml_http_request.abort();

		// check whether there is something in the queue and if so abort resend
		for ( var i = 0; i < aQueueBuffer.length; i++) {
			if (aQueueBuffer[i] && (aQueueBuffer[i] == oBufferObject)) {
				fClearFromSendQueue(oBufferObject.element);
				return;
			}
		}

		// this runs the request. change property is a handle to the xmlHTTP
		// request which is handling the data
		oBufferObject.change = new fChange(oBufferObject.element);
	}

	function fClearFromSendQueue(oDOMObject) {
		var iSendElementIndex = -1;	
		// try and find the element in the queue
		for ( var i = 0; i < aSendBuffer.length; i++) {
			if (aSendBuffer[i]) {			
				if (aSendBuffer[i].element == oDOMObject) {
					iSendElementIndex = i;				
				}
			}
		}			
		if (iSendElementIndex == -1) {
			return;
		}
		
		oBufferObject = aSendBuffer[iSendElementIndex];
		window.clearInterval(oBufferObject.timerId);
		aSendBuffer[iSendElementIndex] = null;

		// Count how many active (non-null) things we've got left in the send
		// queue.
		// If zero we know
		// a) any and all re-sends have completed
		// b) we can delete all send queue array elements
		var numActiveQueueElements = fNumActiveElements(aSendBuffer);
		if (numActiveQueueElements == 0)
			iNumberOfResends = 0;
		fDisplayResendMessage(numActiveQueueElements, iNumberOfResends);
	}

	function fDisplayResendMessage(iSendQueueLength, iNumberOfResends) {
		var resendMessage = '';
		if (iNumberOfResends > 0) {
			resendMessage = ' ';
			for ( var i = 0; i < iSendQueueLength; i++) {
				resendMessage = resendMessage + '-';
			}
			resendMessage = resendMessage + ' saving';
			for ( var i = 0; i < iNumberOfResends; i++) {
				resendMessage = resendMessage + '.';
			}
			resendMessage = resendMessage + ' ';
			for ( var i = 0; i < iSendQueueLength; i++) {
				resendMessage = resendMessage + '-';
			}
		}
		document.title = sNormalWindowTitle + resendMessage;
	}

	function fNumActiveElements(aArray) {
		var numActiveElements = 0;
		for (i = 0; i < aArray.length; i++) {
			if (aArray[i] != null) {
				numActiveElements = numActiveElements + 1;
			}
		}
		return numActiveElements;
	}

	// declare the buffer
	var aQueueBuffer = new Array();
	var aSendBuffer = new Array();
	// expose the functions for writing to the buffer and sending from a timer
	this.writeBuffer = fWriteBuffer;
	this.send = fSend;
	this.resend = fResend;
	this.clearFromSendQueue = fClearFromSendQueue;
	// to deal with notifications when network is slow
	var sNormalWindowTitle = document.title;
	var iNumberOfResends = 0;
}

var oBuffer = new fBuffer();

/*
 * ===========================================
 * Below = formerly editData.js
 * Low level functions to do with data editing
 */

function fInit() {  
	
  // function to match the fields displayed in pane2 with the input fields in pane3
  // oCells is a collection of the cells in the heading row of the table
  function fMatchFields() {  
  	var oViewPane = top.document.getElementById('oViewPane');
	if (oViewPane == null) {
	  return;
	}
	if (!oViewPane.contentWindow.pane_2) {
	  return;
	}
    var oCells = oViewPane.contentWindow.pane_2.document.getElementById('reportData').tHead.rows[0].cells;
    // the index of the current row
    if(oViewPane.contentWindow.pane_2.document.getElementById('currentRow'))
      var iRowIndex = oViewPane.contentWindow.pane_2.document.getElementById('currentRow').rowIndex;
    else var iRowIndex=-1;
      
    /* for each of the cells, look at its field name and see whether
       there are any fields in pane3 whose name matches */
    for (var i=0;i<oCells.length;i++) { 
      var aMatchingElements = document.getElementsByName(oCells[i].getAttribute('internalName'));
      for (var j=0; j<aMatchingElements.length; j++) {	
        // attach the matching header cell to the input field
      	aMatchingElements[j].pane2_field = oCells[i];
      	// attach the actual cell to the input field
      	if (iRowIndex>-1)
      	  aMatchingElements[j].pane2_cell = oViewPane.contentWindow.pane_2.document.getElementById('reportData').rows[iRowIndex].cells[i];
      	}
    } 
  }
	 
  function fMatchTree() {  
  	var oViewPane = top.document.getElementById('oViewPane');
	if (oViewPane == null) {
	  return;
	}
	if (!parent.pane_1) {
	  return;
	}
    var oCaptions=parent.pane_1.document.getElementsByName('caption');
	 
	for (var i=0;i<oCaptions.length;i++) { 
	  var aMatchingElements=document.getElementsByName(oCaptions[i].getAttribute('type'));
	  for (var j=0;j<aMatchingElements.length;j++) {	
	    // attach the matching cell to the caption
      	if (aMatchingElements[j].getAttribute('identifier')==oCaptions[i].getAttribute('identifier'))
      	aMatchingElements[j].pane1_field=oCaptions[i];
      }
	}
  }
	 
  // match the input fields to those in pane2   
  fMatchFields();
	 
  // match the input fields to those in pane1
  fMatchTree();
}

function fSetPBfn(oObj) {	
  with(oObj){	
    removeAttribute('gtpb_'+getAttribute('fn_checked'));
	removeAttribute('gtpb_'+getAttribute('fn_unchecked'));
	setAttribute('gtpb_'+getAttribute(oObj.checked?'fn_checked':'fn_unchecked'),'');
  }
}

/* algorithm from http://jroller.com/page/rmcmahon?entry=resizingtextarea_with_prototype 
   as seen on ajaxian.com */
function fResizeTextArea(oObj) {
  var lines = oObj.value.split('\n');
  var newRows = lines.length; /*  + 1; */
  var oldRows = oObj.rows;
  for (var i = 0; i <lines.length; i++)
  {
      var line = lines[i];
      if (line.length>= oObj.cols) newRows += Math.floor(line.length / oObj.cols);
  }
  if (newRows > 20) {
    newRows = 20;
  }
  if (newRows > oldRows) oObj.rows = newRows;
  if (newRows < oldRows) oObj.rows = Math.max(3, newRows);
}

function fIsBooleanType(oObj) { 
  /* if the input field is a non-boolean type return false
     otherwise return true */
  var jqObj = $(oObj);
  if (oObj.tagName == "INPUT") {
	var type = jqObj.attr("type");
	if (!type) {
	  type = "text";
	}
	if (type.toLowerCase() == 'checkbox') {
	  return true;
	}
  }
  return false;
}

/**
 * TODO: duplicated in request_setFilter.js, remove one
 */
function fEnableDisable(sAction, oFormObject) {
  function fSetObjectCollection() {
    /* create a collection of all the objects with the name.  If this is a single
       object e.g. a text box it should just be one object but if it's a radio
       group or a bunch of checkboxes then we can set them all */
       
    /* now only works on radio groups (why did it ever work on checkboxes?) */
     
    function fFormObjDocument() {
      var oDocument=oFormObject.parentNode;
      while (oDocument.parentNode) oDocument=oDocument.parentNode;
      return oDocument;
    }
        
    var oDocument=fFormObjDocument();
    //if(oFormObject.getAttribute('type')!='radio') var cObjects= new Array(oFormObject);
    //else
    var cObjects=oDocument.getElementsByName(oFormObject.getAttribute('name'));
    oFormObject.objectCollection=cObjects;
    return cObjects;
  }	
  
  function fBusyAttr() {
    if(oFormObject.getAttribute('busyAttribute')) return oFormObject.getAttribute('busyAttribute');
    else return (fIsBooleanType(oFormObject)?'disabled':'changed');
  }
  // enable or disable the current object or object group

	var cObjects = oFormObject.objectCollection?oFormObject.objectCollection:fSetObjectCollection();
	for ( var i = 0; i < cObjects.length; i++) {
		// see whether we've passed in the hidden field from a picker
		var oObjToChange=((cObjects[i].getAttribute('type')=='hidden') && cObjects[i].label)?cObjects[i].label:cObjects[i];
		// if we're enabling remember to *remove* the busy attribute
		var sBusyAttr = fBusyAttr();
		if (sAction == 'enable') {
			oObjToChange.removeAttribute(sBusyAttr);
			// For IE: because style doesn't work on expando property, add/remove class as well
			if (sBusyAttr=='changed') {
				$(oObjToChange).removeClass('changed');
			}
		} else {
			oObjToChange.setAttribute(sBusyAttr, 'true');
			if (sBusyAttr=='changed') {
				$(oObjToChange).addClass('changed');
			}
		}
	}

/*
  var cObjects=oFormObject.objectCollection?oFormObject.objectCollection:fSetObjectCollection();
  //var xx=cObjects.length;
  for(var i=0;i<cObjects.length;i++) {  
	// see whether we've passed in the hidden field from a picker
	var oObjToChange=((cObjects[i].getAttribute('type')=='hidden') && cObjects[i].label)?cObjects[i].label:cObjects[i];
	// if we're enabling remember to *remove* the busy attribute
    if(sAction=='enable') oObjToChange.removeAttribute(fBusyAttr());
    else oObjToChange.setAttribute(fBusyAttr(),'true');   
  }	
  */  
}

function fChange(oObj)  {  
  /* object to handle changing the value of a form element and update the db immediately
	 over XMLHTTP.
			
	 The object stores the form object that it's working on, marks it as being updated, 
	 fires the request, collects the response and handles it. 
			
	 The object contains compatibility for text, checkbox, radio inputs and contentEditable divs.
			
	 Any expando property of the form object prefixed with 'gtpb_' will be sent
	 as a key value pair in the post parameters of the request.  In addition the name
	 and calculated value of the object are sent by default */
	 
  function fReqComplete(sResponseText,sResponseXML) { 
    function fDisplayValue() {  
      // note that vValue and vCurrentValue are currently equal
   	
   	  // obj is a select so show the value display, not the option value
   	  // we can use selected index as the value sent is the current value i.e. that shown
   	  if(oObj.tagName=='SELECT') return oObj.options[oObj.selectedIndex].innerHTML;   	 		
   	  if((oObj.tagName=='INPUT')&&(oObj.getAttribute('type')=='checkbox')) return vValue?'true':'false';
   	  if((oObj.tagName=='INPUT')&&(oObj.getAttribute('type')=='hidden') && oObj.label) return oObj.label.value;	
   	  // if there is a P2 field, it has a maxTextLength property, this is a number and the length of vValue is longer than this	
   	  if(oObj.pane2_field) 
   	    with (oObj.pane2_field)
   	      if (getAttribute('maxTextLength')&&(!isNaN(getAttribute('maxTextLength')))&&(vValue.length>getAttribute('maxTextLength'))) {
   	    // truncate the text if it exceeds the maximum value
   	    // this ensures that the JS update of P2 will show the same content as if it was loaded from the server
   	    return vValue.substring(0,getAttribute('maxTextLength'))+'...'; 	    
   	  }
   	  return vValue;
    }
    
    function fUpdatePane3() {
	    // Invalidate the tabs as their data will be obsolete
    	var frameset = frames['oViewPane'];
    	if (typeof(frameset) != "undefined") {
    		var pane3 = frameset.frames['pane_3'];
    		if (pane3 != "undefined") {
    			if (typeof(pane3.pane3TabInterface) != "undefined") {
    				pane3.pane3TabInterface.invalidate();
    			}
			}
    	}
    }
   	 
   	function fUpdatePane2() {	
   	  // the input field should have been matched with a field in pane2
   	  if(oObj.getAttribute('gtpb_global_edit') !== null) {
   	    // if editing globally, update the whole column of the current record
   	    if (!oObj.pane2_field) return;
        var iIndex=oObj.pane2_field.cellIndex;
    	var oViewPane = top.document.getElementById('oViewPane');
    	if (oViewPane == null) {
    	  return;
    	}
        var oRows=oViewPane.contentWindow.pane_2.document.getElementById('reportBody').rows;
        for (var i=0;i<oRows.length;i++) {
          if(oRows[i].getAttribute('class')!='trailing') oRows[i].cells[iIndex].innerHTML=fDisplayValue();
   	    }
   	  } else {
        if (!oObj.pane2_cell) return;
        var oCell=oObj.pane2_cell;
        // and update its value
        var sNewVal=fDisplayValue();
        if(sNewVal) oCell.innerHTML=sNewVal;
        //control wrapping
        if (sNewVal && (sNewVal.length<=20)) oCell.setAttribute('nowrap','true');
        else oCell.removeAttribute('nowrap');
      }
    }
     
    function fUpdatePane1() {  
    	var pane1_report_name = oObj.getAttribute("pane1_report_name");

    	if (typeof(pane1_report_name) == "string") {
    		$(top.oViewPane.pane_1.document).find("[name="+pane1_report_name+"]").find("a").eq(0).text(fDisplayValue());
    	}
    }
     
    function fSetError(sMessage) {	
      var oLink=document.createElement('A');
      oLink.innerHTML='error, click for more info';
      oLink.setAttribute('error','true');
      sMessage = sMessage.replace(/\'/,'&quot;')
      oLink.setAttribute('href','javascript:alert(\''+sMessage+'\')');
     		
      fClearError(true);
      oObj.errorMessageObj=oLink;
      oObj.parentNode.appendChild(oLink);
      oObj.setAttribute('title',sMessage);
      oObj.setAttribute('error','true');
    }
     
    function fClearError(bLeaveAttrs) {  
      // even if one doesn't exist, this will be OK
      if (oObj.errorMessageObj) {  
        oObj.errorMessageObj.parentNode.removeChild(oObj.errorMessageObj);
        oObj.errorMessageObj=false;
      }
      if (bLeaveAttrs) return;
      oObj.removeAttribute('error');
      oObj.removeAttribute('title');
    }
    
    function fClearWarning() {
    	var oViewPane = top.document.getElementById('oViewPane');
    	if (oViewPane == null) {
    	  return;
    	}
        var jqDoc = jQuery(oViewPane.contentWindow.pane_3.document);
        var warningId = $(oObj).attr("name") + "_warning";
        jqDoc.find("#" + warningId).hide("slow");
    }
    
    function fSetWarning(sMessage) {
      var oViewPane = top.document.getElementById('oViewPane');
      if (oViewPane == null) {
    	return;
      }
      var jqDoc = jQuery(oViewPane.contentWindow.pane_3.document);
      var warningId = $(oObj).attr("name") + "_warning";
      jqDoc.find("#" + warningId).text(sMessage).show("slow");
    }
     
    top.oBuffer.clearFromSendQueue(oObj);
    var jqObj = jQuery(oObj);
    if (oObj.tagName == 'DIV' && (!jqObj.hasClass("date"))) {
      var vCurrentValue = jqObj.html();
    } else {
      // TODO: should this line be exactly the same as the similar one earlier?
      var vCurrentValue=(fIsBooleanType(oObj)?oObj.checked:(oObj.getAttribute('e_value')?oObj.getAttribute('e_value'):oObj.value));
    }
    sResponse=sResponseXML.getElementsByTagName('response')[0].firstChild.nodeValue;
    if(sResponse!='ok') { // the action was not successfully processed by the server
      fSetError(sResponseXML.getElementsByTagName('exception')[0].firstChild.nodeValue);
      return; 
    }

    if(vCurrentValue!=vValue) return; // the current value has changed since this request was sent
    fClearError(false);
    fClearWarning();
    var warningElements = sResponseXML.getElementsByTagName('warning');
    if (warningElements.length > 0) {
      var warningElement = warningElements[0];
      var sWarning = warningElement.firstChild.nodeValue;
      fSetWarning(sWarning);
    }
    fUpdatePane1();
    fUpdatePane2();
    fUpdatePane3();
    fEnableDisable('enable',oObj);
  }
   
  function fSetPostVars() {  
    // create a key value array of the variables to post with the request to the server
   
    var aPostVars=new Array();
   
    // always pass these in the post request
    aPostVars['return']='gui/administration/xmlreturn_fieldchange';
    aPostVars['returntype']='xml';
    aPostVars[oObj.getAttribute('name')]=vValue;
   
    /* look at all the attributes that the DOM object has and pass all the 
       ones prefixed gtpb_ to the server in the post request   */
    for (var i=0;i<oObj.attributes.length;i++)
      with(oObj.attributes.item(i))
        if (nodeName.search(/^gtpb_/)>=0) {
          aPostVars[nodeName.replace(/^gtpb_/,'')]=nodeValue;
        }
      
    return aPostVars;
  }
   
  /* snapshot the state of the object now so that we can check if it's the same when
     the server returns.  The state we store depends on the type of form object */
  // Get value differently depending on whether the element is a div or form element
  var jqObj = jQuery(oObj);
  if (oObj.tagName == 'DIV' && (!jqObj.hasClass("date"))) {
	var vValue = jqObj.html();
  } else {
    var vValue=(fIsBooleanType(oObj)?oObj.checked:(oObj.getAttribute('e_value')?oObj.getAttribute('e_value'):jqObj.val()));
  }
  // a key value pair array of data to pass in the Post request
  var aPostVars=new Array();
  // the form object we're operating 
  var oFormObject=oObj;
   
  // show the form object as busy
  fEnableDisable('disable', oObj);
  var aPostVars=fSetPostVars();
  
  var oReq=new fRequest('AppController.servlet', aPostVars, fReqComplete, -1); 
  this.xml_http_request=oReq.xml_http_request;      
}
