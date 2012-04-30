/*
 *  Copyright 2012 GT webMarque Ltd
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
var pane3TabInterface;
var pane3InterfaceUpdateFunctions = new Array();

// Store and execute scripts required for different content
var pane3Scripts = function() {
	pane3ScriptsPub = new Object();
	pane3ScriptsPub.functionList = new Array();
	pane3ScriptsPub.update = function() {
		var len = pane3ScriptsPub.functionList.length;
		for (var i = 0; i < len; i++) {
			pane3ScriptsPub.functionList[i]();
		}
	}
	return pane3ScriptsPub;
}();

// Tab interface object
var TabInterfaceObject = function(containerElem) {
	var TabInterfaceObjectPub = new Object();

	var currentRowId = -1;
	var jqTabInterface = $(containerElem);
	var jqTabContentContainer = jqTabInterface.find('.tab-content');
	var tabList = new Array();
	var currentTab;

	var loadSpinner = function() {
		loadSpinnerLoadingPub = new Object;
		var spinnerElem = $("<div class='load-spinner' />");

		loadSpinnerLoadingPub.show = function() {
			jqTabContentContainer.append(spinnerElem);
			spinnerElem.show();
		}

		loadSpinnerLoadingPub.hide = function() {
			spinnerElem.hide();
		}

		return loadSpinnerLoadingPub;
	}();

	var TabObject = function(tabNumber, linkElem, current) {
		var TabObjectPub = new Object();

		var jqLinkElem = $(linkElem);
		var tabSource = jqLinkElem.attr('href');

		var jqStrongElem = $("<strong />");
		jqStrongElem.append(jqLinkElem.contents().clone());

		var currentElem = jqLinkElem;

		var jqTabContainer = $("<div class='tab-item _" + tabNumber + "'></div>");

		var tabLoaded = false;

		TabObjectPub.queueTab = function() {
			tabLoaded = false;

			loadSpinner.show();
			jqTabContainer.load(tabSource, null, function() {
				TabObjectPub.showTab();

				loadSpinner.hide();
				if (parent.pane_2 && currentRowId != -1) {
					var rowFound = parent.pane_2.fSetRowSelection(currentRowId);
					if ((!rowFound) && (tabNumber < 2)) {
						$.get("?return=gui/reports_and_tables/tabs/edit_warning", function(
								warningRowHtml) {
							appendWarning(warningRowHtml);
						});
					}
				}
				pane3Scripts.update();
			});
			tabLoaded = true;
		}

		TabObjectPub.showTab = function(evt) {
			// First, if there is an active relation picker, close it
			$("#relationPicker").remove();

			var len = tabList.length;
			for ( var i = 0; i < len; i++) {
				if (tabList[i] != TabObjectPub) {
					tabList[i].hideTab();
				}
			}

			if (!tabLoaded) {
				jqTabContainer.empty();
				loadSpinner.show();
				jqTabContainer.load(tabSource, null, function() {
					loadSpinner.hide();
					if (parent.pane_2 && currentRowId != -1) {
						var rowFound = parent.pane_2.fSetRowSelection(currentRowId);
						if ((!rowFound) && (tabNumber < 2)) {
							$.get("?return=gui/reports_and_tables/tabs/edit_warning",
									function(warningRowHtml) {
										appendWarning(warningRowHtml);
									});
						}
					}
					pane3Scripts.update();
				});
				tabLoaded = true;
			}

			jqTabContainer.show();

			// Set session variables
			aPostVars = new Object();
			aPostVars['set_custom_string'] = '1';
			aPostVars['key'] = 'report_tabindex';
			aPostVars['value'] = tabNumber + 1;
			aPostVars['return'] = 'blank';
			var oReq = new fRequest('AppController.servlet', aPostVars, fNothing, -1);

			// Disable and replace link
			if (currentElem != jqStrongElem) {
				currentElem.replaceWith(jqStrongElem);
				currentElem = jqStrongElem;
			}

			currentTab = TabObjectPub;
			pane3Scripts.update();
			return false;
		}

		TabObjectPub.hideTab = function() {
			// Reset and enable link
			if (currentElem != jqLinkElem) {
				currentElem.replaceWith(jqLinkElem);
				currentElem = jqLinkElem;
				$(linkElem).bind("click", TabObjectPub.showTab);
			}
			jqTabContainer.hide();
		}

		TabObjectPub.invalidate = function() {
			tabLoaded = false;
		}

		jqTabContentContainer.append(jqTabContainer);
		TabObjectPub.hideTab();

		jqLinkElem.bind("click", TabObjectPub.showTab);

		if (current) {
			TabObjectPub.showTab();
		}

		return TabObjectPub;
	}

	jqTabContentContainer.empty();

	var linkElem;
	var current;
	jqTabInterface.find('.tab-list li').each(function(i) {
		linkElem = $(this).find("a")[0];
		current = false;
		if ($(this).hasClass("current")) {
			current = true;
		}
		tabList.push(new TabObject(i, linkElem, current));
	});

	TabInterfaceObjectPub.invalidate = function() {
		var len = tabList.length;
		for ( var i = 0; i < len; i++) {
			tabList[i].invalidate();
		}
	}

	TabInterfaceObjectPub.refresh = function(rowId) {
		TabInterfaceObjectPub.invalidate();
		if (typeof rowId != "undefined") {
		  currentRowId = rowId;
		}
		currentTab.queueTab();
	}

	TabInterfaceObjectPub.getNumberOfTabs = function() {
		return tabList.length;
	}

	return TabInterfaceObjectPub;
}

// Initialise
$(document).ready(function() {
	pane3TabInterface = new TabInterfaceObject($('.tab-interface')[0]);
	if (typeof fInit != 'undefined') {
		pane3Scripts.functionList.push(fInit);
	}
	pane3Scripts.update();
});

/*
 * ========================================================= End of tab engine
 * functions. Start of general functions for to be used by the tabs themselves.
 * First, the edit tab
 */

