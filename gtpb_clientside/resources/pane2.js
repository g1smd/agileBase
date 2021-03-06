/*
 *  Copyright 2013 GT webMarque Ltd
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
;

$(document).ready(function() {
  fSetupAppPreview();
  var hoverIntentConfig = {
       over: showTooltip,
       out: hideTooltip,
       interval: 400
  };
  $("#filterhelp").hoverIntent(hoverIntentConfig);
  $(".ab_field_title").hoverIntent(hoverIntentConfig);
  $("tr.sectioned_detail_row").mouseenter(function() {
    $("tr.sectioned_detail_heading").hide();
    $(this).prevAll("tr.sectioned_detail_heading").first().show();
  });
  // See if tbody is writable (it's not in IE)
  // TODO: see if there is a better way than user agent sniffing
  // http://stackoverflow.com/questions/16234410/detecting-whether-innerhtml-is-readonly
  var userAgent = navigator.userAgent.toLowerCase();
  if ((!userAgent.match(/msie/)) && ($("#tiles").size() == 0)) {
    alignTableHeaderCells();
    setInterval("alignTableHeaderCells()", 2000);
    $(window).scroll(function() {
      var left = document.body.scrollLeft || window.pageXOffset;
      $("thead").css("left", -1 * left);
    });
  }
  checkboxesSetup();
});

/**
 * Live checkboxes allow booleans to be updated directly from the report
 */
function checkboxesSetup() {
	$("#reportBody input[type=checkbox]").click(function(event) {
		event.stopPropagation();
		var checkbox = $(this);
		var value = checkbox.is(":checked");
		var rowId = checkbox.closest("tr").attr("name");
		var internalTableName = checkbox.closest("table").attr("data-internaltablename");
		var internalFieldName = checkbox.attr("name");
		var options = {
				"return": "gui/resources/xmlreturn_rowid",
				update_record: true,
				rowid: rowId,
				internaltablename: internalTableName
		};
		options[internalFieldName] = value;
		$.post("AppController.servlet", options, function(data) {
			if ($(data).find("response").text() == "ok") {
				if (value) {
					checkbox.closest("td").addClass("colored").css("background-color","#8DC63F");
				} else {
					checkbox.closest("td").removeClass("colored").css("background-color","white");
				}
			} else {
				var error = $(data).find("exception").text();
				alert("Error saving: " + error);
			}
		});
	});
}

