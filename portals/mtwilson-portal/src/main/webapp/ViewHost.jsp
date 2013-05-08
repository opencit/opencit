<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loMLEe.dtd">
<html>
<body>
<div class="container">
		<div class="nagPanel">View an existing host </div>
		<div id="mainViewHostDetailsDiv">
		<div id="mainViewHostDivHidden" style="display: none;" class="mainTableDisplayDiv">
			<table class="tableDisplay" width="100%" cellpadding="0" cellspacing="0">
				<thead>
					<tr>
						<th class="viewRow1">Host Name</th>
						<th class="viewRow2">Host IP Address</th>
						<th class="viewRow3">Host Port</th>
						<th class="viewRow4">BIOS Name</th>
						<th class="viewRow5">BIOS Build</th>
						<th class="viewRow6">VMM Name</th>
						<th class="viewRow7">VMM Build</th>
						<th class="viewRow8">Email Address</th>
						<th class="viewRow9">VCenter Details</th>
						<th class="viewRow10">Host Description</th>
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