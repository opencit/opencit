<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loMLEe.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title data-i18n="title.edit_mle">Edit MLE</title>
</head>
<body>
<div class="container">
		<div class="nagPanel"><span data-i18n="title.whitelist">Whitelist</span> &gt; <span data-i18n="title.edit_mle">Edit MLE</span>    <!--<span style="float: right"><a href="javascript:getAddMLE();">Add MLE...</a></span>--></div>
		<div id="nameOfPage" class="NameHeader" data-i18n="header.edit_mle">Edit Measured Launch Environment (MLE) Configuration</div>
		<div id="mainEditMleDisplayDiv">
			<div class="tableDivMLE" style="margin-left: 61px;display: none;" id="mainTableDivEditMle">
			<table class="tableDisplay" width="100%" cellpadding="0" cellspacing="0">
				<thead>
					<tr>
						<th class="rowr3" data-i18n="table.name">Name</th>
						<th class="row2"><span data-i18n="table.version">Version</span>&nbsp;&nbsp;&nbsp;&nbsp;</th>
						<th class="rowr3"><span data-i18n="table.attestation_type">Attestation Type</span>&nbsp;&nbsp;</th>
					 <!-- 	<th class="rowr7">MLE Manifests</th> -->
						<th class="row4">&nbsp;&nbsp;&nbsp;<span data-i18n="table.mle_type">MLE Type</span></th>
						<th class="rowr4" data-i18n="table.os_info">OS Info</th>
						<th class="rowr2" data-i18n="table.oem_name">OEM Name</th>
						<th class="rowr3" data-i18n="table.description">Description</th>
						<th class="row1"></th>
					</tr>
				</thead>
				</table>
				<div class="tableDivEditMle" style=" overflow: auto;" id="editMleContentDiv">
					<table class="tableDisplay" width="100%" cellpadding="0" cellspacing="0">
						<tbody>
						</tbody> 
					</table>
					
				</div>
				<div id="editMlePaginationDiv"></div>					
		</div>
		</div>
		<div id="errorEditMle" class="errorMessage">	</div>
		<div id="messageSpace"></div>
                <span id="alert_update_mle" data-i18n="alert.update_mle" style="display: none;">Are you Sure you want to update this MLE?</span>
                <span id="alert_delete_mle" data-i18n="alert.delete_mle" style="display: none;">Are you sure you want to delete this MLE?</span>
		<div id="dialog-confirm" title="Delete MLE?" style="display:none;">
                	<p>Are you sure you want to delete this MLE?</p>
       		</div>
	</div>
	<script type="text/javascript" src="Scripts/EditMle.js"></script>
</body>
</html>