//clear all the selected rows
//there should only be one but will clear all if more than this.
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

	//Oliver: Disable delete temporarily
	//return;


	function fControlCheckboxes(bDisable) {
		var aCheckedRows = new Array();

		// set the header checkbox
		var jqCheckbox = $(document.getElementById('reportData').rows[0]).find(
				"input:checkbox");
		if (jqCheckbox.length > 0) {
			if (bDisable)
				jqCheckbox.attr('disabled', 'true');
			else {
				jqCheckbox.removeAttr('disabled');
				jqCheckbox.removeAttr('checked');
			}
		}

		// set the row checkboxes
		var oRows = document.getElementById('reportBody').rows;
		for ( var i = 0; i < oRows.length; i++) {
			var jqCheckbox = $(oRows[i]).find("input:checkbox.del");
			if (jqCheckbox.length == 0) {
				continue;
			}
			if (bDisable)
				jqCheckbox.attr('disabled', 'true');
			else
				jqCheckbox.removeAttr('disabled');
			if (jqCheckbox.is(":checked")) {
				aCheckedRows.push(oRows[i]);
			}
		}

		return aCheckedRows;
	}

	function fReqComplete(sResponseText, sResponseXML) {
		function fReformatTable() {
			$('#reportBody tr:even').not('.trailing').not('.seemorerows').not('.h1')
					.not('.h2').not('.h3').removeClass('rowa').removeClass('rowb')
					.addClass('rowa');
			$('#reportBody tr:odd').not('.trailing').not('.seemorerows').not('.h1')
					.not('.h2').not('.h3').removeClass('rowa').removeClass('rowb')
					.addClass('rowb');
		}

		function fRetryDeletions() {
			var sExceptionMessage = sResponseXML.getElementsByTagName('exception')[0].firstChild.nodeValue;
			if (!confirm('Some rows were not deleted because they are linked to data in other tables.\n'
					+ 'DELETE ALL THIS?\n\n'
					+ sExceptionMessage
					+ '\n\nWould you like to delete these rows and the related data or CANCEL this operation?'))
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
			if ($("#tiles").size() > 0) {
				// Back to the view
				tileLoaded($(".tile.expanded"));
				return;
			} else {
				if (oCurrentRow == oSessionItem)
					bRemovedSessionItem = true;
				var oRowParent = oCurrentRow.parentNode;
				var iRowIndex = oRowParent.tagName == 'TABLE' ? oCurrentRow.rowIndex
						: oCurrentRow.sectionRowIndex;
				oRowParent.deleteRow(iRowIndex);
			}
		} else {
			bFailedDeletions = true;
			sException = sResponseXML.getElementsByTagName('exception')[0]
					.getAttribute('type');
			var sExceptionMessage = sResponseXML.getElementsByTagName('exception')[0].firstChild.nodeValue;
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
				alert('Some rows were not deleted because they are linked to data in other tables that you do not have permission to change.\n\n'
						+ sExceptionMessage);
		}

		// clean everything up
		// if we've removed the session set a new one as the first row
		if (bRemovedSessionItem) {
			if (document.getElementById('reportBody').rows.length > 1) {
				var onClick = document.getElementById('reportBody').rows[0]
						.getAttribute('onclick');
				try {
					eval(onClick);
				} catch (err) {
				}
			} else {
				parent.pane_3.document.location = 'AppController.servlet?return=gui/reports_and_tables/pane3';
			}
		}
		fControlCheckboxes(false); // re-enable the checkboxes
		fReformatTable();
	}

	function deleteTileRecord() {
		var aPostVars = new Array();
		// always pass these in the post request
		aPostVars['return'] = 'gui/administration/xmlreturn_fieldchange';
		aPostVars['returntype'] = 'xml';
		if (bDeleteRelatedData) {
			aPostVars['cascadedelete'] = '1';
		}
		aPostVars[sAction] = 'true';
		aPostVars[sRowIdentifier] = $("#reportData").attr("data-rowid");
		aPostVars["set_table"] = $("#reportData").attr("data-internaltablename");
		oReq = new fRequest('AppController.servlet', aPostVars, fReqComplete, 1);
		return true;
	}

	function fDeleteFirstItem() {
		var tiles = ($("#tiles").size() > 0);
		if (tiles) {
			return deleteTileRecord();
		} else {
			if (aCheckedRows.length < 1) {
				return false;
			}
			oCurrentRow = aCheckedRows.shift();
			var aPostVars = new Array();
			// always pass these in the post request
			aPostVars['return'] = 'gui/administration/xmlreturn_fieldchange';
			aPostVars['returntype'] = 'xml';
			if (bDeleteRelatedData) {
				aPostVars['cascadedelete'] = '1';
			}
			aPostVars[sAction] = 'true';
			aPostVars[sRowIdentifier] = oCurrentRow.getAttribute('name');
			oReq = new fRequest('AppController.servlet', aPostVars, fReqComplete, 1);
			return true;
		}
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
	if ($("#tiles").size() > 0) {
		fDeleteFirstItem();
	} else {
		var aCheckedRows = fControlCheckboxes(true);
		if (aCheckedRows.length > 0) {
			fDeleteFirstItem();
		}
	}
}

function showPane3IfNecessary(oEvent) {
	if (oEvent) {
		if ($(oEvent.target).attr("type") == "checkbox") {
			return;
		}
	}
	var jqButt = $(top.document).find("#pane3butt")
	if (!jqButt.hasClass("selected")) {
		jqButt.click();
		if (!jqButt.hasClass("REPORT")) {
			$(top.document).find("#pane2butt").click();
		}
		// For forms, show full screen
		// if($(parent.pane_3.document).find(".form_tabber").size() > 0) {
		// var jqPane1Butt = $(top.document).find("#pane1butt");
		// if (jqPane1Butt.hasClass("selected")) {
		// jqPane1Butt.click();
		// }
		// }
	}
}

