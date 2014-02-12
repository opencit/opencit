<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title data-i18n="title.ca_status">CA Status</title>
</head>
<body>
<div class="container">
		<div class="nagPanel"><span data-i18n="title.cert_auth">Certificate Authority</span> > <span data-i18n="title.ca_status">CA Status</span></div>
		<div id="nameOfPage" class="NameHeader" data-i18n="header.ca_status">View CA Status</div>
		<div id="mainLoadingDiv" class="mainContainer">
			<div id="caStatusTable" class="registerHostTable" style="display: none;">
				<table cellpadding="0" cellspacing="0" width="100%" class="tableHeader">
					<tr>
						<td class="viewRow1" data-i18n="table.name">Name</td>
						<!--  <td class="viewRow2">Fingerprint</td>-->
						<td class="viewRow3" data-i18n="table.download">Download</td>
						<td class="viewRow4" data-i18n="table.status">Status</td>
						<td class="viewRow4" data-i18n="table.expires">Expires On</td>
						<td class="viewRow5Header" data-i18n="table.comments">Comments</td>
					</tr>
				</table>
				<div class="requestDetailsTableContent">
					<table width="100%" cellpadding="0" cellspacing="0" id="caStatusContent">
						
					</table>
				</div>
			</div>
			<br>
			<div id="successMessage"></div>
			
		</div>
	</div>
	<script type="text/javascript" src="Scripts/CAStatus.js"></script>

</body>
</html>