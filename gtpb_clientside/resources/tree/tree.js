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
	var mtiw = $(".module-tree-item-wrap");
	mtiw.removeClass("loading");
	if (mtiw.hasClass("needs_pane2_click")) {
		$(top.document.getElementById("pane2butt")).click();
		mtiw.removeClass("needs_pane2_click");
	}
	
}

function fUpdateTitle(sName, sNewTitle) {
	var jqCurrentItem = $("#"+sName);
	jqCurrentItem.find('a').text(sNewTitle);
}

$(document).ready(function(){
	pane1Setup();
	// If in a table (or admin section), show pane 3
	var currentOption = $("li.currentOption");
	if (currentOption.closest("#setup").length > 0) {
		top.showPane3IfNecessary();
	}
	var socketUrl = window.location.href;
	socketUrl = socketUrl.replace(":8080","");
	socketUrl = socketUrl.replace(/\/agileBase\/.*$/,"") + ":8181";
  var socket = io.connect(socketUrl);
  socket.on('notification', function (data) {
  	var n = $.parseJSON(data);
    notify(n.forename, n.internaltablename, n.internalreportname, n.messagetype, n.notification);
  });
});

function notify(forename, internalTableName, internalReportName, messageType, message) {
	// Find module containing the report that's the source of the notification
	var reportId = internalTableName + internalReportName;
	var reportItem = $("#" + reportId);
	if (reportItem.size() == 0) {
		// Maybe we don't have privileges to see the source report
		return;
	}
	var notifications = reportItem.closest("ul").closest("li").find(".notifications");
	notifications.children(".notification").each(function() {
		var notification = $(this);
		if (notification.attr("data-forename") == forename) {
			notification.remove();
		}
	});
	var notification = $("<span class='notification' data-forename='" + forename + "'>" + forename + "</span>");
	notifications.prepend(notification);
	if (messageType == "comment") {
		var tooltip = notification + " - " + forename + " " + surname;
		notification.attr("title", tooltip);
		alert(tooltip);
	}
	// Start to fade out notification after a few seconds
	setTimeout(function() {
		$(".notification").not(".going").addClass("going");
	}, 10000);
}

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
	$('#tree a.report_tooltip').click(function() {
		var mtiw = $(this).closest(".module-tree-item-wrap");
		mtiw.addClass('loading');
		// If pane 2 not visible, show
		var pane2Butt = $(top.document.getElementById("pane2butt"));
		if (!pane2Butt.hasClass("selected")) {
			mtiw.addClass("needs_pane2_click");
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
