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
function fInit() {  
	
  function fMatchFields() {  

    // function to match the fields displayed in pane2 with the input fields in pane3
    // oCells is a collection of the cells in the heading row of the table
    var oCells = top.document.getElementById('oViewPane').contentWindow.pane_2.document.getElementById('reportData').tHead.rows[0].cells;
    // the index of the current row
    if(top.document.getElementById('oViewPane').contentWindow.pane_2.document.getElementById('currentRow'))
      var iRowIndex = top.document.getElementById('oViewPane').contentWindow.pane_2.document.getElementById('currentRow').rowIndex;
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
      	  aMatchingElements[j].pane2_cell = top.document.getElementById('oViewPane').contentWindow.pane_2.document.getElementById('reportData').rows[iRowIndex].cells[i];
      	}
    } 
  }
	 
  function fMatchTree() {  
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
  with (oObj) {  
    switch (tagName) { 
      case 'TEXTAREA': return false;
      case 'SELECT': return false;
      case 'INPUT': { 
        switch(getAttribute('type')) { 
          case 'text': return false;
          case 'password': return false;
          case 'hidden': return false;
       	  default: return true;
     	}
      }
      return true;
    }
  }
}

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

  var cObjects=oFormObject.objectCollection?oFormObject.objectCollection:fSetObjectCollection();
  var xx=cObjects.length;
  for(var i=0;i<cObjects.length;i++) {  
	// see whether we've passed in the hidden field from a picker
	var oObjToChange=((cObjects[i].getAttribute('type')=='hidden') && cObjects[i].label)?cObjects[i].label:cObjects[i];
	// if we're enabling remember to *remove* the busy attribute
    if(sAction=='enable') oObjToChange.removeAttribute(fBusyAttr());
    else oObjToChange.setAttribute(fBusyAttr(),'true');   
  }	  
}

function fChange(oObj)  {  
  /* object to handle changing the value of a form element and update the db immediately
	 over XMLHTTP.
			
	 The object stores the form object that it's working on, marks it as being updated, 
	 fires the request, collects the response and handles it. 
			
	 The object contains compatibility for text, checkbox and radio input.
			
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
        var oRows=top.document.getElementById('oViewPane').contentWindow.pane_2.document.getElementById('reportBody').rows;
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
    	
    	// the input field should have been matched with a caption in pane1
      // check that the input was matched to a caption

      //if (!oObj.pane1_field) return;
      //update the caption's value

      //oObj.pane1_field.updateTitle(fDisplayValue());
      /*
      for(var i=0; i<oObj.pane1_field.childNodes.length; i++) {
        if(oObj.pane1_field.childNodes[i].nodeName=='#text') {
          oObj.pane1_field.replaceChild(document.createTextNode(fDisplayValue()),oObj.pane1_field.childNodes[i]);
          break;
        }
      }*/ 
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
     
    top.oBuffer.clearFromSendQueue(oObj);
    var vCurrentValue=(fIsBooleanType(oObj)?oObj.checked:(oObj.getAttribute('e_value')?oObj.getAttribute('e_value'):oObj.value));    
    sResponse=sResponseXML.getElementsByTagName('response')[0].firstChild.nodeValue;
    if(sResponse!='ok') { // the action was not successfully processed by the server
      fSetError(sResponseXML.getElementsByTagName('exception')[0].firstChild.nodeValue);
      return; 
    }

    if(vCurrentValue!=vValue) return; // the current value has changed since this request was sent
    fClearError(false);
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
  var vValue=(fIsBooleanType(oObj)?oObj.checked:(oObj.getAttribute('e_value')?oObj.getAttribute('e_value'):$(oObj).val()));
  /* mark the input as busy.  What should be set depends on the type of form object.
     NOTE: changed attribute is an expando with a style set to show it's applied */
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