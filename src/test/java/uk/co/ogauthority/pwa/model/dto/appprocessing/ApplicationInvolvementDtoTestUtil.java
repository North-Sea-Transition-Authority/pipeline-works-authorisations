package uk.co.ogauthority.pwa.model.dto.appprocessing;

import static uk.co.ogauthority.pwa.model.dto.appprocessing.ApplicationInvolvementDtoTestUtil.InvolvementFlag.AT_LEAST_ONE_SATISFACTORY_VERSION;
import static uk.co.ogauthority.pwa.model.dto.appprocessing.ApplicationInvolvementDtoTestUtil.InvolvementFlag.CASE_OFFICER_STAGE_AND_USER_ASSIGNED;
import static uk.co.ogauthority.pwa.model.dto.appprocessing.ApplicationInvolvementDtoTestUtil.InvolvementFlag.INDUSTRY_INVOLVEMENT_ONLY;
import static uk.co.ogauthority.pwa.model.dto.appprocessing.ApplicationInvolvementDtoTestUtil.InvolvementFlag.OPEN_CONSENT_REVIEW;
import static uk.co.ogauthority.pwa.model.dto.appprocessing.ApplicationInvolvementDtoTestUtil.InvolvementFlag.PWA_MANAGER_STAGE;

import java.util.EnumSet;
import java.util.Set;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.service.enums.appprocessing.appinvolvement.OpenConsentReview;

public final class ApplicationInvolvementDtoTestUtil {

  private ApplicationInvolvementDtoTestUtil() {
    throw new UnsupportedOperationException("No util for you!");
  }

  public static ApplicationInvolvementDto generatePwaContactInvolvement(PwaApplication pwaApplication,
                                                                        Set<InvolvementFlag> versionFlags,
                                                                        Set<PwaContactRole> pwaContactRoleSet) {
    return generateAppInvolvement(
        pwaApplication,
        versionFlags,
        pwaContactRoleSet,
        Set.of(),
        null
    );

  }

  public static ApplicationInvolvementDto generateAppInvolvement(PwaApplication pwaApplication,
                                                                 Set<InvolvementFlag> versionFlags,
                                                                 Set<PwaContactRole> pwaContactRoles,
                                                                 Set<PwaOrganisationRole> pwaOrganisationRoles,
                                                                 ConsultationInvolvementDto consultationInvolvementDto) {
    return new ApplicationInvolvementDto(
        pwaApplication,
        pwaContactRoles,
        consultationInvolvementDto,
        versionFlags.contains(CASE_OFFICER_STAGE_AND_USER_ASSIGNED),
        versionFlags.contains(PWA_MANAGER_STAGE),
        versionFlags.contains(AT_LEAST_ONE_SATISFACTORY_VERSION),
        pwaOrganisationRoles,
        versionFlags.contains(INDUSTRY_INVOLVEMENT_ONLY),
        versionFlags.contains(OPEN_CONSENT_REVIEW) ? OpenConsentReview.YES : OpenConsentReview.NO);

  }

  public static ApplicationInvolvementDto generatePwaHolderTeamInvolvement(PwaApplication pwaApplication,
                                                                           Set<InvolvementFlag> versionFlags,
                                                                           Set<PwaOrganisationRole> pwaOrganisationRoles) {
    return generateAppInvolvement(
        pwaApplication,
        versionFlags,
        EnumSet.noneOf(PwaContactRole.class),
        pwaOrganisationRoles,
        null
    );

  }

  public static ApplicationInvolvementDto generateConsulteeInvolvement(PwaApplication pwaApplication,
                                                                           Set<InvolvementFlag> versionFlags,
                                                                           ConsultationInvolvementDto consultationInvolvementDto) {
    return generateAppInvolvement(
        pwaApplication,
        versionFlags,
        EnumSet.noneOf(PwaContactRole.class),
        EnumSet.noneOf(PwaOrganisationRole.class),
        consultationInvolvementDto
    );

  }

  public static ApplicationInvolvementDto generateConsulteeInvolvement(PwaApplication pwaApplication,
                                                                       ConsultationInvolvementDto consultationInvolvementDto) {
    return generateConsulteeInvolvement(
        pwaApplication,
        getDefaultFlags(),
        consultationInvolvementDto
    );

  }

  public static ApplicationInvolvementDto generatePwaHolderTeamInvolvement(PwaApplication pwaApplication,
                                                                           Set<PwaOrganisationRole> pwaOrganisationRoles) {
    var flags = getDefaultFlags();
    flags.add(INDUSTRY_INVOLVEMENT_ONLY);

    return generateAppInvolvement(
        pwaApplication,
        flags,
        EnumSet.noneOf(PwaContactRole.class),
        pwaOrganisationRoles,
        null
    );

  }

  public static ApplicationInvolvementDto noInvolvementAndNoFlags(PwaApplication pwaApplication) {
    return generateAppInvolvement(
        pwaApplication,
        EnumSet.noneOf(InvolvementFlag.class),
        EnumSet.noneOf(PwaContactRole.class),
        EnumSet.noneOf(PwaOrganisationRole.class),
        null
    );

  }

  public static ApplicationInvolvementDto fromInvolvementFlags(PwaApplication pwaApplication, Set<InvolvementFlag> involvementFlags) {
    return generateAppInvolvement(
        pwaApplication,
        involvementFlags,
        EnumSet.noneOf(PwaContactRole.class),
        EnumSet.noneOf(PwaOrganisationRole.class),
        null
    );

  }

  public static ApplicationInvolvementDto generatePwaContactInvolvement(PwaApplication pwaApplication,
                                                                        Set<PwaContactRole> pwaContactRoleSet) {
    var flags = getDefaultFlags();
    flags.add(INDUSTRY_INVOLVEMENT_ONLY);
    return generateAppInvolvement(
        pwaApplication,
        flags,
        pwaContactRoleSet,
        EnumSet.noneOf(PwaOrganisationRole.class),
        null
    );

  }

  private static Set<InvolvementFlag> getDefaultFlags() {
    return EnumSet.of(AT_LEAST_ONE_SATISFACTORY_VERSION);
  }

  public enum InvolvementFlag {
    AT_LEAST_ONE_SATISFACTORY_VERSION,
    CASE_OFFICER_STAGE_AND_USER_ASSIGNED,
    PWA_MANAGER_STAGE,
    OPEN_CONSENT_REVIEW,
    INDUSTRY_INVOLVEMENT_ONLY
  }

}