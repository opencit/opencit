/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.attestation.client.jaxrs.Oems;
import com.intel.mtwilson.attestation.client.jaxrs.MleSources;
import com.intel.mtwilson.attestation.client.jaxrs.Oss;
import com.intel.mtwilson.attestation.client.jaxrs.MlePcrs;
import com.intel.mtwilson.attestation.client.jaxrs.Mles;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.rest.v2.model.Mle;
import com.intel.mtwilson.as.rest.v2.model.MleCollection;
import com.intel.mtwilson.as.rest.v2.model.MleFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.MlePcr;
import com.intel.mtwilson.as.rest.v2.model.MlePcrCollection;
import com.intel.mtwilson.as.rest.v2.model.MlePcrFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.MleSource;
import com.intel.mtwilson.as.rest.v2.model.MleSourceCollection;
import com.intel.mtwilson.as.rest.v2.model.MleSourceFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.Oem;
import com.intel.mtwilson.as.rest.v2.model.OemCollection;
import com.intel.mtwilson.as.rest.v2.model.OemFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.Os;
import com.intel.mtwilson.as.rest.v2.model.OsCollection;
import com.intel.mtwilson.as.rest.v2.model.OsFilterCriteria;
import com.intel.mtwilson.datatypes.ManifestData;
import java.util.ArrayList;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class MleTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MleTest.class);
    private static Mles client = null;
    private static Oems oemClient = null;
    private static Oss osClient = null;
    private static MlePcrs pcrClient = null;
    private static MleSources mleSourceClient = null;

    @BeforeClass
    public static void init() throws Exception {
        client = new Mles(My.configuration().getClientProperties());
        oemClient = new Oems(My.configuration().getClientProperties());
        osClient = new Oss(My.configuration().getClientProperties());
        pcrClient = new MlePcrs(My.configuration().getClientProperties());
        mleSourceClient = new MleSources(My.configuration().getClientProperties());
    }

    @Test
    public void testMleOps() {
                
        UUID oemUuid = null, osUuid = null;
        OemFilterCriteria oemCriteria = new OemFilterCriteria();
        oemCriteria.nameEqualTo = "EPSD";
        OemCollection oems = oemClient.searchOems(oemCriteria);
        for (Oem oem : oems.getOems()) {
            oemUuid = oem.getId();
        }

        OsFilterCriteria osCriteria = new OsFilterCriteria();
        osCriteria.nameContains = "VMWare";
        OsCollection oss = osClient.searchOss(osCriteria);
        for (Os os : oss.getOss()) {
            osUuid = os.getId();
        }

        Mle biosMle = new Mle();
        biosMle.setName("Intel BIOS Mle");
        biosMle.setVersion("1.2.3");
        biosMle.setAttestationType(Mle.AttestationType.PCR);
        biosMle.setMleType(Mle.MleType.BIOS);
        biosMle.setOemUuid(oemUuid.toString());
        biosMle.setSource("192.168.0.1");

        List<ManifestData> biosPcrs = new ArrayList<>();
        biosPcrs.add(new ManifestData("0", Sha1Digest.digestOf(new UUID().toByteArray().getBytes()).toString()));
        biosPcrs.add(new ManifestData("17", Sha1Digest.digestOf(new UUID().toByteArray().getBytes()).toString()));
        biosMle.setMleManifests(biosPcrs);

        biosMle = client.createMle(biosMle);

        MlePcr mlePcr = new MlePcr();
        mlePcr.setPcrIndex("1");
        mlePcr.setPcrValue(Sha1Digest.digestOf(new UUID().toByteArray().getBytes()).toString());
        mlePcr.setMleUuid(biosMle.getId().toString());
        mlePcr = pcrClient.createMlePcr(mlePcr);

        MlePcrFilterCriteria pcrCriteria = new MlePcrFilterCriteria();
        pcrCriteria.mleUuid = biosMle.getId();
        MlePcrCollection pcrs = pcrClient.searchMlePcrs(pcrCriteria);
        for (MlePcr pcr : pcrs.getMlePcrs()) {
            log.debug("Retrieved PCR {} with value {} for MLE with UUID {}.", pcr.getPcrIndex(), pcr.getPcrValue(), pcr.getMleUuid());
        }

        mlePcr.setDescription("Added description");
        mlePcr.setPcrValue(Sha1Digest.digestOf(new UUID().toByteArray().getBytes()).toString());
        mlePcr = pcrClient.editMlePcr(mlePcr);
        log.debug("Updated PCR {} with value {} for MLE with UUID {}.", mlePcr.getPcrIndex(), mlePcr.getPcrValue(), mlePcr.getMleUuid());

        MleSourceFilterCriteria sourceCriteria = new MleSourceFilterCriteria();
        sourceCriteria.mleUuid = biosMle.getId();
        MleSourceCollection mleSources = mleSourceClient.searchMleSources(sourceCriteria);
        if (mleSources != null && mleSources.getMleSources().size() == 1) {
            MleSource mleSource = mleSources.getMleSources().get(0);
            mleSource.setName("192.168.20.20");
            mleSource = mleSourceClient.editMleSource(mleSource);
            log.debug("Updated MLE {} with source {}", mleSource.getMleUuid(), mleSource.getName());

        } else {
            MleSource mleSource = new MleSource();
            mleSource.setName("192.168.10.10");
            mleSource.setMleUuid(biosMle.getId().toString());
            mleSource = mleSourceClient.editMleSource(mleSource);
            log.debug("Created MLE {} with source {}", mleSource.getMleUuid(), mleSource.getName());
        }

        MleFilterCriteria mleCriteria = new MleFilterCriteria();
        mleCriteria.id = UUID.valueOf(biosMle.getId().toString()); //biosMle.getId();
        MleCollection mles = client.searchMles(mleCriteria);
        for (Mle mle : mles.getMles()) {
            log.debug("Mle details : {} - {} - {} - {}", mle.getName(), mle.getVersion(), mle.getSource(), mle.getId().toString());
        }

        Mle newBiosMle = client.retrieveMle(biosMle.getId().toString());
        log.debug("Mle details : {} - {} - {} - {}", newBiosMle.getName(), newBiosMle.getVersion(), newBiosMle.getSource(), newBiosMle.getId().toString());

        client.deleteMle(biosMle.getId().toString());
    }
}
