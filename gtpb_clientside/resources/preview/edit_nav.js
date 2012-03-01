$(document).ready(
	function() {
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

function bigImage(oImg) {
	var jqImg = $(oImg);
	var src = jqImg.attr("src");
	src = src.replace(/\.500\.(jpg|png)/,'');
	jqImg.attr("src", src);
	var block = jqImg.closest(".block");
	block.css('width','500px');
	block.css('height','300px');
}