function fUnlockButton() {

	var jqUnlockButton = $("#recordUnlockButton");
	var jqUnlockDiv = $("#recordUnlock");
	var jqBlankDiv = $("#recordBlank");

	function fUnlockRecord() {
		function fUnlockResult(sText, sXML) {
			try {
				if (sXML.getElementsByTagName('response')[0].firstChild.nodeValue != 'ok')
					throw 'result_not_ok';
				jqUnlockDiv.remove()
				jqBlankDiv.remove();
			} catch (e) {
				jqUnlockButton.removeAttr('disabled');
				jqUnlockButton.find("div").empty().text("unlock record");
				alert('the record could not be unlocked\nplease try again\n(' + e + ')');
			}
		}

		jqUnlockButton.attr('disabled', 'true');
		jqUnlockButton.find("div").empty().text("unlocking...");

		var aPostVars = new Array();
		aPostVars['return'] = 'gui/administration/xmlreturn_fieldunlockrequest';
		aPostVars['set_lock_override'] = 'true';
		aPostVars['returntype'] = 'xml';
		var oReq = new fRequest('AppController.servlet', aPostVars, fUnlockResult,
				-1);
	}

	if (jqUnlockButton.length >= 1) {
		jqUnlockButton.bind("click", fUnlockRecord);
	}
}

function fComboComponents() {
	// ComboComponent object
	function fComboComponent(elem) {
		var jqSelect = $(elem);
		var globalEdit = false;

		// Extract unique field name and id end-string
		var internalFieldName = jqSelect.attr("id");
		if (internalFieldName.indexOf("global-edit") != -1) {
			var globalEdit = true;
		}

		var idEndString = internalFieldName.substring(internalFieldName
				.indexOf("_dropdown") + 9);
		internalFieldName = internalFieldName.substring(0, internalFieldName
				.indexOf("_dropdown"));

		// Get associated text box
		var jqTextBox = $("#" + internalFieldName + "_text" + idEndString);
		if (jqTextBox.length > 0) {
			jqTextBox.hide();
		}

		// Either show the text box, or populate and update
		function fDropdownChange(evt) {
			if (jqTextBox.length > 0) {
				if (jqSelect.val() == "new-custom-option") {
					jqTextBox.show();
					jqTextBox.val("");
				} else {
					jqTextBox.val(jqSelect.val())
					jqTextBox.hide();
					if (!globalEdit) {
						new fChange(jqTextBox[0]);
					}
				}
			} else {
				new fChange(elem);
			}
		}

		jqSelect.bind("change", fDropdownChange);
	}

	// Setup all combo-components
	$('.combo-component').each(function(i) {
		fComboComponent(this);
	});
}

// TODO: move this in to the mobile override CSS, doesn't need to be JS
function fSetOverflowHack() {
	document.getElementById('gtpb_wrapper').style.overflow = 'auto';
}

function fUpdateGlobalRelation() {
	function fResponse(sResponseText, sResponseXML) {
		if (sResponseXML.getElementsByTagName('rowsTotal')[0]) {
			var sRowsToChange = sResponseXML.getElementsByTagName('rowsTotal')[0].firstChild.nodeValue;
			sFieldName = $(oField).attr("field_name");
			if (confirm('Are you sure that you want to change the value of '
					+ sFieldName + ' to ' + oField.label.value + '?\nThis will update '
					+ sRowsToChange + ' records')) {
				new fChange(oField);
			}
		}
	}
	var oField = this.field;
	var aPostVars = new Array();
	aPostVars['returntype'] = 'xml';
	aPostVars['return'] = 'gui/resources/sessionReportInfo';
	var oReq = new fRequest('AppController.servlet', aPostVars, fResponse, 0);
}

function fRelationPickers() {
	$("input.relation_hidden").each(function() {
				var oHidden = this;
				var jqHidden = $(oHidden);
				if (jqHidden.attr("ab_setup_complete") == "true") {
					return;
				}
				jqHidden.attr("ab_setup_complete", "true");
				oHidden.doUpdate = function(sValue) {
					var bIsGlobalEdit = false;
					var bIsAutoUpdate = false;
					if (jqHidden.attr("gtpb_global_edit")) {
						bIsGlobalEdit = true;
					}
					if (typeof jqHidden.attr("gtpb_set_row_id") != 'undefined') {
						bIsAutoUpdate = true;
					}
					jqHidden.val(sValue);
					if ((!bIsGlobalEdit) && bIsAutoUpdate) {
						jqHidden.attr("gtpb_set_row_id", sValue);
					}
					// if it's not a global edit, do the update if we're updating as we
					// type
					// if it is a global edit, only update when the button is clicked i.e.
					// not a global update
					if ((!bIsGlobalEdit && bIsAutoUpdate)
							|| (bIsGlobalEdit && !bIsAutoUpdate)) {
						if ((!bIsGlobalEdit) && (sValue == "-2")) {
							// create a new record to link to
							relationNewRecord(oHidden);
						} else {
							// a normal save
							if(jqHidden.attr("gtpb_refresh_after") == "true") {
							  new fChange(oHidden, function() {
							  	pane3TabInterface.refresh();
							  });
							} else {
							  new fChange(oHidden);
							}
							try {
								// any additional actions to the save
								relationChangeActions(oHidden);
							} catch (err) {
								// relationChangeActions may not exist,
								// it is only defined in some circumstances
							}
						}
					}
					oHidden.label = jqHidden.next()[0];
				};
				oHidden.doUpdateObj = function(oValue) {
					oHidden.doUpdate(oValue.id);
				};
			});

	$("a.clicker").each(function() {
		var jqClicker = $(this);
		if (jqClicker.attr("ab_setup_complete") == "true") {
			return;
		}
		jqClicker.attr("ab_setup_complete", "true");
		jqClicker.click(fPicker);
		var deviceAgent = navigator.userAgent.toLowerCase();
		if (deviceAgent.match(/(iphone|ipod|ipad)/)) {
			jqClicker.click(fSetOverflowHack, false);
		}
		var jqHidden = jqClicker.siblings("input.relation_hidden");
		this.formEl = jqHidden[0];
		this.formEl.clicker = this;
	});

	// TODO: this isn't just a relation function, it affects all fields
	// Perhaps it should be in a separate function
	$("button.globalEditRelation").each(function() {
		var jqButton = $(this);
		if (jqButton.attr("ab_setup_complete") == "true") {
			return;
		}
		jqButton.attr("ab_setup_complete", "true");
		this.field = jqButton.siblings("input.relation_hidden")[0];
		jqButton.click(fUpdateGlobalRelation);
	});

	function bindAutoComplete(jqElement, internalTableName, internalFieldName) {
		jqElement.autocomplete("AppController.servlet", {
			autoFill : false,
			cacheLength : 1, // 100
			max : 50,
			minChars : 2,
			extraParams : {
				gtpb_return : "gui/resources/input/return_relation_values",
				set_custom_field : true,
				fieldkey : "relationField",
				custominternaltablename : internalTableName,
				custominternalfieldname : internalFieldName
			},
			mustMatch : true,
			selectFirst : true,
			width : 296,
			formatItem : function(rawValue, i, optionsDataLength) {
				var formattedValue = rawValue[0].replace("{", " <span class='secondary'>");
				formattedValue = formattedValue.replace("}", "</span>");
				return formattedValue;
			}
		});
		jqElement.result(function(event, data, formatted) {
			if (data) {
				jqElement[0].formEl.doUpdate(data[1]);
			} else {
				alert('Error saving data');
			}
		});
	}

	$('.autocomplete').each(function(i) {
		var jqThis = $(this);
		if (jqThis.hasClass("ab_setup_complete")) {
			return;
		}
		jqThis.addClass("ab_setup_complete");
		this.update = function(sValue) {
			this.value = sValue;
		};
		this.formEl = jqThis.siblings("input.relation_hidden")[0]; // the hidden
		// field will be
		// just before
		// this
		// TODO: test newer IE, the code may work now
		if ($.browser.msie) {
			// $('<span
			// style="padding-right:10px">'+this.value+'</span>').insertBefore($(this));
			// $(this).remove();
			return;
		} else {
			var internalTableName = jqThis.attr('internalTableName');
			var internalFieldName = jqThis.attr('internalFieldName');
			bindAutoComplete(jqThis, internalTableName, internalFieldName);
		}
	});

	function relationNewRecord(oHidden) {
		var jqHidden = $(oHidden);
		// Identify the related table and identifying field for this relation
		var relatedInternalTableName = jqHidden.attr("gtpb_rowidinternaltablename");
		var displayFieldInternalName = jqHidden.attr("displayFieldInternalName");
		var displayFieldName = jqHidden.attr("displayFieldName");
		var newValue = prompt("Please enter a new " + displayFieldName);
		var postData = {
			"return" : "gui/resources/input/xmlreturn_record_info",
			set_table : relatedInternalTableName,
			save_new_record : true
		};
		postData[displayFieldInternalName] = newValue;
		$.post("AppController.servlet", postData, function(data) {
			var newRowId = $(data).find("rowId").text();
			jqHidden.val(newRowId);
			jqHidden.attr("gtpb_set_row_id", newRowId);
			jqHidden.next("input").val(newValue);
			jqHidden.next("input").addClass("new_relation_value"); // bit of a hack -
																															// see
																															// jquery.autocomplete.js
			// relationChangeActions = any additional actions after the save
			if (typeof relationChangeActions == 'function') {
				new fChange(jqHidden[0], relationChangeActions);
			} else {
				new fChange(jqHidden[0], null);
			}
		});
	}

}

