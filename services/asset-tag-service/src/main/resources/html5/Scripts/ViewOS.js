$(function() {
	$('#ViewOSDisplayDiv').prepend(disabledDiv);
	sendJSONAjaxRequest(false, 'getData/getAllOSList.html', null, fnUpdateViewOSTable, null);
});

function fnUpdateViewOSTable(responseJson) {
	$('#disabledDiv').remove();
	if (responseJson.result) {
		$('#viewOSMainDataDisplay').show();
		fuCreateViewOSTable(responseJson.OSDataVo)
		applyPagination('viewOSPaginationDiv',responseJson.noOfPages,fngetOSNextPage,1);
	}else {
		$('#viewOSError').html(responseJson.message);
	}
}

function fuCreateViewOSTable(mleData){
	var str = "";
	$('#viewOSContentDiv table tbody').html("");
	for ( var items in mleData) {
		var classValue = null; 
		if(items % 2 === 0){classValue='oddRow';}else{classValue='evenRow';}
		str+='<tr class="'+classValue+'">'+
		'<td class="row2" id="osName">'+mleData[items].osName+'</td>'+
		'<td class="row3" id="osVer">'+mleData[items].osVersion+'</td>';
		var val1 = mleData[items].osDescription == undefined ? ' ' : mleData[items].osDescription;
		str+='<td class="row4" id="osDec">'+val1+'&nbsp;</td></tr>';
	}
	$('#viewOSContentDiv table tbody').html(str);
}

/*function to get the value for give page no*/
function fngetOSNextPage(pageNo) {
	$('#viewOSMainDataDisplay').prepend(disabledDiv);
	sendJSONAjaxRequest(false, 'getData/getViewOSForPageNo.html', "pageNo="+pageNo, fnUpdateViewOSTableForPage, null);
}

function fnUpdateViewOSTableForPage(responseJSON) {
	$('#disabledDiv').remove();
	if (responseJSON.result) {
		fuCreateViewOSTable(responseJSON.OSDataVo);
	}else {
		$('#viewOSError').html(responseJSON.message);
	}
}