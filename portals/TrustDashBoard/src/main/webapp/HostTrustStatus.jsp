<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Host Trust Status</title>
</head>
<body>
<div class="container">
        		<div class="mainHeader"><span class="labelPageTitle" id="MainContent_lblPageTitle">Trust Status Dash Board</span></div>
        		<div class="mainTableDiv" id="mainTrustDetailsDiv">
        		<div id="mainTrustDetailsDivHidden" style="display: none;" class="mainTableDisplayDiv">
        			<div class="tableDisplay">
        				<table width="100%" cellpadding="0" cellspacing="0">
		      				<thead>
        					<tr>
								<th class="row1"></th>
								<th class="row2">Host Name</th>
								<th class="row3">&nbsp;</th>
								<th class="row4">&nbsp;</th>
								<th class="row5">Location</th>
								<!-- Soni_Begin_03/10/2012_added one nbsp in below line for bug 455 -->
								<th class="row6">&nbsp;BIOS Trust Status</th>
								<!--/* Soni_End_03/10/2012_added one nbsp in below line for bug 455 -->
								<th class="row7">VMM Trust Status</th>
								<th class="row8">Overall Trust Status</th>								
								<th class="row9">Updated On</th>
								<th class="row10">Trust Status</th>
								<th class="row11">Trust Assertion</th>
								<!--/* Soni_Begin_03/10/2012_added center align in below line for bug 455 -->
								<th class="rowHelp" align="center">Trust Report</th>
								<!--/* Soni_End_03/10/2012_added one nbsp in below line for bug 455 -->
								<th class="row12">Status</th>
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
        		
        	</div>
        	<script type="text/javascript" src="Scripts/HostTrustStatus.js"></script>
</body>
</html>