<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loMLEe.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title data-i18n="title.approve_request">Pending Access Requests</title>
</head>
<body>
<div class="container">
		<div class="nagPanel"><span data-i18n="title.administration">Administration</span> &gt; <span data-i18n="title.approve_request">Pending Access Requests</span></div>
		<div id="nameOfPage" class="NameHeader" data-i18n="header.approve_request">View Pending Registrations</div>
		<div id="mainLoadingDiv" class="mainContainer">
			<div id="approveRegisterHostTable" class="registerHostTable" style="display: none;">
				<table cellpadding="0" cellspacing="0" width="100%" class="tableHeader">
					<tr>
						<td class="viewRequestRow1" data-i18n="table.name">Name</td>
						<!-- <td class="row3">Finger Print</td> -->
						<td class="viewRequestRow2" data-i18n="table.requested_roles">Requested Roles</td>
						<td class="viewRequestRow3Header"></td>
					</tr>
				</table>
				<div class="requestDetailsTableContent">
					<table width="100%" cellpadding="0" cellspacing="0" id="approveRegisterHostTableContent">
						
					</table>
				</div>
			</div>
			<br>
			<div id="successMessage"></div>
			
		</div>
                <span id="alert_request_approved" data-i18n="alert.request_approved" style="display: none;">Request is approved successfully.</span>
                <span id="alert_request_rejected" data-i18n="alert.request_rejected" style="display: none;">Request is rejected successfully.</span>
	</div>
	<script type="text/javascript" src="Scripts/ApproveRequest.js"></script>
</body>
</html>