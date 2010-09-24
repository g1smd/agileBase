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

// clear all the selected rows
// there should only be one but will clear all if more than this.
function fClearRowSelection() {
	while (oSelected = document.getElementById('currentRow'))
		oSelected.id = null;
}

// set the row with the name sName to show it as selected
function fSetRowSelection(sName) {
	fClearRowSelection();
	var oBodyRows = document.getElementById('reportBody').rows;
	// remove excluded row when any other row clicked
	var jqExcludedRow = $('tr.excludedRecord');
	if (jqExcludedRow.attr('name') != sName) {
		jqExcludedRow.remove();
	}
	for ( var i = 0; i < oBodyRows.length; i++) {
		if (oBodyRows[i].getAttribute('name') == sName) {
			var currentRow = oBodyRows[i];
			currentRow.id = 'currentRow';
			// Work out whether we need to scroll the row into view (if it's off
			// screen)
			var excluded = $(currentRow).hasClass("excludedRecord")
			if (!excluded) {
				var currentRowOffsetTop = currentRow.offsetTop;
				// var visiblePortionTop =
				// document.getElementById('wrapper').scrollTop;
				var visiblePortionTop = document.scrollTop;
				// var visiblePortionBottom = visiblePortionTop +
				// document.body.clientHeight;
				var visiblePortionBottom = visiblePortionTop
						+ document.body.offsetHeight;
				if ((currentRowOffsetTop < visiblePortionTop)
						|| ((currentRowOffsetTop + 15) > visiblePortionBottom)) {
					currentRow.scrollIntoView(true);
				}
				// row was found
				return true;
			}
		}
	}
	return false;
}

