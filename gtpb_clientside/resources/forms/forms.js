$(document).ready(function() {
  // Show initial form radio buttons
  $("#report_selection input:checked").each(function() {
    addRemoveForm(this);
  });
  
  // Add/remove forms from form selector on click
  $(".report_selection input").change(function() {
    var jqCheckbox = $(this);
    addRemoveForm(this);
    var internalTableName = jqCheckbox.attr("internaltablename");
    if (jqCheckbox.is(":checked")) {
  	  var addFormOptions = {
          'return': 'blank',
          'add_form_table': 'true',
          'internaltablename': internalTableName
  	  }
      $.post("AppController.servlet", addFormOptions);
    } else {
  	  var removeFormOptions = {
          'return': 'blank',
          'remove_form_table': 'true',
          'internaltablename': internalTableName
  	  }
      $.post("AppController.servlet", removeFormOptions);
    }
  });

  $(".report_selection_header").click(function() {
	if(fMobileDevice()) {
	  $(this).next(".report_selection").toggle();
	} else {
	  $(this).next(".report_selection").toggle('normal');
    }
  });
	  
});

function fMobileDevice() {
  if($("body").attr("id") == "mobile_device") {
    return true;
  }
  return false;
}

function addRemoveForm(checkboxElement) {
  var jqCheckbox = $(checkboxElement);
  var internalTableName = jqCheckbox.attr("internaltablename");
  var simpleTableName = jqCheckbox.attr("simpletablename");
  if (jqCheckbox.is(":checked")) {
	var checked = "";
	if (jqCheckbox.attr("sessiontable") == "true") {
	  checked = "checked";
	} 
    var legendElement = $("<input type='radio' name='form_radio' id='legend_" + internalTableName + "' " + checked + " />");
    $("#report_selection_header").append(legendElement);
    var labelElement = $("<label for='legend_" + internalTableName + "' >" + simpleTableName + "&nbsp;</label>")
    $("#report_selection_header").append(labelElement);
  } else {
	var legendId = "legend_" + internalTableName;
	$("#" + legendId).next("label").remove();
	$("#" + legendId).remove();
  }
}
