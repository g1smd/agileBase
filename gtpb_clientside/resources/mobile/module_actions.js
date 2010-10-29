
function assignButtonModuleActions() {
	$('button.moduleaction').click(function() {
		var internalModuleName = $(this).attr('internalmodulename');
		var actionName = $(this).attr('actionname');
		document.location = "?return=gui/mobile/module_action&set_module=" + internalModuleName + "&set_custom_string=true&key=moduleaction&value=" + actionName;
	});
}
