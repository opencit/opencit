<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%
response.setContentType("application/octet-stream ");
response.setHeader("Content-Disposition",
"attachment;filename=mtwilson-rootCA");
%>
<html>
<head>
<meta http-equiv="Content-Type" content="application/octet-stream; charset=ISO-8859-1">

<title>Download Certificate</title>
</head>
<body>
<div class="container">
		<div class="nagPanel">Administration &gt; View Certificates</div> <!-- was: "Management Console Certificate &gt; View " -->
		<div id="nameOfPage" class="NameHeader">Certificate Download</div>
		<div id="mainLoadingDiv" class="mainContainer">
               
			<div  id ="fdownload"class="registerUser">Click on Download icon to download Root Ca Certificate
			<input type="image" onclick="fnforRootCACertificate()" src="images/download.jpg"></div>
            <div  id ="fdownload"class="registerUser">Click on Download icon to download this hosts Privacy CA Certificate
			<input type="image" onclick="fnforPrivacyCACertificate()" src="images/download.jpg"></div>
            <div  id ="fdownload"class="registerUser">Click on Download icon to download all trusted Privacy CA Certificates
			<input type="image" onclick="fnforPrivacyCACertificateList()" src="images/download.jpg"></div>
            <div  id ="fdownload"class="registerUser">Click on Download icon to download TLS Certificate
			<input type="image" onclick="fnforTLSCertificate()" src="images/download.jpg"></div>
            <div  id ="fdownload"class="registerUser">Click on Download icon to download SAML Certificate
			<input type="image" onclick="fnforSAMLCertificate()" src="images/download.jpg"></div>
                        
			<div id="successMessage"></div>
			
		</div>
	</div>
<script type="text/javascript" src="Scripts/CertDownload.js"></script>
</body>
</html>