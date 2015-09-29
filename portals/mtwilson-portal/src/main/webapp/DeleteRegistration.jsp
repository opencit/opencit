<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title data-i18n="title.delete_registration">Delete User</title>
</head>
<body>
<div class="container">
		<div class="nagPanel"><span data-i18n="title.administration">Administration</span> &gt; <span data-i18n="title.delete_registration">Delete User</span></div>
		<div id="nameOfPage" class="NameHeader" data-i18n="header.delete_registration">Delete Currently Registered Api Clients</div>
		<div id="mainLoadingDiv" class="mainContainer">
			<div id="registerHostTable" class="registerHostTable" style="display: none;">
				<table cellpadding="0" cellspacing="0" width="100%" class="tableHeader">
					<tr>
						<td class="deleteRequestRow1" data-i18n="table.name">Name</td>
						<td class="deleteRequestRow2" data-i18n="table.roles">Roles</td>
						<td class="deleteRequestRow3" data-i18n="table.expires">Expires</td>
						<td class="deleteRequestRow4Header" data-i18n="table.options">Options</td>
					</tr>
				</table>
				<div class="requestDetailsTableContent">
					<table width="100%" cellpadding="0" cellspacing="0" id="deleteRegisterHostTableContent">
						
					</table>
				</div>
			</div>
			<br>
			<div id="successMessage"></div>
			
		</div>
                <span id="alert_delete_request" data-i18n="alert.delete_request" style="display: none;">Are you sure you want to delete this request?</span>
	</div>
	<script type="text/javascript" src="Scripts/DeleteRegistration.js"></script>
</body>
</html>
