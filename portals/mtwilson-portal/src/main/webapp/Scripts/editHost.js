$(function() {
	$('#mainAddHostContainer').prepend(disabledDiv);
	sendJSONAjaxRequest(false, 'getData/getAllHostForView.html', null, populateEditHostDetails, null);
});

function populateEditHostDetails(responseJSON) {
	$('#disabledDiv').remove();
	if (responseJSON.result) {
		$('#mainEditHostDivHidden').show();
		populateEditHostDataIntoTable(responseJSON.hostVo);
		applyPagination('editHostPaginationDiv',responseJSON.noOfPages,fngetEditHostNextPage,1);
	}else {
		$('#mleMessage').html('<span class="errorMessage">'+getHTMLEscapedMessage(responseJSON.message)+'</span>');
	}
}

function populateEditHostDataIntoTable(hostDetails) {
	var str = "";
	for ( var item in hostDetails) {
                if(item != parseInt(item)) {
                        continue;
                }
		var classValue = null;
		if(item % 2 === 0){classValue='evenRow';}else{classValue='oddRow';}
                var vCenterDetails = hostDetails[item].vCenterDetails;
                if(( hostDetails[item].hostPort == 0) || (hostDetails[item].hostPort == '')){ hostDetails[item].hostPort= '';}
		str+='<tr class="'+classValue+'">'+
			'<td hostID="' + getHTMLEscapedMessage(hostDetails[item].hostId) +'" class="editRow0"><a href="javascript:;" onclick="fnEditHostInfo(this)" data-i18n="link.edit"> Edit </a><span> | </span><a href="javascript:;" onclick="fnDeleteHostInfo(this)" data-i18n="link.delete"> Delete </a></td>'+
			'<td class="editRow1">' + getHTMLEscapedMessage(hostDetails[item].hostName) + '</td>'+
			//'<td class="editRow2">'+hostDetails[item].hostIPAddress+'&nbsp;</td>'+
			'<td class="editRow3">'+ getHTMLEscapedMessage(hostDetails[item].hostPort) + '&nbsp;</td>'+
			'<td class="editRow10">'+ getHTMLEscapedMessage(hostDetails[item].hostDescription) + '&nbsp;</td>'+
			'<td class="editRow4">'+ getHTMLEscapedMessage(hostDetails[item].biosName) + '&nbsp;</td>'+
			'<td class="editRow5">'+ getHTMLEscapedMessage(hostDetails[item].biosBuildNo) +'&nbsp;</td>'+
			'<td class="editRow6">'+ getHTMLEscapedMessage(hostDetails[item].vmmName) +'&nbsp;</td>'+
			'<td class="editRow7">'+ getHTMLEscapedMessage(hostDetails[item].vmmBuildNo) +'&nbsp;</td>'+
			'<td class="editRow8">'+ getHTMLEscapedMessage(hostDetails[item].emailAddress) +'&nbsp;</td>'+
			'<td class="editRow9">'+ getHTMLEscapedMessage(vCenterDetails) +'&nbsp;</td>'+
		'</tr>';
	}
	$('#mainEditHostDetailsContent table').html(str);
}

function fngetEditHostNextPage(pageNo) {
	$('#errorMessage').html('');
	$('#mainAddHostContainer').prepend(disabledDiv);
	sendJSONAjaxRequest(false, 'getData/getHostForViewForPage.html', "pageNo="+pageNo, fnUpdateEditTableForPage, null);
}

function fnUpdateEditTableForPage(responseJSON) {
	$('#disabledDiv').remove();
	if (responseJSON.result) {
		populateEditHostDataIntoTable(responseJSON.hostVo);
	}else {
		$('#mleMessage').html(getHTMLEscapedMessage(responseJSON.message));
	}
}
