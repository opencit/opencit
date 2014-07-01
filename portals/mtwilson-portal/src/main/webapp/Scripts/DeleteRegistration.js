$(function() {
	$('#mainLoadingDiv').prepend(disabledDiv);
	sendJSONAjaxRequest(false, 'getData/getApiClientsForDelete.html', null, fnDeleteRegistrationDetailsSuccess, null);
});

function fnDeleteRegistrationDetailsSuccess(responseJSON) {
	$('#disabledDiv').remove();
	if (responseJSON.result) {
		$('#registerHostTable').show();
		var str="";
		var request = responseJSON.apiClientList;
		for ( var item in request) {
			var classValue = null;
			if(item % 2 === 0){classValue='oddRow';}else{classValue='evenRow';}
                        var roles = "";
                        for (var role in request[item].roles) {
                            roles += '<span data-i18n="label.role_' + request[item].roles[role].toLowerCase() + '" data-status="' + request[item].roles[role] + '">' + request[item].roles[role] + '</span>, ';
                        }
                        roles = roles.replace(/,\s*$/, "");
			str+='<tr class="'+classValue+'" fingerprint="'+ escapeForHTMLAttributes(request[item].fingerprint) +'">'+
					'<td class="deleteRequestRow1" name="name">'+ getHTMLEscapedMessage(request[item].name) +'</td>'+
					'<td class="deleteRequestRow2" name="requestedRoles">'+roles+'</td>'+
					'<td class="deleteRequestRow3" name="expires">'+fnGetFormatedDate(request[item].expires)+'</td>'+
					'<td class="deleteRequestRow4"><input type="button" value="Delete" onclick="fnDeleteSelectedRequest(this)" data-i18n="[value]button.delete"></td>'+
				'</tr>';
		}
		$('#deleteRegisterHostTableContent').html(str);
	}else {
		$('#successMessage').html('<span class="errorMessage">'+ getHTMLEscapedMessage(responseJSON.message)+'</span>');
	}
}

function fnDeleteSelectedRequest(element) {
        $('#successMessage').html('');
	var row = $(element).parent().parent();
	var data="fingerprint="+$(row).attr('fingerprint');
        if (confirm($("#alert_delete_request").text())) {
            $('#mainLoadingDiv').prepend(disabledDiv);
            sendJSONAjaxRequest(false, 'getData/deleteSelectedRequest.html', data, deleteSelectedRequestSuccess, null,element);
        }
}

function deleteSelectedRequestSuccess(responsJSON,element) {
	$('#disabledDiv').remove();
        if(responsJSON.result){
            $('#successMessage').html('<div class="successMessage">Request has been successfully deleted.</div>');
            $(element).parent().parent().remove();
        }else{
            $('#successMessage').html('<div class="errorMessage">'+ getHTMLEscapedMessage(responsJSON.message)+'</div>');
        }
        
}
