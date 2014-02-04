<?php
require_once 'MtWilson.php';
$api = new MtWilson("https://mtwilson.example.com:8181");
// Secure (you must download the SSL certificate first):
$api->setTrustedCertificatePath("/usr/local/share/mtwilson/certs");
// Insecure:
//$api->setRequireTrustedCertificate(false);
//$api->setVerifyHostname(false);

// Most installations will not need a proxy
if( false ) {
	$api->setProxy("http://proxy.server.net:3128");
}


$result = $api->getHostTrustStatus('myhostname');

?>

Host: <?= $result->hostname ?>

Trusted BIOS? <?= $result->trust->bios ? "Yes" : "No" ?>

Trusted VMM? <?= $result->trust->vmm ? "Yes" : "No" ?>

Trusted Location? <?= $result->trust->location ? "Yes" : "No" ?>

Trusted Overall? <?= $result->trust->bios && $result->trust->vmm ? "Yes" : "No" ?> (not considering location)
