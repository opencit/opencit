<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Edit OS</title>
</head>
<body>
	<div class="container">
		<div class="nagPanel">OS \> Edit OS</div>
		<div id="nameOfPage" class="NameHeader">OS/Hypervisor Combination Edit/Delete</div>
		<div id="mainEditOSDisplayDiv">
			<div class="tableDiv" style="margin-left: 61px; display: none;" id="mainEditTable">
				<table class="tableDisplay" width="100%" cellpadding="0" cellspacing="0">
					<thead>
						<tr>
							<th class="row1">Options</th>
							<th class="row2">OS Name</th>
							<th class="row3">Version</th>
							<th class="row4">Description</th>
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