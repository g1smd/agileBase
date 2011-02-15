$(document).ready(function() {
  $('#calendar').fullCalendar({
    header: {
      left:   'title',
      center: 'month,agendaWeek,agendaDay',
      right:  'today prev,next'
	},
	events: 'AppController.servlet?return=gui/calendar/feed',
	height: 650,
	contentHeight: 600
  });
  //updateSelectedReports();
});

function updateSelectedReports() {
  $("#selected_reports").children().remove();
  $("#report_selection input:checked").each(function() {
	var reportName = $(this).text();
	$("#selected_reports").append("<span class='active_report'>" + reportName + "</span>");
  });
}

$("#report_selection input").change(function() {
  var internalReportName = $(this).attr("internalreportname");
  var reportName = $(this).text();
  
});
