function open_in_same_tab(url )
{
  var win=window.open(url, '_blank');
  win.focus();
}

function getAuthenticationStatus(){
    
    var l = window.location;
    var base_url = l.protocol + "//" + l.host + "/" + l.pathname.split('/')[1];
    base_url = base_url.replace("mtwilson-portal","");
    open_in_same_tab(base_url + "mtwilson/v2/authentication-status");
}