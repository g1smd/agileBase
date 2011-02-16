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
	  if (view.name == 'month') {
        jqElement.height(15);
  	    jqElement.qtip({
          content: event.title
        });
      }
    },
    eventClick: function(calEvent, jsEvent, view) {
      var eventId = calEvent.id;
      fShowModalDialog('gui/calendar/edit_event&set_table=' + calEvent.internalTableName + '&set_row_id=' + calEvent.rowId,'edit event','fEditEventOK()','ok cancel','width=800px; height=600px');
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
