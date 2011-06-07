var gtwmJson = [{
	anonymise : true,
	'return': 'blank',
	set_table : 'a3b09a609b4c70624',
	table_description: 'organisations',
	a7bcdde21f252dd7d : 'COMPANY_NAME',
	a3793882e13c7ee65 : 'OTHER', // source
	a467b03e93435a25e : 'OTHER', // industry
	aabc2c00deb4863ff : 'OTHER', // main activity
	a24908ad77b2621ab : 'OTHER', // employee nos.
	abbafc4efa06458f1 : 'OTHER', // primary relation type
	ad22bc52f732c4d93 : 'EMAIL_ADDRESS',
	a294d9d46f8eca840 : 'OTHER', // websites
	aab5ae948ced84d8c : 'OTHER',
	boptueichiufrmm6h : 'OTHER',
	bs3rxzqeqg9p4kn72 : 'OTHER',
	abe3f97ed6b177562 : 'NOTES'
}];

function anonymiseCompany() {
  for (var i = 0; i < gtwmJson.length; i++) {
  	tableCommand = gtwmJson[i];
  	$("#results").append("Anonymising...<br>");
  	$.post("AppController.servlet", tableCommand, function() {
  		$("#results").append("Table anonymised: " + tableCommand.table_description + "<br>");
  	});
  }
}