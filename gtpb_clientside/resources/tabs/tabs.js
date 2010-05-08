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
var pane3TabInterface;
var pane3InterfaceUpdateFunctions = new Array();

// Store and execute scripts required for different content
var pane3Scripts = function() {
	pane3ScriptsPub = new Object();
	
	pane3ScriptsPub.functionList = new Array();
	
	pane3ScriptsPub.update = function() {
		var len = pane3ScriptsPub.functionList.length;
		for (var i=0; i<len; i++) {
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

        var jqTabContainer = $("<div class='tab-item'></div>");

        var tabLoaded = false;


        TabObjectPub.queueTab = function() {
        	tabLoaded = false;

        	loadSpinner.show();
            jqTabContainer.load(tabSource, null, function(){
            	TabObjectPub.showTab();
            	
            	loadSpinner.hide();
            	if (parent.pane_2 && currentRowId != -1) {
            		var rowFound = parent.pane_2.fSetRowSelection(currentRowId);
            		if ((!rowFound) && (tabNumber < 2)) {
            			$.get("?return=gui/reports_and_tables/tabs/edit_warning", function(warningRowHtml) {appendWarning(warningRowHtml);} );
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
            for (var i=0; i<len; i++) {
                if (tabList[i] != TabObjectPub) {
                    tabList[i].hideTab();
                }
            }

            if (!tabLoaded) {
            	jqTabContainer.empty();	                	
            	loadSpinner.show();
                jqTabContainer.load(tabSource, null, function(){
                	loadSpinner.hide();
                	if (parent.pane_2 && currentRowId != -1) {
                		var rowFound = parent.pane_2.fSetRowSelection(currentRowId);
                		if ((!rowFound) && (tabNumber < 2)) {
                			$.get("?return=gui/reports_and_tables/tabs/edit_warning", function(warningRowHtml) {appendWarning(warningRowHtml);} );
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
    jqTabInterface.find('.tab-list li').each(function(i){
        linkElem = $(this).find("a")[0];
        current = false;
        if ($(this).hasClass("current")) {
            current = true;
        }
        tabList.push(new TabObject(i, linkElem, current));
    });
    
    TabInterfaceObjectPub.invalidate = function() {
        var len = tabList.length;
        for (var i=0; i<len; i++) {
        	tabList[i].invalidate();
        }    	
    }    
    
    TabInterfaceObjectPub.refresh = function(rowId) {
    	TabInterfaceObjectPub.invalidate();
        currentRowId = rowId;
        
    	currentTab.queueTab();
    }
    
    TabInterfaceObjectPub.getNumberOfTabs = function() {
    	return tabList.length;
    }
    
    return TabInterfaceObjectPub;
}

// Initialise
$(document).ready(function(){
	pane3TabInterface = new TabInterfaceObject($('.tab-interface')[0]);
	pane3Scripts.functionList.push(fInit);
	pane3Scripts.update();
})

/* 
 * =========================================================
 * End of tab engine functions.
 * Start of general functions for to be used by the tabs themselves.
 * First, the edit tab
 */

function fUnlockButton() {

	var jqUnlockButton = $("#recordUnlockButton");
	var jqUnlockDiv = $("#recordUnlock");
	var jqBlankDiv = $("#recordBlank");

	function fUnlockRecord() {
		function fUnlockResult(sText, sXML) {
			try {
				if(sXML.getElementsByTagName('response')[0].firstChild.nodeValue!='ok') throw 'result_not_ok';
				jqUnlockDiv.remove()
				jqBlankDiv.remove();
			}
			catch(e) {
				jqUnlockButton.removeAttr('disabled');
				jqUnlockButton.find("div").empty().text("unlock record");
				alert('the record could not be unlocked\nplease try again\n('+e+')');
			}
		}

		jqUnlockButton.attr('disabled','true');
		jqUnlockButton.find("div").empty().text("unlocking...");

		var aPostVars=new Array();
		aPostVars['return']='gui/administration/xmlreturn_fieldunlockrequest';
		aPostVars['set_lock_override']='true';	    
		aPostVars['returntype']='xml';
		var oReq=new fRequest('AppController.servlet',aPostVars,fUnlockResult,-1);	        
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
		
		var idEndString = internalFieldName.substring(internalFieldName.indexOf("_dropdown")+9);
		internalFieldName = internalFieldName.substring(0, internalFieldName.indexOf("_dropdown"));
		
		// Get associated text box
		var jqTextBox = $("#"+internalFieldName+"_text"+idEndString);
		jqTextBox.hide();
		
		// Either show the text box, or populate and update
		function fDropdownChange(evt) {
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
		}
		
		jqSelect.bind("change", fDropdownChange);
	}

	// Setup all combo-components
	$('.combo-component').each(function(i) {
		fComboComponent(this);
	});
}

function fRelationPickers() {
  function bindAutoComplete(jqElement, internalTableName, internalFieldName) {
	      jqElement.autocomplete(
				    "AppController.servlet", 
					{
					  autoFill: true,
					  cacheLength: 100,
					  max: 99,
					  extraParams:{
						    gtpb_return: "gui/resources/input/return_relation_values",
						  	set_custom_field: true,
						  	fieldkey: "relationField",
						  	custominternaltablename: internalTableName,
						  	custominternalfieldname: internalFieldName
						  },
					  mustMatch: true,
					  selectFirst: true,
					  width: 296
				    }
		   );
		  
		  jqElement.result(function(event, data, formatted) {
			  if(data) {
				  jqElement[0].formEl.doUpdate(data[1], true);
			  } else {
				  alert('Error saving data');
			  }
		  });
  }
    
  $('.autocomplete').each(function(i) {
	  if($.browser.msie) {
		  //$('<span style="padding-right:10px">'+this.value+'</span>').insertBefore($(this));
		  //$(this).remove();
		  return;
	  }
	  else {
		  var jqThis = $(this);
		  var internalTableName = jqThis.attr('internalTableName');
		  var internalFieldName = jqThis.attr('internalFieldName');
		  bindAutoComplete(jqThis, internalTableName, internalFieldName);
	  }
  });
				    
}

function appendWarningAction() {
  var jqTableBody = $("#reportData > tbody");
  if (jqTableBody.length > 0) {
    // if message not already there
    if (jqTableBody.find(".warningmessage").length == 0) {
	  jqTableBody.append(warningRowHtmlSaved);
    }
  } else {
	// TODO: possibility of infinite heap building up if reportData > tbody is never there, deal with this
    setTimeout('appendWarningAction', 1000);
  }
}

/* Called if the current record isn't visible in pane 2, to add a warning to that effect in the edit tab */
function appendWarning(warningRowHtml) {
  warningRowHtmlSaved = warningRowHtml;
  setTimeout('appendWarningAction()', 1000);
}

var warningRowHtmlSaved = '';

// TODO: update when browser support becomes more standard and improves
// as per https://developer.mozilla.org/En/Using%5FXMLHttpRequest#In%5FFirefox%5F3.5%5Fand%5Flater
function uploadFile(fileInputElement) {
  // https://developer.mozilla.org/en/DOM/File
  var fileObject = fileInputElement.files[0];
  if (fileObject) {
	var fileSize = fileObject.size; // the new way
	if (!fileSize) {
	  fileSize = fileObject.fileSize; // the deprecated way
	}	
    if (fileSize) {
      var jqFileInput = jQuery(fileInputElement);
      var jqUploadInfo = jqFileInput.next();
      var fileSizeInfo = parseInt(fileSize / 1000000)
      if (fileSizeInfo == 0) {
        fileSizeInfo = 'Uploading &frac12; MB';
      } else {
        fileSizeInfo = "Uploading " + fileSizeInfo + " MB";
      }
      // upload speed is in bytes per sec.
      var uploadSpeed = parseInt(jqUploadInfo.attr("upload_speed"));
      var expectedMinutes = parseInt(fileSize / (uploadSpeed * 60));
      if (expectedMinutes == 0) {
        fileSizeInfo = fileSizeInfo + ". Please wait a few seconds...";
      } else if (expectedMinutes > 6) {
    	// Round the expected time
        expectedMinutes = Math.round(expectedMinutes / 10) * 10;
        fileSizeInfo = fileSizeInfo + ". This is likely to take about " + expectedMinutes + " minutes";
      } else if (expectedMinutes > 3) {
        fileSizeInfo = fileSizeInfo + ". This will take five minutes or so";
      }
      jqUploadInfo.html(fileSizeInfo);
    }
  }
  fileInputElement.form.submit();
}

/* 
 * Management tabs functions 
 */

function fShowTableUsage() {
  // If no usage log table exists yet, get from server
  if ($("#table_usage_loader table").size() == 0) {
	  $.get("AppController.servlet",
	  {"return":"gui/administration/tables/option_sets/table_usage_data_loader"},
	  function(returned_content) {
	    $("#table_usage_loader").html(returned_content);
		$("#table_usage_loader").removeClass("load_spinner");
	  });
  }
}

function fShowReportUsage() {
  // If no usage log table exists yet, get from server
  if ($("#report_usage_loader table").size() == 0) {
	  $.get("AppController.servlet",
	  {"return":"gui/administration/reports/option_sets/report_usage_data_loader"},
	  function(returned_content) {
	    $("#report_usage_loader").html(returned_content);
		$("#report_usage_loader").removeClass("load_spinner");
	  });
  }
}

/* Calculation editor */
function fEnableCalcSyntaxHighlight() {
  if($("#calculationdefn").length > 0) {
    editAreaLoader.init({
	  id: "calculationdefn"		// textarea id
	  ,syntax: "sql"			    // syntax to be uses for highlighting
	  ,start_highlight: true		// to display with highlight mode on start-up
	  ,allow_toggle: false
	  ,browsers: "all"
	  ,toolbar: ""
	  ,replace_tab_by_spaces: 2
	  ,EA_load_callback: "fEALoaded"
    });
  }
}

function fEALoaded() {
  $('#frame_calculationdefn').contents().find('.area_toolbar').hide();
}

/*
 * Summary tab functions
 */

/* Charts in pane 3 need some behaviours added */
function fSetupCharts() {
  $('.summary_chart').each(function(i) {
	  var summaryDivName = $(this).attr('id');
	  var summaryId = summaryDivName.replace('chart_','');
	  $(this).append("<div class='chart_remover'><a href='?return=gui/reports_and_tables/pane3&remove_report_summary=true&summaryid=" + summaryId + "'><img border='0' src='resources/icons/cross-greyscale-round.png' /></a></div>");
	  $(this).hover(
		function() {
		  $(this).find('.chart_remover').fadeIn("normal");
	    }, 
	    function() {
	      $(this).find('.chart_remover').fadeOut("normal");
	    }
	  );
  });
}

/* ---------- Add functions to the callFunctions list ---------- */
/* ------ These will be called every time a tab refreshes ------ */

pane3Scripts.functionList.push(fUnlockButton);
pane3Scripts.functionList.push(fComboComponents);
pane3Scripts.functionList.push(fRelationPickers);
pane3Scripts.functionList.push(fSetupCharts);
