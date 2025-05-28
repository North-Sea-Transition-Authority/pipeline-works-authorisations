package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.features.email.EmailRecipientWithName;
import uk.co.ogauthority.pwa.features.email.emailproperties.applicationworkflow.HolderChangeConsentedEmailProps;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailService;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentOrganisationRole;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentOrganisationRoleTestUtil;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.management.view.TeamMemberView;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class HolderChangeEmailServiceTest {

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Mock
  PwaHolderTeamService pwaHolderTeamService;

  @Mock
  private EmailService emailService;

  @Mock
  private MasterPwaService masterPwaService;

  @Captor
  private ArgumentCaptor<HolderChangeConsentedEmailProps> emailPropsCaptor;

  @Captor
  private ArgumentCaptor<EmailRecipient> emailRecipientArgumentCaptor;

  @InjectMocks
  private HolderChangeEmailService holderChangeEmailService;

  private PortalOrganisationUnit shellOrgUnit, bpOrgUnit, wintershallOrgUnit;
  private PortalOrganisationGroup shellOrgGroup, bpOrgGroup, wintershallOrgGroup;

  private PwaConsentOrganisationRole shellConsentHolderRole, bpConsentHolderRole, wintershallConsentHolderRole;

  private TeamMemberView shellCreator, shellSubmitter, shellFinance;
  private TeamMemberView bpCreator, bpSubmitter, bpFinance;
  private TeamMemberView wintershallCreator, wintershallSubmitter, wintershallFinance;

  private PwaApplicationDetail detail;
  private MasterPwaDetail masterPwaDetail;

  @BeforeEach
  void setUp() throws Exception {

    shellOrgGroup = PortalOrganisationTestUtils.generateOrganisationGroup(1, "Shell", "S");
    shellOrgUnit = PortalOrganisationTestUtils.generateOrganisationUnit(1, "Shell", shellOrgGroup);

    bpOrgGroup = PortalOrganisationTestUtils.generateOrganisationGroup(2, "BP", "B");
    bpOrgUnit = PortalOrganisationTestUtils.generateOrganisationUnit(2, "BP", bpOrgGroup);

    wintershallOrgGroup = PortalOrganisationTestUtils.generateOrganisationGroup(3, "Wintershall", "W");
    wintershallOrgUnit = PortalOrganisationTestUtils.generateOrganisationUnit(3, "Wintershall", wintershallOrgGroup);

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.CAT_1_VARIATION);

    var consent = new PwaConsent();

    shellConsentHolderRole = PwaConsentOrganisationRoleTestUtil
        .createOrganisationRole(consent, new OrganisationUnitId(shellOrgUnit.getOuId()), HuooRole.HOLDER);

    bpConsentHolderRole = PwaConsentOrganisationRoleTestUtil
        .createOrganisationRole(consent, new OrganisationUnitId(bpOrgUnit.getOuId()), HuooRole.HOLDER);

    wintershallConsentHolderRole = PwaConsentOrganisationRoleTestUtil
        .createOrganisationRole(consent, new OrganisationUnitId(wintershallOrgUnit.getOuId()), HuooRole.HOLDER);

    shellCreator = new TeamMemberView(1L, "Mr.", "shell", "creator", "c@s.com", null, null, List.of(Role.APPLICATION_CREATOR));
    shellSubmitter = new TeamMemberView(2L, "Mr.", "shell", "submitter", "s@s.com", null, null, List.of(Role.APPLICATION_SUBMITTER));
    shellFinance = new TeamMemberView(3L, "Mr.", "shell", "finance", "f@s.com", null, null, List.of(Role.FINANCE_ADMIN));

    bpCreator = new TeamMemberView(4L, "Mr.", "bp", "creator", "c@b.com", null, null, List.of(Role.APPLICATION_CREATOR));
    bpSubmitter = new TeamMemberView(5L, "Mr.", "bp", "submitter", "s@b.com", null, null, List.of(Role.APPLICATION_SUBMITTER));
    bpFinance = new TeamMemberView(6L, "Mr.", "bp", "finance", "f@b.com", null, null, List.of(Role.FINANCE_ADMIN));

    wintershallCreator = new TeamMemberView(7L, "Mr.", "wintershall", "creator", "c@w.com", null, null, List.of(Role.APPLICATION_CREATOR));
    wintershallSubmitter = new TeamMemberView(8L, "Mr.", "wintershall", "submitter", "s@w.com", null, null, List.of(Role.APPLICATION_SUBMITTER));
    wintershallFinance = new TeamMemberView(9L, "Mr.", "wintershall", "finance", "f@w.com", null, null, List.of(Role.FINANCE_ADMIN));

    masterPwaDetail = new MasterPwaDetail();
    masterPwaDetail.setReference("1/W/1");

  }

  @Test
  void sendHolderChangeEmail_parentOrgIdentical() {

    var shellUkOrgUnit = PortalOrganisationTestUtils.generateOrganisationUnit(1, "Shell UK", shellOrgGroup);
    var shellClairOrgUnit = PortalOrganisationTestUtils.generateOrganisationUnit(1, "Shell Clair", shellOrgGroup);

    var shellUkConsentHolderRole = PwaConsentOrganisationRoleTestUtil
        .createOrganisationRole(shellConsentHolderRole.getAddedByPwaConsent(), new OrganisationUnitId(shellUkOrgUnit.getOuId()), HuooRole.HOLDER);

    var shellClairConsentHolderRole = PwaConsentOrganisationRoleTestUtil
        .createOrganisationRole(shellConsentHolderRole.getAddedByPwaConsent(), new OrganisationUnitId(shellClairOrgUnit.getOuId()), HuooRole.HOLDER);

    when(portalOrganisationsAccessor.getOrganisationUnitsByIdIn(any())).thenReturn(List.of(shellUkOrgUnit, shellClairOrgUnit));

    holderChangeEmailService.sendHolderChangeEmail(detail.getPwaApplication(), List.of(shellUkConsentHolderRole), List.of(shellClairConsentHolderRole));

    verify(emailService, never()).sendEmail(emailPropsCaptor.capture(), emailRecipientArgumentCaptor.capture(), any());
  }

  @Test
  void sendHolderChangeEmail_holderEnded_holderAdded() {
    when(masterPwaService.getCurrentDetailOrThrow(detail.getMasterPwa())).thenReturn(masterPwaDetail);

    when(portalOrganisationsAccessor.getOrganisationUnitsByIdIn(List.of(shellOrgUnit.getOuId()))).thenReturn(List.of(shellOrgUnit));
    when(portalOrganisationsAccessor.getOrganisationUnitsByIdIn(List.of(bpOrgUnit.getOuId()))).thenReturn(List.of(bpOrgUnit));

    when(pwaHolderTeamService.getMembersWithinHolderTeamForOrgGroups(any())).thenReturn(List.of(shellCreator, shellSubmitter, shellFinance, bpCreator, bpSubmitter, bpFinance));

    holderChangeEmailService.sendHolderChangeEmail(detail.getPwaApplication(), List.of(shellConsentHolderRole), List.of(bpConsentHolderRole));

    verify(emailService, times(6))
        .sendEmail(emailPropsCaptor.capture(), emailRecipientArgumentCaptor.capture(), eq(detail.getPwaApplicationRef()));

    assertThat(emailPropsCaptor.getAllValues())
        .anySatisfy(p -> assertThat(p.getRecipientFullName()).isEqualTo(shellCreator.getFullName()))
        .anySatisfy(p -> assertThat(p.getRecipientFullName()).isEqualTo(shellSubmitter.getFullName()))
        .anySatisfy(p -> assertThat(p.getRecipientFullName()).isEqualTo(shellFinance.getFullName()))
        .anySatisfy(p -> assertThat(p.getRecipientFullName()).isEqualTo(bpCreator.getFullName()))
        .anySatisfy(p -> assertThat(p.getRecipientFullName()).isEqualTo(bpSubmitter.getFullName()))
        .anySatisfy(p -> assertThat(p.getRecipientFullName()).isEqualTo(bpFinance.getFullName()))
        .allSatisfy(p -> {

          assertThat(p.getApplicationReference()).isEqualTo(detail.getPwaApplication().getAppReference());
          assertThat(p.getNewHolderName()).isEqualTo(bpOrgGroup.getName());
          assertThat(p.getOldHolderName()).isEqualTo(shellOrgGroup.getName());
          assertThat(p.getApplicationType()).isEqualTo(detail.getPwaApplication().getApplicationType().getDisplayName());
          assertThat(p.getPwaReference()).isEqualTo(masterPwaDetail.getReference());

        });

    assertThat(emailRecipientArgumentCaptor.getAllValues())
        .anySatisfy(emailRecipient -> assertThat(emailRecipient.getEmailAddress()).isEqualTo(EmailRecipientWithName.from(shellCreator).getEmailAddress()))
        .anySatisfy(emailRecipient -> assertThat(emailRecipient.getEmailAddress()).isEqualTo(EmailRecipientWithName.from(shellSubmitter).getEmailAddress()))
        .anySatisfy(emailRecipient -> assertThat(emailRecipient.getEmailAddress()).isEqualTo(EmailRecipientWithName.from(shellFinance).getEmailAddress()))
        .anySatisfy(emailRecipient -> assertThat(emailRecipient.getEmailAddress()).isEqualTo(EmailRecipientWithName.from(bpCreator).getEmailAddress()))
        .anySatisfy(emailRecipient -> assertThat(emailRecipient.getEmailAddress()).isEqualTo(EmailRecipientWithName.from(bpSubmitter).getEmailAddress()))
        .anySatisfy(emailRecipient -> assertThat(emailRecipient.getEmailAddress()).isEqualTo(EmailRecipientWithName.from(bpFinance).getEmailAddress()));

  }

  @Test
  void sendHolderChangeEmail_multipleHoldersEnded_holderAdded() {
    when(masterPwaService.getCurrentDetailOrThrow(detail.getMasterPwa())).thenReturn(masterPwaDetail);

    when(portalOrganisationsAccessor.getOrganisationUnitsByIdIn(List.of(shellOrgUnit.getOuId(), wintershallOrgUnit.getOuId()))).thenReturn(List.of(shellOrgUnit, wintershallOrgUnit));
    when(portalOrganisationsAccessor.getOrganisationUnitsByIdIn(List.of(bpOrgUnit.getOuId()))).thenReturn(List.of(bpOrgUnit));

    when(pwaHolderTeamService.getMembersWithinHolderTeamForOrgGroups(any())).thenReturn(List.of(shellCreator, shellSubmitter, shellFinance, bpCreator, bpSubmitter, bpFinance, wintershallCreator, wintershallSubmitter, wintershallFinance));

    holderChangeEmailService.sendHolderChangeEmail(detail.getPwaApplication(), List.of(shellConsentHolderRole, wintershallConsentHolderRole), List.of(bpConsentHolderRole));

    verify(emailService, times(9))
        .sendEmail(emailPropsCaptor.capture(), emailRecipientArgumentCaptor.capture(), eq(detail.getPwaApplicationRef()));

    assertThat(emailPropsCaptor.getAllValues())
        .anySatisfy(p -> assertThat(p.getRecipientFullName()).isEqualTo(shellCreator.getFullName()))
        .anySatisfy(p -> assertThat(p.getRecipientFullName()).isEqualTo(shellSubmitter.getFullName()))
        .anySatisfy(p -> assertThat(p.getRecipientFullName()).isEqualTo(shellFinance.getFullName()))
        .anySatisfy(p -> assertThat(p.getRecipientFullName()).isEqualTo(bpCreator.getFullName()))
        .anySatisfy(p -> assertThat(p.getRecipientFullName()).isEqualTo(bpSubmitter.getFullName()))
        .anySatisfy(p -> assertThat(p.getRecipientFullName()).isEqualTo(bpFinance.getFullName()))
        .anySatisfy(p -> assertThat(p.getRecipientFullName()).isEqualTo(wintershallCreator.getFullName()))
        .anySatisfy(p -> assertThat(p.getRecipientFullName()).isEqualTo(wintershallSubmitter.getFullName()))
        .anySatisfy(p -> assertThat(p.getRecipientFullName()).isEqualTo(wintershallFinance.getFullName()))
        .allSatisfy(p -> {

          assertThat(p.getApplicationReference()).isEqualTo(detail.getPwaApplication().getAppReference());
          assertThat(p.getNewHolderName()).isEqualTo(bpOrgGroup.getName());
          assertThat(p.getOldHolderName().split(", ")).containsExactly(shellOrgGroup.getName(), wintershallOrgGroup.getName());
          assertThat(p.getApplicationType()).isEqualTo(detail.getPwaApplication().getApplicationType().getDisplayName());
          assertThat(p.getPwaReference()).isEqualTo(masterPwaDetail.getReference());

        });

    assertThat(emailRecipientArgumentCaptor.getAllValues())
        .anySatisfy(emailRecipient -> assertThat(emailRecipient.getEmailAddress()).isEqualTo(EmailRecipientWithName.from(shellCreator).getEmailAddress()))
        .anySatisfy(emailRecipient -> assertThat(emailRecipient.getEmailAddress()).isEqualTo(EmailRecipientWithName.from(shellSubmitter).getEmailAddress()))
        .anySatisfy(emailRecipient -> assertThat(emailRecipient.getEmailAddress()).isEqualTo(EmailRecipientWithName.from(shellFinance).getEmailAddress()))
        .anySatisfy(emailRecipient -> assertThat(emailRecipient.getEmailAddress()).isEqualTo(EmailRecipientWithName.from(bpCreator).getEmailAddress()))
        .anySatisfy(emailRecipient -> assertThat(emailRecipient.getEmailAddress()).isEqualTo(EmailRecipientWithName.from(bpSubmitter).getEmailAddress()))
        .anySatisfy(emailRecipient -> assertThat(emailRecipient.getEmailAddress()).isEqualTo(EmailRecipientWithName.from(bpFinance).getEmailAddress()))
        .anySatisfy(emailRecipient -> assertThat(emailRecipient.getEmailAddress()).isEqualTo(EmailRecipientWithName.from(wintershallCreator).getEmailAddress()))
        .anySatisfy(emailRecipient -> assertThat(emailRecipient.getEmailAddress()).isEqualTo(EmailRecipientWithName.from(wintershallSubmitter).getEmailAddress()))
        .anySatisfy(emailRecipient -> assertThat(emailRecipient.getEmailAddress()).isEqualTo(EmailRecipientWithName.from(wintershallFinance).getEmailAddress()));

  }

}