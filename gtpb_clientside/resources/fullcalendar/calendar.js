$(document).ready(function() {
  $('#calendar').fullCalendar({
    header: {
      left:   'title',
      center: 'month,agendaWeek,agendaDay',
      right:  'today prev,next'
	},
	events: 'AppController.servlet?return=gui/calendar/feed',
	editable: true,
    eventRender: function(event, jqElement, view) {
	  jqElement.qtip({
        content: event.description
      });
    },
    eventClick: function(calEvent, jsEvent, view) {
      var eventId = calEvent.id.toString();
      var internalTableName = eventId.replace("\_.*$","");
      var rowId = eventId.replace("^.*\_","");
      fShowModalDialog('gui/calendar/edit_event&set_table=' + internalTableName + '&set_row_id=' + rowId,'edit event','fEditEventOK()','ok cancel','width=800px; height=600px');
    }
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
