var apiClientList = [];
var roleList = [];
var expire_date;

$(function() {
	$('#mainLoadingDiv').prepend(disabledDiv);
	sendJSONAjaxRequest(false, 'getData/getAllPendingRegistrationRequest.html', null, fnRetriveHostSuccess, null);
});

function fnRetriveHostSuccess(responseJSON) {
	$('#disabledDiv').remove();
	if (responseJSON.result) {
		var request = responseJSON.pendingRequest;
                if (request.length == 0) {
                    $('#successMessage').html('<span data-i18n="label.no_pending_api_requests">No requests are currently pending to be reviewed.</span>');
                    return;
                } 

		$('#approveRegisterHostTable').show();
		var str="";
                // Add the list of all the roles to the global variable
                roleList = responseJSON.allRoles;
                
		for ( var item in request) {
                        apiClientList[request[item].fingerprint] = request[item];
			var classValue = null;
			if(item % 2 === 0){classValue='oddRow';}else{classValue='evenRow';}
                        var roles = "";
                        for (var role in request[item].requestedRoles) {
                            roles += '<span data-i18n="label.role_' + request[item].requestedRoles[role].toLowerCase() + '" data-status="' + request[item].requestedRoles[role] + '">' + request[item].requestedRoles[role] + '</span>, ';
                        }
                        roles = roles.replace(/,\s*$/, "");
			str+='<tr class="'+classValue+'">'+
					'<td class="viewRequestRow1" name="name" fingerprint="'+request[item].fingerprint+'">'+request[item].name+'</td>'+
					'<td class="viewRequestRow2" name="requestedRoles">'+roles+'</td>'+
					'<td class="viewRequestRow3"><input type="button" value="Details" onclick="fnGetRequestDetails(this)" data-i18n="[value]button.details"></td>'+
				'</tr>';
		}
		$('#approveRegisterHostTableContent').html(str);
	}else {
		$('#successMessage').html('<span class="errorMessage">'+ getHTMLEscapedMessage(responseJSON.message) +'</span>');
	}
}

function fnGetRequestDetails(element) {
	var data = [];
	var row = $(element).parent().parent();
        var fp = $(row).find("td:eq(0)").attr("fingerprint");
        data = apiClientList[fp];
        //alert(clientName);
	setLoadImage('mainLoadingDiv', '40px', '510px');
	sendHTMLAjaxRequest(false, 'getView/getApproveRejectPage.html', null, fnApproveRequestDataPolulate, null,'mainLoadingDiv',data);
}

// the administrator must explicitly approve all roles, so instead of populate any defaults requested by the user,
// we only highlight the roles requested by the user and the administrator must still check each of the boxes.
function fnApproveRequestDataPolulate(response,elementIDToBePublised,data) {
        //alert("Coming into the function");
	$('#'+elementIDToBePublised).html(response);
	$('#mainApiClient_Name').val(data.name);
	$('#mainApiClient_Finger_Print').val(data.fingerprint);
	$('#mainApiClient_Issuer').val(data.issuer);
        
	var apiRoles = data.requestedRoles; //.toString().split(',');
	var str='<span style="font-weight: bold; color: #555555; font-size: 0.9em" data-i18n="label.mtw_1x_roles">Mt Wilson 1.x roles for backward compatibility:</span><br>';
        for (var globalRole in roleList) {
            var index = findIndex(roleList[globalRole], apiRoles);
            if (roleList[globalRole] == 'Administrator')
                str+='<br><br><span style="font-weight: bold; color: #555555; font-size: 0.9em" data-i18n="label.mtw_2x_roles">Mt Wilson 2.x roles:</span><br>';
            if (index != -1) {
                str+='<input type="checkbox" role="'+ roleList[globalRole] +'"><span class="requestedRolesDispaly requestedRoleHighlight" id="mainApiClient_'+ roleList[globalRole] +'" data-i18n="label.role_' + roleList[globalRole].toLowerCase() + '">'+ roleList[globalRole] +'</span>';               
            } else {
                str+='<input type="checkbox" role="'+ roleList[globalRole] +'"><span class="requestedRolesDispaly" id="mainApiClient_'+ roleList[globalRole] +'" data-i18n="label.role_' + roleList[globalRole].toLowerCase() + '">'+ roleList[globalRole] +'</span>';
            }
        }
	/*for ( var items in apiRoles) {
		str+='<input type="checkbox" role="'+apiRoles[items]+'" checked="checked"><span class="requestedRolesDispaly" id="mainApiClient_'+apiRoles[items]+'">'+apiRoles[items]+'</span>';
	}*/
	
	$('#mainApiClient_Roles').html('<div>'+str+'</div>');
        $('#mainApiClient_Expires').val(new Date(data.expires).toISOString());
        expire_date = data.expires;
        $('#mainApiClient_Comments').val(data.comments);
}

//Add by Soni on 4th oct for bug 462
function fnApproveSelectedRequest() {
	var vo = fnGetRequestVOForApprovalOrReject();
    if( vo.requestedRoles.length === 0 ) {
        $('#approveRejectButtonFeedback').html('<span style="color:red">At least one role must be granted</span>');
        return false;
    }
    var data = $.toJSON(vo);
	if($('#mainApiClient_Comments').val()){
	   if(!fnvalidateComments('mainApiClient_Comments'))
	   { $('#approveRejectButtonFeedback').html('<span style="color:red">Invalid Characters "<" or ">"in the Comment field.</span>');
	   return false;
	   }}
	
	$('#mainLoadingDiv').prepend(disabledDiv);
	sendJSONAjaxRequest(false, 'getData/approveSelectedRequest.html', "requestVO="+data, approveSelectedRequestSuccess, null);
	
}

function fnGetRequestVOForApprovalOrReject() {
	var vo = new RegistrationDetailsVo();
	vo.name = $('#mainApiClient_Name').val();
	vo.fingerprint = $('#mainApiClient_Finger_Print').val();
	var roles = [];
	$('#mainApiClient_Roles').find('input:checked').each(function() {
		roles.push($(this).attr('role'));
	});
	vo.requestedRoles = roles;
        vo.expires = expire_date;
	//vo.expires= $('#mainApiClient_Expires').val();
	vo.comments= $('#mainApiClient_Comments').val();
	//alert(vo.comments);
	
	//return $.toJSON(vo);
    return vo;
}

function approveSelectedRequestSuccess(responseJSON) {
        if(responseJSON.result == false)
            alert(responseJSON.message);
        else
            alert($("#alert_request_approved").text());
	getApproveRequestPage();
}

function fnRejectSelectedRequest() {
	var vo = fnGetRequestVOForApprovalOrReject();
    var data = $.toJSON(vo);
	$('#mainLoadingDiv').prepend(disabledDiv);
	sendJSONAjaxRequest(false, 'getData/rejectSelectedRequest.html', "requestVO="+data, rejectSelectedRequestSuccess, null);
}

function rejectSelectedRequestSuccess() {
	alert($("#alert_request_rejected").text());
	getApproveRequestPage();
}

function findIndex(item, arr) {
    var index;
    var arrSize = arr.length;
    if (arrSize == 0) 
        return -1; // since there are no roles requested by the user
    for (var i = 0; i < arrSize; i++) {
        index = (item == arr[i]) ? i : -1;
        if (-1 != index) break;
    }
    return index;
}

function showDialogRoleHelp() {
        var str = '<div class="helpDiv" data-i18n="[html]help.role_description_help"></div>';
	fnOpenDialog(str, "Help", 500, 350,false);
}
