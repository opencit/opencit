<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loMLEe.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title data-i18n="title.show_reports">Show Reports</title>
</head>
<body>
<div class="container">
		<div class="nagPanel"><span data-i18n="title.show_reports">Host Trust Status Reports</span> ></div>
		<div id="mainConatinerForReportPage">
			<div class="mainTableDisplayDiv" style="display: none;" id="mainTableForReports">
			<table class="tableDisplay" width="100%" cellpadding="0" cellspacing="0">
				<thead>
					<tr>
						<th class="reportViewRow1"><input type="checkbox" checked="checked" onclick="fnSelectAllCheckBox(checked)"></th>
						<th class="reportViewRow2" data-i18n="table.host_name">Host Name</th>
						<th class="reportViewRow3" data-i18n="table.bios_name">BIOS Name</th>
						<th class="reportViewRow4" data-i18n="table.bios_build">BIOS Build</th>
						<th class="reportViewRow5" data-i18n="table.vmm_name">VMM Name</th>
						<th class="reportViewRow6" data-i18n="table.vmm_build">VMM Build</th>
					</tr>
				</thead>
				</table>
				<div class="tableContentStyle hostTableContent" id="mainReportHostDetailsContent">
					<table class="tableDisplay" width="100%" cellpadding="0" cellspacing="0">
						<tbody>
							<%-- <tr style="${rowStyle}">
								<td class="reportViewRow1"><input type="checkbox"></td>
								<td class="reportViewRow2">${HostData.hostName}</td>
								<td class="reportViewRow3">${HostData.biosName}</td>
								<td class="reportViewRow4">${HostData.biosBuildNo}&nbsp;</td>
								<td class="reportViewRow5">${HostData.vmmName}&nbsp;</td>
								<td class="reportViewRow6">${HostData.vmmBuildNo}&nbsp;</td>
							</tr> --%>
						</tbody> 
					</table>
				</div>
				<div id="ReportHostPaginationDiv"></div>
				<div class="getReportButton">
					<input type="button" id="MainContent_BtnGetReport" onclick="fnGetReportUpdateForHost()" value="Get Report" data-i18n="[value]button.get_report">
				</div>
			</div>
			<div id="errorMessage"></div>
		</div>
		
</div>
<script type="text/javascript" src="Scripts/showReports.js"></script>
</body>
</html>