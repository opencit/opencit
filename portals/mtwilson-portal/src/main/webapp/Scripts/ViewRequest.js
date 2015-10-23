$(function() {
    $('#mainLoadingDiv').prepend(disabledDiv);
    sendJSONAjaxRequest(false, 'getData/getAllApprovedRequests.html', null, fnViewRequestSuccess, null);
});

function fnViewRequestSuccess(responseJSON) {
    $('#disabledDiv').remove();
    if (responseJSON.result) {
        $('#viewRequestTable').show();
        var str = "";
        var request = responseJSON.approvedRequests;
        for (var item in request) {
            var classValue = null;
            if (item % 2 === 0) {
                classValue = 'oddRow';
            } else {
                classValue = 'evenRow';
            }
            var roles = "";
            for (var role in request[item].roles) {
                roles += '<span data-i18n="label.role_' + request[item].roles[role].toLowerCase() + '" data-status="' + request[item].roles[role] + '">' + request[item].roles[role] + '</span>, ';
            }
            roles = roles.replace(/,\s*$/, "");
            str += '<tr class="' + classValue + '">' +
                    '<td class="viewRow1" title="' + request[item].fingerprint + '" name="name">' + request[item].name + '</td>' +
                    '<td class="viewRow2" name="status" data-status="' + request[item].status + '" data-i18n="table.' +
                            request[item].status.toLowerCase() + '">' + request[item].status + '</td>' +
                    '<td class="viewRow3">' + roles + '</td>' +
                    '<td class="viewRow4" name="expires">' + fnGetFormatedDate(request[item].expires) + '</td>';
            var comment = request[item].comments == undefined || request[item].comments == null || request[item].comments == "" ? "&nbsp;" : request[item].comments;
            str += '<td class="viewRow5" name="expires">' + comment + '</td>';
	    str += '<td><a href="#" onclick="fnDeleteSelectedRequest(this)" data-toggle="tooltip" title="Delete"><span class="glyphicon glyphicon-trash"></span></a></td>';
	    str += '</tr>';
        }
        $('#viewRequestTableContent').html(str);
    } else {
        $('#successMessage').html('<span class="errorMessage">' + responseJSON.message + '</span>');
    }
}

function fnDeleteSelectedRequest(element) {
        $('#successMessage').html('');
        var row = $(element).parent().parent();
        var data="fingerprint="+$(row).find("td:eq(0)").attr('title');
	$("#dialog-confirm").dialog("open");
	//$("#dialog-confirm").dialog("option", "title", "Confirm Delete");
	//$("#dialog-confirm").html($("#alert_delete_request").text());
	
	// Define the Dialog and its properties.
	$("#dialog-confirm").dialog({
		resizable: false,
		modal: true,
		height: 250,
		width: 400,
		buttons: {
			"Delete": function () {
				$(this).dialog('close');
				$('#mainLoadingDiv').prepend(disabledDiv);
				sendJSONAjaxRequest(false, 'getData/deleteSelectedRequest.html', data, deleteSelectedRequestSuccess, null,element);
			},
				"Cancel": function () {
				$(this).dialog('close');
			}
		}
	});

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
