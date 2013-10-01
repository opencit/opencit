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
			str+='<tr class="'+classValue+'" fingerprint="'+request[item].fingerprint+'">'+
					'<td class="deleteRequestRow1" name="name">'+request[item].name+'</td>'+
					'<td class="deleteRequestRow2" name="requestedRoles">'+request[item].requestedRoles.toString()+'</td>'+
					'<td class="deleteRequestRow3" name="expires">'+fnGetFormatedDate(request[item].expires)+'</td>'+
					'<td class="deleteRequestRow4"><input type="button" value="Delete" onclick="fnDeleteSelectedRequest(this)"></td>'+
				'</tr>';
		}
		$('#deleteRegisterHostTableContent').html(str);
	}else {
		$('#successMessage').html('<span class="errorMessage">'+responseJSON.message+'</span>');
	}
}

function fnDeleteSelectedRequest(element) {
        $('#successMessage').html('');
	var row = $(element).parent().parent();
	var data="fingerprint="+$(row).attr('fingerprint');
        if (confirm("Are you sure you want to delete this Request ?")) {
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
            $('#successMessage').html('<div class="errorMessage">'+responsJSON.message+'</div>');
        }
        
}