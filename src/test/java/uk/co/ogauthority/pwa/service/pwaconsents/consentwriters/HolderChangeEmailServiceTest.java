package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.features.email.emailproperties.applicationworkflow.HolderChangeConsentedEmailProps;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.govuknotify.NotifyService;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentOrganisationRole;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentOrganisationRoleTestUtil;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.model.teams.PwaTeamMember;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.TeamTestingUtils;

@RunWith(MockitoJUnitRunner.class)
public class HolderChangeEmailServiceTest {

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Mock
  private TeamService teamService;

  @Mock
  private NotifyService notifyService;

  @Mock
  private MasterPwaService masterPwaService;

  @Captor
  private ArgumentCaptor<HolderChangeConsentedEmailProps> emailPropsCaptor;

  @Captor
  private ArgumentCaptor<String> emailAddressCaptor;

  private HolderChangeEmailService holderChangeEmailService;

  private PortalOrganisationUnit shellOrgUnit, bpOrgUnit, wintershallOrgUnit;
  private PortalOrganisationGroup shellOrgGroup, bpOrgGroup, wintershallOrgGroup;

  private PwaConsentOrganisationRole shellConsentHolderRole, bpConsentHolderRole, wintershallConsentHolderRole;

  private Person shellCreatorPerson, shellSubmitterPerson, shellFinancePerson;
  private Person bpCreatorPerson, bpSubmitterPerson, bpFinancePerson;
  private Person wintershallCreatorPerson, wintershallSubmitterPerson, wintershallFinancePerson;

  private PwaTeamMember shellCreator, shellSubmitter, shellFinance;
  private PwaTeamMember bpCreator, bpSubmitter, bpFinance;
  private PwaTeamMember wintershallCreator, wintershallSubmitter, wintershallFinance;

  private PwaOrganisationTeam shellOrgTeam, bpOrgTeam, wintershallOrgTeam;

  private PwaApplicationDetail detail;
  private MasterPwaDetail masterPwaDetail;

  @Before
  public void setUp() throws Exception {

    holderChangeEmailService = new HolderChangeEmailService(portalOrganisationsAccessor, teamService, notifyService, masterPwaService);

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

    shellOrgTeam = TeamTestingUtils.getOrganisationTeam(shellOrgGroup);

    shellCreatorPerson = PersonTestUtil.createPersonFrom(new PersonId(1), "c@s.com", "creator");
    shellCreator = TeamTestingUtils.createOrganisationTeamMember(shellOrgTeam, shellCreatorPerson, Set.of(
        PwaOrganisationRole.APPLICATION_CREATOR));

    shellSubmitterPerson = PersonTestUtil.createPersonFrom(new PersonId(2), "s@s.com", "submitter");
    shellSubmitter = TeamTestingUtils.createOrganisationTeamMember(shellOrgTeam, shellSubmitterPerson, Set.of(
        PwaOrganisationRole.APPLICATION_SUBMITTER));

    shellFinancePerson = PersonTestUtil.createPersonFrom(new PersonId(3), "f@s.com", "finance");
    shellFinance = TeamTestingUtils.createOrganisationTeamMember(shellOrgTeam, shellFinancePerson, Set.of(
        PwaOrganisationRole.FINANCE_ADMIN));

    bpOrgTeam = TeamTestingUtils.getOrganisationTeam(bpOrgGroup);

    bpCreatorPerson = PersonTestUtil.createPersonFrom(new PersonId(4), "c@b.com", "creator");
    bpCreator = TeamTestingUtils.createOrganisationTeamMember(bpOrgTeam, bpCreatorPerson, Set.of(
        PwaOrganisationRole.APPLICATION_CREATOR));

    bpSubmitterPerson = PersonTestUtil.createPersonFrom(new PersonId(5), "s@b.com", "submitter");
    bpSubmitter = TeamTestingUtils.createOrganisationTeamMember(bpOrgTeam, bpSubmitterPerson, Set.of(
        PwaOrganisationRole.APPLICATION_SUBMITTER));

    bpFinancePerson = PersonTestUtil.createPersonFrom(new PersonId(6), "f@b.com", "finance");
    bpFinance = TeamTestingUtils.createOrganisationTeamMember(bpOrgTeam, bpFinancePerson, Set.of(
        PwaOrganisationRole.FINANCE_ADMIN));

    wintershallOrgTeam = TeamTestingUtils.getOrganisationTeam(wintershallOrgGroup);

    wintershallCreatorPerson = PersonTestUtil.createPersonFrom(new PersonId(7), "c@w.com", "creator");
    wintershallCreator = TeamTestingUtils.createOrganisationTeamMember(wintershallOrgTeam, wintershallCreatorPerson, Set.of(
        PwaOrganisationRole.APPLICATION_CREATOR));

    wintershallSubmitterPerson = PersonTestUtil.createPersonFrom(new PersonId(8), "s@w.com", "submitter");
    wintershallSubmitter = TeamTestingUtils.createOrganisationTeamMember(wintershallOrgTeam, wintershallSubmitterPerson, Set.of(
        PwaOrganisationRole.APPLICATION_SUBMITTER));

    wintershallFinancePerson = PersonTestUtil.createPersonFrom(new PersonId(9), "f@w.com", "finance");
    wintershallFinance = TeamTestingUtils.createOrganisationTeamMember(wintershallOrgTeam, wintershallFinancePerson, Set.of(
        PwaOrganisationRole.FINANCE_ADMIN));

    when(teamService.getTeamMembers(shellOrgTeam)).thenReturn(List.of(shellCreator, shellSubmitter, shellFinance));
    when(teamService.getTeamMembers(bpOrgTeam)).thenReturn(List.of(bpCreator, bpSubmitter, bpFinance));
    when(teamService.getTeamMembers(wintershallOrgTeam)).thenReturn(List.of(wintershallCreator, wintershallSubmitter, wintershallFinance));

    masterPwaDetail = new MasterPwaDetail();
    masterPwaDetail.setReference("1/W/1");

    when(masterPwaService.getCurrentDetailOrThrow(detail.getMasterPwa())).thenReturn(masterPwaDetail);

  }