function appendWarningAction() {
	var jqTableBody = $("#reportData > tbody");
	if (jqTableBody.length > 0) {
		// if message not already there
		if (jqTableBody.find(".warningmessage").length == 0) {
			jqTableBody.append(warningRowHtmlSaved);
		}
	} else {
		// TODO: possibility of infinite heap building up if reportData > tbody is
		// never there, deal with this
		setTimeout('appendWarningAction', 1000);
	}
}

/*
 * Called if the current record isn't visible in pane 2, to add a warning to
 * that effect in the edit tab
 */
function appendWarning(warningRowHtml) {
	warningRowHtmlSaved = warningRowHtml;
	setTimeout('appendWarningAction()', 1000);
}

var warningRowHtmlSaved = '';

function uploadFile(fileInputElement) {
	// https://developer.mozilla.org/en/DOM/File
	var fileObject = fileInputElement.files[0];
	if (fileObject) {
		var fileSize = fileObject.size; // the new way
		if (!fileSize) {
			fileSize = fileObject.fileSize; // the deprecated way
		}
		if (fileSize) {
			var jqFileInput = $(fileInputElement);
			var jqProgressContainer = jqFileInput.next();
			var jqUploadInfo = jqProgressContainer.find(".upload_info");
			var fileSizeInfo = parseInt(fileSize / 1000000);
			if (fileSizeInfo == 0) {
				fileSizeInfo = '<img src="resources/upload_ajax-loader.gif" /> Uploading &frac12; MB';
			} else {
				fileSizeInfo = '<img src="resources/upload_ajax-loader.gif" /> Uploading '
						+ fileSizeInfo + " MB";
			}
			// upload speed is in bytes per sec.
			var uploadSpeed = parseInt(jqUploadInfo.attr("upload_speed"));
			var expectedMinutes = parseInt(fileSize / (uploadSpeed * 60));
			if (expectedMinutes == 0) {
				fileSizeInfo = fileSizeInfo + ". Please wait a few seconds...";
			} else if (expectedMinutes > 6) {
				// Round the expected time
				expectedMinutes = Math.round(expectedMinutes / 10) * 10;
				fileSizeInfo = fileSizeInfo + ". This is likely to take about "
						+ expectedMinutes + " minutes";
			} else if (expectedMinutes > 3) {
				fileSizeInfo = fileSizeInfo + ". This will take five minutes or so";
			} else {
				fileSizeInfo = fileSizeInfo + ". This should take about "
						+ expectedMinutes + " minutes";
			}
			jqUploadInfo.html(fileSizeInfo);
		}
	} else {
		fileInputElement.form.submit();
	}
}

/*
 * Adds progress bar to file uploads
 * https://github.com/jurisgalang/jquery-sexypost#readme
 */
function fSexyUpload() {
	$("form.fileUploader").each(function() {
		var jqForm = $(this);
		if (jqForm.hasClass("uploadEventRegistered")) {
			return;
		}
		var jqProgressBar = jqForm.find(".upload_progress_bar");
		var jqUploadInfo = jqForm.find(".upload_info");
		jqForm.sexyPost({
			start : function(event) {
				jqProgressBar.parent().show();
				jqProgressBar.html(jqUploadInfo.html());
				jqProgressBar.removeClass("upload_complete");
				jqProgressBar.css("width", "0%");
			},
			progress : function(event, completed, loaded, total) {
				jqProgressBar.css("width", (completed * 100).toFixed(1) + "%");
			},
			complete : function(event, responseText) {
				jqProgressBar.text("Upload complete");
				jqProgressBar.addClass("upload_complete");
				jqProgressBar.css("width", "100%");
				var returnTemplate = jqForm.find("input[name=return]").val();
				document.location = "?return=" + returnTemplate + "&cachebust=" + (new Date()).getTime();
			},
			error : function(event) {
				jqProgressBar.text("Error, file upload incomplete");
				jqUploadInfo.text("Error, file upload incomplete");
			}/*
				 * , abort: function(event) { jqProgressBar.text("Upload aborted");
				 * jqUploadInfo.text("Upload aborted"); }
				 */
		});
		jqForm.addClass("uploadEventRegistered");
	});
}

