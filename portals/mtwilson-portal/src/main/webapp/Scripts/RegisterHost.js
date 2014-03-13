$(function() {
    /*$('#vmwareHostType').find('input:text').each(function() {
     $(this).blur(function() {
     fnValidateEmptyValue($(this).attr('id'));
     });
     });*/

//	$('#mainRegisterHost_ClusterName').blur(function() {
//                        // Since Cluster name can have any special characters, we are not going to validate it. We will just check for empty value
//		fnMWValidateField('mainRegisterHost_ClusterName',true);
//	});

    $('#mainRegisterHost_vCenterServer').blur(function() {
        fnMWValidateIpAddressOrHostName('mainRegisterHost_vCenterServer', true);
    });

    $('#mainRegisterHost_loginID').blur(function() {
        fnMWValidateField('mainRegisterHost_loginID', true);
    });

    $('#mainRegisterHost_password').blur(function() {
        fnMWValidateField('mainRegisterHost_password', true);
    });

    fnChangehostType($('#MainContent_ddlHOSTProvider'), false);
});

function fnUploadFlatFile() {
    $('#registerHostMainContainer').prepend(disabledDiv);
    $('#successMessage').html('');
    $('#registerHostTableContent').html('');
    $('#registerHostTable').hide();
    $.ajaxFileUpload({
        url: 'getData/uploadFlatFileRegisterHost.html',
        secureuri: false,
        fileElementId: 'fileToUpload',
        dataType: 'jsonp',
        data: {
            "AuthorizationToken": authorizationToken // part of fix for issue #1038, see commonUtils.js
        },
        success: function(data, status) {
            fnuploadSuccess(data);
        },
        error: function(data, status, e) {
            alert("data = " + data);
            alert("status = " + status);
            alert("e = " + e);
            $('#successMessage').html('<div class="errorMessage">* File is not processed properly. Please check file format. Use help button to know more.</div>');
        }
    });
}


function fnuploadSuccess(responseHTML) {
    var validResponse = responseHTML;
    var response = $('<div>' + responseHTML + '</div>');
    if ($(response).find('div').html() == null) {
        if ($(response).find('pre').html() != null) {
            validResponse = $(response).find('pre').html();
        }
        validResponse = validResponse.split(':')[1];
        validResponse = validResponse.substring(0, validResponse.length - 1);
    } else {
        validResponse = $.trim($(response).find('.bool').text());
    }

    if (validResponse == 'true') {
        $('#registerHostTableContent').html("");
        sendJSONAjaxRequest(false, 'getData/getUploadedRegisterHostValues.html', null, updateListHostToBeRegister, null);
    } else {
        $('#disabledDiv').remove();
        $('#messageForFileUpload').html('<span class="errorMessage">* File is not processed properly. Please check file format. Use help Button to know More.</span>');
    }
}

