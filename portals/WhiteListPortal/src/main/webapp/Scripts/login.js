/*
 * This File is used for Login.jsp
 */

//This function will called on load of page.
$(function() {
	
	//put pointer in username field on load of page.
	$('#userNameValue').focus();
	
	//changing default functionality of form submit. Adding validation for username and password before it submit the form.
	$('#loginForm').submit(function(){
		var valid1 = true;
		var valid2 = true;
		$('.validationErrorDiv').each(function() {
			$(this).remove();
		});
		valid1 = validateValue('userNameValue');
		valid2 = validateValue('passwordValue');
		if (!valid1 || !valid2) {
			return false;
		}
	});
});

//This function is called when user will click on register link. it will send request to get Register.jsp page.
function getRegisterUserPage(){
    $('#mainContainer').prepend(disabledDiv);
    sendHTMLAjaxRequest(true, "getView/getRegisterPage.htm", null,registerUserPageSuccess , null);
}

//This function will put Register.jsp into body of html page.
function registerUserPageSuccess(responseHTML){
    $('#mainContainer').parent().html(responseHTML);
}