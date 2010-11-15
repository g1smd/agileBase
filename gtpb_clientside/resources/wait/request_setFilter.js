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
          //alert('Warning - network connection problem!\n'+oReqObj.statusText);
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

/*
 * =========================================
 * The below is originally from setFilter.js
 */

function fLoadReport(sResponseText, oElement, fCallback) {
	if (!$.browser.msie) {
		// Everything but IE can use innerHTML for this
		oElement.innerHTML = sResponseText;
		fUpdateOtherPanes();
		if(fCallback) fCallback();
		return;
	}

	/*
	 * parsing the whole xml document is very slow and causes the application to
	 * 'lock' unacceptably instead, the xml document is parsed 10 top level
	 * elements at at time. These are inserted into the document and the
	 * function calls itself again using a setTimeout. This pause allows the
	 * application to update the screen and respond to other input at the
	 * expense of the loading taking even longer.
	 * 
	 * However, because the user can see the data loading and still use the
	 * application, the delay becomes far more acceptable.
	 */

	// empty the element
	$(oElement).empty();

	// Get the rows
	var aRowStrings = sResponseText.split("<tr");

	var lastRenderIndex = 0;

	// Update the row count in pane 1 and the summary view (if applicable) in pane 3
	function fUpdateOtherPanes() {
		var numRows = $('#numrows').html();
		var pane1Id = $('#pane1id').text();
		if ((numRows != null) && (self != top)) {
			parent.pane_1.fSetCurrentOption(pane1Id, numRows);
		}
		if (document.getElementById('pane3tab')) {
		  var pane3tab = document.getElementById('pane3tab').innerHTML;
		  if (pane3tab == '3') {
			parent.pane_2
					.loadIntoPane3(
							'AppController.servlet?return=gui/reports_and_tables/pane3',
							-1, null);
		  }
		}
	}
	
	function fRenderRows(begin, end) {
		var len = aRowStrings.length;
		var tempRenderIndex = 0;

		// Parse 10 rows at a time
		for ( var i = lastRenderIndex; i < lastRenderIndex + 10; i++) {
			if (i < aRowStrings.length) {
				$(oElement).append("<tr" + aRowStrings[i]);
				// oElement.innerHTML(oElement.innerHTML+"<tr"+aRowStrings[i]);
				tempRenderIndex = i;
			} else {
				break;
			}
		}

		lastRenderIndex = tempRenderIndex + 1;

		if (lastRenderIndex < aRowStrings.length) {
			setTimeout(fRenderRows, 0);
		} else {
			fUpdateOtherPanes();
			if(fCallback) fCallback()
		}
	}

	fRenderRows();
}

/*
 * ---------------------------------------------------- Set filter function
 * ----------------------------------------------------
 */