function updateListHostToBeRegister(responsJson) {
    $('#disabledDiv').remove();
    $('#messageForFileUpload').html('<span class="successMessage">* File is uploaded Successfully.</span>');
    if (responsJson.result) {
        $('#registerHostTable').show();

        var values = responsJson.hostVO;
        var wlBiosList = responsJson.wlBiosList;
        var wlVMMList = responsJson.wlVMMList;
        for (var val in values) {
            var str = "";
            var classValue = null;
            var portNo = null;
            var vCenter = null;
            var hostNameReplaced = values[val].hostName.replace(/\./g, "_");
            if (val % 2 === 0) {
                classValue = 'oddRow';
            } else {
                classValue = 'evenRow';
            }
            if (values[val].vmWareType) {
                portNo = "";
                vCenter = values[val].vCenterString;
            } else {
                vCenter = "";
                portNo = values[val].hostPortNo;
            }

            str += '<tr class="' + classValue + '" registered="' + values[val].registered + '" isVMMType="' + values[val].vmWareType + '" hostType="' + values[val].hostType + '">' +
                    '<td class="registerHostRow1">' + values[val].hostName + '</td>' +
                    '<td class="registerHostRow2">&nbsp;' + portNo + '</td>' +
                    '<td class="registerHostRow3" vCenterString="' + vCenter + '">' + vCenter.split(';')[0] + '&nbsp;</td>' +
                    '<td class="registerHostRow4"><input id="reg_check_' + hostNameReplaced + '" type="checkbox" checked="checked"></td>' +
                    '<td class="registerHostRow5Sub"><select class="registerHostConfigDropDown" id="biosConfigValue_' + hostNameReplaced + '">';
            for (var iteam in wlBiosList) {
                str += '<option>' + wlBiosList[iteam] + '</option>';
            }
            str += '</select>&nbsp;';
            if (values[val].registered) {
                str += '<a id="editConfig_' + hostNameReplaced + '" class="editConfig_' + hostNameReplaced + '" onclick="fnUpdateregisteredHostConfig(this,\'vmmConfigValue_' + hostNameReplaced + '\')" href="javascript:;">Edit</a>';
            }
            str += '</td>';
            str += '<td class="registerHostRow5Sub"><select class="registerHostConfigDropDown" id="vmmConfigValue_' + hostNameReplaced + '">';
            for (var iteam in wlVMMList) {
                str += '<option>' + wlVMMList[iteam] + '</option>';
            }
            str += '</select>&nbsp;';
            if (values[val].registered) {
                str += '<a id="editConfig_' + hostNameReplaced + '" class="editConfig_' + hostNameReplaced + '" onclick="fnUpdateregisteredHostConfig(this,\'biosConfigValue_' + hostNameReplaced + '\')" href="javascript:;">Edit</a>';
            }
            str += '</td>';
            str += '<td class="registerHostRow6"><textarea id="status_' + hostNameReplaced + '" class="textAreaBoxClass" cols="16" rows="1" readonly="readonly"></textarea></td>' +
                    '</tr>';

            $('#registerHostTableContent').append(str);

            if (responsJson.SpecificHostValue != responsJson["biosConfigValue_" + values[val].hostName]) {
                $('#biosConfigValue_' + hostNameReplaced).find('option').each(function() {
                    if ($(this).text() == responsJson.SpecificHostValue) {
                        $(this).remove();
                    }
                });
            }
            if (responsJson.SpecificHostValue != responsJson["vmmConfigValue_" + values[val].hostName]) {
                $('#vmmConfigValue_' + hostNameReplaced).find('option').each(function() {
                    if ($(this).text() == responsJson.SpecificHostValue) {
                        $(this).remove();
                    }
                });
            }

            if (values[val].registered) {
                $('#reg_check_' + hostNameReplaced).attr('checked', false);
                $('#reg_check_' + hostNameReplaced).attr("disabled", "disabled");
                $('#biosConfigValue_' + hostNameReplaced).find('option').each(function() {
                    if ($(this).text() == responsJson["biosConfigValue_" + values[val].hostName]) {
                        $(this).attr("selected", "selected");
                    }
                });
                $('#vmmConfigValue_' + hostNameReplaced).find('option').each(function() {
                    if ($(this).text() == responsJson["vmmConfigValue_" + values[val].hostName]) {
                        $(this).attr("selected", "selected");
                    }
                });
                $('#biosConfigValue_' + hostNameReplaced).attr("disabled", "disabled");
                $('#vmmConfigValue_' + hostNameReplaced).attr("disabled", "disabled");
                $('#status_' + hostNameReplaced).val(values[val].status);

            } else {
                $('.editConfig_' + hostNameReplaced).attr("disabled", "disabled");
            }
        }
    }
}

function fnUpdateregisteredHostConfig(element, nextDropDownID) {
    $(element).text("Cancel");
    $(element).attr("onclick", "fnCancelButtonHostConfig(this,\"" + nextDropDownID + "\")");
    $(element).parent().find('select').removeAttr("disabled");
    $(element).parent().parent().find('input:checkbox').removeAttr("disabled");
    $(element).parent().parent().find('input:checkbox').attr("checked", "checked");
}

function fnCancelButtonHostConfig(element, nextDropDownID) {
    $(element).text("Edit");
    $(element).attr("onclick", "fnUpdateregisteredHostConfig(this,\"" + nextDropDownID + "\")");
    $(element).parent().find('select').attr("disabled", "disabled");
    if ($('#' + nextDropDownID).attr('disabled') == 'disabled') {
        $(element).parent().parent().find('input:checkbox').attr("checked", false);
        $(element).parent().parent().find('input:checkbox').attr("disabled", "disabled");
    }
}

function showDialogUpFlatFileHelp() {
    var str = "";
    for (var iteam in uploadFileHelp) {
        str += '<div class="helpDiv">' + uploadFileHelp[iteam] + '</div>';
    }
    fnOpenDialog(str, "Upload Flat File Help", 500, 275, false);
}

