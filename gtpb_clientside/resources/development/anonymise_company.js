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
},
{
	anonymise : true,
	'return': 'blank',
	set_table : 'a61b56c25acb0f216',
	table_description: 'sites',
	a2ae7ddbfa2daa251: 'COMPANY_NAME', // site
	a7ac98f59217ce0b: 'OTHER', // address
	af0ebf562cac7137b: 'OTHER',
	ad3557581c013cd86: 'OTHER',
	ab5c71f5e5022ec: 'OTHER', // country
	af98779e8dbb62f5e: 'OTHER', // postcode
	ad43d49defc72cf4f: 'EMAIL_ADDRESS',
	a80074403ae840c3d: 'NOTES',
	a6ba239b0d8775d21: 'PHONE_NUMBER',
	a4908dffd94c67d9: 'PHONE_NUMBER',
	aca527858d90c6937: 'PHONE_NUMBER'
},
{
	anonymise : true,
	'return': 'blank',
	set_table : 'abc1bfbb798d8ed70',
	table_description: 'contacts',
	a18a771dfdeea5156: 'FULL_NAME',
	a682678df8837750a: 'EMAIL_ADDRESS',
	a9c455c4ae8ab6336: 'EMAIL_ADDRESS',
	a13447896ac3cabe5: 'PHONE_NUMBER',
	ae8266ca45c1fb4ec: 'PHONE_NUMBER',
	a5b16f1fc5dc4abfa: 'PHONE_NUMBER',
	a9133e0c5c7a36258: 'PHONE_NUMBER',
	tdhzkreounzgjzkus: 'OTHER', // twitter
	mn5kpz8zdk332agpl: 'OTHER', // linkedin
	aca70cd262c1a420c: 'OTHER', // interests
	af752f749d68f6ad1: 'NOTES', // friends and colleagues
	a182f3ed5cae2c47d: 'NOTES', // family
	a8dc1652dcf676329: 'OTHER', // notes
},
{
	anonymise : true,
	'return': 'blank',
	set_table : 'abc1bfbb798d8ed70',
	table_description: '...'
}];

// a recursive function that moves on to the next table on completion of the
// previous
function anonymiseCompany(tableIndex) {
  	tableCommand = gtwmJson[tableIndex];
  	$("#results").append("Anonymising...<br>");
  	$.post("AppController.servlet", tableCommand, function() {
  		$("#results").append("Table anonymised: " + tableCommand.table_description + "<br>");
  		var newTableIndex = tableIndex + 1;
  		if (newTableIndex < gtwmJson.length) {
  			anonymiseCompany(newTableIndex);
  		} else {
  	  	$("#results").append("Finished.");
  		}
  	});
}