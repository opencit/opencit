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
            str += '<td class="viewRow5" name="expires">' + comment + '</td>' +
                    '</tr>';
        }
        $('#viewRequestTableContent').html(str);
    } else {
        $('#successMessage').html('<span class="errorMessage">' + responseJSON.message + '</span>');
    }
}