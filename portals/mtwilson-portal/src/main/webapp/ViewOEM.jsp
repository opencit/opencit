<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>View OEM</title>
</head>
<body>
	<div class="container">
		<div class="nagPanel">OEM \> View OEM</div>
		<div id="nameOfPage" class="NameHeader">View OEM</div>
		<div id="ViewOEMDisplayDiv">
		<div class="tableDiv" style="margin-left: 61px; display: none;" id="viewOEMMainDataDisplay">
			<table class="tableDisplay" width="100%" cellpadding="0" cellspacing="0">
				<thead>
					<tr>
						<th class="row2">Name</th>
						<th class="row4">Description</th>
					</tr>
				</thead>
				</table>
				<div class="tableDiv" style=" overflow: auto;" id="viewOEMContentDiv">
					<table class="tableDisplay" width="100%" cellpadding="0" cellspacing="0">
						<tbody>
							<%-- <tr class="${rowStyle}">
								<td class="row2" id="osName">${OemData.oemName}</td>
								<td class="row4" id="osDec">${OemData.oemDescription}&nbsp;</td>
							</tr> --%>
						</tbody> 
					</table>
					
			</div>
			<div id="viewOEMPaginationDiv"></div>
				</div>
			<div id="viewOEMError" class="errorMessage">
			</div>
		</div>
	</div>
	<script type="text/javascript" src="Scripts/ViewOEM.js"></script>
</body>
</html>