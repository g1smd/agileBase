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
            		if (!rowFound) {
            			$.get("?return=gui/reports_and_tables/tabs/edit_warning", appendWarning);
            		}
            	}
            	pane3Scripts.update();
            });
            tabLoaded = true;
        }
        
        
        TabObjectPub.showTab = function(evt) {
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

                		if (!rowFound) {
                			$.get("?return=gui/reports_and_tables/tabs/edit_warning", appendWarning);
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