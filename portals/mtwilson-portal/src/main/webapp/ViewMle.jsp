<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loMLEe.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
<div class="container">
		<div class="nagPanel">MLE \> View MLE</div>
		<div id="nameOfPage" class="NameHeader">View MLE Configuration</div>
		<div id="mainLoadingDiv">
			<div class="tableDivMLE" style="margin-left: 61px; display: none;" id="viewMleMainDataDisplay">
			<table class="tableDisplay" width="100%" cellpadding="0" cellspacing="0">
				<thead>
					<tr>
					<!-- Soni_Begin_05/10/2012_for_header_alignment   -->
						<th class="rowv">Name</th>
						<th class="row3">&nbsp;&nbsp;&nbsp;&nbsp;Version</th>
						<th class="rowr1">Attestation Type&nbsp;&nbsp;&nbsp;&nbsp;</th>
						<th class="row4">MLE Type&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</th>
						<th class="row4">OS Info</th>
						<th class="row4">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;OEM Name</th>
						<th class="row4">Description</th>
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