<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%
response.setContentType("application/octet-stream");
response.setHeader("Content-Disposition",
"attachment;filename=mtwilson-rootCA");
%>
<html>
<head>
    <meta http-equiv="Content-Type" content="application/octet-stream; charset=ISO-8859-1">
    <meta http-equiv="AuthorizationToken" value="<%=request.getAttribute("AuthorizationToken")%>"/>
    <title data-i18n="title.cert_download">Download Certificate</title>
</head>
<body onload="fnLookforRootCACertificate()">
<div class="container">
		<div class="nagPanel"><span data-i18n="title.administration">Administration</span> &gt; <span data-i18n="title.cert_download">Download Certificates</span></div> <!-- was: "Management Console Certificate &gt; View " -->
		<div id="nameOfPage" class="NameHeader" data-i18n="header.cert_download">Certificate Download</div>
		<div id="mainLoadingDiv" class="mainContainer">                
              
            <div  id ="fdownloadRCA"class="registerUser"><span data-i18n="label.dl_root_ca">Click on Download icon to download Root Ca Certificate</span>
			<input type="image" onclick="fnforRootCACertificate()" src="images/download.jpg"></div>
            <div  id ="fdownload"class="registerUser"><span data-i18n="label.dl_privacy_ca">Click on Download icon to download the Privacy CA Certificates</span>
			<input type="image" onclick="fnforPrivacyCACertificate()" src="images/download.jpg"></div>
            <!-- <div  id ="fdownload"class="registerUser">Click on Download icon to download all trusted Privacy CA Certificates
			<input type="image" onclick="fnforPrivacyCACertificateList()" src="images/download.jpg"></div> -->
            <div  id ="fdownload"class="registerUser"><span data-i18n="label.dl_tls">Click on Download icon to download TLS Certificate</span>
			<input type="image" onclick="fnforTLSCertificate()" src="images/download.jpg"></div>
            <div  id ="fdownload"class="registerUser"><span data-i18n="label.dl_saml">Click on Download icon to download SAML Certificate</span>
			<input type="image" onclick="fnforSAMLCertificate()" src="images/download.jpg"></div>
                
			<div id="successMessage"></div>
			
		</div>
                <span id="alert_no_root_ca" data-i18n="alert.no_root_ca" style="display: none;">This system is not currently configured with a Root CA</span>
	</div>
<script type="text/javascript" src="Scripts/CertDownload.js"></script>
</body>
</html>