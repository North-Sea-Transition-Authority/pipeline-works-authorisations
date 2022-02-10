package uk.co.ogauthority.pwa.testutils;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementDto;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementDtoTestUtil;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.ProcessingPermissionsDto;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ConsultationInvolvementDto;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ConsultationInvolvementDtoTestUtil;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;

public class PwaAppProcessingContextDtoTestUtils {

  private static final ProcessingPermissionsDto EMPTY = new ProcessingPermissionsDto(null, Set.of());

  private PwaAppProcessingContextDtoTestUtils() {
    throw new AssertionError();
  }

  /**
   * Should be replaced by method within ApplicationInvolvementDtoTestUtil.
   */
  @Deprecated
  public static ApplicationInvolvementDto appInvolvementWithConsultationRequest(String groupName,
                                                                                ConsultationRequest request) {

    var groupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup(groupName, groupName.substring(0, 1));
    var consultationInvolvement = new ConsultationInvolvementDto(groupDetail, Set.of(), request, List.of(), false);

    return ApplicationInvolvementDtoTestUtil.generateAppInvolvement(
        null,
        EnumSet.noneOf(ApplicationInvolvementDtoTestUtil.InvolvementFlag.class),
        EnumSet.noneOf(PwaContactRole.class),
        EnumSet.noneOf(PwaOrganisationRole.class),
        consultationInvolvement
    );

  }

  /**
   * Should be replaced by method within ApplicationInvolvementDtoTestUtil.
   */
  @Deprecated
  public static ApplicationInvolvementDto appInvolvementWithConsultationRequest(String groupName,
                                                                                ConsultationRequest request,
                                                                                boolean atLeastOneSatisfactoryVersion) {

    var groupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup(groupName, groupName.substring(0, 1));
    var consultationInvolvement = new ConsultationInvolvementDto(groupDetail, Set.of(), request, List.of(), false);

    var involvementFlags = atLeastOneSatisfactoryVersion
        ? EnumSet.of(ApplicationInvolvementDtoTestUtil.InvolvementFlag.AT_LEAST_ONE_SATISFACTORY_VERSION)
        :  EnumSet.noneOf(ApplicationInvolvementDtoTestUtil.InvolvementFlag.class);

    return ApplicationInvolvementDtoTestUtil.generateConsulteeInvolvement(
        null,
        involvementFlags,
        consultationInvolvement
    );

  }

  public static ProcessingPermissionsDto emptyPermissionsDto() {
    return EMPTY;
  }

  /**
   * Replaced By ApplicationInvolvementDtoTestUtil methods
   */
  @Deprecated
  public static ApplicationInvolvementDto emptyAppInvolvement(PwaApplication application) {
    return ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(application);
  }

  /**
   * Replaced By ApplicationInvolvementDtoTestUtil methods
   */
  @Deprecated
  public static ApplicationInvolvementDto appInvolvementSatisfactoryVersions(PwaApplication application) {

    return ApplicationInvolvementDtoTestUtil.generateAppInvolvement(
        application,
        EnumSet.of(ApplicationInvolvementDtoTestUtil.InvolvementFlag.AT_LEAST_ONE_SATISFACTORY_VERSION),
        EnumSet.noneOf(PwaContactRole.class),
        EnumSet.noneOf(PwaOrganisationRole.class),
        ConsultationInvolvementDtoTestUtil.emptyConsultationInvolvement()
    );
  }

}
