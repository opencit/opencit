<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loMLEe.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title data-i18n="title.view_mle">Insert title here</title>
</head>
<body>
<div class="container">
		<div class="nagPanel"><span data-i18n="title.whitelist">Whitelist</span> &gt; <span data-i18n="title.view_mle">View MLE</span></div>
		<div id="nameOfPage" class="NameHeader" data-i18n="header.view_mle">View MLE Configuration</div>
		<div id="mainLoadingDiv">
			<div class="tableDivMLE" style="margin-left: 61px; display: none;" id="viewMleMainDataDisplay">
			<table class="tableDisplay" width="100%" cellpadding="0" cellspacing="0">
				<thead>
					<tr>
					<!-- Soni_Begin_05/10/2012_for_header_alignment   -->
						<th class="rowv" data-i18n="table.name">Name</th>
						<th class="row3">&nbsp;&nbsp;&nbsp;&nbsp;<span data-i18n="table.version">Version</span></th>
						<th class="rowr1"><span data-i18n="table.attestation_type">Attestation Type</span>&nbsp;&nbsp;&nbsp;&nbsp;</th>
						<th class="row4"><span data-i18n="table.mle_type">MLE Type</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</th>
						<th class="row4" data-i18n="table.os_info">OS Info</th>
						<th class="row4">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span data-i18n="table.oem_name">OEM Name</span></th>
						<th class="row4" data-i18n="table.description">Description</th>
						<!-- Soni_Begin_05/10/2012_for_header_alignment -->
					</tr>
				</thead>
				</table>
				<div class="tableDivMLE" style=" overflow: auto;" id="viewMleContentDiv">
					<table class="tableDisplay" width="100%" cellpadding="0" cellspacing="0">
						<tbody>
						</tbody> 
					</table>
				</div>
				<div id="viewMlePaginationDiv">
					</div>
			</div>
			<div id="viewMleError" class="errorMessage">
			</div>
		</div>
		
	</div>
	<script type="text/javascript" src="Scripts/viewMle.js"></script>
</body>
</html>