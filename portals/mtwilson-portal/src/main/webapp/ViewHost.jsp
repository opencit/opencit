<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loMLEe.dtd">
<html>
<body>
<div class="container">
		<div class="nagPanel">Host Management &gt; View an existing host </div>
		<div id="mainViewHostDetailsDiv">
		<div id="mainViewHostDivHidden" style="display: none;" class="mainTableDisplayDiv">
			<table class="tableDisplay" id="viewHostTable" width="100%" cellpadding="0" cellspacing="0">
				<thead>
					<tr>
						<th class="vh_viewRow1">Host Name</th>
						<!-- <th class="vh_viewRow2">Host IP Address</th> -->
						<th class="vh_viewRow3">Host Port</th>
						<th class="vh_viewRow4">BIOS Name</th>
						<th class="vh_viewRow5">BIOS Build</th>
						<th class="vh_viewRow6">VMM Name</th>
						<th class="vh_viewRow7">VMM Build</th>
						<th class="vh_viewRow8">Email Address</th>
						<th class="vh_viewRow9">Connection Details</th>
						<th class="vh_viewRow10">Host Description</th>
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