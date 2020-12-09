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
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.notify.emailproperties.ApplicationOptionsApprovalDeadlineChangedEmailProps;
import uk.co.ogauthority.pwa.model.notify.emailproperties.ApplicationOptionsApprovedEmailProps;
import uk.co.ogauthority.pwa.service.appprocessing.ApplicationInvolvementService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentOrganisationRoleService;
import uk.co.ogauthority.pwa.util.DateUtils;

@Service
class OptionsCaseManagementEmailService {

  private static final Logger LOGGER = LoggerFactory.getLogger(OptionsCaseManagementEmailService.class);

  private static final DateTimeFormatter DEADLINE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMMM-yyyy");

  private final EmailCaseLinkService emailCaseLinkService;

  private final NotifyService notifyService;

  private final PwaContactService pwaContactService;

  private final PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService;

  private final ConsultationRequestService consultationRequestService;

  private final ApplicationInvolvementService applicationInvolvementService;

  public OptionsCaseManagementEmailService(EmailCaseLinkService emailCaseLinkService,
                                           NotifyService notifyService,
                                           PwaContactService pwaContactService,
                                           PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService,
                                           ConsultationRequestService consultationRequestService,
                                           ApplicationInvolvementService applicationInvolvementService) {
    this.emailCaseLinkService = emailCaseLinkService;
    this.notifyService = notifyService;
    this.pwaContactService = pwaContactService;
    this.pwaConsentOrganisationRoleService = pwaConsentOrganisationRoleService;
    this.consultationRequestService = consultationRequestService;
    this.applicationInvolvementService = applicationInvolvementService;
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
    var caseLink = emailCaseLinkService.generateCaseManagementLink(pwaApplication);

    if (!recipients.isEmpty()) {
      recipients.forEach(person ->
          notifyService.sendEmail(
              new ApplicationOptionsApprovedEmailProps(
                  person.getFullName(),
                  pwaApplication.getAppReference(),
                  holderCsv,
                  formattedDeadlineDate,
                  caseLink
              ),
              person.getEmailAddress()
          )
      );

    } else {
      LOGGER.error(
          "Tried to send application options approved email, but no recipients found. pwaApplication.id:" +
              pwaApplication.getId()
      );
    }

  }

  private String deadlineDateAsString(Instant deadlineDate) {
    return DEADLINE_FORMATTER.format(DateUtils.instantToLocalDate(deadlineDate));
  }

  public void sendOptionsDeadlineChangedEmail(PwaApplicationDetail pwaApplicationDetail, Instant deadlineDate) {

    var pwaApplication = pwaApplicationDetail.getPwaApplication();

    var caseOfficerPersonOpt = applicationInvolvementService.getCaseOfficerPerson(pwaApplication);

    var caseLink = emailCaseLinkService.generateCaseManagementLink(pwaApplication);
    var formattedDeadlineDate = deadlineDateAsString(deadlineDate);

    var pwaContactRecipients = pwaContactService.getPeopleInRoleForPwaApplication(
        pwaApplication,
        PwaContactRole.PREPARER
    );

    var recipients = new ArrayList<>(pwaContactRecipients);
    caseOfficerPersonOpt.ifPresent(recipients::add);

    if (!recipients.isEmpty()) {
      recipients.forEach(person ->
          notifyService.sendEmail(
              new ApplicationOptionsApprovalDeadlineChangedEmailProps(
                  person.getFullName(),
                  pwaApplication.getAppReference(),
                  formattedDeadlineDate,
                  caseLink
              ),
              person.getEmailAddress()
          )
      );

    } else {
      LOGGER.error(
          "Tried to send application options approved deadline changed email, but no recipients found. pwaApplication.id:" +
              pwaApplication.getId()
      );
    }

  }

  private List<String> getPwaApplicationConsentedHolderNames(PwaApplication pwaApplication) {

    return pwaConsentOrganisationRoleService.getCurrentHoldersOrgRolesForMasterPwa(pwaApplication.getMasterPwa())
        .stream()
        .filter(masterPwaHolderDto -> masterPwaHolderDto.getHolderOrganisationUnit().isPresent())
        .map(masterPwaHolderDto -> masterPwaHolderDto.getHolderOrganisationUnit().get())
        .map(PortalOrganisationUnit::getName)
        .sorted(Comparator.comparing(String::toLowerCase))
        .collect(toList());

  }


  public void sendOptionsCloseOutEmails(PwaApplication pwaApplication){
    // TODO PWA-1025 fill this in
  }

}