function fSetFilter(oObj, fReqCompleteOverride) {
	/*
	 * object to handle changing a filter element and update the db immediately
	 * over XMLHTTP.
	 * 
	 * The object stores the form object that it's working on, marks it as being
	 * updated, fires the request, collects the response and handles it.
	 * 
	 * The object contains compatibility for text, checkbox and radio input.
	 * 
	 * Any expando property of the form object prefixed with 'gtpb_' will be
	 * sent as a key value pair in the post parameters of the request. In
	 * addition the name and calculated value of the object are sent by default
	 */

	function fEnableDisable(sAction) {
		// enable or disable the current object or object group

		/*
		 * create a collection of all the objects with the name. If this is a
		 * single object e.g. a text box it should just be one object but if
		 * it's a radio group or a bunch of checkboxes then we can set them all
		 */
		var cObjects = document.getElementsByName(oFormObject
				.getAttribute('name'));
		for ( var i = 0; i < cObjects.length; i++)
			// if we're enabling remember to *remove* the busy attribute
			if (sAction == 'enable') {
				cObjects[i].removeAttribute(sBusyAttr);
				// For IE: because style doesn't work on expando property, add/remove class as well
				if (sBusyAttr=='changed') {
					$(cObjects[i]).removeClass('changed');
				}
			} else {
				cObjects[i].setAttribute(sBusyAttr, 'true');
				if (sBusyAttr=='changed') {
					$(cObjects[i]).addClass('changed');
				}
			}
		// oFormObject and sBusyAttr are global to the fChange object
	}

	function fIsBooleanType(oObj) {
		/*
		 * if the input field is a non-boolean type return false otherwise
		 * return true
		 */
		with (oObj) {
			  if (tagName == "INPUT") {
				  var type=getAttribute(type);
				  if(!type) {
					  type='text';
				  }
				  if (type.toLowerCase() == 'checkbox') {
					  return true;
				  }
			  }
			  return false;
			  /*
			if (tagName == 'TEXTAREA')
				return false;
			if (tagName == 'INPUT') {
				switch (getAttribute('type')) {
				case 'text':
					return false;
				default:
					return true;
				}
			}
			return true;
			*/
		}
	}

	function fReqComplete(sResponseText, sResponseXML) {
		if (!sResponseText)
			return;
		if (sResponseText == null)
			return;
		if (sResponseText == '')
			return;
		var bIsXMLRequest = (oObj.getAttribute('gtpb_returntype') == 'xml');
		var keyupBug = (oObj.getAttribute('keyup_bug'));
		// TODO --> find a way to make this more generic
		var oReportBody = document.getElementById('reportBody');

		//var vCurrentValue = (fIsBooleanType(oObj) ? oFormObject.checked
		//		: oFormObject.value);
		var vCurrentValue = $(oFormObject).val();
		if ((vCurrentValue != vValue) && (!keyupBug))
			return; // the current value has changed since this request was sent
		fLoadReport(sResponseText, oReportBody,null);
		fEnableDisable('enable');
	}

	function fSetPostVars() {
		// create a key value array of the variables to post with the request to
		// the server

		var aPostVars = new Array();

		// always pass these in the post request
		// return value now set in the filter input element
		// aPostVars['return']='gui/reports_and_tables/report_data_only';
		if (oObj.getAttribute('name')) { // if the object has a name
			aPostVars[oObj.getAttribute('name')] = vValue;
			aPostVars['fieldvalue'] = vValue;
			aPostVars['set_report_filter_value'] = 'true';
		}

		/*
		 * look at all the attributes that the DOM object has and pass all the
		 * ones prefixed gtpb_ to the server in the post request
		 */
		for ( var i = 0; i < oObj.attributes.length; i++)
			with (oObj.attributes.item(i))
				if (nodeName.search(/^gtpb_/) >= 0)
					aPostVars[nodeName.replace(/^gtpb_/, '')] = nodeValue;

		return aPostVars;
	}

	function fPostString(aPostVars) {
		if (!aPostVars)
			return null;
		var sPostString = '';
		for ( var sKey in aPostVars) {
			if (sKey == 'containsValue')
				continue; // this is a special case. containsValue is a
							// prototype method. I can't think of another way to
							// exclude it from the array :(
			if (sKey != 'return')
				aPostVars[sKey] = encodeURIComponent(aPostVars[sKey]); // return
																		// is a
																		// special
																		// case.
																		// Otherwise
																		// URI
																		// encode
																		// non-word
																		// chars
			sPostString += sKey
					+ (aPostVars[sKey] ? '=' + aPostVars[sKey] : '') + '&';
		}
		// remove the trailing &
		return sPostString.slice(0, -1);
	}

	/*
	 * snapshot the state of the object now so that we can check if it's the
	 * same when the server returns. The state we store depends on the type of
	 * form object
	 */
	var vValue = (fIsBooleanType(oObj) ? oObj.checked : oObj.value);
	/*
	 * mark the input as busy. What should be set depends on the type of form
	 * object. NOTE: changed attribute is an expando with a style set to show
	 * it's applied. Won't work in IE :(
	 */
	var sBusyAttr = (fIsBooleanType(oObj) ? 'disabled' : 'changed');
	// a key value pair array of data to pass in the Post request
	var aPostVars = new Array();
	// make the form object we're operating on private so that the sub objects
	// can reference it
	var oFormObject = oObj;

	// show the form object as busy
	fEnableDisable('disable');
	aPostVars = fSetPostVars();
	var sPostString = fPostString(aPostVars);
	var fReqCompleteToRun = fReqCompleteOverride ? fReqCompleteOverride : fReqComplete;
	if (ajaxManager == null) {
		ajaxManager = $.manageAjax( {
			manageType :'queue',
			maxReq :2,
			blockSameRequest :true
		});
	}
	;
	if (fReqCompleteOverride) {
		var oReq = new fRequest('AppController.servlet', aPostVars,
				fReqCompleteToRun, -1);
	} else {
		ajaxManager.add( {
			type :"POST",
			url :"AppController.servlet",
			timeout :15000,
			data :sPostString,
			success :fReqCompleteToRun,
			error : function(XMLHttpRequest, textStatus, errorThrown) {
				alert('problem setting filter: ' + textStatus);
			}
		});
	}

}

