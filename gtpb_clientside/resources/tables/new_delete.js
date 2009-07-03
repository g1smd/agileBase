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
//Create a new record and activate the edit tab
function fNew()
{	location='AppController.servlet?return=gui/reports_and_tables/report_data&save_new_record&set_custom_string=true&key=report_tabindex&value=2';
}

//Clone a record and activate the edit tab
function fClone()
{   location='AppController.servlet?return=gui/reports_and_tables/report_data&clone_record&set_custom_string=true&key=report_tabindex&value=2';
}

function fDelete()
{ var sAction='remove_record';
	var sRowIdentifier='rowid';
	var oDelete=new fDeleteObj(sAction,sRowIdentifier);
}

function fImport() { 
	//window.open("https://appserver.gtportalbase.com/portalBase/AppController.servlet?return=gui/import_csv","Data import");
	//top.spawnWindow('AppController.servlet?return=gui/import_csv','data_import','toolbar=no,location=no,directories=no,status=no,copyhistory=no,menubar=no,resizable=yes,dialog=yes')
	top.fShowModalDialog('gui/import/1_update_or_insert','import data','fImportOK()','ok cancel next','width=800px; height=600px');
}

// Download the current session report
function fExport()
{    this.location.href='ReportDownloader.servlet';
}

function fLinks()
{
	$.get('AppController.servlet?return=gui/links',
	  function(mailtoUrl) {
		document.location.href=mailtoUrl;
	  }
	);
	//this.location.href='AppController.servlet?return=gui/links&returntype=download&returnfilename=links.htm';
}

