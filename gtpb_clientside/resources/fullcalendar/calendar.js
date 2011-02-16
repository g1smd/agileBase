$(document).ready(function() {
  $('#calendar').fullCalendar({
    header: {
      left:   'title',
      center: 'month,agendaWeek,agendaDay',
      right:  'today prev,next'
	},
    editable: true,
    eventRender: function(event, jqElement, view) {
	  if ((view.name == 'month') || ((view.name == 'agendaWeek') && event.allDay)) {
        jqElement.height(15);
  	    jqElement.qtip({
          content: event.title
        });
      }
    },
    eventClick: function(calEvent, jsEvent, view) {
      var eventId = calEvent.id;
      fShowModalDialog('gui/calendar/edit_event&set_table=' + calEvent.internalTableName + '&set_row_id=' + calEvent.rowId,'edit event','fEditEventOK()','ok cancel','width=800px; height=600px');
    },
    minTime: 6
  });

  	$("#report_selection input").change(function() {
  	  var jqCheckbox = $(this);
	  var reportName = jqCheckbox.text();
  	  var internalTableName = jqCheckbox.attr("internaltablename");
	  var internalReportName = jqCheckbox.attr("internalreportname");
	  var feedUrl = "AppController.servlet?return=gui/calendar/feed&internaltablename=" + internalTableName + "&internalreportname=" + internalReportName;
  	  if (jqCheckbox.is(":checked")) {
  	    $("#calendar").fullCalendar('addEventSource', feedUrl);
  	  } else {
    	$("#calendar").fullCalendar('removeEventSource', feedUrl);
  	  }
	});

	$("#report_selection_header").click(function() {
	  $("#report_selection").toggle('normal');
	});
});

function updateSelectedReports() {
  $("#selected_reports").children().remove();
  $("#report_selection input:checked").each(function() {
	var reportName = $(this).text();
	$("#selected_reports").append("<span class='active_report'>" + reportName + "</span>");
  });
}
