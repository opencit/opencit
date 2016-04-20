$(function() {
	$('#mainLoadingDiv').prepend(disabledDiv);
	sendJSONAjaxRequest(false, 'getData/getAllExpiringApiClients.html', null, fnViewExpiringRequestSuccess, null);
});

function fnViewExpiringRequestSuccess(responseJSON) {
	$('#disabledDiv').remove();
	if (responseJSON.result) {
		var request = responseJSON.expiringApiClients;
            
                if (request.length == 0) {
                    $('#successMessage').html('<span data-i18n="label.expiring_api_time_start">No Api Client keys are expiring in the next </span>' + responseJSON.expirationMonths + '<span data-i18n="label.expiring_api_time_end"> month(s).</span>');
                    return;
                } 
            
		$('#registerHostTable').show();
		var str="";
		for ( var item in request) {
			var classValue = null;
			if(item % 2 === 0){classValue='oddRow';}else{classValue='evenRow';}
			str+='<tr class="'+classValue+'" expireDate="'+request[item].expiresOn+'">'+
					'<td class="deleteRequestRow1" name="name">'+request[item].name+'</td>'+
					'<td class="deleteRequestRow2" name="requestedRoles">'+request[item].requestedRoles.toString()+'</td>'+
					'<td class="deleteRequestRow3" name="expireDate">'+fnGetFormatedDate(request[item].expires)+'</td>'+
					'<td class="deleteRequestRow4"><input type="button" value="Extend" onclick="fnGetRekeyApiClient(this)"></td>'+
				'</tr>';
		}
		$('#viewExpiringTableContent').html(str);
	}else {
		$('#successMessage').html('<span class="errorMessage">'+responseJSON.message+'</span>');
	}
}

function fnGetRekeyApiClient(element) {
    alert($("#alert_reregister_with_api").text());
    /*
	var data = [];
	var row = $(element).parent().parent();
	$(row).find("td:not(:last-child)").each(function(){
        var val = $.trim($(this).text());
        var name = $(this).attr('name');
        data[name]=val;
    });
	setLoadImage('mainLoadingDiv', '40px', '510px');
	sendHTMLAjaxRequest(false, 'getView/getRekeyApiClient.html', null, fnRekeyApiClientDataPolulate, null,'mainLoadingDiv',data);
        */
}

function fnRekeyApiClientDataPolulate(response,elementIDToBePublised,data) {
	$('#'+elementIDToBePublised).html(response);
	$('#mainRegisterHost_IP_ADDRESS').val(data.ipAddress);
	$('#mainRegisterHost_Identity').val(data.identity);
	
	var roles = data.requestedRoles.split(',');
	var str="";
	for ( var items in roles) {
		str+='<input type="checkbox" role="'+roles[items]+'"><span class="requestedRolesDispaly" id="mainRegisterHost_'+roles[items]+'">'+roles[items]+'</span>';
	}
	$('#mainRegisterHost_Roles').html('<div>'+str+'</div>');
	$('#mainRegisterHost_login').blur(function() {
		fnValidateEmptyValue($(this).attr('id'));
	});
	$('#mainRegisterHost_password').blur(function() {
		fnValidateEmptyValue($(this).attr('id'));
	});
	
}

function fnReKeySelectedRequest() {
	var valid1 = false;	
	var valid2 = false;	
	valid1 = fnValidateEmptyValue('mainRegisterHost_login');
	valid2 =	fnValidateEmptyValue('mainRegisterHost_password');
	if (valid1 && valid2) {
		var data = fnGetReKeyData();
		$('#mainLoadingDiv').prepend(disabledDiv);
		sendJSONAjaxRequest(false, 'getData/reKeySelectedRequest.html', "requestVO="+data+"&loginName="+$('#mainRegisterHost_login').val()+"&pass="+$('#mainRegisterHost_password').val(), reKeySelectedRequestSuccess, null);
	}
}

function fnGetReKeyData() {
	var vo = new RegistrationDetailsVo();
	vo.ipAddress = $('#mainRegisterHost_IP_ADDRESS').val();
	vo.identity = $('#mainRegisterHost_Identity').val();
	var roles = [];
	$('#mainRegisterHost_Roles').find('input:checked').each(function() {
		roles.push($(this).attr('role'));
	});
	vo.requestedRoles = roles;
	vo.comments = null;
	vo.expiresOn = null;
	return $.toJSON(vo);
}

function reKeySelectedRequestSuccess() {
	alert($("#alert_request_extended").text());
	getViewExpiringPage();
}