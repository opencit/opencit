<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%
response.setContentType("application/octet-stream ");
response.setHeader("Content-Disposition",
"attachment;filename=mtwilson-saml.crt");
%>
<html>
<head>
    <meta http-equiv="Content-Type" content="application/octet-stream; charset=ISO-8859-1">
    <title data-i18n="title.saml_download">Insert title here</title>
</head>
<body>
<div class="container">
		<div class="nagPanel" data-i18n="title.saml_download">SAML Certificate Download</div>
		<div id="nameOfPage" class="NameHeader" data-i18n="header.saml_download">SAML Certificate Download</div>
		<div id="mainLoadingDiv" class="mainContainer">
			<div  id ="fdownload"class="registerUser"><span data-i18n="label.click_download_saml">Click on Download icon to download SAML Certificate</span>
			<input type="image" onclick="fnforSAMLCertificate()" src="images/download.jpg"></div>
		
			<div id="successMessage"></div>
			
		</div>
	</div>
<script type="text/javascript" src="Scripts/SAMLDownload.js"></script>
</body>
</html>

