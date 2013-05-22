/*
 * 2012, U.S. Government, National Security Agency, National Information Assurance Research Laboratory
 * 
 * This is a work of the UNITED STATES GOVERNMENT and is not subject to copyright protection in the United States. Foreign copyrights may apply.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * �Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * �Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * �Neither the name of the NATIONAL SECURITY AGENCY/NATIONAL INFORMATION ASSURANCE RESEARCH LABORATORY nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package gov.niarl.his.webservices.hisPrivacyCAWebService2.client;

import gov.niarl.his.webservices.hisPrivacyCAWebService2.IHisPrivacyCAWebService2;
import gov.niarl.his.webservices.hisPrivacyCAWebServices2.clientWsImport.ByteArray;
import gov.niarl.his.webservices.hisPrivacyCAWebServices2.clientWsImport.HisPrivacyCAWebService2;
import gov.niarl.his.webservices.hisPrivacyCAWebServices2.clientWsImport.HisPrivacyCAWebService2FactoryService;
import gov.niarl.his.webservices.hisPrivacyCAWebServices2.clientWsImport.HisPrivacyCAWebService2FactoryServiceService;
import gov.niarl.his.webservices.hisPrivacyCAWebServices2.clientWsImport.HisPrivacyCAWebService2Service;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;

public class HisPrivacyCAWebServices2ClientInvoker {

	public static IHisPrivacyCAWebService2 getHisPrivacyCAWebService2(String url) {
		try {
            System.err.println("getHisPrivacyCAWebService2 trying to create service factory!");
			HisPrivacyCAWebService2FactoryServiceService hisPrivacyCAWebService2FactoryServiceService = new HisPrivacyCAWebService2FactoryServiceService(new URL(url + "/hisPrivacyCAWebService2FactoryService?wsdl"), new QName("http://server.hisPrivacyCAWebService2.webservices.his.niarl.gov/", "HisPrivacyCAWebService2FactoryServiceService"));
            System.err.println("getHisPrivacyCAWebService2 created it!");
			HisPrivacyCAWebService2FactoryService hisPrivacyCAWebService2FactoryService = hisPrivacyCAWebService2FactoryServiceService.getHisPrivacyCAWebService2FactoryServicePort();
			HisPrivacyCAWebService2Service hisPrivacyCAWebService2Service = new HisPrivacyCAWebService2Service(new URL(url + "/hisPrivacyCAWebService2?wsdl"), new QName("http://server.hisPrivacyCAWebService2.webservices.his.niarl.gov/", "HisPrivacyCAWebService2Service"));
			return new HisPrivacyCAWebServices2ClientImpl(hisPrivacyCAWebService2Service.getPort(hisPrivacyCAWebService2FactoryService.getHisPrivacyCAWebService2(), HisPrivacyCAWebService2.class));
		} catch (MalformedURLException e) {
            System.err.println("getHisPrivacyCAWebService2 exception was " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}

class HisPrivacyCAWebServices2ClientImpl implements IHisPrivacyCAWebService2 {

	HisPrivacyCAWebService2 hisPrivacyCAWebService2;

	public HisPrivacyCAWebServices2ClientImpl(HisPrivacyCAWebService2 hisPrivacyCAWebService2) {
		this.hisPrivacyCAWebService2 = hisPrivacyCAWebService2;
	}

	public byte[] identityRequestGetChallenge(byte[] identityRequest, byte[] endorsementCertificate) {
		ByteArray identityRequestByteArray = new ByteArray();
		identityRequestByteArray.setBytes(identityRequest);
		ByteArray endorsementCertificateByteArray = new ByteArray();
		endorsementCertificateByteArray.setBytes(endorsementCertificate);
		return hisPrivacyCAWebService2.identityRequestGetChallenge(identityRequestByteArray, endorsementCertificateByteArray).getBytes();
	}

	public byte[] identityRequestSubmitResponse(byte[] identityRequestResponseToChallenge) {
		ByteArray identityRequestResponseToChallengeByteArray = new ByteArray();
		identityRequestResponseToChallengeByteArray.setBytes(identityRequestResponseToChallenge);
		return hisPrivacyCAWebService2.identityRequestSubmitResponse(identityRequestResponseToChallengeByteArray).getBytes();
	}

}