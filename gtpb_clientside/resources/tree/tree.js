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

function fClearCurrentOption() {
	$("li.currentOption").removeClass("currentOption");
}       

function fSetCurrentOption(sName, sRecordCount){
	fClearCurrentOption();
	var jqCurrentItem = $("#"+sName);
	jqCurrentItem.addClass("currentOption");
	jqCurrentItem.find(".recordcount").html("("+sRecordCount+")");
	// open the report's module, if not already open
	var parentElem = jqCurrentItem.parent().parent();
	if (parentElem.hasClass('modulecollapsed')) {
		parentElem.children('ul').slideDown('fast');
		parentElem.removeClass('modulecollapsed');
		parentElem.addClass('moduleexpanded');
	}
}

function fUpdateTitle(sName, sNewTitle) {
	var jqCurrentItem = $("#"+sName);
	jqCurrentItem.find('a').text(sNewTitle);
}

$(document).ready(function(){
	pane1Setup();
});

function pane1Setup() {
	if ($("#tree").hasClass("setup")) {
		return;
	}
	$("#tree").addClass("setup");
	// Tree expanding and contracting
	$('#tree h2').click(function(event) {
		var parentElem = $(this).parent();
		if (parentElem.hasClass('modulecollapsed')) {
			parentElem.children('ul').slideDown('fast');
			parentElem.removeClass('modulecollapsed');
			parentElem.addClass('moduleexpanded');
		} else {
			parentElem.children('ul').slideUp('fast');
			parentElem.removeClass('moduleexpanded');
			parentElem.addClass('modulecollapsed');			
		}
	});
	
	// Initial expand?
	var numReports = $('.module-tree-item-wrap').size();
	if (numReports < 20) {
	  $('.modulecollapsed').each(function() {
		  if ($(this).parents('li#setup').size() == 0) {
			  $(this).children('ul').slideDown('fast');
			  $(this).removeClass('modulecollapsed');
			  $(this).addClass('moduleexpanded');
		  }
	  });
	}
	
	$('.expandable').click(function(event){
		// Prevent the children triggering open/close
		if (event.target == this) {
			if ($(this).hasClass('collapsed')) {
				$(this).removeClass('collapsed');
				$(this).addClass('expanded');
			} else {
				$(this).addClass('collapsed');
				$(this).removeClass('expanded');
			}
		}
	});
	
}