function hidePane3() {
	var jqPane2Butt = $(top.document).find("#pane2butt");
	if (!jqPane2Butt.hasClass("selected")) {
		jqPane2Butt.click();
	}
	$(top.document).find("#pane3butt").click();
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
	// ? means non greedy, after the escaped ?, i.e. replace everything up to and
	// including the first question mark
	var params = replacedTemplateUrl.replace(/^.*?\?/, '');
	var paramsObj = $.deparam(params);
	paramsObj['abCache'] = new Date().getTime();
	var baseUrl = replacedTemplateUrl.replace(/\?.*$/, '');
	$.post(baseUrl, paramsObj,
			function(data) {
				// Refresh frame 3
				if (typeof (parent.pane_3) != "undefined") {
					// If user is loading a new report, it may have
					// different privileges to the last one.
					// If so, reload the whole of pane 3 to refresh the tab
					// list, otherwise just refresh the current tab
					if (typeof (parent.pane_3.pane3TabInterface) == "undefined") {
						// something in pane 3 but not a tabset
						// Elaine
						// console.log("1) parent.pane_3.document.location = " + url);
						parent.pane_3.document.location = url;
					} else if (document.location.href.match('set_module')
							|| document.location.href
									.match('gui/reports_and_tables/report_data')) {
						// means we must be viewing a report
						if ((numberOfTabsExpected == null)
								|| (numberOfTabsExpected == parent.pane_3.pane3TabInterface
										.getNumberOfTabs())) {
							// if pane 3 has the right number of tabs,
							// we can just refresh the one tab
							try {
								// Elaine
								// console.log("2) parent.pane_3.pane3TabInterface.refresh(" +
								// rowId + ")");
								parent.pane_3.pane3TabInterface.refresh(rowId);
							} catch (err) {
								// Fast refresh failed, falling back to
								// slow - don't worry about this
								// Elaine
								// console.log("3) parent.pane_3.document.location = " + url);
								parent.pane_3.document.location = url;
							}
						} else {
							// if it doesn't have the right number of tabs, we
							// need to refresh the whole frame to reload the tabset
							// Elaine
							// console.log("4) parent.pane_3.document.location = " + url);
							parent.pane_3.document.location = url;
						}
					} else {
						// fallback after everything else: simple
						// refresh of pane 3
						// Elaine
						// console.log("5) parent.pane_3.document.location = " + url);
						parent.pane_3.document.location = url;
					}
				}
				var internalTableName = $("table#reportData").attr(
						"data-internaltablename");
				parent.pane_1.appSelect(internalTableName, rowId, false);
			});
}

/*
 * Add in a checkbox to the header row and each record row to allow the record
 * to be deleted
 */
function fSelectAll(oCheckbox) {
	$("#reportBody").find("input:checkbox.del").each(function() {
		this.checked = oCheckbox.checked;
	});
}

var abTooltipTimeout;
function showTooltip() {
	// First, hide any other tooltips visible
	$(".ab_tooltip").hide();
	var tooltip = $(this).next(".ab_tooltip");
	var href = tooltip.attr("rel");
	tooltip.load(href, function() {
		tooltip.fadeIn("fast");
		tooltip.fadeTo("fast", 0.95);
		tooltip.find(".sparkline").sparkline('html', {
			type : 'bar'
		});
		abTooltipTimeout = setTimeout("hideTooltip()", 13000);
	});
}

function hideTooltip() {
	// Hide any visible tooltips
	$(".ab_tooltip").fadeOut("normal");
	clearTimeout(abTooltipTimeout);
}

function clearFilters() {
	var oReportBody = document.getElementById('reportBody');
	$("input[is_filter=true]").val("");
	$("input[is_filter=true]").attr("disabled", "true");
	$.post("AppController.servlet", {
		'return' : 'gui/reports_and_tables/report_data_only',
		'clear_all_report_filter_values' : true,
		'clear_custom_variable' : 'filtering_on',
		abCache : new Date().getTime()
	}, function(sResponseText) {
		$("input[is_filter=true]").removeAttr("disabled");
		fLoadReport(sResponseText, oReportBody, null);
	});
}

function launchDateFilterControls(event, inputObj) {
	$(inputObj).addClass("waitingForFilterControls");
	setTimeout(dateFilterControls, 1000, event, inputObj);
}

