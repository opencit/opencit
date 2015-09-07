
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loMLEe.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title data-i18n="title.edit_host">Edit Host</title>
</head>
<body>
<div class="container">
		<div class="nagPanel"><span data-i18n="title.host_management">Host Management</span> &gt; <span data-i18n="title.edit_host">Edit Host</span> </div> <!-- was: "Update/Delete an existing host" -->
		<div id="mainAddHostContainer">
		<div id="mainEditHostDivHidden" style="display: none;" class="mainTableDisplayDiv">
			<table class="tableDisplay" width="100%" cellpadding="0" cellspacing="0">
				<thead>
					<tr>
						<th class="editRow1" data-i18n="table.host_name">Host Name</th>
						<!-- <th class="editRow2">Host IP Address</th> -->
						<th class="editRow3" data-i18n="table.host_port">Host Port</th>
						<th class="editRow10" data-i18n="table.host_description">Host Description</th>
                                                <th class="editRow4" data-i18n="table.bios_name">BIOS Name</th>
						<th class="editRow5" data-i18n="table.bios_build">BIOS Build</th>
						<th class="editRow6" data-i18n="table.vmm_name">VMM Name</th>
						<th class="editRow7" data-i18n="table.vmm_build">VMM Build</th>
						<th class="editRow8" data-i18n="table.email">Email Address</th>
						<th class="editRow9" data-i18n="table.connection_details">Connection Details</th>
						<th class="editRow0">Options</th>
					</tr>
				</thead>
				</table>
				<div class="tableContentStyle" id="mainEditHostDetailsContent">
					<table class="tableDisplay" width="100%" cellpadding="0" cellspacing="0">
						<tbody>
						</tbody> 
					</table>
				</div>
				<div id="editHostPaginationDiv"></div>
		</div>
			<div id="mleMessage" class="errorDiv"></div>
		</div>
		<div id="dialog-confirm" title="Delete Host?" style="display:none;">
                	<p>Are you sure you want to delete this host?</p>
        	</div>
	</div>
	<script type="text/javascript" src="Scripts/editHost.js"></script>
</body>
</html>
