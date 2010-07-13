<html>
<head>
  <meta http-equiv="X-UA-Compatible" content="chrome=1">
  <link rel="icon" href="/agilebase/website/gtpb.ico" type="image/x-icon"> <!-- favicon -->
  <title>
    A3 reports collaboration
  </title>
  <script type="text/javascript" src="/agileBase/website/scripts/jquery.js"></script>
  <script type="text/javascript" src="resources/wait/editBuffer_editData.js"></script>
  <script type="text/javascript" src="resources/wait/request_setFilter.js"></script>
  <script src="/agileBase/website/scripts/jquery-ui/jquery-ui.js" language="Javascript"></script>
  <script src="/agileBase/website/a3/a3.js" language="Javascript"></script>
  <link type="text/css" href="/agileBase/website/a3/a3.css" rel="stylesheet">
  <link type="text/css" href="/agileBase/website/scripts/jquery-ui/jquery-ui.css" rel="stylesheet">
</head>
<body>
<img src="/agileBase/website/a3/paper.jpg" style="max-width:100%; z-index: 0">
#if($viewTools.getBrowser() == "Internet Explorer")
  During this early release of A3 reports, please use a web browser such as Firefox, Chrome or Safari.<p>
  Internet Explorer support is not yet ready I'm afraid.
  #stop
#end
<div id="a3_report">
## Will be loaded over AJAX
</div>

<div class="actions_area left" opacity="0.4">
  <button type="button" id="new_report" class="centered" title="start a new report">new report</button><p>
  <input class="centered" type="search" id="search" placeholder="search..." results="5" title="search all reports"/>
  <span class="centered">
	<button type="button" id="previous_report" title="go to the previous report">&larr;</button><button type="button" id="next_report" title="go to the next report">&rarr;</button>
  </span><p>
  <hr />
  <button type="button" id="print" class="centered" title="print this report"></button>
  <button type="button" id="share" class="centered" title="share this report with others"></button>
<!--
  <hr />
  <button type="button" id="delete" title="delete this report"></button><p>
  <button type="button" id="manage" title="manage all of your reports, users etc. in agileBase"></button>
-->
</div>

<div class="actions_area right" opacity="0.1">
  <button type="button" id="delete" title="delete this report"></button><p>
  <hr />
  <button type="button" id="manage" title="manage all of your reports, users etc. in agileBase"></button>
</div>

<div id="delete_dialog" title="Confirm deletion">
Should this report be deleted?
</div>
</body>
</html>
