package uk.co.ogauthority.pwa.testutils;

import java.time.Instant;
import java.util.Random;
import java.util.Set;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.enums.consultations.ConsultationResponseDocumentType;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOptionGroup;

public class ConsulteeGroupTestingUtils {

  private ConsulteeGroupTestingUtils() {
    throw new AssertionError();
  }

  public static ConsulteeGroupDetail createConsulteeGroup(String name, String abbr) {

    var group = new ConsulteeGroup();
    group.setId(new Random().nextInt());

    var detail = new ConsulteeGroupDetail();
    detail.setId(new Random().nextInt());
    detail.setName(name);
    detail.setAbbreviation(abbr);
    detail.setConsulteeGroup(group);
    detail.setStartTimestamp(Instant.now());
    detail.setTipFlag(true);
    detail.setResponseOptionGroups(Set.of(ConsultationResponseOptionGroup.CONTENT));
    detail.setConsultationResponseDocumentType(ConsultationResponseDocumentType.DEFAULT);

    return detail;

  }

}