function dateFilterControls(event, inputObj) {
	if (!$(inputObj).hasClass("filter_date")) {
		// A filter other than a date filter clicked on
		$("#fieldFilterControls").fadeOut();
		return;
	}
	var resolution = parseInt($(inputObj).attr("data-resolution"));
	if (resolution < 5) {
		// Filter is a year or month resolution date, can't use filter
		$("#fieldFilterControls").fadeOut();
		return;
	}
	// Filter is already active
	if ($("#fieldFilterControls").is(":visible")) {
		return;
	}
	// Timeout cleared by user typing something immediately
	if (!$(inputObj).hasClass("waitingForFilterControls")) {
		return;
	}
	$(inputObj).removeClass("waitingForFilterControls");
	var zoomLevel = 0;
	var firstCallback = true;
	var firstRangeCallback = true;
	var inputLeft = $(inputObj).position().left;
	if (inputLeft > 1000) {
		left = inputLeft - 980;
		$("#fieldFilterControls").css("left", left + "px");
	}
	if (inputLeft > 900) {
		var left = inputLeft - 880;
		$("#fieldFilterControls").css("left", left + "px");
	} else {
		$("#fieldFilterControls").removeAttr("style");
	}
	// reset to clear previous actions
	$("#dateControlWrapper").children().remove();
	$("#dateControlWrapperTemplate").clone().children().appendTo(
			$("#dateControlWrapper"));
	$("#fieldFilterControls").show();
	$("#dateControlWrapper")
			.load(
					"AppController.servlet?return=gui/pane2/filter_controls",
					function() {
						$("#dateControlWrapper .close").click(function() {
							$("#fieldFilterControls").fadeOut();
						});
						var months = [ 'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul',
								'Aug', 'Sep', 'Oct', 'Nov', 'Dec' ];
						// individual date
						$("#individualDateSelector")
								.calendarPicker(
										{
											years : 3,
											months : 6,
											days : 12,
											callback : function(cal) {
												$(".calElement").click(
														function() {
															if ($(this).closest(".calDay").size() > 0) {
																$("#fieldFilterControls").attr(
																		"data-day_selected", "true");
															} else {
																$("#fieldFilterControls").removeAttr(
																		"data-day_selected");
															}
														});
												if (firstCallback) {
													firstCallback = false;
												} else {
													var selected = cal.currentDate;
													var selectedString = selected.getDate() + " "
															+ months[selected.getMonth()] + " "
															+ selected.getFullYear();
													$(inputObj).val(selectedString);
													$(inputObj).keyup();
													if ($("#fieldFilterControls").attr(
															"data-day_selected") == "true") {
														$("#fieldFilterControls").fadeOut();
													}
												}
											}
										});
						// date range
						var today = new Date();
						var maxDate = today;
						maxDate.setDate(today.getDate() + 60);
						var minDate = new Date();
						minDate.setDate(maxDate.getDate() - 730);
						var rangeStart = new Date();
						rangeStart.setMonth(today.getMonth() - 6);
						rangeStart.setDate(1);
						$("#dateRangeSelector").dateRangeSlider({
							arrows : false,
							bounds : {
								max : maxDate,
								min : minDate
							},
							defaultValues : {
								max : new Date(),
								min : rangeStart
							},
							formatter : function(val) {
								var days = val.getDate();
								var month = months[val.getMonth()];
								var year = val.getFullYear();
								return days + " " + month + " " + year;
							}
						});
						$("#dateRangeSelector").bind(
								"valuesChanged",
								function(e, data) {
									if (firstRangeCallback) {
										firstRangeCallback = false;
									} else {
										var minDate = data.values.min;
										var maxDate = data.values.max;
										var minString = minDate.getDate() + " "
												+ months[minDate.getMonth()] + " "
												+ minDate.getFullYear();
										var maxString = maxDate.getDate() + " "
												+ months[maxDate.getMonth()] + " "
												+ maxDate.getFullYear();
										/*
										 * $("#rangeStart").val(minString);
										 * $("#rangeEnd").val(maxString);
										 */
										$(inputObj).val(">" + minString + " and <" + maxString);
										$(inputObj).keyup();
									}
								});
						$("#dateRangePresets button").click(function() {
							var range = $(this).text();
							if (range == "all") {
								range = "";
							}
							$(inputObj).val(range);
							$(inputObj).keyup();
							$("#fieldFilterControls").fadeOut();
						});
						$("#rangeWrapper .zoom").click(
								function() {
									var bounds = $("#dateRangeSelector").dateRangeSlider(
											"option", "bounds");
									var now = new Date();
									var minRange = (now.getTime() - bounds.min.getTime());
									var maxRange = (now.getTime() - bounds.max.getTime());
									if ($(this).hasClass("in")) {
										minRange = minRange / 2;
										maxRange = maxRange / 2;
										zoomLevel += 1;
									} else {
										minRange = minRange * 2;
										maxRange = maxRange * 2;
										zoomLevel -= 1;
									}
									$("#dateRangeSelector").dateRangeSlider("option", "bounds", {
										max : new Date(now - maxRange),
										min : new Date(now - minRange)
									});
									/*
									 * Zoom level options seem to cause Firefox to hang if
									 * (zoomLevel < -3) {
									 * $("#dateRangeSelector").dateRangeSlider("option","step",
									 * {months: 1}); } else if (zoomLevel < -1) {
									 * $("#dateRangeSelector").dateRangeSlider("option","step",
									 * {weeks: 1}); } else {
									 * $("#dateRangeSelector").dateRangeSlider("option","step",
									 * {days: 1}); }
									 */
								});
						/*
						 * $("#rangeStart,#rangeEnd").keyup(function() { var minString =
						 * $("#rangeStart").val(); var maxString = $("#rangeEnd").val(); if
						 * ((minString != "") && (maxString != "")) { $(inputObj).val(">" +
						 * minString + " and <" + maxString); $(inputObj).keyup(); } });
						 */
					}); // end of load function
}