/* for date fields */
function fKeyUpEvent(inputElement, e) {
	// update the relevant value in the wrapper
	var jqWrapper = $(inputElement).closest("div");
	var sAttribute = $(inputElement).attr('wrapperAttribute');
	jqWrapper.attr(sAttribute, $(inputElement).val());
	fSetValueAtt(jqWrapper[0]);
	var globalEdit = false;
	// Note: why doesn't var globalEdit = (jqWrapper.attr("gtpb_global_edit") !==
	// "undefined") work?
	// A: see
	// http://stackoverflow.com/questions/1318076/jquery-hasattr-checking-to-see-if-there-is-an-attribute-on-an-element
	// A2: should be != rather than !==
	if (jqWrapper.attr("gtpb_global_edit")) {
		globalEdit = true;
	}
	;
	var updateAsType = false;
	if (jqWrapper.attr("update_as_type")) {
		updateAsType = true;
	}
	;
	if (!globalEdit && updateAsType) {
		top.oBuffer.writeBuffer(jqWrapper[0], e);
	}
}

function fChangeEvent(inputElement) {
	// update the relevant value in the wrapper
	var jqWrapper = $(inputElement).closest("div");
	var sAttribute = $(inputElement).attr('wrapperAttribute');
	jqWrapper.attr(sAttribute, $(inputElement).val());
	if (navigator.userAgent.match(/Konqueror/i)) {
		jqWrapper[0].setAttribute(sAttribute, $(inputElement).val());
	}
	fSetValueAtt(jqWrapper[0]);
	var globalEdit = false;
	if (jqWrapper.attr("gtpb_global_edit")) {
		globalEdit = true;
	}
	;
	var updateAsType = false;
	if (jqWrapper.attr("update_as_type")) {
		updateAsType = true;
	}
	;
	if (!globalEdit && updateAsType) {
		new fChange(jqWrapper[0]);
	}
}

function fUpdateGlobalDate(oButton) {
	function fResponse(sResponseText, sResponseXML) {
		if (sResponseXML.getElementsByTagName('rowsTotal')[0]) {
			var sRowsToChange = sResponseXML.getElementsByTagName('rowsTotal')[0].firstChild.nodeValue;
			var sFieldValue = oField.getAttribute('e_value');
			if (confirm('Are you sure that you want to change the value to '
					+ sFieldValue + '?\nThis will update ' + sRowsToChange + ' records')) {
				new fChange(oField);
			}
		}
	}
	var field = $(oButton).closest('div')[0];
	var oField = field;
	var aPostVars = new Array();
	aPostVars['returntype'] = 'xml';
	aPostVars['return'] = 'gui/resources/sessionReportInfo';
	var oReq = new fRequest('AppController.servlet', aPostVars, fResponse, 0);
}

function fSetValueAtt(oWrapperDiv) {
	function fLPad(sString, iLength) {
		if (sString.length >= iLength)
			return sString;
		while (sString.length < iLength)
			sString = '0' + sString;
		return sString;
	}
	var jqWrapperDiv = $(oWrapperDiv);
	// Java Calendar constants
	var constMonth = 2;
	var constDayOfMonth = 5;
	var constHourOfDay = 11;
	var constMinute = 12;
	var constSecond = 13;
	var dateResolution = parseInt(jqWrapperDiv.attr("date_resolution"));
	var internalFieldName = jqWrapperDiv.attr('name');
	var aMonths = new Array("January", "February", "March", "April", "May",
			"June", "July", "August", "September", "October", "November", "December");
	with (oWrapperDiv) {
		var sValue = '';
		if (dateResolution >= constDayOfMonth) {
			if (getAttribute('gtpb_' + internalFieldName + '_days') == 0)
				return;
			sValue += fLPad(getAttribute('gtpb_' + internalFieldName + '_days'), 2)
					+ ' ';
		}
		if (dateResolution >= constMonth) {
			if (getAttribute('gtpb_' + internalFieldName + '_months') == 0)
				return;
			sValue += aMonths[getAttribute('gtpb_' + internalFieldName + '_months') - 1]
					.substr(0, 3)
					+ ' ';
		}
		if (isNaN(getAttribute('gtpb_' + internalFieldName + '_years')))
			return;
		var yearValue = getAttribute('gtpb_' + internalFieldName + '_years');
		if (yearValue.length == 1) {
			yearValue = "200" + yearValue;
		} else if (yearValue.length == 2) {
			yearValue = "20" + yearValue;
		} else {
			yearValue = fLPad(yearValue, 4);
		}
		sValue += yearValue;
		if (dateResolution >= constHourOfDay) {
			if (isNaN(getAttribute('gtpb_' + internalFieldName + '_hours')))
				return;
			sValue += ' '
					+ fLPad(getAttribute('gtpb_' + internalFieldName + '_hours'), 2);
		}
		if (dateResolution >= constMinute) {
			if (isNaN(getAttribute('gtpb_' + internalFieldName + '_minutes')))
				return;
			sValue += ':'
					+ fLPad(getAttribute('gtpb_' + internalFieldName + '_minutes'), 2);
		}
		if (dateResolution >= constSecond) {
			if (isNaN(getAttribute('gtpb_' + internalFieldName + '_seconds')))
				return;
			sValue += ':'
					+ fLPad(getAttribute('gtpb_' + internalFieldName + '_seconds'), 2);
		}
		setAttribute('e_value', sValue);
	} // end with oWrapperDiv
}

