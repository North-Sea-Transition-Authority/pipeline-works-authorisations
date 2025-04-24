package uk.co.ogauthority.pwa.features.webapp.devtools.testharness;

import jakarta.transaction.Transactional;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.SetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.webapp.devtools.testharness.applicationstage.TestHarnessApplicationStageService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.integrations.energyportal.teams.external.PortalTeamAccessor;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.teams.management.view.TeamMemberView;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Service
@Profile("test-harness")
public class TestHarnessService {


  private final GenerateApplicationValidator generateApplicationValidator;
  private final GenerateVariationApplicationValidator generateVariationApplicationValidator;
  private final PortalTeamAccessor portalTeamAccessor;
  private final PersonService personService;
  private final GenerateApplicationService generateApplicationService;
  private final TestHarnessApplicationStageService testHarnessApplicationStageService;
  private final TestHarnessUserRetrievalService testHarnessUserRetrievalService;

  private static final Set<PwaApplicationType> APP_TYPES_FOR_PIPELINES =
      EnumSet.complementOf(EnumSet.of(PwaApplicationType.DEPOSIT_CONSENT));

  private static final Set<PwaApplicationStatus> TEST_HARNESS_APP_STATUSES =
      EnumSet.complementOf(EnumSet.of(PwaApplicationStatus.UPDATE_REQUESTED, PwaApplicationStatus.WITHDRAWN, PwaApplicationStatus.DELETED));

  private static final Set<PwaApplicationStatus> APP_STATUSES_FOR_CASE_OFFICER =
      SetUtils.difference(TEST_HARNESS_APP_STATUSES, Set.of(PwaApplicationStatus.DRAFT, PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW));


  private static final Logger LOGGER = LoggerFactory.getLogger(TestHarnessService.class);
  private final TeamQueryService teamQueryService;


  @Autowired
  public TestHarnessService(
      GenerateApplicationValidator generateApplicationValidator,
      GenerateVariationApplicationValidator generateVariationApplicationValidator,
      PortalTeamAccessor portalTeamAccessor,
      PersonService personService,
      GenerateApplicationService generateApplicationService,
      TestHarnessApplicationStageService testHarnessApplicationStageService,
      TestHarnessUserRetrievalService testHarnessUserRetrievalService, TeamQueryService teamQueryService) {
    this.generateApplicationValidator = generateApplicationValidator;
    this.generateVariationApplicationValidator = generateVariationApplicationValidator;
    this.portalTeamAccessor = portalTeamAccessor;
    this.personService = personService;
    this.generateApplicationService = generateApplicationService;
    this.testHarnessApplicationStageService = testHarnessApplicationStageService;
    this.testHarnessUserRetrievalService = testHarnessUserRetrievalService;
    this.teamQueryService = teamQueryService;
  }

  @Transactional
  public void generatePwaApplication(PwaApplicationType applicationType,
                                    Integer consentedMasterPwaId,
                                    Integer nonConsentedMasterPwaId,
                                    PwaApplicationStatus targetAppStatus,
                                    Integer pipelineQuantity,
                                    Integer assignedCaseOfficerId,
                                    Integer applicantWuaId,
                                    PwaResourceType resourceType) {


    LOGGER.info("Starting application generation");

    var applicantUser = testHarnessUserRetrievalService.getWebUserAccount(applicantWuaId);
    PwaApplicationDetail pwaApplicationDetail;

    switch (applicationType) {
      case INITIAL:
        pwaApplicationDetail = generateApplicationService.generateInitialPwaApplication(pipelineQuantity, applicantUser, resourceType);
        break;
      case HUOO_VARIATION:
      case CAT_1_VARIATION:
      case CAT_2_VARIATION:
      case DEPOSIT_CONSENT:
      case OPTIONS_VARIATION:
      case DECOMMISSIONING:
        pwaApplicationDetail = generateApplicationService.generateVariationPwaApplication(
            applicationType,
            consentedMasterPwaId,
            nonConsentedMasterPwaId,
            pipelineQuantity,
            applicantUser,
            resourceType);
        break;
      default:
        throw new RuntimeException("Pwa application type not recognised for type: " + applicationType.name());
    }

    if (!PwaApplicationStatus.DRAFT.equals(targetAppStatus)) {
      testHarnessApplicationStageService.pushApplicationToTargetStage(
          pwaApplicationDetail, targetAppStatus, applicantUser, assignedCaseOfficerId, pipelineQuantity);
    }

  }


  public Map<String, String> getApplicantsSelectorMap() {

    return teamQueryService.getMembersOfTeamTypeWithRoleIn(TeamType.ORGANISATION, Set.of(Role.APPLICATION_CREATOR)).stream()
        .collect(StreamUtils.toLinkedHashMap(member -> String.valueOf(member.wuaId()), TeamMemberView::getFullName));
  }


  public BindingResult validateGenerateApplicationForm(GenerateApplicationForm form, BindingResult bindingResult) {
    generateApplicationValidator.validate(form, bindingResult);
    return bindingResult;
  }

  public BindingResult validateGenerateVariationApplicationForm(GenerateVariationApplicationForm form, BindingResult bindingResult) {
    generateVariationApplicationValidator.validate(form, bindingResult);
    return bindingResult;
  }

  public static Set<PwaApplicationType> getAppTypesForPipelines() {
    return APP_TYPES_FOR_PIPELINES;
  }

  public static Set<PwaApplicationStatus> getTestHarnessAppStatuses() {
    return TEST_HARNESS_APP_STATUSES;
  }

  public static Set<PwaApplicationStatus> getAppStatusesForCaseOfficer() {
    return APP_STATUSES_FOR_CASE_OFFICER;
  }
}
