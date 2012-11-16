$(function() {
	$('#mainLoadingDiv').prepend(disabledDiv);
	sendJSONAjaxRequest(false, 'getData/getViewMle.html', null, fnUpdateViewMleTable, null);
});


function fnUpdateViewMleTable(responseJson) {
	$('#disabledDiv').remove();
	if (responseJson.result) {
		$('#viewMleMainDataDisplay').show();
		fuCreateViewMleTable(responseJson.MLEDataVo);
		applyPagination('viewMlePaginationDiv',responseJson.noOfPages,fngetMleNextPage,1);
	}else {
		$('#viewMleError').html(responseJson.message);
	}
}


/*function to get the value for give page no*/
function fngetMleNextPage(pageNo) {
	$('#viewMleMainDataDisplay').prepend(disabledDiv);
	sendJSONAjaxRequest(false, 'getData/getViewMleForPageNo.html', "pageNo="+pageNo, fnUpdateViewMleTableForPage, null);
}

function fnUpdateViewMleTableForPage(responseJSON) {
	$('#disabledDiv').remove();
	if (responseJSON.result) {
		fuCreateViewMleTable(responseJSON.MLEDataVo);
	}else {
		$('#viewMleError').html(responseJSON.message);
	}
}

function fuCreateViewMleTable(mleData){
	var str = "";
	$('#viewMleContentDiv table tbody').html("");
	for ( var items in mleData) {
		var classValue = null; 
		if(items % 2 === 0){classValue='oddRow';}else{classValue='evenRow';}
		str+='<tr class="'+classValue+'">'+
		'<td class="rowv" style="word-wrap: break-word;max-width:140px;">'+mleData[items].mleName+'</td>'+
		'<td class="row3">'+mleData[items].mleVersion+'</td>'+
		'<td class="row1">'+mleData[items].attestation_Type+'</td>'+
		'<td class="row4">'+mleData[items].mleType+'</td>';
		var val1 = mleData[items].osName == undefined ? ' ' : mleData[items].osName;
		var val2 = mleData[items].osVersion == undefined ? ' ' : mleData[items].osVersion;
		str+='<td class="row4">'+val1 +' '+val2+'&nbsp;</td>';
		val1 = mleData[items].oemName == undefined ? ' ' : mleData[items].oemName;
		str+='<td class="row4">'+val1+'&nbsp;</td>';
		val1 = mleData[items].mleDescription == undefined ? ' ' : mleData[items].mleDescription;
		str+='<td class="row4"  style="word-wrap: break-word;max-width:80px;">'+val1+'&nbsp;</td></tr>';
	}
	$('#viewMleContentDiv table tbody').html(str);
}