function fDatePickers() {
	$('.dp-choose-date').each(
			function() {
				if (this.tagName == "A") {
					// The links to launch the picker also have the dp-choose-date class
					return;
				}
				var jqDateSelector = $(this);
				if (jqDateSelector.hasClass("tabActionRegistered")) {
					return;
				}
				jqDateSelector.addClass("tabActionRegistered");
				var internalFieldName = jqDateSelector.attr("id").replace(
						"date_picker_", "");
				var year = $(
						'input[wrapperAttribute="gtpb_' + internalFieldName + '_years'
								+ '"]').val();
				var month = $(
						'select[wrapperAttribute="gtpb_' + internalFieldName + '_months'
								+ '"]').val();
				var day = $(
						'select[wrapperAttribute="gtpb_' + internalFieldName + '_days'
								+ '"]').val();
				var currentDate = new Date(year, month - 1, day);
				var inlineAttr = jqDateSelector.attr('inline');
				if (inlineAttr == "true") {
					inlineAttr = true;
				} else {
					inlineAttr = false;
				}
				jqDateSelector.datePicker({
					startDate : '01/01/1901',
					inline : inlineAttr
				}).bind(
						'dateSelected',
						function(e, selectedDate, $td, status) {
							var day = selectedDate.getDate();
							var month = selectedDate.getMonth();
							var year = selectedDate.getFullYear();
							var jqDay = $('select[wrapperAttribute="gtpb_'
									+ internalFieldName + '_days' + '"]');
							jqDay.val(day);
							jqDay.change();
							var jqMonth = $('select[wrapperAttribute="gtpb_'
									+ internalFieldName + '_months' + '"]');
							jqMonth.val(month + 1);
							jqMonth.change();
							var jqYear = $('input[wrapperAttribute="gtpb_'
									+ internalFieldName + '_years' + '"]');
							jqYear.val(year);
							jqYear.keyup();
						});
				if (month != '') {
					// Setting current date doesn't seem to work
					// jqDateSelector.dpSetSelected(currentDate.toString()).change();
					jqDateSelector.dpSetDisplayedMonth(month - 1, year);
				}
			});
}

function fAssignButtonTableActions() {
	$('button.tableaction').click(
		function() {
			var actionName = $(this).attr('actionname');
			if (self == top) { // if mobile un-framed version
				document.location = "?return=gui/mobile/module_action&set_custom_string=true&key=actionname&value="
						+ actionName;
			} else {
				var actionTemplate = $(this).attr('actiontemplate');
				var actionButtons = $(this).attr('actionbuttons');
				var callbackFunction = $(this).attr('callbackfunction');
				top.fShowModalDialog(actionTemplate, actionName,
						callbackFunction, actionButtons, 'width=800px; height=600px');
			}
			return false;
		});
}

/** http://tweet.seaofclouds.com/ */
function fTwitter() {
	// Fetch 20 tweets, but filter out @replies, and display only 1:
	$(".twitter").each(function() {
		if ($(this).hasClass("tweetRegistered")) {
			return;
		} else {
			$(this).addClass("tweetRegistered");
		}
		var username = $(this).attr("username");
		$(this).tweet({
			avatar_size : 32,
			count : 1,
			fetch : 20,
			filter : function(t) {
				return !/^@\w+/.test(t["tweet_raw_text"]);
			},
			username : username
		}).bind("loaded",function(){$(this).find("a").attr("target","_blank");});
	});
}

/*
 * Management tabs functions
 */
function fShowTableUsage() {
	// If no usage log table exists yet, get from server
	if ($("#table_usage_loader table").size() == 0) {
		$
				.get(
						"AppController.servlet",
						{
							"return" : "gui/administration/tables/option_sets/table_usage_data_loader"
						}, function(returned_content) {
							$("#table_usage_loader").html(returned_content);
							$("#table_usage_loader").removeClass("load_spinner");
						});
	}
}

function fShowReportUsage() {
	// If no usage log table exists yet, get from server
	if ($("#report_usage_loader table").size() == 0) {
		$
				.get(
						"AppController.servlet",
						{
							"return" : "gui/administration/reports/option_sets/report_usage_data_loader"
						}, function(returned_content) {
							$("#report_usage_loader").html(returned_content);
							$("#report_usage_loader").removeClass("load_spinner");
						});
	}
}

/* Calculation editor */
function fEnableCalcSyntaxHighlight() {
	if ($("#calculationdefn").length > 0) {
		editAreaLoader.init({
			id : "calculationdefn" // textarea id
			,
			syntax : "sql" // syntax to be used for highlighting
			,
			start_highlight : true // to display with highlight mode on start-up
			,
			allow_toggle : false,
			browsers : "all",
			toolbar : "",
			replace_tab_by_spaces : 2,
			EA_load_callback : "fEALoaded"
		});
	}
}

function fEALoaded() {
	$('#frame_calculationdefn').contents().find('.area_toolbar').hide();
	fWebkitGlitch();
}

function showEditCalc(internalFieldName) {
	$(".calculation_form").slideUp();
	$("#editcalc_" + internalFieldName).slideDown();
}

function addComment(jqCommentInput) {
	var internalFieldName = jqCommentInput.attr("internalfieldname");
	var internalTableName = jqCommentInput.attr("internaltablename");
	var rowId = jqCommentInput.attr("rowid");
	var text = jqCommentInput.val();
	jqCommentInput.attr("disabled", "true");
	jqCommentInput.next("input[type=button]").attr("disabed", "true");
	if (text.length > 0) {
		$.post("AppController.servlet", {
			"return" : "gui/resources/input/comments",
			set_custom_field : true,
			fieldkey : "comment",
			custominternaltablename : internalTableName,
			custominternalfieldname : internalFieldName,
			set_custom_integer: true,
			integerkey: "comment_rowid",
			customintegervalue: rowId,
			add_comment : true,
			internalfieldname : internalFieldName,
			internaltablename: internalTableName,
			rowid: rowId,
			comment : text
		}, function(data) {
			$("#comments_" + internalFieldName).html(data);
			jqCommentInput.val("");
			jqCommentInput.removeAttr("disabled");
			jqCommentInput.next("input[type=button]").removeAttr("disabed");
			$(".add_comment_row").hide();
		});
	}
}

function fComments() {
	$("input.comment_input").each(function() {
		var jqInput = $(this);
		if (!jqInput.hasClass("keypressRegistered")) {
			jqInput.keypress(function(event) {
				if (event.which == 13) {
					addComment(jqInput);
				}
			});
			jqInput.next("input[type=button]").click(function() {
				addComment(jqInput);
			});
			jqInput.addClass("keypressRegistered");
		}
	})
	$(".comment_toggle").click(function() {
		// Two col. method
		var jqCommentRow = $(this).nextUntil(".add_comment_row").next().last();
		if (jqCommentRow.length == 0) {
			// For comments in one col. layout
			jqCommentRow = $(this).next(".add_comment_row");
		}
		jqCommentRow.show("normal");
		jqCommentRow.find(".comment_input").focus();
	});
}

/*
 * From interdependent_fields.js
 */
function fArrayContains(aArray, vTest) {
	for ( var i = 0; i < aArray.length; i++)
		if (aArray[i] == vTest)
			return true;
	return false;
}

function fSelectChange(oEvent) {
	var oParent = oEvent.target;
	var oChange = new fDoSelectChange(oParent);
}

