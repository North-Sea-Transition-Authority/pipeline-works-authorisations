package uk.co.ogauthority.pwa.service.appprocessing.options;

import static java.util.stream.Collectors.toList;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementService;
import uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation.PadOptionConfirmedService;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.applicationworkflow.OptionsVariationClosedWithoutConsentEmailProps;
import uk.co.ogauthority.pwa.features.email.emailproperties.optionsapplications.ApplicationOptionsApprovalDeadlineChangedEmailProps;
import uk.co.ogauthority.pwa.features.email.emailproperties.optionsapplications.ApplicationOptionsApprovedEmailProps;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailService;
import uk.co.ogauthority.pwa.model.entity.enums.ConfirmedOptionType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentOrganisationRoleService;
import uk.co.ogauthority.pwa.util.DateUtils;

@Service
class OptionsCaseManagementEmailService {

  private static final Logger LOGGER = LoggerFactory.getLogger(OptionsCaseManagementEmailService.class);

  private static final DateTimeFormatter DEADLINE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMMM-yyyy");

  private final CaseLinkService caseLinkService;

  private final PwaContactService pwaContactService;

  private final PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService;

  private final ApplicationInvolvementService applicationInvolvementService;

  private final PadOptionConfirmedService padOptionConfirmedService;

  private final EmailService emailService;

  public OptionsCaseManagementEmailService(CaseLinkService caseLinkService,
                                           PwaContactService pwaContactService,
                                           PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService,
                                           ApplicationInvolvementService applicationInvolvementService,
                                           PadOptionConfirmedService padOptionConfirmedService,
                                           EmailService emailService) {
    this.caseLinkService = caseLinkService;
    this.pwaContactService = pwaContactService;
    this.pwaConsentOrganisationRoleService = pwaConsentOrganisationRoleService;
    this.applicationInvolvementService = applicationInvolvementService;
    this.padOptionConfirmedService = padOptionConfirmedService;
    this.emailService = emailService;
  }

  public void sendInitialOptionsApprovedEmail(PwaApplicationDetail pwaApplicationDetail, Instant deadlineDate) {

    var pwaApplication = pwaApplicationDetail.getPwaApplication();

    var recipients = pwaContactService.getPeopleInRoleForPwaApplication(
        pwaApplication,
        PwaContactRole.PREPARER
    );

    var holderNames = getPwaApplicationConsentedHolderNames(pwaApplication);
    var formattedDeadlineDate = deadlineDateAsString(deadlineDate);
    var holderCsv = String.join(", ", holderNames);
    var caseLink = caseLinkService.generateCaseManagementLink(pwaApplication);

    if (!recipients.isEmpty()) {
      recipients.forEach(person ->
          emailService.sendEmail(
              new ApplicationOptionsApprovedEmailProps(
                  person.getFullName(),
                  pwaApplication.getAppReference(),
                  holderCsv,
                  formattedDeadlineDate,
                  caseLink
              ),
              person,
              pwaApplication.getAppReference()
          )
      );

    } else {
      LOGGER.error("Tried to send application options approved email, but no recipients found. pwaApplication.id: {}",
          pwaApplication.getId());
    }

  }

  private String deadlineDateAsString(Instant deadlineDate) {
    return DEADLINE_FORMATTER.format(DateUtils.instantToLocalDate(deadlineDate));
  }

  public void sendOptionsDeadlineChangedEmail(PwaApplicationDetail pwaApplicationDetail, Instant deadlineDate) {

    var pwaApplication = pwaApplicationDetail.getPwaApplication();

    var caseOfficerPersonOpt = applicationInvolvementService.getCaseOfficerPerson(pwaApplication);

    var caseLink = caseLinkService.generateCaseManagementLink(pwaApplication);
    var formattedDeadlineDate = deadlineDateAsString(deadlineDate);

    var pwaContactRecipients = pwaContactService.getPeopleInRoleForPwaApplication(
        pwaApplication,
        PwaContactRole.PREPARER
    );

    var recipients = new ArrayList<>(pwaContactRecipients);
    caseOfficerPersonOpt.ifPresent(recipients::add);

    if (!recipients.isEmpty()) {
      recipients.forEach(person ->
          emailService.sendEmail(
              new ApplicationOptionsApprovalDeadlineChangedEmailProps(
                  person.getFullName(),
                  pwaApplication.getAppReference(),
                  formattedDeadlineDate,
                  caseLink
              ),
              person,
              pwaApplication.getAppReference()
          )
      );

    } else {
      LOGGER.error(
          "Tried to send application options approved deadline changed email, but no recipients found. pwaApplication.id: {}",
          pwaApplication.getId());
    }

  }

  private List<String> getPwaApplicationConsentedHolderNames(PwaApplication pwaApplication) {

    return pwaConsentOrganisationRoleService.getCurrentConsentedHoldersOrgRolesForMasterPwa(pwaApplication.getMasterPwa())
        .stream()
        .filter(masterPwaHolderDto -> masterPwaHolderDto.getHolderOrganisationUnit().isPresent())
        .map(masterPwaHolderDto -> masterPwaHolderDto.getHolderOrganisationUnit().get())
        .map(PortalOrganisationUnit::getName)
        .sorted(Comparator.comparing(String::toLowerCase))
        .collect(toList());

  }

  public void sendOptionsCloseOutEmailsIfRequired(PwaApplicationDetail pwaApplicationDetail, Person closingPerson) {
    var confirmedOptionTypeOptional = padOptionConfirmedService.getConfirmedOptionType(pwaApplicationDetail);
    confirmedOptionTypeOptional.ifPresent(confirmedOptionType -> {
      if (confirmedOptionType == ConfirmedOptionType.WORK_DONE_BUT_NOT_PRESENTED_AS_OPTION
          || confirmedOptionType == ConfirmedOptionType.NO_WORK_DONE) {
        generateOptionsCloseOutEmailPropsAndSendEmails(pwaApplicationDetail.getPwaApplication(), confirmedOptionType, closingPerson);
      }
    });
  }

  private void generateOptionsCloseOutEmailPropsAndSendEmails(PwaApplication pwaApplication,
                                                              ConfirmedOptionType confirmedOptionType,
                                                              Person closingPerson) {
    var emailRecipients = pwaContactService.getPeopleInRoleForPwaApplication(
        pwaApplication,
        PwaContactRole.PREPARER
    );
    emailRecipients.forEach(recipient -> {
      var emailProps = new OptionsVariationClosedWithoutConsentEmailProps(
          recipient.getFullName(),
          pwaApplication.getAppReference(),
          confirmedOptionType,
          closingPerson.getFullName(),
          caseLinkService.generateCaseManagementLink(pwaApplication)
      );

      emailService.sendEmail(emailProps, recipient, pwaApplication.getAppReference());
    });
  }

}
