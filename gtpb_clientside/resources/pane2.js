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

//numberOfTabsExpected parameter:
// -1 will always force a complete reload of pane 3
// null will always force a single tab refresh only
// Any other number will refresh if there are that many tabs in pane 3, otherwise reload
function loadIntoPane3(url, rowId, numberOfTabsExpected) {
	try {
		var pane_3_doc = parent.pane_3.document;
	} catch (err) {
		// If accessing the document property causes an error then it's most likely there's
		// not a HTML document in pane 3. It could be a document the user downloaded that has
		// displayed in an embedded fashion. The Adobe Acrobat Reader does this for PDF files
		// for example.
		// In this case, reload completely with window.open targeted at the pane_3 frame.
		window.open(url,"pane_3");
		return;
	}
	// POST session variables
	$.post(url, null, function(data) {
		// Refresh frame 3
		if (typeof(parent.pane_3) != "undefined") {
			// If user is loading a new report, it may have different privileges to the last one.
			// If so, reload the whole of pane 3 to refresh the tab list, otherwise just refresh the current tab
			if (typeof(parent.pane_3.pane3TabInterface) == "undefined") {
				// something in pane 3 but not a tabset
				parent.pane_3.document.location = url;
			} else if (document.location.href.match('set_module')) { // set_module means we must be viewing a report
				if((numberOfTabsExpected == null) || (numberOfTabsExpected == parent.pane_3.pane3TabInterface.getNumberOfTabs())) {
					// if pane 3 has the right number of tabs, we can just refresh the one tab
					try {
						parent.pane_3.pane3TabInterface.refresh(rowId);
					} catch(err) {
						alert("Fast refresh failed, falling back to slow - don't worry about this");
						parent.pane_3.document.location = url;
					}
				} else {
					// if it doesn't have the right number of tabs, we need to refresh the whole frame to reload the tabset
					parent.pane_3.document.location = url;
				}
			} else {
				// fallback after everything else: simple refresh of pane 3
				parent.pane_3.document.location = url;
			}
		}
	});
}

/* Add in a checkbox to the header row and each record row to allow the record to be deleted */

  function fSelectAll(oCheckbox){ 
    var iCellIndex=oCheckbox.parentNode.cellIndex;
  	  var oRows=document.getElementById('reportBody').rows;
	    for (var i=0;i<oRows.length;i++){ 
	      try { 
	        oRows[i].cells[iCellIndex].getElementsByTagName('INPUT')[0].checked=oCheckbox.checked;
	      }
	      catch(e) {  
	        // don't do anything
	      }
	    }
  }
 
  function fLocateDeleteMarkers(oCheckbox){
    // lets the delete object know what column the delete checkboxes are in 
    // cell could be a TD or a TH 
    function fParentCell() {
      var oObject=oCheckbox;
      // find the TD or the TH
      while((oObject.parentNode)&&(oObject.parentNode.tagName!='TR')) oObject=oObject.parentNode;
      // was it found?
      if ((oObject.parentNode)&&(oObject.parentNode.tagName=='TR')) {
        // cache the cell found
        oCheckbox.parentCell=oObject;
        return oObject;
      }
      return null;
    }
    // find the parent cell, if one has not been cached, find it
    var oCell=oCheckbox.parentCell?oCheckbox.parentCell:fParentCell();
    // if there is a cell
    if (oCell) iDeleteCellIndex=oCell.cellIndex;
  }