function fDoSelectChange(oParent) {
	function fRepopulateList(oParent, oDependent) {
		function fReqComplete(sResponseText, sResponseXML) {
			function fCreateOptions() {
				// clear down the list
				while (oDependent.firstChild)
					oDependent.removeChild(oDependent.firstChild);
				aOptions = sResponseXML.getElementsByTagName('request')[0]
						.getElementsByTagName('option');
				for ( var iOptions = 0; iOptions < aOptions.length; iOptions++) {
					sDisplay = aOptions[iOptions].getElementsByTagName('display_value')[0].firstChild.nodeValue;
					sValue = aOptions[iOptions].getElementsByTagName('internal_value')[0].firstChild.nodeValue;
					oOption = new Option(sDisplay);
					oOption.value = sValue;
					oDependent.options.add(oOption);
				}
			}

			// if the value of the parent has changed, quit
			if (sValue != oParent.value)
				return;

			fCreateOptions();
			var oChange = new fDoSelectChange(oDependent); // update any children
			oDependent.removeAttribute('disabled');
		}

		function fSetPostVars() {
			// create a key value array of the variables to post with the request to
			// the server
			var aPostVars = new Array();
			if (oDependent.getAttribute('return')) {
				aPostVars['return'] = 'gui/resources/input/'
						+ oDependent.getAttribute('return');
				aPostVars['key'] = 'parent' + oDependent.getAttribute('parentType');
			} else {
				switch (oParent.name.replace(/.*internal/ig, '')) {
				case 'reportname':
					aPostVars['return'] = 'gui/resources/input/xmlreturn_all_reportfields';
					aPostVars['key'] = 'parentreport';
					break;
				case 'tablename':
					aPostVars['return'] = 'gui/resources/input/xmlreturn_tablefields';
					aPostVars['key'] = 'parenttable';
					break;
				}
			}

			aPostVars['returntype'] = 'xml';
			aPostVars['set_custom_string'] = 'true';
			aPostVars['value'] = sValue;

			return aPostVars;
		}

		oDependent.setAttribute('disabled', 'true');
		var sValue = oParent.value;
		var oReq = new fRequest('AppController.servlet', fSetPostVars(),
				fReqComplete, -1);
	}

	if (!oParent.dependents) {
		return;
	}
	for ( var iDependent = 0; iDependent < oParent.dependents.length; iDependent++) {
		fRepopulateList(oParent, oParent.dependents[iDependent]);
	}
}

function fInitialiseDependencies() {
	function fRegisterDependent(oChild, oParent) {
		if (!oParent.dependents)
			oParent.dependents = new Array;
		else // no need to do this if the dependents list is being created for the
		// first time
		if (fArrayContains(oParent.dependents, oChild))
			return false; // the child is already stored as a dependent
		oParent.dependents.push(oChild);
		// add a listener. Duplicate listeners are discarded so this will only be
		// triggered once if it's added multiple times
		$(oParent).change(fSelectChange);
		return true;
	}

	var aInitialised = new Array();
	var lastUsedParentNameIndexes = new Object();
	// var aSelect = document.getElementsByTagName('SELECT');
	var aSelect = $.makeArray($('.interdependent select'));
	// for every select
	for ( var iSelect = 0; iSelect < aSelect.length; iSelect++) {
		var selectName = aSelect[iSelect].name;
		// see whether the child has been registered already, continue if it has
		if (aSelect[iSelect].getAttribute('registered') == 'true') {
			continue;
		}
		// find its parents' name. If none, continue to next select element
		var sParent = aSelect[iSelect].getAttribute('parent');
		var sParentId = aSelect[iSelect].getAttribute('parentid');
		if (!sParent) {
			continue;
		}
		var oForm = aSelect[iSelect].form;

		// only allow dependencies within the same form

		// var oParent=oForm.elements[sParent]; // if there is more than one element
		// with this name a collection will be returned
		var oParent = $(oForm).find("#" + sParentId)[0];
		if (!oParent.form) { // if the parent doesn't have an associated form, it's
			// not a form element
			if (aSelect[iSelect].getAttribute('uselastordinal')) {
				oParent = oParent[oParent.length - 1]; // if option set to use last
				// ordinal element, use the last
				// element in the array
			} else {
				continue;
			}
		}

		if (fRegisterDependent(aSelect[iSelect], oParent)) {
			aSelect[iSelect].setAttribute('registered', 'true');
		}
		var oChange = new fDoSelectChange(oParent);
	}
}

/**
 * Used when clicking on a selector report in a tab
 */
function loadIntoTabTable(oRow, internalTableName, rowId) {
	var jqSelector = $(oRow).closest(".selectorReport");
	if (jqSelector.closest(".block").hasClass("grandchildren")) {
		var activeTab = $(".tab_choice.active");
		var tabTableInternalName = activeTab.attr("data-internaltablename");
		var tabReportInternalName = activeTab.attr("data-internalreportname");
		var tabRowId = $("#rowid_" + tabTableInternalName).attr("data-rowid");
		parent.pane_2.document.location = "AppController.servlet?return=gui/reports_and_tables/report_data&set_table="
				+ tabTableInternalName
				+ "&set_report="
				+ tabReportInternalName
				+ "&set_row_id=" + rowId + "&rowidinternaltablename=" + internalTableName
				+ "&preset_row_id=" + tabRowId + "&preset_rowidinternaltablename=" + tabTableInternalName
				+ "&set_custom_table=true&tablekey=tabTable&custominternaltablename=" + internalTableName
				+ "&set_custom_boolean=true&booleankey=overrideTableForm&custombooleanvalue=true"
				+ "&cachebust=" + (new Date()).getTime();
	} else {
		var targetDiv = jqSelector.next("div");
		targetDiv.fadeOut();
		targetDiv.load("AppController.servlet", {
			set_row_id : rowId,
			rowidinternaltablename : internalTableName,
			"return" : "gui/reports_and_tables/tabs/tab_content_table",
			set_custom_table : true,
			tablekey : "tabTable",
			custominternaltablename : internalTableName
		}, function() {
			jqSelector.find("tr#currentRow").removeAttr("id");
			$(oRow).attr("id", "currentRow");
			targetDiv.fadeIn();
			editTabFunctions();
		});
	}
}

