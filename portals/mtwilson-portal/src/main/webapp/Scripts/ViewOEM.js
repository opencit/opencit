$(function() {
	$('#ViewOEMDisplayDiv').prepend(disabledDiv);
	sendJSONAjaxRequest(false, 'getData/getAllOEMList.html', null, fnUpdateViewOEMTable, null);
});

function fnUpdateViewOEMTable(responseJson) {
	$('#disabledDiv').remove();
	if (responseJson.result) {
		$('#viewOEMMainDataDisplay').show();
		fuCreateViewOEMTable(responseJson.OEMDataVo)
		applyPagination('viewOEMPaginationDiv',responseJson.noOfPages,fngetOEMNextPage,1);
	}else {
		$('#viewOEMError').html(responseJson.message);
	}
}

function fuCreateViewOEMTable(mleData){
	var str = "";
	$('#viewOEMContentDiv table tbody').html("");
	for ( var items in mleData) {
		var classValue = null; 
		if(items % 2 === 0){classValue='oddRow';}else{classValue='evenRow';}
		str+='<tr class="'+classValue+'">'+
		'<td class="row2" id="osName">'+mleData[items].oemName+'</td>'+
		'<td class="row3" id="osDec">'+mleData[items].oemDescription+'&nbsp;</td></tr>';
	}
	$('#viewOEMContentDiv table tbody').html(str);
}

/*function to get the value for give page no*/
function fngetOEMNextPage(pageNo) {
	$('#viewOEMMainDataDisplay').prepend(disabledDiv);
	sendJSONAjaxRequest(false, 'getData/getViewOEMForPageNo.html', "pageNo="+pageNo, fnUpdateViewOEMTableForPage, null);
}

function fnUpdateViewOEMTableForPage(responseJSON) {
	$('#disabledDiv').remove();
	if (responseJSON.result) {
		fuCreateViewOEMTable(responseJSON.OEMDataVo);
	}else {
		$('#viewOEMError').html(responseJSON.message);
	}
}