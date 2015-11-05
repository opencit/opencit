function getAuthenticationStatus(){
    
    var l = window.location;
    var base_url = l.protocol + "//" + l.host + "/" + l.pathname.split('/')[1];
    base_url = base_url.replace("mtwilson-portal","");
    open_in_new_tab(base_url + "mtwilson-portal/v2proxy/ca-certificates/tls");

    
}