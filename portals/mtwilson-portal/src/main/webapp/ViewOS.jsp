<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>View OS</title>
</head>
<body>
	<div class="container">
		<div class="nagPanel">OS \> View OS</div>
		<div id="nameOfPage" class="NameHeader">View OS/Hypervisor Combination</div>
		<div id="ViewOSDisplayDiv">
			<div class="tableDiv" style="margin-left: 61px; display: none;" id="viewOSMainDataDisplay">
			<table class="tableDisplay" width="100%" cellpadding="0" cellspacing="0">
				<thead>
					<tr>
						<th class="row2">OS Name</th>
						<th class="row3">Version</th>
						<th class="row4">Description</th>
					</tr>
				</thead>
				</table>
				<div class="tableDiv" style=" overflow: auto;" id="viewOSContentDiv">
					<table class="tableDisplay" width="100%" cellpadding="0" cellspacing="0">
						<tbody>
							<%-- <tr class="${rowStyle}">
								<td class="row2" id="osName">${OSData.osName}</td>
								<td class="row3" id="osVer">${OSData.osVersion}&nbsp;</td>
								<td class="row4" id="osDec">${OSData.osDescription}&nbsp;</td>
							</tr> --%>
						</tbody> 
					</table>
					
				</div>
				<div id="viewOSPaginationDiv">
					</div>
			</div>
			<div id="viewOSError" class="errorMessage">
			</div>
		</div>
	</div>
		<script type="text/javascript" src="Scripts/ViewOS.js"></script>
</body>
</html>