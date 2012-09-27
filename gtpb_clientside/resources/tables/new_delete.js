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
//Create a new record and activate the edit tab
;
function fNew() {
	document.location='AppController.servlet?return=gui/reports_and_tables/report_data&save_new_record&set_custom_string=true&key=report_tabindex&value=2';
}

//Clone a record and activate the edit tab
function fClone()
{   document.location='AppController.servlet?return=gui/reports_and_tables/report_data&clone_record&set_custom_string=true&key=report_tabindex&value=2';
}

function fDelete()
{ var sAction='remove_record';
	var sRowIdentifier='rowid';
	var oDelete=new fDeleteObj(sAction,sRowIdentifier);
}

function fImport() { 
	top.fShowModalDialog('gui/import/1_update_or_insert','import data','fImportOK()','ok cancel next','width=800px; height=600px');
}

function fSetPassword() {
	top.fShowModalDialog('gui/set_password/1_set_password','set password','fSetPasswordOK()','ok cancel','width=800px; height=600px;');
}

function fHelp() {
	top.fShowModalDialog('gui/help/1_help_centre','help centre','fHelpOK()','ok','width=800px; height=600px;');
}

function fAppStore() {
	top.fShowModalDialog('gui/appstore/1_app_store','app store','fHelpOK()','ok','width=800px; height=600px;');
}

// Download the current session report
function fExport() {
	this.location.href='ReportDownloader.servlet';
}

function fLinks()
{
	$.get('AppController.servlet?return=gui/links', {
		abCache: new Date().getTime()
	}, function(mailtoUrl) {
		  document.location.href=mailtoUrl;
	  }
	);
}

function fLoadFromPreview(oBlock, event) {
	var jqBlock = $(oBlock);
	// Note other objects have to be selected relative to jqBlock because this JS file is in a different frame
	jqBlock.siblings(".block.selected").removeClass("selected");
	jqBlock.siblings(".block").addClass("notselected");
	var rowId = jqBlock.attr("data-rowid");
	// Find the row in pane 2, click it
	//var jqRow = $(document).find("tr[name=" + rowId + "]");
  //jqRow.click();
	parent.pane_2.loadIntoPane3('AppController.servlet?return=gui/reports_and_tables/pane3&set_row_id=' + rowId + '&set_custom_string=1&key=report_tabindex&value=2', rowId, 6);
  showPane3IfNecessary(event);
  var target = $(event.target);
  if (target.hasClass("image")) {
  	var left = jqBlock.position().left;
  	var right = jqBlock.position().right;
  	var areaRight = $("#preview").width();
  	if (left < 50) {
  		jqBlock.css("left", "400px");
  	} else if (right > (areaRight - 50)) {
  		jqBlock.css("left", "-400px");
  	}
  	jqBlock.addClass("selected").removeClass("notselected");
  	var img = jqBlock.find("img.image");
  	img.attr("src", img.attr("data-bigsrc"));
  } else {
  	top.closePreview();
  	//TODO: also clear quick search string in session
  }
}
