<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loMLEe.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Api Client Pending Registration Page</title>
</head>
<body>
<div class="container">
		<div class="nagPanel">Administration &gt; Pending Access Requests</div>
		<div id="nameOfPage" class="NameHeader">View Pending Registrations</div>
		<div id="mainLoadingDiv" class="mainContainer">
			<div id="approveRegisterHostTable" class="registerHostTable" style="display: none;">
				<table cellpadding="0" cellspacing="0" width="100%" class="tableHeader">
					<tr>
						<td class="viewRequestRow1">Name</td>
						<!-- <td class="row3">Finger Print</td> -->
						<td class="viewRequestRow2">Requested Roles</td>
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
	</div>
	<script type="text/javascript" src="Scripts/ApproveRequest.js"></script>
</body>
</html>