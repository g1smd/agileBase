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
      scroll(0,0); // workaround for popup showing at the top of the screen rather than the current scroll position
      fShowModalDialog('gui/calendar/edit_event&set_table=' + calEvent.internalTableName + '&set_row_id=' + calEvent.rowId,'edit event','fEditEventOK()','ok cancel','width=800px; height=600px');
    },
    minTime: 6
  });
  
  // Show initial calendars
  $("#report_selection input:checked").each(function() {
	var jqCheckbox = $(this);
    var internalTableName = jqCheckbox.attr("internaltablename");
    var internalReportName = jqCheckbox.attr("internalreportname");
    var feedUrl = "AppController.servlet?return=gui/calendar/feed&internaltablename=" + internalTableName + "&internalreportname=" + internalReportName;
    $("#calendar").fullCalendar('addEventSource', feedUrl); 
  });
  // Show report selector if no reports are initially selected
  if($("#report_selection input:checked").length == 0) {
	$("#report_selection").show("normal");
  }

  // Add/remove calendars on click
  $("#report_selection input").change(function() {
    var jqCheckbox = $(this);
    var reportName = jqCheckbox.text();
    var internalTableName = jqCheckbox.attr("internaltablename");
    var internalReportName = jqCheckbox.attr("internalreportname");
    var feedUrl = "AppController.servlet?return=gui/calendar/feed&internaltablename=" + internalTableName + "&internalreportname=" + internalReportName;
    if (jqCheckbox.is(":checked")) {
      $("#calendar").fullCalendar('addEventSource', feedUrl); 
	  var addReportOptions = {
        'return': 'blank',
        'add_operational_dashboard_report': 'true',
        'internaltablename': internalTableName,
        'internalreportname': internalReportName
	  }
      $.post("AppController.servlet", addReportOptions);
    } else {
	  $("#calendar").fullCalendar('removeEventSource', feedUrl);
	  var removeReportOptions = {
        'return': 'blank',
        'remove_operational_dashboard_report': 'true',
        'internaltablename': internalTableName,
        'internalreportname': internalReportName
	  }
      $.post("AppController.servlet", removeReportOptions);
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