var ajaxManager = null;

/*
 * ---------------------------------------------------- Sort column function
 * ----------------------------------------------------
 */

function fSetSort(oColHeader) {
	function fReqComplete(sResponseText, sResponseXML) {
		function fRemoveWait() {
		  $(oColHeader).removeClass('waiting');				
		}
		if ((!sResponseText)||(sResponseText == null)||(sResponseText == '')) {
			fRemoveWait();
			return;
		}
		// TODO --> find a way to make this more generic
		var oReportBody = document.getElementById('reportBody');

		// currently agileBase doesn't return over XML but the option was developed
		// for GTtT 4.0 where records in p2 load incrementally
		// this is less of an issue in FF than in IE. To set the return type as
		// XML set gtpb_returntype=true
		// if(bIsXMLRequest) new fParseXML(sResponseXML,oReportBody);
		// else oReportBody.innerHTML=sResponseText;
		//oReportBody.innerHTML = sResponseText;
		
		//$('#reportBody')
		
		fLoadReport(sResponseText, oReportBody, fRemoveWait);
	}

	var oSortedColumn = document.getElementById('sortedColumn');
	var aPostVars = new Array();
	aPostVars['return'] = 'gui/reports_and_tables/report_data_only';

	if (oSortedColumn && (oSortedColumn != oColHeader)) {
		// a sort is set on another column
		oSortedColumn.removeAttribute('id');
	}

	if (oSortedColumn == oColHeader) {
		// the sort already exists on this column
		// change the sort direction or clear the sort
		switch (oColHeader.getAttribute('sortDirn')) {
		case 'true':
			oColHeader.setAttribute('sortDirn', 'false');
			aPostVars['set_report_sort'] = 'true';
			aPostVars['sortdirection'] = 'false';
			aPostVars['internalfieldname'] = oColHeader.getAttribute('internalName');
			break;

		case 'false':
			oColHeader.removeAttribute('sortDirn');
			oColHeader.removeAttribute('id');
			aPostVars['clear_all_report_sorts'] = 'true';
			break;

		// this probably doesn't happen. If a sort isn't set then the current
		// column isn't the sorted column
		default:
			alert('unexpected error, unable to sort report\n[client side error]');
			return;
		}
	} else {
		// the sort was set on another column or no sort is set - set the column
		// as sorted
		oColHeader.setAttribute('id', 'sortedColumn');
		// set the sort direction (ascending = true)
		oColHeader.setAttribute('sortDirn', 'true')
		aPostVars['set_report_sort'] = 'true';
		aPostVars['sortdirection'] = 'true';
		aPostVars['internalfieldname'] = oColHeader.getAttribute('internalName');
	}
	
	$(oColHeader).addClass('waiting');
	var oReq = new fRequest('AppController.servlet', aPostVars, fReqComplete, -1);
}
