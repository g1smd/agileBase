/*
 *  Copyright 2011 GT webMarque Ltd
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

function assignButtonModuleActions() {
	$('button.moduleaction').click(function() {
		var internalModuleName = $(this).attr('internalmodulename');
		var actionName = $(this).attr('actionname');
		document.location = "?return=gui/mobile/module_action&set_module=" + internalModuleName + "&set_custom_string=true&key=actionname&value=" + actionName;
		return false;
	});
	$('button.tableaction').click(function() {
		var actionName = $(this).attr('actionname');
		document.location = "?return=gui/mobile/module_action&set_custom_string=true&key=actionname&value=" + actionName;
		return false;
	});
}

function loadCharts() {
	$('#abCharts').load("AppController.servlet?return=gui/reports_and_tables/tabs/summary", function() {
		fSetupCharts();
	});
}
