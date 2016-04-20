<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title data-i18n="title.edit_os">Edit OS</title>
</head>
<body>
	<div class="container">
		<div class="nagPanel"><span data-i18n="title.whitelist">Whitelist</span> &gt; <span data-i18n="title.edit_os">Edit OS</span><span style="float: right"><a href="javascript:fnGetAddOS()" data-i18n="link.add_os">Add OS</a></span></div>
		<div id="nameOfPage" class="NameHeader" data-i18n="header.edit_os">OS/Hypervisor Combination Edit/Delete</div>
		<div id="mainEditOSDisplayDiv">
			<div class="tableDiv" style="margin-left: 61px; display: none;" id="mainEditTable">
				<table class="tableDisplay" width="100%" cellpadding="0" cellspacing="0">
					<thead>
						<tr>
							<th class="row1" data-i18n="table.options">Options</th>
							<th class="row1" data-i18n="table.os_name">OS Name</th>
							<th class="row1" data-i18n="table.version">Version</th>
							<th class="row1" data-i18n="table.description">Description</th>
						</tr>
					</thead>
					</table>
					<div class="tableDiv" style=" overflow: auto;" id="editOSContentDiv">
						<table class="tableDisplay" width="100%" cellpadding="0" cellspacing="0">
                                                    <tbody>
                                                    </tbody>
						</table>
					</div>
						<div id="editOSPaginationDiv">
					</div>
			</div>
		</div>
				<div id="messageSpace"></div>
			    <div id="errorEditOS" class="errorMessage">	</div>
			
	</div>
		
	<script type="text/javascript" src="Scripts/EditOS.js"></script>
</body>
</html>