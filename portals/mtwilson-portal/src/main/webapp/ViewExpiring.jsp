<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loMLEe.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title data-i18n="title.view_expiring">API Client Extend</title>
</head>
<body>
<div class="container">
		<div class="nagPanel"><span data-i18n="title.view_expiring">API Client Extend</span> ></div>
		<div id="nameOfPage" class="NameHeader" data-i18n="header.view_expiring">Expiring API Clients</div>
		<div id="mainLoadingDiv" class="mainContainer">
			<div id="registerHostTable" class="registerHostTable" style="display: none;">
				<table cellpadding="0" cellspacing="0" width="100%" class="tableHeader">
					<tr>
						<td class="deleteRequestRow1" data-i18n="table.name">Name</td>
						<td class="deleteRequestRow2" data-i18n="table.requested_roles">Requested Roles</td>
						<td class="deleteRequestRow3" data-i18n="table.expires">Expires</td>
						<td class="deleteRequestRow4Header" data-i18n="table.options">Options</td>
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
                <span id="alert_reregister_with_api" data-i18n="alert.reregister_with_api" style="display: none;">Please log into the system on which the client is installed and re-register the client using the API register functionality.</span>
                <span id="alert_request_extended" data-i18n="alert.request_extended" style="display: none;">Request is extended successfully.</span>
	</div>
	<script type="text/javascript" src="Scripts/ViewExpiring.js"></script>
</body>
</html>