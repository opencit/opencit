<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>View Api Clients Page</title>
</head>
<body>
<div class="container">
		<div class="nagPanel">Administration &gt; View User</div>
		<div id="nameOfPage" class="NameHeader">View Registered Api Client</div>
		<div id="mainLoadingDiv" class="mainContainer">
			<div id="viewRequestTable" class="registerHostTable" style="display: none;">
				<table cellpadding="0" cellspacing="0" width="100%" class="tableHeader">
					<tr>
						<td class="viewRow1">Name</td>
						<td class="viewRow2">Status</td>
						<td class="viewRow3">Requested Roles</td>
						<td class="viewRow4">Expires On</td>
						<td class="viewRow5Header">Comments</td>
					</tr>
				</table>
				<div class="requestDetailsTableContent">
					<table width="100%" cellpadding="0" cellspacing="0" id="viewRequestTableContent">
						
					</table>
				</div>
			</div>
			<br>
			<div id="successMessage"></div>
			
		</div>
	</div>
	<script type="text/javascript" src="Scripts/ViewRequest.js"></script>
</body>
</html>