
package gov.niarl.his.webservices.hisPrivacyCAWebService2.server.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "identityRequestGetChallengeResponse", namespace = "http://server.hisPrivacyCAWebService2.webservices.his.niarl.gov/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "identityRequestGetChallengeResponse", namespace = "http://server.hisPrivacyCAWebService2.webservices.his.niarl.gov/")
public class IdentityRequestGetChallengeResponse {

    @XmlElement(name = "identityRequestChallenge", namespace = "")
    private gov.niarl.his.webservices.hisPrivacyCAWebService2.server.ByteArray identityRequestChallenge;

    /**
     * 
     * @return
     *     returns ByteArray
     */
    public gov.niarl.his.webservices.hisPrivacyCAWebService2.server.ByteArray getIdentityRequestChallenge() {
        return this.identityRequestChallenge;
    }

    /**
     * 
     * @param identityRequestChallenge
     *     the value for the identityRequestChallenge property
     */
    public void setIdentityRequestChallenge(gov.niarl.his.webservices.hisPrivacyCAWebService2.server.ByteArray identityRequestChallenge) {
        this.identityRequestChallenge = identityRequestChallenge;
    }

}
