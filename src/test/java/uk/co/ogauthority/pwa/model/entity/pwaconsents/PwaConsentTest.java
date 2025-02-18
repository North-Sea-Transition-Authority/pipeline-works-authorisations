package uk.co.ogauthority.pwa.model.entity.pwaconsents;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;

class PwaConsentTest {

  @Test
  void equalsAndHashCode() {
    var firstPwaDetail = new MasterPwaDetail();
    var differentPwaDetail = new MasterPwaDetail();
    differentPwaDetail.setReference("rfefafsd");

    var firstPipeline = new Pipeline();
    var differentPipeline = new Pipeline();

    var pwa = new MasterPwa();
    pwa.setId(132);
    differentPipeline.setMasterPwa(firstPwaDetail.getMasterPwa());
    firstPipeline.setMasterPwa(pwa);

    EqualsVerifier.forClass(PwaConsent.class)
        .withIgnoredAnnotations(Entity.class, Id.class)
        .withPrefabValues(MasterPwaDetail.class, firstPwaDetail, differentPwaDetail)
        .withPrefabValues(Pipeline.class, firstPipeline, differentPipeline)
        .suppress(Warning.NONFINAL_FIELDS, Warning.STRICT_INHERITANCE)
        .verify();
  }
}