$(document).ready(function() {
	$(".searchbox").live(
		'keyup',
		function() {
			var jqSearchBox = $(this);
			jqSearchBox.addClass("changed");
			var filterString = jqSearchBox.val();
			var internalReportName = $("#searchbox").attr(
					"internalreportname");
			$.get(
				"AppController.servlet?return=gui/preview/report_content&set_report="
						+ internalReportName
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
			});
		});
});

function loadFromPreview(oBlock) {
	document.getElementById('oViewPane').contentWindow.pane_2.fExport();
	return;
	var jqBlock = $(oBlock);
	var rowId = jqBlock.attr("data-rowid");
	// Find the row in pane 2, click it
	var jqPane2 = $("#oViewPane")[0].frames[0];
	alert("pane 2 " + jqPane2.tagName);
	var jqRow = jqPane2.find("tr[name=" + rowId + "]");
	alert("row " + jqRow.attr("name"));
}