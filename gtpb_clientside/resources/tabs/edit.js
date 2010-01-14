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

/* Manage functions */
function fShowTableUsage() {
  // If no usage log table exists yet, get from server
  if (jQuery("#table_usage_loader table").size() == 0) {
	  jQuery.get("AppController.servlet",
	  {"return":"gui/administration/tables/option_sets/table_usage_data_loader"},
	  function(returned_content) {
	    jQuery("#table_usage_loader").html(returned_content);
		jQuery("#table_usage_loader").removeClass("load_spinner");
	  });
  }
}

function fShowReportUsage() {
  // If no usage log table exists yet, get from server
  if (jQuery("#report_usage_loader table").size() == 0) {
	  jQuery.get("AppController.servlet",
	  {"return":"gui/administration/reports/option_sets/report_usage_data_loader"},
	  function(returned_content) {
	    jQuery("#report_usage_loader").html(returned_content);
		jQuery("#report_usage_loader").removeClass("load_spinner");
	  });
  }
}

/* Calculation editor */
function fEnableCalcSyntaxHighlight() {
editAreaLoader.init({
	id : "calculationdefn"		// textarea id
	,syntax: "sql"			// syntax to be uses for highgliting
	,start_highlight: true		// to display with highlight mode on start-up
	,allow_toggle: false
	,browsers: "all"
	,toolbar: ""
	,replace_tab_by_spaces: 2
});
}


/* ---------- Add functions to the callFunctions list ---------- */
/* ------ These will be called every time a tab refreshes ------ */

pane3Scripts.functionList.push(fUnlockButton);
pane3Scripts.functionList.push(fComboComponents);
pane3Scripts.functionList.push(fRelationPickers);
pane3Scripts.functionList.push(fEnableCalcSyntaxHighlight);