function deleteTabRecord(oElement, deleteRelatedData) {
	var jqElement = $(oElement);
	var internalTableName;
	var rowId;
	if (jqElement.closest("#tab_deleter").size() > 0) {
		var jqTab = jqElement.closest(".form_tabber").find(".tab_choice.active");
		if (jqTab.size() == 0) {
			alert("Coding error: active tab not found");
			return;
		}
		internalTableName = jqTab.attr("data-internaltablename");
		rowId = $("#rowid_" + internalTableName).attr("data-rowid");
	} else {
		rowId = jqElement.closest("tr").attr("name");
		internalTableName = jqElement.closest(".selectorReport").attr(
				"data-internaltablename");
	}
	var options = {
		remove_record : true,
		internaltablename : internalTableName,
		rowid : rowId,
		"return" : "gui/administration/xmlreturn_fieldchange",
		returntype : "xml"
	}
	if (deleteRelatedData) {
		options["cascadedelete"] = true;
	}
	$.post("AppController.servlet", options, function(sResponseXML) {
		var sResponse = sResponseXML.getElementsByTagName('response')[0].firstChild.nodeValue;
		if (sResponse == 'ok') {
			var parentRowId = $(".form_tabber").attr("data-rowid");
			pane3TabInterface.refresh(parentRowId);
		} else {
			sException = sResponseXML.getElementsByTagName('exception')[0]
					.getAttribute('type');
			var sExceptionMessage = sResponseXML
					.getElementsByTagName('exception')[0].firstChild.nodeValue;
			if (deleteRelatedData) {
				alert('Record was not deleted because it is linked to data in other tables that you do not have permission to change.\n\n'
						+ sExceptionMessage);
			} else if (confirm('Record was not deleted because it is linked to data in other tables.\n'
					+ 'DELETE ALL THIS?\n\n'
					+ sExceptionMessage
					+ '\n\nWould you like to delete this row and all related data or CANCEL this operation?')) {
				// recurse, but set it to delete related data
				deleteTabRecord(oElement, true);
			}
		}
	}, "xml");
}

/**
 * Mark tabs which have no content in them Use the server to look up whether
 * there's content or not
 */
function fMarkInactiveTabs() {
	if ($(".form_tabber").size() == 0) {
		return;
	}
	$.getJSON(
			"AppController.servlet?return=gui/reports_and_tables/tabs/tabs_inactive",
			function(data) {
				for (i = 0; i < data.length; i++) {
					var jqTab = $("#tab_choice_" + data[i]);
					jqTab.addClass("no_records");
					if (jqTab.hasClass("active")) {
						// Hide the deleter if the active tab has no record(s)
						$("#tab_deleter").fadeOut();
					}
				}
			});
}

/*
 * Summary tab functions
 */

/* Charts in pane 3 need some behaviours added */
function fSetupCharts() {
	$('.summary_chart')
			.each(
					function(i) {
						var summaryDivName = $(this).attr('id');
						var summaryId = summaryDivName.replace('chart_', '');
						$(this)
								.append(
										"<div class='chart_remover'><a href='?return=gui/reports_and_tables/pane3&remove_chart=true&summaryid="
												+ summaryId
												+ "'><img border='0' src='resources/icons/edit.png' /></a></div>");
						$(this).hover(function() {
							$(this).find('.chart_remover').fadeIn("normal");
						}, function() {
							$(this).find('.chart_remover').fadeOut("normal");
						});
					});
}

/**
 * When you $.show() a pane 3 element, the toolbar vanishes in webkit Have to
 * reset the oViewPane height property to get it back
 */
function fWebkitGlitch() {
	if ($.browser.webkit) {
		setTimeout(
				"$(top.document.getElementById('oViewPane')).css('height', '0')", 1000);
		setTimeout(
				"$(top.document.getElementById('oViewPane')).css('height', '100%')",
				1500);
	}
}

function fShowAddGroup() {
	$("#addGroup").show();
	$("#addGroupLink").hide();
	fWebkitGlitch();
}

function fShowAddAgg() {
	$("#addAgg").show();
	$("#addAggLink").hide();
	fWebkitGlitch();
}

function fSubmitGroup() {
	if ($("#groupselect").val() != "") {
		document.addgrouping.submit();
	}
}

function fSubmitWordCloud() {
	document.wordcloud.submit();
}

function fSubmitFunction() {
	var aggregateFunction = jQuery("#aggregatefunction").val();
	var aggregateFunctionField = jQuery("#aggregatefunctionfield").val();
	if (aggregateFunction != "" && aggregateFunctionField != "") {
		document.addaggregate.submit();
	}
}

/**
 * Select a form style and set up tabs
 */
function fFormStyle() {
	$(".select_layout").click(function() {
		var clicked = $(this);
		$(".select_layout").removeClass("selected_layout");
		var formStyle = $(this).attr("id");
		$.post("AppController.servlet", {
			"return" : "blank",
			"update_table" : true,
			"formstyle" : formStyle
		}, function() {
			clicked.addClass("selected_layout");
		});
	});
	$(".tab_choice").each(function() {
		var jqTab = $(this);
		if (jqTab.hasClass("tabActionRegistered")) {
			return;
		}
		jqTab.addClass("tabActionRegistered");
		var parentInternalTableName = jqTab.closest(".form_tabber").attr(
				"data-internaltablename");
		// Set parent row ID for use if we're using a form table
		var parentRowId = jqTab.closest(".form_tabber").attr("data-rowid");
		jqTab.click(function() {
			jqTab.addClass("tabLoading");
			var tabInternalTableName = $(this).attr("data-internaltablename");
			$(".tab_choice").removeClass("active");
			var tabContainer = $("#form_tabs_" + parentInternalTableName + "_"
					+ tabInternalTableName);
			$("#tab_deleter").fadeOut();
			if (tabContainer.children().size() == 0) {
				$(".tab_container").fadeOut();
				tabContainer.addClass("load-spinner").css("position", "relative");
				tabContainer.fadeIn();
				tabContainer.load("AppController.servlet", {
					"return" : "gui/reports_and_tables/tabs/tab_content",
					set_custom_table : true,
					tablekey : "tabTable",
					custominternaltablename : tabInternalTableName,
					set_row_id: parentRowId,
					rowidinternaltablename: parentInternalTableName
				}, function() {
					tabContainer.removeClass("load-spinner");
					jqTab.addClass("active");
					jqTab.removeClass("tabLoading");
					editTabFunctions();
					// If only one child record, show delete button in tabs bar
					if ((tabContainer.find(".selectorReport").size() == 0)
							&& jqTab.attr("data-singular") && (!jqTab.hasClass("no_records"))) {
						var singularName = jqTab.attr("data-singular");
						$("#tab_deleter").find("img").attr("title",
								"delete this " + singularName);
						$("#tab_deleter").fadeIn();
					}
				});
			} else {
				$(".tab_container").hide();
				tabContainer.show();
				jqTab.addClass("active");
				// Fire off a post to set the session tab - don't need any results
				$.post("AppController.servlet", {
					"return" : "blank",
					set_custom_table : true,
					tablekey : "tabTable",
					custominternaltablename : tabInternalTableName
				});
				// If only one child record, show delete button in tabs bar
				if ((tabContainer.find(".selectorReport").size() == 0)
						&& jqTab.attr("data-singular")) {
					var singularName = jqTab.attr("data=singular");
					$("#deleter_text").text(singularName);
					$("#tab_deleter").fadeIn();
				}
			}
		}); // end of jqTab.click()
		jqTab.find("img.new").click(
				function() {
					var tabInternalTableName = jqTab.attr("data-internaltablename");
					var relationFieldInternalName = jqTab.attr("data-relationfield");
					var parentRowId = jqTab.closest(".form_tabber")
							.attr("data-rowid");
					var tabContainer = $("#form_tabs_" + parentInternalTableName
							+ "_" + tabInternalTableName);
					tabContainer.fadeOut();
					var options = {
						"return" : "gui/reports_and_tables/tabs/tab_content",
						save_new_record : true,
						internaltablename : tabInternalTableName,
						set_custom_table : true,
						tablekey : 'tabTable',
						custominternaltablename : tabInternalTableName
					}
					// Set relationFieldInternalName like this as it is a variable
					// property name
					options[relationFieldInternalName] = parentRowId;
					tabContainer.load("AppController.servlet", options, function() {
						tabContainer.fadeIn();
						editTabFunctions();
					});
				});
		// Initialise to session tab on load (if not already the active tab)
		if (jqTab.hasClass("session_tab") && (!jqTab.hasClass("active")) && (!jqTab.hasClass("tabLoading"))) {
			jqTab.click();
		}
	}); // end of .tab_choice.each
}

