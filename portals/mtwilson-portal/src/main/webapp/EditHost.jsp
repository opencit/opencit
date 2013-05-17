
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loMLEe.dtd">
<html>
<body>
<div class="container">
		<div class="nagPanel">Host Management &gt; Edit Hosts </div> <!-- was: "Update/Delete an existing host" -->
		<div id="mainAddHostContainer">
		<div id="mainEditHostDivHidden" style="display: none;" class="mainTableDisplayDiv">
			<table class="tableDisplay" width="100%" cellpadding="0" cellspacing="0">
				<thead>
					<tr>
						<th class="editRow0"></th>
						<th class="editRow1">Host Name</th>
						<th class="editRow2">Host IP Address</th>
						<th class="editRow3">Host Port</th>
						<th class="editRow10">Host Description</th>
						<th class="editRow4">BIOS Name</th>
						<th class="editRow5">BIOS Build</th>
						<th class="editRow6">VMM Name</th>
						<th class="editRow7">VMM Build</th>
						<th class="editRow8">Email Address</th>
						<th class="editRow9">Connection Details</th>
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
		
	</div>
	<script type="text/javascript" src="Scripts/editHost.js"></script>
</body>
</html>