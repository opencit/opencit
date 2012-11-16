<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loMLEe.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Edit Mle</title>
</head>
<body>
<div class="container">
		<div class="nagPanel">MLE \> Edit MLE</div>
		<div id="nameOfPage" class="NameHeader">Edit Measured Launch Environment (MLE) Configuration</div>
		<div id="mainEditMleDisplayDiv">
			<div class="tableDivMLE" style="margin-left: 61px;display: none;" id="mainTableDivEditMle">
			<table class="tableDisplay" width="100%" cellpadding="0" cellspacing="0">
				<thead>
					<tr>
						<th class="row1">Options</th>
						<th class="rowr3">Name</th>
						<th class="row2">Version&nbsp;&nbsp;&nbsp;&nbsp;</th>
						<th class="rowr3">Attestation Type&nbsp;&nbsp;</th>
					 <!-- 	<th class="rowr7">MLE Manifests</th> -->
						<th class="row4">&nbsp;&nbsp;&nbsp;MLE Type</th>
						<th class="rowr4">OS Info</th>
						<th class="rowr2">OEM Name</th>
						<th class="rowr3">Description</th>
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
				<div id="messageSpace"></div>
		</div>
		</div>
		
	</div>
	<script type="text/javascript" src="Scripts/EditMle.js"></script>
</body>
</html>