function showAdvanced() {
	$("tr.advanced").show("normal");
	$("tr#showAdvanced").hide("normal");
}

function fMap() {
	if ($("#map_canvas").is(":visible")) {
		var myOptions = {
			zoom : 6,
			center : new google.maps.LatLng(54, 2),
			mapTypeId : google.maps.MapTypeId.ROADMAP,
			panControl : false,
			scrollwheel : false,
			mapTypeControl : false
		};
		var markers = [];
		var colourValues = [];
		var colourIcons = [];
		var map = new google.maps.Map($("#map_canvas")[0], myOptions);
		var mapBounds = new google.maps.LatLngBounds();
		var popup = new google.maps.InfoWindow({
			content : "holding..."
		});
		var mapJSON = $
				.getJSON(
						"AppController.servlet?return=gui/reports_and_tables/tabs/map_json",
						function(data) {
							var len = data.length;
							for ( var i = 0; i < len; i++) {
								var row = data[i];
								var latLng = new google.maps.LatLng(row.latitude, row.longitude);
								var colour = "FFFFFF";
								var firstLetter = ".";
								var pinStyle = "d_map_pin_letter";
								var pinStyle2 = "";
								var pinStarFill = "";
								if (row.colourValue) {
									colour = hsl2hex(row.h, row.s, row.l);
									colourValues[row.colourValue] = row.colourValue;
									firstLetter = row.colourValue.charAt(0);
									if ((firstLetter >= '0') && (firstLetter <= '9')) {
										pinStyle = "d_map_xpin_letter";
										pinStyle2 = "pin_star|";
										pinStarFill = "|FFFF00";
									}
									colourIcons[row.colourValue] = "https://chart.googleapis.com/chart?chst="
											+ pinStyle
											+ "&chld="
											+ pinStyle2
											+ firstLetter
											+ "|"
											+ colour + "|000000" + pinStarFill;
								}
								// mapBounds.extend(latLng);
								markers[i] = new google.maps.Marker({
									position : latLng,
									map : map,
									title : row.postcode,
									icon : "https://chart.googleapis.com/chart?chst=" + pinStyle
											+ "&chld=" + pinStyle2 + firstLetter + "|" + colour
											+ "|000000" + pinStarFill,
									html : row.title
								/* our own property */
								});
								google.maps.event.addListener(markers[i], 'click', function() {
									popup.setContent(this.html);
									popup.open(map, this);
								});
							}
							for ( var colourValue in colourValues) {
								$("#map_legend").append(
										"<div><img src='" + colourIcons[colourValue] + "' />&nbsp;"
												+ colourValue + "</div>");
							}
						});
	}
}

function hsl2hex(h, s, l) {
	var m1, m2, hue;
	var r, g, b
	s /= 100;
	l /= 100;
	if (s == 0)
		r = g = b = (l * 255);
	else {
		if (l <= 0.5)
			m2 = l * (s + 1);
		else
			m2 = l + s - l * s;
		m1 = l * 2 - m2;
		hue = h / 360;
		r = HueToRgb(m1, m2, hue + 1 / 3);
		g = HueToRgb(m1, m2, hue);
		b = HueToRgb(m1, m2, hue - 1 / 3);
	}
	var r16 = r.toString(16).substr(0, 2).replace(".", "");
	var g16 = g.toString(16).substr(0, 2).replace(".", "");
	var b16 = b.toString(16).substr(0, 2).replace(".", "");
	return (r16.length == 2 ? r16 : '0' + r16)
			+ (g16.length == 2 ? g16 : '0' + g16)
			+ (b16.length == 2 ? b16 : '0' + b16);
}

function HueToRgb(m1, m2, hue) {
	var v;
	if (hue < 0)
		hue += 1;
	else if (hue > 1)
		hue -= 1;
	if (6 * hue < 1)
		v = m1 + (m2 - m1) * hue * 6;
	else if (2 * hue < 1)
		v = m2;
	else if (3 * hue < 2)
		v = m1 + (m2 - m1) * (2 / 3 - hue) * 6;
	else
		v = m1;
	return 255 * v;
}

/* ---------- Add functions to the callFunctions list ---------- */
/* ------ These will be called every time a tab refreshes ------ */
pane3Scripts.functionList.push(editTabFunctions);
pane3Scripts.functionList.push(fUnlockButton);
pane3Scripts.functionList.push(fSetupCharts);
pane3Scripts.functionList.push(fAssignButtonTableActions);
pane3Scripts.functionList.push(fMarkInactiveTabs);
pane3Scripts.functionList.push(fMap);
pane3Scripts.functionList.push(fInitialiseDependencies);
pane3Scripts.functionList.push(fFormStyle);

function editTabFunctions() {
	fComboComponents();
	fRelationPickers();
	fComments();
	fDatePickers();
	fSexyUpload();
	fTwitter();
}
