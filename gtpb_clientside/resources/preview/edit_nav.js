$(document).ready(function() {
	$(".searchbox").keyup(function(event) {
		if (event.keyCode == 27) {
			closePreview();
			$(".searchbox").blur();
			return;
		}
		var jqSearchBox = $(this);
		jqSearchBox.addClass("changed");
		var filterString = jqSearchBox.val();
		if (filterString.length == 1) {
			return;
		}
		//var internalReportName = $("#searchbox").attr("internalreportname");
		$.get(
			"AppController.servlet?return=gui/preview/report_content"
					+ "&set_global_report_filter_string=true&filterstring="
					+ filterString, function(data) {
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
		});
	});
	$(".searchbox").focus(function() {
		fFullScreen();
	});
	$(".searchbox").blur(function() {
		closePreview();
	});
});