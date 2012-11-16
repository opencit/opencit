
package gov.niarl.his.webservices.hisPrivacyCAWebService2.server.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "identityRequestGetChallenge", namespace = "http://server.hisPrivacyCAWebService2.webservices.his.niarl.gov/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "identityRequestGetChallenge", namespace = "http://server.hisPrivacyCAWebService2.webservices.his.niarl.gov/", propOrder = {
    "identityRequest",
    "endorsementCertificate"
})
public class IdentityRequestGetChallenge {

    @XmlElement(name = "identityRequest", namespace = "")
    private gov.niarl.his.webservices.hisPrivacyCAWebService2.server.ByteArray identityRequest;
    @XmlElement(name = "endorsementCertificate", namespace = "")
    private gov.niarl.his.webservices.hisPrivacyCAWebService2.server.ByteArray endorsementCertificate;

    /**
     * 
     * @return
     *     returns ByteArray
     */
    public gov.niarl.his.webservices.hisPrivacyCAWebService2.server.ByteArray getIdentityRequest() {
        return this.identityRequest;
    }

    /**
     * 
     * @param identityRequest
     *     the value for the identityRequest property
     */
    public void setIdentityRequest(gov.niarl.his.webservices.hisPrivacyCAWebService2.server.ByteArray identityRequest) {
        this.identityRequest = identityRequest;
    }

    /**
     * 
     * @return
     *     returns ByteArray
     */
    public gov.niarl.his.webservices.hisPrivacyCAWebService2.server.ByteArray getEndorsementCertificate() {
        return this.endorsementCertificate;
    }

    /**
     * 
     * @param endorsementCertificate
     *     the value for the endorsementCertificate property
     */
    public void setEndorsementCertificate(gov.niarl.his.webservices.hisPrivacyCAWebService2.server.ByteArray endorsementCertificate) {
        this.endorsementCertificate = endorsementCertificate;
    }

}
