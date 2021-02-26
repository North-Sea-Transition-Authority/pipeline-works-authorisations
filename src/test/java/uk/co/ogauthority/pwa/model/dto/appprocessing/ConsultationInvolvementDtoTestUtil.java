package uk.co.ogauthority.pwa.model.dto.appprocessing;

import java.util.List;
import java.util.Set;

public final class ConsultationInvolvementDtoTestUtil {

  private ConsultationInvolvementDtoTestUtil() {
    throw new UnsupportedOperationException("no util for you!");
  }

  public static ConsultationInvolvementDto emptyConsultationInvolvement(){
    return new ConsultationInvolvementDto(null, Set.of(), null, List.of(), false);
  }
}