package uk.co.ogauthority.pwa.service.appprocessing.options;

import static java.util.stream.Collectors.toList;

import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.notify.emailproperties.ApplicationOptionsApprovedEmailProps;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentOrganisationRoleService;

@Service
class OptionsCaseManagementEmailService {

  private static final Logger LOGGER = LoggerFactory.getLogger(OptionsCaseManagementEmailService.class);

  private final EmailCaseLinkService emailCaseLinkService;

  private final NotifyService notifyService;

  private final PwaContactService pwaContactService;

  private final PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService;

  public OptionsCaseManagementEmailService(EmailCaseLinkService emailCaseLinkService,
                                           NotifyService notifyService,
                                           PwaContactService pwaContactService,
                                           PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService) {
    this.emailCaseLinkService = emailCaseLinkService;
    this.notifyService = notifyService;
    this.pwaContactService = pwaContactService;
    this.pwaConsentOrganisationRoleService = pwaConsentOrganisationRoleService;
  }

  public void sendInitialOptionsApprovedEmail(PwaApplicationDetail pwaApplicationDetail) {

    var pwaApplication = pwaApplicationDetail.getPwaApplication();

    var recipients = pwaContactService.getPeopleInRoleForPwaApplication(
        pwaApplication,
        PwaContactRole.PREPARER
    );

    var holderNames = getPwaApplicationConsentedHolderNames(pwaApplication);

    if (!recipients.isEmpty()) {
      recipients.forEach(person ->
          notifyService.sendEmail(
              new ApplicationOptionsApprovedEmailProps(
                  person.getFullName(),
                  pwaApplication.getAppReference(),
                  String.join(", ", holderNames),
                  emailCaseLinkService.generateCaseManagementLink(pwaApplication)
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

  private List<String> getPwaApplicationConsentedHolderNames(PwaApplication pwaApplication) {

    return pwaConsentOrganisationRoleService.getCurrentHoldersOrgRolesForMasterPwa(pwaApplication.getMasterPwa())
        .stream()
        .filter(masterPwaHolderDto -> masterPwaHolderDto.getHolderOrganisationUnit().isPresent())
        .map(masterPwaHolderDto -> masterPwaHolderDto.getHolderOrganisationUnit().get())
        .map(PortalOrganisationUnit::getName)
        .sorted(Comparator.comparing(String::toLowerCase))
        .collect(toList());

  }

}
