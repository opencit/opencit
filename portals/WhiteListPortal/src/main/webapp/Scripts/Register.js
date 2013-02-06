/**
 * This file is used to register new user.
 */
var normalReg = new RegExp(/^[a-zA-Z0-9_. -]+$/);

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
    
    //checking for empty username.
    if (!(validateValue('userNameValue'))) {
		return false;
	}
    
    //checking for any special character present in username. If yes throw error.
    if (!normalReg.test($('#userNameValue').val())) {
        $('#userNameValue').parent().parent().find('td:eq(2)').append(validationSpecialDiv);
        return false;
	}
	
    //Checking for the length of username not less than 6 character.
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
    
	//checking for empty value for password and confirm password.
    valid2 = validateValue('passwordValue');
    valid3 = validateValue('confirmPasswordValue');
    if (!valid2 || !valid3) {
            return false;
    }
    
    //check for equality for password and confirm password.
    if($('#confirmPasswordValue').val() != $('#passwordValue').val()){
       $('#confirmPasswordValue').parent().parent().find('td:eq(2)').append('<div class="errorMessage validationErrorDiv">Passwords do not match.</div>');
		return false; 
    }
    
    //Sending request to a server for user registration after all validation.
    var data = "userNameTXT="+$.trim($('#userNameValue').val())+"&passwordTXT="+$.trim($('#passwordValue').val());
    $('#mainContainer').prepend(disabledDiv);
    sendJSONAjaxRequest(false, "RegisterUser.htm", data, registerUserSuccess, null);
    
}

//Method called after server responded for user registration. 
function registerUserSuccess(responseJSON){
    $('#disabledDiv').remove();
    if(responseJSON.result){
    	/* Soni_Begin_18/09/2012_issue_RC2: Change the text after successful user registration_Bug#392  */
        alert("User is successfully registered. Contact administrator for access approval before accessing the portal.");
        /* Soni_End_18/09/2012_issue_RC2: Change the text after successful user registration_Bug#392  */
        window.location.href = "login.htm";
    }else{
        var str = responseJSON.message;
        str =str.replace(/\</g, "&lt;");
        str =str.replace(/\>/g, "&gt;");
        $('#errorMessage').html('<div class="errorMessage">'+str+'</div>');
    }
}