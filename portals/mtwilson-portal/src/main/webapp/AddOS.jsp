<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>View OS</title>
</head>
<body>
	<div class="container">
		<div class="nagPanel">OS \> Add OS</div>
		<div id="nameOfPage" class="NameHeader">New OS/Hypervisor Combination</div>
		<div class="tableDiv" style="margin-left: 61px;">
		
		
		<table  cellpadding="3"  cellspacing="3" style="font-size: 14px;margin-top: 10px;border: none;" id="addOSDataTable">
			<tr>
				<td>Name :</td>
				<td><input id="MainContent_tbOSName" type="text" class="textBox_Border" name="osName" /></td>
				<td><span class="requiredField">*</span></td>
			</tr>
			<tr>
				<td>Version :</td>
				<td><input type="text" class="textBox_Border" name="osVersion" id="osVerID"/></td>
				<td><span class="requiredField">*</span></td>
			</tr>
			<tr>
				<td>Description :</td>
				<td><input type="text" class="textBox_Border" name="osDescription" /></td>
				<td></td>
			</tr>
			<tr>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td></td>
				<td><input class="button" type="submit" value="Add" onclick="fnAddNewOS(this)"/><input class="button" type="button" value="Clear" onclick="resetDataTable('addOSDataTable')"/></td>
				<td></td>
			</tr>
		</table>
		<div id="messageSpace"></div>
		
		</div>
	</div>
	<!-- <script type="text/javascript">
		$(function(){
			$('#MainContent_tbOSName').blur(function() {
				fnTestValidation('MainContent_tbOSName',normalReg);
			});
		});
	</script> -->
</body>
</html>