  @Test
  public void sendHolderChangeEmail_parentOrgIdentical() {

    var shellUkOrgUnit = PortalOrganisationTestUtils.generateOrganisationUnit(1, "Shell UK", shellOrgGroup);
    var shellClairOrgUnit = PortalOrganisationTestUtils.generateOrganisationUnit(1, "Shell Clair", shellOrgGroup);

    var shellUkConsentHolderRole = PwaConsentOrganisationRoleTestUtil
        .createOrganisationRole(shellConsentHolderRole.getAddedByPwaConsent(), new OrganisationUnitId(shellUkOrgUnit.getOuId()), HuooRole.HOLDER);

    var shellClairConsentHolderRole = PwaConsentOrganisationRoleTestUtil
        .createOrganisationRole(shellConsentHolderRole.getAddedByPwaConsent(), new OrganisationUnitId(shellClairOrgUnit.getOuId()), HuooRole.HOLDER);

    when(portalOrganisationsAccessor.getOrganisationUnitsByIdIn(any())).thenReturn(List.of(shellUkOrgUnit, shellClairOrgUnit));

    holderChangeEmailService.sendHolderChangeEmail(detail.getPwaApplication(), List.of(shellUkConsentHolderRole), List.of(shellClairConsentHolderRole));

    verify(notifyService, never()).sendEmail(emailPropsCaptor.capture(), emailAddressCaptor.capture());
  }

  @Test
  public void sendHolderChangeEmail_holderEnded_holderAdded() {

    when(portalOrganisationsAccessor.getOrganisationUnitsByIdIn(List.of(shellOrgUnit.getOuId()))).thenReturn(List.of(shellOrgUnit));
    when(portalOrganisationsAccessor.getOrganisationUnitsByIdIn(List.of(bpOrgUnit.getOuId()))).thenReturn(List.of(bpOrgUnit));

    when(teamService.getOrganisationTeamsForOrganisationGroups(any()))
        .thenReturn(List.of(shellOrgTeam, bpOrgTeam));

    holderChangeEmailService.sendHolderChangeEmail(detail.getPwaApplication(), List.of(shellConsentHolderRole), List.of(bpConsentHolderRole));

    verify(notifyService, times(6)).sendEmail(emailPropsCaptor.capture(), emailAddressCaptor.capture());

    assertThat(emailPropsCaptor.getAllValues())
        .anySatisfy(p -> assertThat(p.getRecipientFullName()).isEqualTo(shellCreatorPerson.getFullName()))
        .anySatisfy(p -> assertThat(p.getRecipientFullName()).isEqualTo(shellSubmitterPerson.getFullName()))
        .anySatisfy(p -> assertThat(p.getRecipientFullName()).isEqualTo(shellFinancePerson.getFullName()))
        .anySatisfy(p -> assertThat(p.getRecipientFullName()).isEqualTo(bpCreatorPerson.getFullName()))
        .anySatisfy(p -> assertThat(p.getRecipientFullName()).isEqualTo(bpSubmitterPerson.getFullName()))
        .anySatisfy(p -> assertThat(p.getRecipientFullName()).isEqualTo(bpFinancePerson.getFullName()))
        .allSatisfy(p -> {

          assertThat(p.getApplicationReference()).isEqualTo(detail.getPwaApplication().getAppReference());
          assertThat(p.getNewHolderName()).isEqualTo(bpOrgGroup.getName());
          assertThat(p.getOldHolderName()).isEqualTo(shellOrgGroup.getName());
          assertThat(p.getApplicationType()).isEqualTo(detail.getPwaApplication().getApplicationType().getDisplayName());
          assertThat(p.getPwaReference()).isEqualTo(masterPwaDetail.getReference());

        });

    assertThat(emailAddressCaptor.getAllValues())
        .anySatisfy(emailAddress -> assertThat(emailAddress).isEqualTo(shellCreatorPerson.getEmailAddress()))
        .anySatisfy(emailAddress -> assertThat(emailAddress).isEqualTo(shellSubmitterPerson.getEmailAddress()))
        .anySatisfy(emailAddress -> assertThat(emailAddress).isEqualTo(shellFinancePerson.getEmailAddress()))
        .anySatisfy(emailAddress -> assertThat(emailAddress).isEqualTo(bpCreatorPerson.getEmailAddress()))
        .anySatisfy(emailAddress -> assertThat(emailAddress).isEqualTo(bpSubmitterPerson.getEmailAddress()))
        .anySatisfy(emailAddress -> assertThat(emailAddress).isEqualTo(bpFinancePerson.getEmailAddress()));

  }

