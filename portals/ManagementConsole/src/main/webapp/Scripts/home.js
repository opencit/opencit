/**
 * @author Yuvraj Singh
 */

$(function() {
	createMenubar("menubarItems");
	getApproveRequestPage();
});

/**
 * function to get different Pages from server.
 */
function getWhiteListConfigurationPage() {
	$('#mainContainer').html('<div id="WhiteListConfigurationPage"></div>');
	setLoadImage('WhiteListConfigurationPage', '40px', '500px');
	sendHTMLAjaxRequest(false, 'getView/getWhiteListConfigurationPage.html', null, fnDisplayContent, null,'WhiteListConfigurationPage');
}
function getRegisterHostPage() {
	$('#mainContainer').html('<div id="RegisterHostPage"></div>');
	setLoadImage('RegisterHostPage', '40px', '500px');
	sendHTMLAjaxRequest(false, 'getView/getRegisterHostPage.html', null, fnDisplayContent, null,'RegisterHostPage');
}
function getApproveRequestPage() {
	$('#mainContainer').html('<div id="ApproveRequestPage"></div>');
	setLoadImage('ApproveRequestPage', '40px', '500px');
	sendHTMLAjaxRequest(false, 'getView/getApproveRequestPage.html', null, fnDisplayContent, null,'ApproveRequestPage');
}

function getViewExpiringPage() {
	$('#mainContainer').html('<div id="viewExpiringPage"></div>');
	setLoadImage('viewExpiringPage', '40px', '500px');
	sendHTMLAjaxRequest(false, 'getView/getViewExpiringPage.html', null, fnDisplayContent, null,'viewExpiringPage');
}
function getDeletePendingRegistration() {
	$('#mainContainer').html('<div id="deletePendingRegistration"></div>');
	setLoadImage('deletePendingRegistration', '40px', '500px');
	sendHTMLAjaxRequest(false, 'getView/getDeleteRegistrationPage.html', null, fnDisplayContent, null,'deletePendingRegistration');
}
function getViewRequest() {
	$('#mainContainer').html('<div id="getViewRequest"></div>');
	setLoadImage('getViewRequest', '40px', '500px');
	sendHTMLAjaxRequest(false, 'getView/getViewRequestPage.html', null, fnDisplayContent, null,'getViewRequest');
}

/*--Begin Added by Soni on 18/10/12 for New Screen for CA */
function getCAStatus() {
	$('#mainContainer').html('<div id="getCAStatus"></div>');
	setLoadImage('getCAStatus', '40px', '500px');
	sendHTMLAjaxRequest(false, 'getView/getCAStatusPage.html', null, fnDisplayContent, null,'getCAStatus');
}
/*--End Added by Soni on 18/10/12 for New Screen for CA */
/*--Begin Added by Soni on 18/10/12 for New Screen for SAML downlaod */
function downloadSAML() {
	$('#mainContainer').html('<div id="getSAML"></div>');
		setLoadImage('getSAML', '40px', '500px');
	sendHTMLAjaxRequest(false, 'getView/getSAMLCertificatePage.html', null, fnDisplayContent, null,'getSAML');
}

/*--Begin Added by Soni on 18/10/12 for New Screen for SAML downlaod */
function fnDisplayContent(response,elementIDToBePublised) {
	$('#'+elementIDToBePublised).html(response);
}
/*--End Added by Soni on 18/10/12 for New Screen for SAML downlaod */
function logoutUser() {
	setLoadImage('mainContainer', '40px', '510px');
	sendHTMLAjaxRequest(false, 'getData/logOutUser.html', null, displayLogingPage, null);
}

function displayLogingPage(responseHTML) {
	$('#mainContainer').parent().html(responseHTML);
}