function fnRetrieveDatacenters() {
    cleanUpAllDivs();
    $('#MainContent_ddlDatacenterName').empty();
    $('#MainContent_ddlDatacenterName').prop('disabled', true);
    $('#MainContent_ddlClusterName').empty();
    $('#MainContent_ddlClusterName').prop('disabled', true);
    $('#retriveHostButton').prop('disabled', true);

    var valid = true;
    if (fnValidateIpAddress($('#mainRegisterHost_vCenterServer').val())) {
        $('#vmwareHostType').find('input:text').each(function() {
            if (!fnValidateEmptyValue($(this).attr('id'))) {
                valid = false;
            }
        });
        if (!fnValidateEmptyValue('mainRegisterHost_password')) {
            valid = false;
        }
        if (valid) {
            var data = "&vCentertConnection=" + getVCenterServerAddress('mainRegisterHost_vCenterServer') + ";" + $('#mainRegisterHost_loginID').val() + ";" + $('#mainRegisterHost_password').val();
            $('#mainLoadingDiv').prepend(disabledDiv);
            sendJSONAjaxRequest(false, 'getData/retrieveDatacenters.html', data, fnRetrieveDatacentersSuccess, null);
        }
    } else {
        alert("Please enter a valid hostname or ip address and try again.");
    }
}

function fnRetrieveDatacentersSuccess(responseJSON) {
    $('#disabledDiv').remove();
    var dcList = [];

    if (responseJSON.result) {
        dcList = responseJSON.datacenters.split(",");
        for (var i = 0; i < dcList.length; i++) {
            $('#MainContent_ddlDatacenterName').append($('<option>', {value: 1, text: dcList[i]}));
        }
        $('#MainContent_ddlDatacenterName').prop('disabled', false);
        fnRetrieveClusters();
    } else {
        $('#successMessage').html('<span class="errorMessage">Server Error : ' + responseJSON.message + '</span>');
    }
}

function fnRetrieveClusters() {
    cleanUpAllDivs();
    $('#MainContent_ddlClusterName').empty();
    $('#MainContent_ddlClusterName').prop('disabled', true);
    $('#retriveHostButton').prop('disabled', true);

    var valid = true;
    if (fnValidateIpAddress($('#mainRegisterHost_vCenterServer').val())) {
        $('#vmwareHostType').find('input:text').each(function() {
            if (!fnValidateEmptyValue($(this).attr('id'))) {
                valid = false;
            }
        });
        if (!fnValidateEmptyValue('mainRegisterHost_password')) {
            valid = false;
        }
        if (valid) {
            var data = "&vCentertConnection=" + getVCenterServerAddress('mainRegisterHost_vCenterServer') + ";" + $('#mainRegisterHost_loginID').val() + ";" + $('#mainRegisterHost_password').val();
            $('#mainLoadingDiv').prepend(disabledDiv);
            sendJSONAjaxRequest(false, 'getData/retrieveAllClusters.html', data, fnRetrieveClustersSuccess, null);
        }
    } else {
        alert("Please enter a valid hostname or ip address and try again.");
    }
}

function fnRetrieveClustersSuccess(responseJSON) {
    $('#disabledDiv').remove();
    var clusterList = [];

    if (responseJSON.result) {
        clusterList = responseJSON.clusters.split(",");
        for (var i = 0; i < clusterList.length; i++) {
            $('#MainContent_ddlClusterName').append($('<option>', {value: 1, text: clusterList[i]}));
        }
        $('#MainContent_ddlClusterName').prop('disabled', false);
        $('#retriveHostButton').prop('disabled', false);
    } else {
        $('#successMessage').html('<span class="errorMessage">Server Error : ' + responseJSON.message + '</span>');
    }
}

