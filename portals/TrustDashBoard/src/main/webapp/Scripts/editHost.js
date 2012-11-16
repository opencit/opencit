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
		var classValue = null;
		if(item % 2 === 0){classValue='evenRow';}else{classValue='oddRow';}
                var vCenterDetails = hostDetails[item].vCenterDetails;
                if( hostDetails[item].hostPort ==0){ hostDetails[item].hostPort='';}
		str+='<tr class="'+classValue+'">'+
			'<td hostID="'+hostDetails[item].hostId+'" class="editRow0"><a href="javascript:;" onclick="fnEditHostInfo(this)"> Edit </a><span> | </span><a href="javascript:;" onclick="fnDeleteHostInfo(this)"> Delete </a></td>'+
			'<td class="editRow1">'+hostDetails[item].hostName+'</td>'+
			'<td class="editRow2">'+hostDetails[item].hostIPAddress+'</td>'+
			'<td class="editRow3">'+hostDetails[item].hostPort+'</td>'+
			'<td class="editRow10">'+hostDetails[item].hostDescription+'&nbsp;</td>'+
			'<td class="editRow4">'+hostDetails[item].biosName+'</td>'+
			'<td class="editRow5">'+hostDetails[item].biosBuildNo+'&nbsp;</td>'+
			'<td class="editRow6">'+hostDetails[item].vmmName+'&nbsp;</td>'+
			'<td class="editRow7">'+hostDetails[item].vmmBuildNo+'&nbsp;</td>'+
			'<td class="editRow8">'+hostDetails[item].emailAddress+'&nbsp;</td>'+
			'<td class="editRow9">'+vCenterDetails+'&nbsp;</td>'+
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