function fDeleteObj(sAction, sRowIdentifier) {
	function fControlCheckboxes(bDisable) {
		var aCheckedRows = new Array();

		// set the header checkbox
		var oCheckbox = document.getElementById('reportData').rows[0].cells[iDeleteCellIndex]
				.getElementsByTagName('INPUT')[0]
		if (oCheckbox) {
			if (bDisable)
				oCheckbox.setAttribute('disabled', 'true');
			else {
				oCheckbox.removeAttribute('disabled');
				oCheckbox.checked = false;
			}
		}

		// set the row checkboxes
		var oRows = document.getElementById('reportBody').rows;
		for ( var i = 0; i < oRows.length; i++) {
			if (!oRows[i].cells[iDeleteCellIndex])
				continue;
			var oCheckbox = oRows[i].cells[iDeleteCellIndex]
					.getElementsByTagName('INPUT')[0];
			if (!oCheckbox)
				continue;
			if (bDisable)
				oCheckbox.setAttribute('disabled', 'true');
			else
				oCheckbox.removeAttribute('disabled');
			if (oCheckbox.checked)
				aCheckedRows.push(oRows[i]);
		}

		return aCheckedRows;
	}

	function fReqComplete(sResponseText, sResponseXML) {
		function fReformatTable() {
			$('#reportBody tr:even').not('.trailing').not('.seemorerows')
					.removeClass().addClass('rowa');
			$('#reportBody tr:odd').not('.trailing').not('.seemorerows')
					.removeClass().addClass('rowb');
		}

		function fRetryDeletions() {
			var sExceptionMessage = sResponseXML
					.getElementsByTagName('exception')[0].firstChild.nodeValue;
			if (!confirm('Some rows were not deleted because they are linked to data in other tables.\n\n' + sExceptionMessage + '\n\nWould you like to delete these rows and the related data or CANCEL this operation?'))
				return false;
			bDeleteRelatedData = true;
			bFailedDeletions = false;
			bRetryDeletions = false;
			aCheckedRows = fControlCheckboxes(true);
			fDeleteFirstItem();
			return true;
		}

		sResponse = sResponseXML.getElementsByTagName('response')[0].firstChild.nodeValue;
		if (sResponse == 'ok') { // the action was successfully processed by
									// the server
			if (oCurrentRow == oSessionItem)
				bRemovedSessionItem = true;
			var oRowParent = oCurrentRow.parentNode;
			var iRowIndex = oRowParent.tagName == 'TABLE' ? oCurrentRow.rowIndex
					: oCurrentRow.sectionRowIndex;
			oRowParent.deleteRow(iRowIndex);
		} else {
			bFailedDeletions = true;
			sException = sResponseXML.getElementsByTagName('exception')[0]
					.getAttribute('type');
			var sExceptionMessage = sResponseXML
					.getElementsByTagName('exception')[0].firstChild.nodeValue;
			if (sException == 'DataDependencyException')
				bRetryDeletions = true;
		}
		if (fDeleteFirstItem())
			return true; // the next item will be deleted

		if (bFailedDeletions) {
			// some records may not have been deleted if they included relations
			if (bRetryDeletions) {
				if (fRetryDeletions())
					return true; // deletions can be retried if the user has
									// permissions on the table containing the
									// data
			} else
				alert('Some rows were not deleted because they are linked to data in other tables that you do not have permission to change.\n\n' + sExceptionMessage);
		}

		// clean everything up
		// if we've removed the session set a new one as the first row
		if (bRemovedSessionItem) {
			if (document.getElementById('reportBody').rows.length > 1) // there
																		// will
																		// always
																		// be
																		// the
																		// filler
																		// row
				eval(document.getElementById('reportBody').rows[0]
						.getAttribute('onclick'));
			else
				parent.pane_3.document.location = 'AppController.servlet?return=$return';
		}
		fControlCheckboxes(false); // re-enable the checkboxes
		fReformatTable();
	}

	function fDeleteFirstItem() {
		if (aCheckedRows.length < 1)
			return false;
		oCurrentRow = aCheckedRows.shift();
		var aPostVars = new Array();

		// always pass these in the post request
		aPostVars['return'] = 'gui/administration/xmlreturn_fieldchange';
		aPostVars['returntype'] = 'xml';

		if (bDeleteRelatedData)
			aPostVars['cascadedelete'] = '1';

		aPostVars[sAction] = 'true';
		aPostVars[sRowIdentifier] = oCurrentRow.getAttribute('name');

		oReq = new fRequest('AppController.servlet', aPostVars, fReqComplete, 1);
		return true;
	}

	// nothing has been selected for deletion
	try {
		if (!iDeleteCellIndex)
			return;
	} catch (e) {
		return;
	}

	var sAction = sAction;
	var sRowIdentifier = sRowIdentifier;
	var bRemovedSessionItem = false;
	var bFailedDeletions = false;
	var bRetryDeletions = false;
	var bDeleteRelatedData = false;
	var oSessionItem = document.getElementById('currentRow');
	var oCurrentRow;
	var oReq;
	var aCheckedRows = fControlCheckboxes(true);
	fDeleteFirstItem();
}

