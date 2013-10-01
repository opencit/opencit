$(function() {
	$('#mainViewHostDetailsDiv').prepend(disabledDiv);
	sendJSONAjaxRequest(false, 'getData/getAllHostForView.html', null, populateViewHostDetails, null);
});

function populateViewHostDetails(responseJSON) {
	$('#disabledDiv').remove();
	if (responseJSON.result) {
		$('#mainViewHostDivHidden').show();
		populateViewHostDataIntoTable(responseJSON.hostVo);
		applyPagination('viewHostPaginationDiv',responseJSON.noOfPages,fngetViewHostNextPage,1);
	}else {
		$('#errorMessage').html('<span class="errorMessage">'+getHTMLEscapedMessage(responseJSON.message)+'</span>');
	}
}

function populateViewHostDataIntoTable(hostDetails) {
	var str = "";
	for ( var item in hostDetails) {
		var classValue = null;
		if(item % 2 === 0){classValue='evenRow';}else{classValue='oddRow';}
        if(( hostDetails[item].hostPort == 0) || (hostDetails[item].hostPort == '')){ hostDetails[item].hostPort= '';}
		str+='<tr class="'+classValue+'">'+
			'<td class="vh_viewRow1">'+hostDetails[item].hostName+'</td>'+
			//'<td class="vh_viewRow2">'+hostDetails[item].hostIPAddress+'</td>'+
			'<td class="vh_viewRow3">'+hostDetails[item].hostPort+'</td>'+
			'<td class="vh_viewRow4">'+hostDetails[item].biosName+'</td>'+
			'<td class="vh_viewRow5">'+hostDetails[item].biosBuildNo+'&nbsp;</td>'+
			'<td class="vh_viewRow6">'+hostDetails[item].vmmName+'&nbsp;</td>'+
			'<td class="vh_viewRow7">'+hostDetails[item].vmmBuildNo+'&nbsp;</td>'+
			'<td class="vh_viewRow8">'+hostDetails[item].emailAddress+'&nbsp;</td>'+
			'<td class="vh_viewRow9">'+hostDetails[item].vCenterDetails+'&nbsp;</td>'+
			'<td class="vh_viewRow10">'+hostDetails[item].hostDescription+'&nbsp;</td>'+
		'</tr>';
	}
    
	$('#mainHostDetailsContent table').html(str);
}

function fngetViewHostNextPage(pageNo) {
	$('#errorMessage').html('');
	$('#mainViewHostDetailsDiv').prepend(disabledDiv);
	sendJSONAjaxRequest(false, 'getData/getHostForViewForPage.html', "pageNo="+pageNo, fnUpdateViewTableForPage, null);
}

function fnUpdateViewTableForPage(responseJSON) {
	$('#disabledDiv').remove();
	if (responseJSON.result) {
		populateViewHostDataIntoTable(responseJSON.hostVo);
	}else {
		$('#errorMessage').html(getHTMLEscapedMessage(responseJSON.message));
	}
}