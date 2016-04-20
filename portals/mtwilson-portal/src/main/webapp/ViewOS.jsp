<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title data-i18n="title.view_os">View OS</title>
</head>
<body>
	<div class="container">
		<div class="nagPanel"><span data-i18n="title.whitelist">Whitelist</span> &gt; <span data-i18n="title.view_os">View OS</span></div>
		<div id="nameOfPage" class="NameHeader" data-i18n="header.view_os">View OS/Hypervisor Combination</div>
		<div id="ViewOSDisplayDiv">
			<div class="tableDiv" style="margin-left: 61px; display: none;" id="viewOSMainDataDisplay">
			<table class="tableDisplay" width="100%" cellpadding="0" cellspacing="0">
				<thead>
					<tr>
						<th class="row2" data-i18n="table.os_name">OS Name</th>
						<th class="row3" data-i18n="table.version">Version</th>
						<th class="row4" data-i18n="table.description">Description</th>
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