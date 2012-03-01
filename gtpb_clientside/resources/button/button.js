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
function fButtonClick(oButton) {  
  //if(oButton.tagName!='BUTTON') return;
   
  if (fToggleButtonState(oButton)) return;
  if (fGroupButtonClick(oButton)) return;
   
  function fToggleButtonState(oButton) {  
	  var jqButton = $(oButton);
	  if (! jqButton.hasClass('toggleButton')) {return false;}
	  if (jqButton.hasClass('selected')) {
		  jqButton.removeClass('selected');
	  } else {
		  jqButton.addClass('selected');
	  }
    //with (oButton) {	
    //  if (getAttribute('toggleButton')!='true') return false;
    //  if (getAttribute('selected')=='true') removeAttribute('selected');
    //  else setAttribute('selected','true');
    //}
  }

  function fGroupButtonClick(oButton) {  
    if (!oButton.parentNode) return false;
    if (oButton.parentNode.className!='buttonRadioGroup') return false;  
      
    fClearGroup(oButton);
    $(oButton).addClass('selected');
    //oButton.setAttribute('selected','true'); 
    return true;
      
    function fClearGroup(oButton) {  
      $(oButton).siblings('button').removeClass('selected');
      //var oButtonsInGroup=oButton.parentNode.getElementsByTagName(oButton.tagName);
      //for (var i=0;i<oButtonsInGroup.length;i++)
      //oButtonsInGroup[i].removeAttribute('selected');
    }
  }
}

function fAnimatePane1(pane1ChangePercent, currentWidth, targetWidth) {
  var difference = (targetWidth - currentWidth);
  var change = difference * (pane1ChangePercent / 100);
  currentWidth = currentWidth + change;
  document.getElementById('oViewPane').contentWindow.document.getElementById('colsWrapper').cols=(Math.round(currentWidth) + ',*');
  if (Math.abs(currentWidth - targetWidth) > 2) {
  	pane1ChangePercent = pane1ChangePercent * 2;
  	setTimeout('fAnimatePane1(' + pane1ChangePercent + ',' + currentWidth + ',' + targetWidth + ');');
  } else {
    document.getElementById('oViewPane').contentWindow.document.getElementById('colsWrapper').cols=(targetWidth + ',*');
  }
}

function fTogglePane1(oButton) {
	var currentWidth = 250;
	var targetWidth = 0;
	if($(oButton).hasClass('selected')) {
		currentWidth = 0;
		targetWidth = 250;
	}
	fAnimatePane1(0.1, currentWidth, targetWidth);
}

function fSummaryPaneReady() {				 
  // return the status of the summary pane by examining its readystate
  with (oViewPane){  
		if (document.readyState != 'complete') return false;
		if (detail_pane.document.readyState != 'complete') return false;
  }
  return true;
}

function showPane3IfNecessary(oEvent) {
  document.getElementById('oViewPane').contentWindow.pane_2.showPane3IfNecessary(oEvent);
}

function hidePane3() {
  document.getElementById('oViewPane').contentWindow.pane_2.hidePane3();
}

function fNew() {  
  document.getElementById('oViewPane').contentWindow.pane_2.fNew();
} 

function fClone() 	{  
  document.getElementById('oViewPane').contentWindow.pane_2.fClone();
} 

function fDelete() 	{  
  document.getElementById('oViewPane').contentWindow.pane_2.fDelete();
}

function fImport() {  
  document.getElementById('oViewPane').contentWindow.pane_2.fImport();
}

function fHelp() {
  document.getElementById('oViewPane').contentWindow.pane_2.fHelp();
}

function fExport() {  
  document.getElementById('oViewPane').contentWindow.pane_2.fExport();
}

function fSetPassword() {
  document.getElementById('oViewPane').contentWindow.pane_2.fSetPassword();
}

function fLinks() {  
  document.getElementById('oViewPane').contentWindow.pane_2.fLinks();
}

function fPrint() {
	// Test FF full screen
	//document.getElementById('oViewPane').contentWindow.document.getElementById('colsWrapper').mozRequestFullScreen();
  var oPrintWin=window.spawnWindow('AppController.servlet?return=gui/printouts/pane2_printout_wrapper','print_window','toolbar=no,location=no,directories=no,status=no,copyhistory=no,menubar=no,resizable=yes,dialog=yes')	
}

function fInfo() {
    var oPrintWin=window.spawnWindow('AppController.servlet?return=gui/dashboard','info_window','toolbar=no,location=no,directories=no,status=no,copyhistory=no,menubar=no,resizable=yes,dialog=yes')	
}

function fDashboard() {
  document.location = 'AppController.servlet?return=gui/customisations/common/dashboard/dashboard';
}
  
function fForms() {
  document.location = 'AppController.servlet?return=gui/edit_nav/edit_nav';
}
  
function fCalendar() {
  document.location = 'AppController.servlet?return=gui/calendar/calendar';
}

function fFullScreen() {
	var previewDiv = $("preview");
	$("preview").show().load("AppController.servlet?return=gui/preview/report_including_content");
}

function fAppStore() {
  document.getElementById('oViewPane').contentWindow.pane_2.fAppStore();
}

function fSpawnWindow(sURL,sName,sParams) {
  function fDestroyWin() {
    oNewWin.close();
  }
  
  function fDisableApp() {
    var oDiv=document.createElement('div');
    oDiv.setAttribute('id','_md_blank');
    $(oDiv).addClass('window_disable');
    
    oH1=document.createElement('h1');
		oH1.appendChild(document.createTextNode('another window is open...'));
		oDiv.appendChild(oH1);
  
    document.body.appendChild(oDiv);
    return oDiv;	
  }
	   
  function fEnableApp() {
  	// the unload event seems to trigger immediately.  Check that the window's closed so that this is called correctly
  	if(!oNewWin.closed) return;
  	clearInterval(sInterval);
    oBlank.parentNode.removeChild(oBlank);	
  }
  
  var oNewWin=window.open(sURL,sName,sParams);
  oNewWin.focus();
  var oBlank=fDisableApp();        	  
  $(window).unload(fDestroyWin);
  $(oNewWin).unload(fEnableApp);

  var sInterval=setInterval(fEnableApp,2000); // if the print window is refreshed, it won't re-enable the app.  Use the timeout to check that the window is still open
  return oNewWin;
} // End of fSpawnWindow
  