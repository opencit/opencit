<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title data-i18n="title.edit_oem">Edit OEM</title>
</head>
<body>
	<div class="container">
		<div class="nagPanel"><span data-i18n="title.whitelist">Whitelist</span> &gt; <span data-i18n="title.edit_oem">Edit OEM</span><span style="float: right"><a href="javascript:fnAddOEM()" data-i18n="link.add_oem">Add OEM</a></span></div>
		<div id="nameOfPage" class="NameHeader" data-i18n="header.edit_oem">OEM - Edit/Delete the Values</div>
		<div id="mainEditOEMDisplayDiv">
			<div class="tableDiv" style="margin-left: 61px; display: none;" id="mainEditTable">
				<table class="tableDisplay" width="100%" cellpadding="0" cellspacing="0">
					<thead>
						<tr>
							<th class="row1" data-i18n="table.options">Options</th>
							<th class="row2" data-i18n="table.name">Name</th>
							<th class="row4" data-i18n="table.description">Description</th>
						</tr>
					</thead>
					</table>
					<div class="tableDiv" style=" overflow: auto;" id="editOEMContentDiv">
						<table class="tableDisplay" width="100%" cellpadding="0" cellspacing="0">
							<tbody>
								<%-- <tr class="${rowStyle}">
									<td class="row1"><a href="javascript:;" onclick="fnEditOEMInfo(this)"> Edit </a><span> | </span><a href="javascript:;" onclick="fnDeleteOemInfo(this)"> Delete </a></td>
									<td class="row2" name="${OemData.oemName}" value="${OemData.oemName}" id="oemName">${OemData.oemName}</td>
									<td class="row4" name="${OemData.oemDescription}" value="${OemData.oemDescription}" id="oemDec">${OemData.oemDescription}&nbsp;</td>
								</tr> --%>
							</tbody> 
						</table>
					
				</div>
				<div id="editOEMPaginationDiv"></div>
					</div>
			<div id="messageSpace"></div>
			<div id="errorEditOEM" class="errorMessage"></div>
		</div>
	</div>
	<script type="text/javascript" src="Scripts/EditOEM.js"></script>
</body>
</html>