  @Test
  public void sendHolderChangeEmail_multipleHoldersEnded_holderAdded() {

    when(portalOrganisationsAccessor.getOrganisationUnitsByIdIn(List.of(shellOrgUnit.getOuId(), wintershallOrgUnit.getOuId()))).thenReturn(List.of(shellOrgUnit, wintershallOrgUnit));
    when(portalOrganisationsAccessor.getOrganisationUnitsByIdIn(List.of(bpOrgUnit.getOuId()))).thenReturn(List.of(bpOrgUnit));

    when(teamService.getOrganisationTeamsForOrganisationGroups(any()))
        .thenReturn(List.of(shellOrgTeam, bpOrgTeam, wintershallOrgTeam));

    holderChangeEmailService.sendHolderChangeEmail(detail.getPwaApplication(), List.of(shellConsentHolderRole, wintershallConsentHolderRole), List.of(bpConsentHolderRole));

    verify(notifyService, times(9)).sendEmail(emailPropsCaptor.capture(), emailAddressCaptor.capture());

    assertThat(emailPropsCaptor.getAllValues())
        .anySatisfy(p -> assertThat(p.getRecipientFullName()).isEqualTo(shellCreatorPerson.getFullName()))
        .anySatisfy(p -> assertThat(p.getRecipientFullName()).isEqualTo(shellSubmitterPerson.getFullName()))
        .anySatisfy(p -> assertThat(p.getRecipientFullName()).isEqualTo(shellFinancePerson.getFullName()))
        .anySatisfy(p -> assertThat(p.getRecipientFullName()).isEqualTo(bpCreatorPerson.getFullName()))
        .anySatisfy(p -> assertThat(p.getRecipientFullName()).isEqualTo(bpSubmitterPerson.getFullName()))
        .anySatisfy(p -> assertThat(p.getRecipientFullName()).isEqualTo(bpFinancePerson.getFullName()))
        .anySatisfy(p -> assertThat(p.getRecipientFullName()).isEqualTo(wintershallCreatorPerson.getFullName()))
        .anySatisfy(p -> assertThat(p.getRecipientFullName()).isEqualTo(wintershallSubmitterPerson.getFullName()))
        .anySatisfy(p -> assertThat(p.getRecipientFullName()).isEqualTo(wintershallFinancePerson.getFullName()))
        .allSatisfy(p -> {

          assertThat(p.getApplicationReference()).isEqualTo(detail.getPwaApplication().getAppReference());
          assertThat(p.getNewHolderName()).isEqualTo(bpOrgGroup.getName());
          assertThat(p.getOldHolderName().split(", ")).containsExactly(shellOrgGroup.getName(), wintershallOrgGroup.getName());
          assertThat(p.getApplicationType()).isEqualTo(detail.getPwaApplication().getApplicationType().getDisplayName());
          assertThat(p.getPwaReference()).isEqualTo(masterPwaDetail.getReference());

        });

    assertThat(emailAddressCaptor.getAllValues())
        .anySatisfy(emailAddress -> assertThat(emailAddress).isEqualTo(shellCreatorPerson.getEmailAddress()))
        .anySatisfy(emailAddress -> assertThat(emailAddress).isEqualTo(shellSubmitterPerson.getEmailAddress()))
        .anySatisfy(emailAddress -> assertThat(emailAddress).isEqualTo(shellFinancePerson.getEmailAddress()))
        .anySatisfy(emailAddress -> assertThat(emailAddress).isEqualTo(bpCreatorPerson.getEmailAddress()))
        .anySatisfy(emailAddress -> assertThat(emailAddress).isEqualTo(bpSubmitterPerson.getEmailAddress()))
        .anySatisfy(emailAddress -> assertThat(emailAddress).isEqualTo(bpFinancePerson.getEmailAddress()))
        .anySatisfy(emailAddress -> assertThat(emailAddress).isEqualTo(wintershallCreatorPerson.getEmailAddress()))
        .anySatisfy(emailAddress -> assertThat(emailAddress).isEqualTo(wintershallSubmitterPerson.getEmailAddress()))
        .anySatisfy(emailAddress -> assertThat(emailAddress).isEqualTo(wintershallFinancePerson.getEmailAddress()));

  }

}