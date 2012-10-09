$(document).ready(function() {
	$("#searchbox").keyup(function(event) {
		var jqSearchBox = $(this);
		jqSearchBox.addClass("changed");
		var filterString = jqSearchBox.val();
		if (filterString.length == 1) {
			return;
		}
    loadPreviewResults(filterString, false);
	});
	$("#searchbox").focus(function() {
		fFullScreen();
	});
	$("document").keyup(function(event) {
		if (event.keyCode == 27) {
			closePreview();
			$("#searchbox").blur();
			return;
		}
	});
	$("#doneSearch").click(function() {
		closePreview();
	});
	appLauncherStandalone();
});

function appLauncherStandalone() {
  $("#apps li.modulecollapsed").click(function() {
  	var internalModuleName = $(this).attr("id");
  	var appId = $(this).find("i").attr("data-appid");
  	var internalTableName = $(this).attr("data-table");
  	var internalReportName = $(this).attr("data-report");
  	var appTemplate = $(this).attr("data-apptemplate");
  	if (appTemplate == "") {
  		appTemplate = "gui/display_application";
  	}
  	document.location = "AppController.servlet?return=" + appTemplate + "&set_table=" + internalTableName + "&set_report=" + internalReportName + "&set_module=" + internalModuleName + "&set_app_id=" + appId + "&cachebust=" + (new Date).getTime();
  });	
  $("#all_apps").click(function() {
  	document.location = "AppController.servlet?return=gui/display_application&clear_app_id=true";
  });
}

function appLauncherIntegrated() {
	$("#apps li.modulecollapsed").click(function() {
  	var internalModuleName = $(this).attr("id");
  	var appId = $(this).find("i").attr("data-appid");
  	var internalTableName = $(this).attr("data-table");
  	var internalReportName = $(this).attr("data-report");
  	var appTemplate = $(this).attr("data-apptemplate");
  	if (appTemplate == "") {
  		top.oViewPane.pane_1.showApp(internalModuleName);
  	} else {
    	document.location = "AppController.servlet?return=" + appTemplate + "&set_table=" + internalTableName + "&set_report=" + internalReportName + "&set_module=" + internalModuleName + "&set_app_id=" + appId + "&cachebust=" + (new Date).getTime();
  	}
  	closePreview();
	});
  $("#all_apps").click(function() {
  	document.location = "AppController.servlet?return=gui/display_application&clear_app_id=true";
  });
}

/**
 * @param internalReportName
 *   Optional parameter to set the session report along with sending the request
 */
function loadPreviewResults(filterString, internalReportName) {
	var jqSearchBox = $("#searchbox");
	var request = "AppController.servlet?return=gui/preview/report_content"
		+ "&set_global_report_filter_string=true&filterstring="
		+ filterString;
	if (internalReportName) {
		request += "&set_report=" + internalReportName;
		// We are being called probably not from a keypress - update the value in the search box
		jqSearchBox.val(filterString);
	}
	$.get(request, {
		abCache: new Date().getTime()
	}, function(data) {
			// response has come back from the
			// server, check it isn't out of
			// date
			if (jqSearchBox.val() != filterString) {
				return;
			}
			$("#homeContent").html(data);
			jqSearchBox.removeClass("changed");
			var goodMatches = $(".goodMatch").closest(".block").detach();
			$("#homeContent").prepend(goodMatches);
			goodMatches.addClass("goodMatchBlock");
			$("#schemaResults ul li").click(function() {
				var jqLi = $(this);
				var internalTableName = jqLi.attr("data-internaltablename");
				var internalReportName = jqLi.attr("data-internalreportname");
				var id=internalTableName + internalReportName;
				var href = $(top.oViewPane.pane_1.document).find("#" + id).find("a").attr("href");
				loadPreviewResults("", internalReportName);
				top.oViewPane.pane_2.document.location = href;
				$("#searchTitle").text(jqLi.attr("data-searchtitle"));
			});
			if($("#schemaResults ul li").size() == 0) {
				$("#schemaResults").hide();
			}
	});	
}