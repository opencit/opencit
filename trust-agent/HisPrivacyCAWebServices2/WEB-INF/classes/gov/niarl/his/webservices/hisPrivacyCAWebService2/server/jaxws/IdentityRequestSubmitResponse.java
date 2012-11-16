
package gov.niarl.his.webservices.hisPrivacyCAWebService2.server.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "identityRequestSubmitResponse", namespace = "http://server.hisPrivacyCAWebService2.webservices.his.niarl.gov/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "identityRequestSubmitResponse", namespace = "http://server.hisPrivacyCAWebService2.webservices.his.niarl.gov/")
public class IdentityRequestSubmitResponse {

    @XmlElement(name = "identityRequestResponseToChallenge", namespace = "")
    private gov.niarl.his.webservices.hisPrivacyCAWebService2.server.ByteArray identityRequestResponseToChallenge;

    /**
     * 
     * @return
     *     returns ByteArray
     */
    public gov.niarl.his.webservices.hisPrivacyCAWebService2.server.ByteArray getIdentityRequestResponseToChallenge() {
        return this.identityRequestResponseToChallenge;
    }

    /**
     * 
     * @param identityRequestResponseToChallenge
     *     the value for the identityRequestResponseToChallenge property
     */
    public void setIdentityRequestResponseToChallenge(gov.niarl.his.webservices.hisPrivacyCAWebService2.server.ByteArray identityRequestResponseToChallenge) {
        this.identityRequestResponseToChallenge = identityRequestResponseToChallenge;
    }

}
