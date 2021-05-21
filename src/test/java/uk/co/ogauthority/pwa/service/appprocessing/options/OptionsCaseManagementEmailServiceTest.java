package uk.co.ogauthority.pwa.service.appprocessing.options;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.notify.emailproperties.optionsapplications.ApplicationOptionsApprovalDeadlineChangedEmailProps;
import uk.co.ogauthority.pwa.model.notify.emailproperties.optionsapplications.ApplicationOptionsApprovedEmailProps;
import uk.co.ogauthority.pwa.service.appprocessing.ApplicationInvolvementService;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.pwaconsents.MasterPwaHolderDto;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentOrganisationRoleService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class OptionsCaseManagementEmailServiceTest {

  private static final String CASE_LINK = "https://link.com";

  private static final PersonId PREPARER_PERSON_ID = new PersonId(1);
  private static final PersonId CASE_OFFICER_PERSON_ID = new PersonId(2);

  @Mock
  private EmailCaseLinkService emailCaseLinkService;

  @Mock
  private NotifyService notifyService;

  @Mock
  private PwaContactService pwaContactService;

  @Mock
  private PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService;

  @Mock
  private ApplicationInvolvementService applicationInvolvementService;

  @Captor
  private ArgumentCaptor<ApplicationOptionsApprovedEmailProps> optionsApprovedEmailCaptor;

  @Captor
  private ArgumentCaptor<ApplicationOptionsApprovalDeadlineChangedEmailProps> optionsDeadlineChangedEmailCaptor;

  private OptionsCaseManagementEmailService optionsCaseManagementEmailService;

  private PwaApplicationDetail pwaApplicationDetail;
  private MasterPwa masterPwa;
  private Instant deadline;

  private Person preparerContactPerson;

  private Person caseOfficerPerson;

  private PortalOrganisationUnit organisationUnit;
  private MasterPwaHolderDto masterPwaHolderDto;

  @Before
  public void setUp() throws Exception {
    deadline = LocalDate.of(2020, 2, 1).atStartOfDay(ZoneId.systemDefault()).toInstant();

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);
    masterPwa = pwaApplicationDetail.getPwaApplication().getMasterPwa();

    preparerContactPerson = PersonTestUtil.createPersonFrom(PREPARER_PERSON_ID, "contact@email.com");
    caseOfficerPerson = PersonTestUtil.createPersonFrom(CASE_OFFICER_PERSON_ID, "caseOfficer@email.com");

    organisationUnit = PortalOrganisationTestUtils.getOrganisationUnitInOrgGroup();
    var pwaConsent = new PwaConsent();
    masterPwaHolderDto = new MasterPwaHolderDto(organisationUnit, pwaConsent);

    when(pwaConsentOrganisationRoleService.getCurrentConsentedHoldersOrgRolesForMasterPwa(masterPwa))
        .thenReturn(Set.of(masterPwaHolderDto));

    when(emailCaseLinkService.generateCaseManagementLink(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(CASE_LINK);

    when(pwaContactService.getPeopleInRoleForPwaApplication(
        pwaApplicationDetail.getPwaApplication(),
        PwaContactRole.PREPARER))
        .thenReturn(List.of(preparerContactPerson));

    optionsCaseManagementEmailService = new OptionsCaseManagementEmailService(
        emailCaseLinkService,
        notifyService,
        pwaContactService,
        pwaConsentOrganisationRoleService,
        applicationInvolvementService
    );
  }

  @Test
  public void sendInitialOptionsApprovedEmail_whenRecipientsFound_andSingleHoldersFound() {
    optionsCaseManagementEmailService.sendInitialOptionsApprovedEmail(pwaApplicationDetail, deadline);

    verify(notifyService, times(1)).sendEmail(optionsApprovedEmailCaptor.capture(),
        eq(preparerContactPerson.getEmailAddress()));
    verifyNoMoreInteractions(notifyService);

    var emailProps = optionsApprovedEmailCaptor.getValue();
    assertThat(emailProps.getEmailPersonalisation()).containsOnly(
        entry("APPLICATION_REFERENCE", pwaApplicationDetail.getPwaApplicationRef()),
        entry("HOLDER", organisationUnit.getName()),
        entry("CASE_MANAGEMENT_LINK", CASE_LINK),
        entry("DEADLINE_DATE", "01-February-2020"),
        entry("TEST_EMAIL", "no"),
        entry("RECIPIENT_FULL_NAME", preparerContactPerson.getFullName())
    );
  }

  @Test
  public void sendInitialOptionsApprovedEmail_whenNoRecipientsFound() {
    when(pwaContactService.getPeopleInRoleForPwaApplication(
        pwaApplicationDetail.getPwaApplication(),
        PwaContactRole.PREPARER
    ))
        .thenReturn(Collections.emptyList());

    optionsCaseManagementEmailService.sendInitialOptionsApprovedEmail(pwaApplicationDetail, deadline);

    verifyNoInteractions(notifyService);

  }

  @Test
  public void sendInitialOptionsApprovedEmail_whenRecipientsFound_andMultipleHoldersFound() {
    var organisationUnit2 = PortalOrganisationTestUtils.generateOrganisationUnit(9, "XXX", null);
    var pwaConsent2 = new PwaConsent();
    var masterPwaHolderDto2 = new MasterPwaHolderDto(organisationUnit2, pwaConsent2);

    when(pwaConsentOrganisationRoleService.getCurrentConsentedHoldersOrgRolesForMasterPwa(masterPwa))
        .thenReturn(Set.of(masterPwaHolderDto2, masterPwaHolderDto));

    optionsCaseManagementEmailService.sendInitialOptionsApprovedEmail(pwaApplicationDetail, deadline);

    verify(notifyService, times(1)).sendEmail(optionsApprovedEmailCaptor.capture(),
        eq(preparerContactPerson.getEmailAddress()));

    var emailProps = optionsApprovedEmailCaptor.getValue();
    assertThat(emailProps.getEmailPersonalisation()).containsEntry(
        "HOLDER", String.format("%s, %s", organisationUnit.getName(), "XXX")
    );

  }

  @Test
  public void sendOptionsDeadlineChangedEmail_whenPwaContactsFound_andCaseOfficerFound() {
    when(applicationInvolvementService.getCaseOfficerPerson(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(Optional.of(caseOfficerPerson));

    optionsCaseManagementEmailService.sendOptionsDeadlineChangedEmail(pwaApplicationDetail, deadline);

    verify(notifyService, times(1)).sendEmail(optionsDeadlineChangedEmailCaptor.capture(),
        eq(preparerContactPerson.getEmailAddress()));
    verify(notifyService, times(1)).sendEmail(optionsDeadlineChangedEmailCaptor.capture(),
        eq(caseOfficerPerson.getEmailAddress()));

    var emailProps = optionsDeadlineChangedEmailCaptor.getAllValues();

    assertThat(emailProps)
        .isNotEmpty()
        .allSatisfy(applicationOptionsApprovedEmailProps -> {
          assertThat(applicationOptionsApprovedEmailProps.getEmailPersonalisation()).containsEntry("DEADLINE_DATE","01-February-2020");
          assertThat(applicationOptionsApprovedEmailProps.getEmailPersonalisation()).containsEntry("APPLICATION_REFERENCE", pwaApplicationDetail.getPwaApplicationRef());
          assertThat(applicationOptionsApprovedEmailProps.getEmailPersonalisation()).containsEntry("CASE_MANAGEMENT_LINK", CASE_LINK);
        });

  }
}