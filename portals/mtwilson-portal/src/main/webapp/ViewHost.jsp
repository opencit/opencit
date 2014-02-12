<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loMLEe.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title data-i18n="title.view_host">View Host</title>
</head>
<body>
<div class="container">
		<div class="nagPanel"><span data-i18n="title.host_management">Host Management</span> &gt; <span data-i18n="title.view_host">View Host</span></div>
		<div id="mainViewHostDetailsDiv">
		<div id="mainViewHostDivHidden" style="display: none;" class="mainTableDisplayDiv">
			<table class="tableDisplay" id="viewHostTable" width="100%" cellpadding="0" cellspacing="0">
				<thead>
					<tr>
						<th class="vh_viewRow1" data-i18n="table.host_name">Host Name</th>
						<!-- <th class="vh_viewRow2">Host IP Address</th> -->
						<th class="vh_viewRow3" data-i18n="table.host_port">Host Port</th>
						<th class="vh_viewRow4" data-i18n="table.bios_name">BIOS Name</th>
						<th class="vh_viewRow5" data-i18n="table.bios_build">BIOS Build</th>
						<th class="vh_viewRow6" data-i18n="table.vmm_name">VMM Name</th>
						<th class="vh_viewRow7" data-i18n="table.vmm_build">VMM Build</th>
						<th class="vh_viewRow8" data-i18n="table.email">Email Address</th>
						<th class="vh_viewRow9" data-i18n="table.connection_details">Connection Details</th>
						<th class="vh_viewRow10" data-i18n="table.host_description">Host Description</th>
					</tr>
				</thead>
				</table>
				<div class="tableContentStyle" id="mainHostDetailsContent">
					<table class="tableDisplay" width="100%" cellpadding="0" cellspacing="0">
						<tbody>
						</tbody>
					</table>
				</div>
				<div id="viewHostPaginationDiv"></div>
			</div>
        	<div id="errorMessage" style="padding-top: 20px;text-align: left;"></div>
		</div>
		
	</div>
	
	<script type="text/javascript" src="Scripts/viewHost.js"></script>
</body>
</html>