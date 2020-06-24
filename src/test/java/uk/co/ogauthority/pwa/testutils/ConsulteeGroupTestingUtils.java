package uk.co.ogauthority.pwa.testutils;

import java.time.Instant;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;

public class ConsulteeGroupTestingUtils {

  private ConsulteeGroupTestingUtils() {
    throw new AssertionError();
  }

  public static ConsulteeGroupDetail createConsulteeGroup(String name, String abbr) {

    var group = new ConsulteeGroup();

    var detail = new ConsulteeGroupDetail();
    detail.setName(name);
    detail.setAbbreviation(abbr);
    detail.setConsulteeGroup(group);
    detail.setStartTimestamp(Instant.now());
    detail.setTipFlag(true);

    return detail;

  }

}
