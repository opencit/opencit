<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title data-i18n="title.host_trust_status">Host Trust Status</title>
</head>
<body>
<div class="container">
        		<div class="mainHeader"><span class="labelPageTitle" id="MainContent_lblPageTitle" data-i18n="header.host_trust_status">Trust Dashboard</span></div> <!-- was: "Trust Status Dash Board" -->
			<a href="#" onclick="fnGetUpdateForAllHosts(this)" data-toggle="tooltip" title="Refresh All Hosts" style="float: right"><span class="glyphicon glyphicon-refresh"></span>Refresh all</a><br>
        		<div class="mainTableDiv" id="mainTrustDetailsDiv">
        		<div id="mainTrustDetailsDivHidden" style="display: none;" class="mainTableDisplayDiv">
        			<div class="tableDisplay">
        				<table width="100%" cellpadding="0" cellspacing="0">
		      				<thead>
        					<tr>
								<th class="row1"></th>
								<th class="row2" data-i18n="table.host_name">Host Name</th>
								<th class="row3">&nbsp;</th>
								<th class="row4">&nbsp;</th>
								<th class="row5" data-i18n="table.asset_tag">Asset Tag Trust</th>
								<!-- Soni_Begin_03/10/2012_added one nbsp in below line for bug 455 -->
								<th class="row6">&nbsp;<span data-i18n="table.bios_trust_status">BIOS Trust</span></th>
								<!--/* Soni_End_03/10/2012_added one nbsp in below line for bug 455 -->
								<th class="row7" data-i18n="table.vmm_trust_status">VMM Trust</th>
								<th class="row8" data-i18n="table.overall_trust_status">Platform Trust</th>								
								<th class="row9" data-i18n="table.updated">Updated On</th>
								<th class="row10" data-i18n="table.trust_status">Trust Status</th>
								<th class="row11" data-i18n="table.trust_assertion">Trust Assertion</th>
								<!--/* Soni_Begin_03/10/2012_added center align in below line for bug 455 -->
								<th class="rowHelp" align="center" data-i18n="table.trust_report">Trust Report</th>
								<!--/* Soni_End_03/10/2012_added one nbsp in below line for bug 455 -->
								<th class="row12" data-i18n="table.status">Status</th>
							</tr>
		      				</thead>
        				</table>
        			</div>
        			<div class="mainTableContant" id="mainTrustDetailsContent">
        				<table width="100%" cellpadding="0" cellspacing="0">
        				</table>
        			</div>
        		</div>
        		<div id="hostTrustPaginationDiv"></div>
        		<div id="errorMessage" style="padding-top: 20px;text-align: left;"></div>
        		</div>
                        <span id="alert_updating_cert" data-i18n="alert.updating_cert" style="display: none;">updating cert now</span>
                        <span id="alert_migrate_vm" data-i18n="alert.migrate_vm" style="display: none;">Are you sure you want to migrate this VM?</span>
        	</div>
        	<script type="text/javascript" src="Scripts/HostTrustStatus.js"></script>
</body>
</html>
