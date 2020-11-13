package uk.co.ogauthority.pwa.testutils;

import java.util.List;
import java.util.Set;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ApplicationInvolvementDto;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ConsultationInvolvementDto;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ProcessingPermissionsDto;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;

public class PwaAppProcessingContextDtoTestUtils {

  private static final ProcessingPermissionsDto EMPTY = new ProcessingPermissionsDto(null, Set.of());

  private PwaAppProcessingContextDtoTestUtils() {
    throw new AssertionError();
  }

  public static ApplicationInvolvementDto appInvolvementWithConsultationRequest(String groupName,
                                                                                ConsultationRequest request) {

    var groupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup(groupName, groupName.substring(0, 1));
    var consultationInvolvement = new ConsultationInvolvementDto(groupDetail, Set.of(), request, List.of(), false);

    return new ApplicationInvolvementDto(null, Set.of(), consultationInvolvement, false);

  }

  public static ProcessingPermissionsDto empty() {
    return EMPTY;
  }

}
