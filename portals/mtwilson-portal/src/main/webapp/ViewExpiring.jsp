<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loMLEe.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Expiring Api Clients Page</title>
</head>
<body>
<div class="container">
		<div class="nagPanel">API Client > Extend</div>
		<div id="nameOfPage" class="NameHeader">Expiring Api Clients</div>
		<div id="mainLoadingDiv" class="mainContainer">
			<div id="registerHostTable" class="registerHostTable" style="display: none;">
				<table cellpadding="0" cellspacing="0" width="100%" class="tableHeader">
					<tr>
						<td class="deleteRequestRow1">Name</td>
						<td class="deleteRequestRow2">Requested Roles</td>
						<td class="deleteRequestRow3">Expires</td>
						<td class="deleteRequestRow4Header">Options</td>
					</tr>
				</table>
				<div class="requestDetailsTableContent">
					<table width="100%" cellpadding="0" cellspacing="0" id="viewExpiringTableContent">
						
					</table>
				</div>
			</div>
			<br>
			<div id="successMessage"></div>
			
		</div>
	</div>
	<script type="text/javascript" src="Scripts/ViewExpiring.js"></script>
</body>
</html>