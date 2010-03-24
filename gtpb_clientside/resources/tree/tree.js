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

	// Tree expanding and contracting
	$('h2').click(function(event){
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
	if (numReports < 15) {
	  $('.modulecollapsed').each(function() {
		  if ($(this).parents('li.setup').size() == 0) {
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
	
	// Searching for a report
	
	  $('#reportsearch').mouseover(function() {
		    var reportsearchbox = $('#reportsearchbox');
			reportsearchbox.show('fast');
			reportsearchbox.focus();
		  });
		  
	$('#reportsearchbox').keyup(function(event) {
	  var box = $(this);
	  var searchstring = box.val();
	  if (searchstring.length == 0) {
		// finished searching, put everything back to normal
		$('.module-tree-item-wrap').removeClass('greytext').removeClass('found');
		// close all modules apart from the one with the current report in
		$('.moduleexpanded').children('h2').click();
		$('#currentOption').parent().siblings('h2').click();
		box.fadeOut('fast');
	  } else if (searchstring.length > 1) {
		$('.module-tree-item-wrap').addClass('greytext').removeClass('found');
		$(".module-tree-item-wrap:contains('"+searchstring+"')").removeClass('greytext').addClass('found');
		$('.modulecollapsed').each(function(i) {
			  var module = $(this);
			  if ((module.find('.found').length > 0) /* || (module.find("h2 span:contains('"+searchstring+"')").length > 0) */) {
				  module.show();
				  module.children('h2').click();
			  } else {
				  module.hide();
			  }
		    });
		$('.moduleexpanded').each(function(i) {
			  var module = $(this);
			  if ((module.find('.found').length == 0) /* || (module.find("h2 span:contains('"+searchstring+"')").length == 0) */) {
				  module.children('h2').click();
				  module.hide();
			  }
		    });
	  }
	});
	
});