function fnRetriveHostFromCluster() {
    cleanUpAllDivs();
    var valid = true;
    if (fnValidateIpAddress($('#mainRegisterHost_vCenterServer').val())) {
        $('#vmwareHostType').find('input:text').each(function() {
            if (!fnValidateEmptyValue($(this).attr('id'))) {
                valid = false;
            }
        });
        if (!fnValidateEmptyValue('mainRegisterHost_password')) {
            valid = false;
        }
        if (valid) {
            var data = "clusterName=" + $('#MainContent_ddlClusterName option:selected').text() + "&vCentertConnection=" + getVCenterServerAddress('mainRegisterHost_vCenterServer') + ";" + $('#mainRegisterHost_loginID').val() + ";" + $('#mainRegisterHost_password').val();
            $('#mainLoadingDiv').prepend(disabledDiv);
            sendJSONAjaxRequest(false, 'getData/retriveHostFromCluster.html', data, fnRetriveHostSuccess, null);
        }
    } else {
        alert("Please enter a valid hostname or ip address and try again.");
    }
}

function fnRetriveHostSuccess(responsJSON) {
    $('#disabledDiv').remove();
    if (responsJSON.result) {
        updateListHostToBeRegister(responsJSON);
    } else {
        $('#MainContent_ddlClusterName').empty();
        $('#MainContent_ddlClusterName').prop('disabled', true);
        $('#retriveHostButton').prop('disabled', true);
        $('#successMessage').html('<span class="errorMessage">Server Error : ' + responsJSON.message + '</span>');
    }
}

function fnRegisterMultipleHost() {
    $('#mainLoadingDiv').prepend(disabledDiv);
    $('#successMessage').html('');
    var listOfHost = [];
    var checked = false;
    $('#registerHostTableContent tr').each(function() {
        var row = $(this);
        var checkBoxValue = $(row).find('td:eq(3)').find('input:checkbox').attr('checked');
        if (checkBoxValue == "checked") {
            checked = true;
            var host = new RegisterHostVo();
            host.hostType = $(row).attr('hostType');
            host.status = null;
            host.hostName = $.trim($(row).find('td:eq(0)').text());
            if ($(row).attr('isvmmtype') == 'false') {
                host.hostPortNo = $.trim($(row).find('td:eq(1)').text());
                host.vmWareType = false;
                host.vCenterString = null;
            } else {
                host.vCenterString = $(row).find('td:eq(2)').attr('vcenterstring');
                host.vmWareType = true;
                host.hostPortNo = null;
            }
            host.biosWLTarget = $(row).find('td:eq(4)').find('select').val();
            host.vmmWLtarget = $(row).find('td:eq(5)').find('select').val();
            host.registered = $(row).attr("registered") == "true" ? true : false;
            listOfHost.push(host);
        }
    });
    if (!checked) {
        $('#successMessage').html('<span class="errorMessage">* Please select atleast one host to be registered.</span>');
    }
    // Earlier we used to make host registration calls for each of the selected hosts individually. Now that we have the multi host registration API, we are using the same.
    var data = "hostToBeRegister=" + $.toJSON(listOfHost);
    sendJSONAjaxRequest(false, 'getData/registerMultipleHost.html', data, fnRegisterMultipleHostSuccess, null);
}


function fnRegisterMultipleHostSuccess(responseJSON) {
    // alert( $.toJSON(responseJSON.hostVOs.hostRecords));
    if (responseJSON.hostVOs.hostRecords) {
        var values = responseJSON.hostVOs.hostRecords;
        for (var val in values) {
            // alert(values[val].hostName);
            var hostname = values[val].hostName;
            var hoststatus = values[val].status;
            var hosterrormessage = values[val].errorMessage;
            $('#registerHostTableContent tr').each(function() {
                var row = $(this);
                if ($(row).find('td:eq(0)').text() == hostname) {
                    if (hoststatus == "true") {
                        $(row).find('td:eq(6)').find('textarea').val("Successfully registered/updated the host.");
                        $(row).find('td:eq(3)').find('input').prop('checked', false);
                    }
                    else
                        $(row).find('td:eq(6)').find('textarea').val(hosterrormessage);
                }
            });
            $('#disabledDiv').remove();
        }
    } else {
        $('#successMessage').html('<span class="errorMessage">' + responseJSON.message + '</span>');
    }
}

//function to show help for VCenter String in registerHost page
function showHelpForVCenterServer() {
    var str = "";
    for (var iteam in vCenterStringHelp) {
        str += '<div class="helpDiv">' + vCenterStringHelp[iteam] + '</div>';
    }
    fnOpenDialog(str, "Help", 500, 285, false);
}

//Function to check all checkbox in table.
function fnSelectAllCheckBox(status) {
    $('.registerHostTableContent table tr td').each(function() {
        $(this).find(':checkbox').attr('checked', status);
    });
}