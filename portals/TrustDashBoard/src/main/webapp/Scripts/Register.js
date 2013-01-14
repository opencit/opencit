/*var userNameRegEx = new RegExp(/^[a-zA-Z0-9]$/);//{4,10}
*/var normalReg = new RegExp(/^[a-zA-Z0-9_. -]+$/);

$(function() {
	$('#userNameValue').focus();
});

function registerUser(){
    
    var valid2 = true;
    var valid3 = true;
    $('.validationErrorDiv').each(function() {
            $(this).remove();
    });
    
    // Bug#514: We need to clear the previous error message if it exists
    $('#errorMessage').html('<div class="errorMessage">'+""+'</div>');
    
    if (!(validateValue('userNameValue'))) {
		return false;
	}
    
    if (!normalReg.test($('#userNameValue').val())) {
        $('#userNameValue').parent().parent().find('td:eq(2)').append(validationSpecialDiv);
        return false;
	}
	
	if ($('#userNameValue').val().length < 6) {
	    $('#userNameValue').parent().parent().find('td:eq(2)').append('<span class="errorMessage validationErrorDiv">User Name must be more than 6 characters.</span>');
	    return false;
	}
	 /* Soni_Begin_21/09/2012_issue_Bug_#398_RC2 -management console allows password on new user to be less than 6 chars   */  
    /*Adding Validation for password not be less than 6 characters*/
	if  ($('#passwordValue').val().length<6){
		 $('#passwordValue').parent().parent().find('td:eq(2)').append('<span class="errorMessage validationErrorDiv">Password must be 6 or more characters.</span>');
		 return false;
		}
	/* Soni_End_21/09/2012_issue_Bug_#398_RC2 -management console allows password on new user to be less than 6 chars   */
    
    valid2 = validateValue('passwordValue');
    valid3 = validateValue('confirmPasswordValue');
    if (!valid2 || !valid3) {
            return false;
    }
    
    if($('#confirmPasswordValue').val() != $('#passwordValue').val()){
       $('#confirmPasswordValue').parent().parent().find('td:eq(2)').append('<div class="errorMessage validationErrorDiv">Passwords do not match.</div>');
		return false; 
    }
    
    var data = "userNameTXT="+$.trim($('#userNameValue').val())+"&passwordTXT="+$.trim($('#passwordValue').val());
    $('#mainContainer').prepend(disabledDiv);
    sendJSONAjaxRequest(false, "RegisterUser.htm", data, registerUserSuccess, null);
    
}
/* Soni_Begin_20/09/2012_issue_Bug_#397_RC2 - Unable to create a new user in the Trust Dash board  */
/*modified function name to function  name called on registration success*/
function registerUserSuccess(responseJSON){
	/* Soni_End_20/09/2012_issue_Bug_#397_RC2 - Unable to create a new user in the Trust Dash board  */
    $('#disabledDiv').remove();
    if(responseJSON.result){
    	/* Soni_Begin_18/09/2012_issue_RC2: Change the text after successful user registration_Bug#392  */
        alert("User is successfully registered. Contact administrator for access approval before accessing the portal.");
        /* Soni_End_18/09/2012_issue_RC2: Change the text after successful user registration_Bug#392  */
        window.location.href = "login.htm";
    }else{
        $('#errorMessage').html('<div class="errorMessage">'+getHTMLEscapedMessage(responseJSON.message)+'</div>');
    }
}