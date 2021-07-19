package uk.co.ogauthority.pwa.service.testharness;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobKey.jobKey;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.energyportal.model.dto.teams.PortalTeamDto;
import uk.co.ogauthority.pwa.energyportal.model.dto.teams.PortalTeamMemberDto;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.service.teams.PortalTeamAccessor;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.testharness.GenerateApplicationForm;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaTeamType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.person.PersonService;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.validators.testharness.GenerateApplicationValidator;

@Service
public class TestHarnessService {


  private final GenerateApplicationValidator generateApplicationValidator;
  private final PortalTeamAccessor portalTeamAccessor;
  private final PersonService personService;
  private final Scheduler scheduler;
  private final GenerateApplicationService generateApplicationService;
  private final ApplicationStatusService applicationStatusService;

  private static final Logger LOGGER = LoggerFactory.getLogger(TestHarnessService.class);


  @Autowired
  public TestHarnessService(
      GenerateApplicationValidator generateApplicationValidator,
      PortalTeamAccessor portalTeamAccessor,
      PersonService personService,
      Scheduler scheduler,
      GenerateApplicationService generateApplicationService,
      ApplicationStatusService applicationStatusService) {
    this.generateApplicationValidator = generateApplicationValidator;
    this.portalTeamAccessor = portalTeamAccessor;
    this.personService = personService;
    this.scheduler = scheduler;
    this.generateApplicationService = generateApplicationService;
    this.applicationStatusService = applicationStatusService;
  }


  public void scheduleGenerateApplicationJob(GenerateApplicationForm form) {

    try {

      JobKey jobKey = jobKey("TEST_HARNESS_JOB_" + Instant.now().toString());
      JobDetail jobDetail = newJob(TestHarnessBean.class)
          .withIdentity(jobKey)
          .build();

      jobDetail.getJobDataMap().put("applicationType", form.getApplicationType());
      jobDetail.getJobDataMap().put("applicationStatus", form.getApplicationStatus());
      jobDetail.getJobDataMap().put("pipelineQuantity", form.getPipelineQuantity());
      jobDetail.getJobDataMap().put("assignedCaseOfficerId", form.getAssignedCaseOfficerId());
      jobDetail.getJobDataMap().put("applicantPersonId", form.getApplicantPersonId());

      Trigger trigger = TriggerBuilder.newTrigger().startNow().build();

      scheduler.scheduleJob(jobDetail, trigger);

      LOGGER.info("Test harness app generation job creation complete");

    } catch (SchedulerException e) {
      throw new RuntimeException("Error scheduling test harness job", e);
    }

  }


  @Transactional
  void generatePwaApplication(PwaApplicationType applicationType,
                              PwaApplicationStatus applicationStatus,
                              Integer pipelineQuantity,
                              Integer assignedCaseOfficerId,
                              Integer applicantPersonId) {


    LOGGER.info("Starting application generation");

    PwaApplicationDetail pwaApplicationDetail;

    switch (applicationType) {
      case INITIAL:
        pwaApplicationDetail = generateApplicationService.generateInitialPwaApplication(pipelineQuantity, applicantPersonId);
        break;
      case HUOO_VARIATION:
      case CAT_1_VARIATION:
      case CAT_2_VARIATION:
      case DEPOSIT_CONSENT:
      case OPTIONS_VARIATION:
      case DECOMMISSIONING:
        pwaApplicationDetail = null; //to do: call variation app generator
        break;
      default:
        throw new RuntimeException("Pwa Application type not recognised for type: " + applicationType.name());
    }

    applicationStatusService.setApplicationStatus(pwaApplicationDetail, applicationStatus, assignedCaseOfficerId);

  }




  public Map<String, String> getApplicantsSelectorMap() {

    var orgTeams = portalTeamAccessor.getTeamsWhereRoleMatching(
        PwaTeamType.ORGANISATION.getPortalTeamType(),
        List.of(PwaOrganisationRole.APPLICATION_CREATOR.getPortalTeamRoleName()));
    var resIds = orgTeams.stream().map(PortalTeamDto::getResId).collect(Collectors.toList());

    var portalTeamMembers = portalTeamAccessor.getPortalTeamMembers(resIds)
        .stream()
        .map(PortalTeamMemberDto::getPersonId)
        .collect(Collectors.toList());

    return personService.findAllByIdIn(portalTeamMembers).stream()
        .collect(StreamUtils.toLinkedHashMap(person -> String.valueOf(person.getId().asInt()),
            Person::getFullName));
  }


  public BindingResult validateGenerateApplicationForm(GenerateApplicationForm form, BindingResult bindingResult) {
    generateApplicationValidator.validate(form, bindingResult);
    return bindingResult;
  }


}