// numberOfTabsExpected parameter:
// -1 will always force a complete reload of pane 3
// null will always force a single tab refresh only
// Any other number will refresh if there are that many tabs in pane 3,
// otherwise reload
function loadIntoPane3(url, rowId, numberOfTabsExpected) {
	try {
		var pane_3_doc = parent.pane_3.document;
	} catch (err) {
		// If accessing the document property causes an error then it's most
		// likely there's
		// not a HTML document in pane 3. It could be a document the user
		// downloaded that has
		// displayed in an embedded fashion. The Adobe Acrobat Reader does this
		// for PDF files
		// for example.
		// In this case, reload completely with window.open targeted at the
		// pane_3 frame.
		window.open(url, "pane_3");
		return;
	}
	// POST session variables
	// When posting, we don't want the server to waste time actually parsing the
	// contents of any return template,
	// we just want any session commands to be posted. So replace the template
	// name with the blank template
	var templateName = url.replace(/^.*return=/, '');
	templateName = templateName.replace(/\&.*$/, '');
	replacedTemplateUrl = url.replace('return=' + templateName, 'return=blank');
	$.post(replacedTemplateUrl, null, function(data) {
				// Refresh frame 3
					if (typeof (parent.pane_3) != "undefined") {
						// If user is loading a new report, it may have
						// different privileges to the last one.
					// If so, reload the whole of pane 3 to refresh the tab
					// list, otherwise just refresh the current tab
					if (typeof (parent.pane_3.pane3TabInterface) == "undefined") {
						// something in pane 3 but not a tabset
						parent.pane_3.document.location = url;
					} else if (document.location.href.match('set_module')) { // set_module
																				// means
																				// we
																				// must
																				// be
																				// viewing
																				// a
																				// report
						if ((numberOfTabsExpected == null)
								|| (numberOfTabsExpected == parent.pane_3.pane3TabInterface
										.getNumberOfTabs())) {
							// if pane 3 has the right number of tabs, we can
							// just refresh the one tab
							try {
								parent.pane_3.pane3TabInterface.refresh(rowId);
							} catch (err) {
								// alert("Fast refresh failed, falling back to
								// slow - don't worry about this");
								parent.pane_3.document.location = url;
							}
						} else {
							// if it doesn't have the right number of tabs, we
							// need to refresh the whole frame to reload the
							// tabset
							parent.pane_3.document.location = url;
						}
					} else {
						// fallback after everything else: simple refresh of
						// pane 3
						parent.pane_3.document.location = url;
					}
				}
			});
}

/*
 * Add in a checkbox to the header row and each record row to allow the record
 * to be deleted
 */
function fSelectAll(oCheckbox) {
	//var iCellIndex = oCheckbox.parentNode.parentNode.cellIndex; // checkbox <-
																// div <- th
	var iCellIndex = jQuery(oCheckbox).closest('th').attr('cellIndex');
	alert('detected cell index ' + iCellIndex);
	var oRows = document.getElementById('reportBody').rows;
	for ( var i = 0; i < oRows.length; i++) {
		try {
			var theCheckbox = oRows[i].cells[iCellIndex]
					.getElementsByTagName('INPUT')[0];
			if (jQuery(theCheckbox).is(":visible")) {
				theCheckbox.checked = oCheckbox.checked;
			}
		} catch (e) {
			// don't do anything
		}
	}
}

//TODO: simplify with jQuery
function fLocateDeleteMarkers(oCheckbox) {
	// lets the delete object know what column the delete checkboxes are in
	// cell could be a TD or a TH
	function fParentCell() {
		var oObject = oCheckbox;
		// find the TD or the TH
		while ((oObject.parentNode) && (oObject.parentNode.tagName != 'TR'))
			oObject = oObject.parentNode;
		// was it found?
		if ((oObject.parentNode) && (oObject.parentNode.tagName == 'TR')) {
			// cache the cell found
			oCheckbox.parentCell = oObject;
			return oObject;
		}
		return null;
	}
	// find the parent cell, if one has not been cached, find it
	var oCell = oCheckbox.parentCell ? oCheckbox.parentCell : fParentCell();
	// if there is a cell
	if (oCell) {
		iDeleteCellIndex = oCell.cellIndex;
	}
}

var abTooltipTimeout;
function showTooltip() {
	// First, hide any other tooltips visible
	$(".ab_tooltip").hide();
	var tooltip = $(this).next(".ab_tooltip");
	var href = tooltip.attr("rel");
	tooltip.load(href, function() {
		tooltip.fadeIn("fast");
		tooltip.fadeTo("fast",0.95);
		tooltip.find(".sparkline").sparkline('html', { type:'bar' });
		abTooltipTimeout = setTimeout("hideTooltip()",8000);
	});
}

function hideTooltip() {
	// Actually, hide any visible tooltips
	$(".ab_tooltip").fadeOut("normal");
	clearTimeout(abTooltipTimeout);
}