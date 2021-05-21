package uk.co.ogauthority.pwa.service.pwaconsents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinehuoo.PadPipelineOrganisationRoleLink;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentOrganisationRoleTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentPipelineOrganisationRoleLink;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentPipelineOrganisationRoleLinkRepository;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PwaConsentPipelineOrganisationRoleServiceTest {

  @Mock
  private PwaConsentPipelineOrganisationRoleLinkRepository repository;

  @Mock
  private Clock clock;

  private PwaConsentPipelineOrganisationRoleService pwaConsentPipelineOrganisationRoleService;

  @Captor
  private ArgumentCaptor<List<PwaConsentPipelineOrganisationRoleLink>> roleLinksCaptor;

  private Instant clockTime;

  @Before
  public void setUp() throws Exception {

    clockTime = Instant.now();
    when(clock.instant()).thenReturn(clockTime);

    pwaConsentPipelineOrganisationRoleService = new PwaConsentPipelineOrganisationRoleService(repository, clock);

  }

  @Test
  public void getActiveConsentedPipelineOrgRoleLinks() {

    var pwa = new MasterPwa();

    pwaConsentPipelineOrganisationRoleService.getActiveConsentedPipelineOrgRoleLinks(pwa);

    verify(repository).findByAddedByPwaConsent_MasterPwaAndEndedByPwaConsentIsNull(pwa);

  }

  @Test
  public void endRoleLinks() {

    var link1 = new PwaConsentPipelineOrganisationRoleLink();
    var link2 = new PwaConsentPipelineOrganisationRoleLink();
    var consent = new PwaConsent();

    pwaConsentPipelineOrganisationRoleService.endRoleLinks(List.of(link1, link2), consent);

    verify(repository).saveAll(roleLinksCaptor.capture());

    assertThat(roleLinksCaptor.getValue())
        .hasSize(2)
        .allSatisfy(link -> {
          assertThat(link.getEndedByPwaConsent()).isEqualTo(consent);
          assertThat(link.getEndTimestamp()).isEqualTo(clock.instant());
        });

  }

  @Test
  public void createRoleLinks() {

    var consent = new PwaConsent();
    var pipeline = new Pipeline();

    var group = PortalOrganisationTestUtils.generateOrganisationGroup(1, "G1", "G");
    var org = PortalOrganisationTestUtils.generateOrganisationUnit(1, "O1", group);

    var consentedOrg = PwaConsentOrganisationRoleTestUtil.createOrganisationRole(consent, new OrganisationUnitId(org.getOuId()), HuooRole.HOLDER);
    var consentedTreaty = PwaConsentOrganisationRoleTestUtil.createTreatyRole(consent, TreatyAgreement.ANY_TREATY_COUNTRY, HuooRole.USER);

    var padOrg = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.HOLDER, org);
    var padTreaty = PadOrganisationRoleTestUtil.createTreatyRole(HuooRole.USER, TreatyAgreement.ANY_TREATY_COUNTRY);

    var orgLink = new PadPipelineOrganisationRoleLink(padOrg, pipeline);
    var treatyLink = new PadPipelineOrganisationRoleLink(padTreaty, pipeline);

    pwaConsentPipelineOrganisationRoleService.createRoleLinks(List.of(orgLink, treatyLink), List.of(consentedOrg, consentedTreaty), consent);

    verify(repository).saveAll(roleLinksCaptor.capture());

    assertThat(roleLinksCaptor.getValue()).hasSize(2);

    // new pipe consent org link
    assertThat(roleLinksCaptor.getValue().get(0)).satisfies(link -> {
      assertThat(link.getPipeline()).isEqualTo(pipeline);
      assertThat(link.getAddedByPwaConsent()).isEqualTo(consent);
      assertThat(link.getStartTimestamp()).isEqualTo(clock.instant());
      assertThat(link.getEndedByPwaConsent()).isNull();
      assertThat(link.getEndTimestamp()).isNull();
      assertThat(link.getFromLocation()).isEqualTo(orgLink.getFromLocation());
      assertThat(link.getFromLocationIdentInclusionMode()).isEqualTo(orgLink.getFromLocationIdentInclusionMode());
      assertThat(link.getToLocation()).isEqualTo(orgLink.getToLocation());
      assertThat(link.getToLocationIdentInclusionMode()).isEqualTo(orgLink.getToLocationIdentInclusionMode());
      assertThat(link.getSectionNumber()).isEqualTo(orgLink.getSectionNumber());
      assertThat(link.getPwaConsentOrganisationRole()).isEqualTo(consentedOrg);
    });

    assertThat(roleLinksCaptor.getValue().get(1)).satisfies(link -> {
      assertThat(link.getPipeline()).isEqualTo(pipeline);
      assertThat(link.getAddedByPwaConsent()).isEqualTo(consent);
      assertThat(link.getStartTimestamp()).isEqualTo(clock.instant());
      assertThat(link.getEndedByPwaConsent()).isNull();
      assertThat(link.getEndTimestamp()).isNull();
      assertThat(link.getFromLocation()).isEqualTo(treatyLink.getFromLocation());
      assertThat(link.getFromLocationIdentInclusionMode()).isEqualTo(treatyLink.getFromLocationIdentInclusionMode());
      assertThat(link.getToLocation()).isEqualTo(treatyLink.getToLocation());
      assertThat(link.getToLocationIdentInclusionMode()).isEqualTo(treatyLink.getToLocationIdentInclusionMode());
      assertThat(link.getSectionNumber()).isEqualTo(treatyLink.getSectionNumber());
      assertThat(link.getPwaConsentOrganisationRole()).isEqualTo(consentedTreaty);
    });

  }

}