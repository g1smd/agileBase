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

//Object.prototype.buttonclick=fButtonClick;
// how can I add this to just buttons?