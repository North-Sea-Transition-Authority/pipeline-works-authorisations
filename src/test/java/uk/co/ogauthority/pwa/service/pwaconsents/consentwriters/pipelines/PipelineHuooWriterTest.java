package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.TreatyAgreement;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.IdentLocationInclusionMode;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinehuoo.PadPipelineOrganisationRoleLink;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentOrganisationRole;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentOrganisationRoleTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentPipelineOrganisationRoleLink;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleTestUtil;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PadPipelinesHuooService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentPipelineOrganisationRoleService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PipelineHuooWriterTest {

  @Mock
  private PadPipelinesHuooService padPipelinesHuooService;

  @Mock
  private PwaConsentPipelineOrganisationRoleService consentPipelineOrgRoleService;

  private PipelineHuooWriter pipelineHuooWriter;

  @Captor
  private ArgumentCaptor<List<PwaConsentPipelineOrganisationRoleLink>> consentOrgRoleLinksCaptor;

  private PwaApplicationDetail detail;
  private PwaConsent pwaConsent;
  private MasterPwa masterPwa;

  private PortalOrganisationUnit org1, org2, org3;

  private PwaConsentOrganisationRole consentOrg1Holder, consentOrg1Owner, consentOrg2User, consentOrg2Operator;
  private PadOrganisationRole padOrg1Holder, padOrg3Owner, padOrg3Operator, padTreatyUser;

  private Pipeline pipe1, pipe2;
  private PipelineDetail pipe1Detail, pipe2Detail;

  private PadPipelineOrganisationRoleLink pipe1PadOrg1Holder, pipe1PadOrg3Owner, pipe2PadOrg3Operator, pipe2PadTreatyUser;
  private List<PadPipelineOrganisationRoleLink> padPipeRoleLinks;

  private PwaConsentPipelineOrganisationRoleLink pipe1ConsentOrg1Holder, pipe1ConsentOrg1Owner, pipe2ConsentOrg2User, pipe2ConsentOrg2Operator;

  private ConsentWriterDto consentWriterDto;

  @Before
  public void setUp() throws Exception {

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    masterPwa = detail.getMasterPwa();
    pwaConsent = new PwaConsent();
    pwaConsent.setMasterPwa(masterPwa);
    pwaConsent.setSourcePwaApplication(detail.getPwaApplication());

    pipelineHuooWriter = new PipelineHuooWriter(padPipelinesHuooService, consentPipelineOrgRoleService);

    pipe1 = new Pipeline();
    pipe1.setId(1);
    pipe1Detail = new PipelineDetail(pipe1);
    pipe1Detail.setPipelineStatus(PipelineStatus.IN_SERVICE);

    pipe2 = new Pipeline();
    pipe2.setId(2);
    pipe2Detail = new PipelineDetail(pipe2);
    pipe2Detail.setPipelineStatus(PipelineStatus.OUT_OF_USE_ON_SEABED);

    var pipeToDetailMap = Map.of(pipe1, pipe1Detail, pipe2, pipe2Detail);
    consentWriterDto = new ConsentWriterDto();
    consentWriterDto.setPipelineToNewDetailMap(pipeToDetailMap);

    var initialConsent = new PwaConsent();
    initialConsent.setVariationNumber(0);
    initialConsent.setConsentType(PwaConsentType.INITIAL_PWA);

    var variationConsent = new PwaConsent();
    variationConsent.setVariationNumber(1);
    variationConsent.setConsentType(PwaConsentType.VARIATION);

    var g1 = PortalOrganisationTestUtils.generateOrganisationGroup(1, "1", "1");
    org1 = PortalOrganisationTestUtils.generateOrganisationUnit(1, "1", g1);

    var g2 = PortalOrganisationTestUtils.generateOrganisationGroup(2, "2", "2");
    org2 = PortalOrganisationTestUtils.generateOrganisationUnit(2, "2", g2);

    var g3 = PortalOrganisationTestUtils.generateOrganisationGroup(3, "3", "3");
    org3 = PortalOrganisationTestUtils.generateOrganisationUnit(3, "3", g3);

    consentOrg1Holder = PwaConsentOrganisationRoleTestUtil.createOrganisationRole(initialConsent, new OrganisationUnitId(org1.getOuId()), HuooRole.HOLDER);
    consentOrg1Owner = PwaConsentOrganisationRoleTestUtil.createOrganisationRole(initialConsent, new OrganisationUnitId(org1.getOuId()), HuooRole.OWNER);

    consentOrg2Operator = PwaConsentOrganisationRoleTestUtil.createOrganisationRole(initialConsent, new OrganisationUnitId(org2.getOuId()), HuooRole.OPERATOR);
    consentOrg2User = PwaConsentOrganisationRoleTestUtil.createOrganisationRole(variationConsent, new OrganisationUnitId(org2.getOuId()), HuooRole.USER);

    padOrg1Holder = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.HOLDER, org1);

    padOrg3Operator = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OPERATOR, org3);
    padOrg3Owner = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OWNER, org3);

    padTreatyUser = PadOrganisationRoleTestUtil.createTreatyRole(HuooRole.USER, TreatyAgreement.ANY_TREATY_COUNTRY);

    pipe1PadOrg1Holder = new PadPipelineOrganisationRoleLink(padOrg1Holder, pipe1);
    pipe1PadOrg3Owner = new PadPipelineOrganisationRoleLink(padOrg3Owner, pipe1);

    pipe2PadOrg3Operator = new PadPipelineOrganisationRoleLink(padOrg3Operator, pipe2);
    pipe2PadTreatyUser = new PadPipelineOrganisationRoleLink(padTreatyUser, pipe2);

    padPipeRoleLinks = List.of(pipe1PadOrg1Holder, pipe1PadOrg3Owner, pipe2PadOrg3Operator, pipe2PadTreatyUser);
    when(padPipelinesHuooService.getPadPipelineOrgRoleLinksForDetail(detail))
        .thenReturn(padPipeRoleLinks);

    pipe1ConsentOrg1Holder = new PwaConsentPipelineOrganisationRoleLink(pipe1, consentOrg1Holder);
    pipe1ConsentOrg1Owner = new PwaConsentPipelineOrganisationRoleLink(pipe1, consentOrg1Owner);

    pipe2ConsentOrg2User = new PwaConsentPipelineOrganisationRoleLink(pipe2, consentOrg2User);
    pipe2ConsentOrg2Operator = new PwaConsentPipelineOrganisationRoleLink(pipe2, consentOrg2Operator);

    when(consentPipelineOrgRoleService.getActiveConsentedPipelineOrgRoleLinks(any()))
        .thenReturn(List.of(pipe1ConsentOrg1Holder, pipe1ConsentOrg1Owner, pipe2ConsentOrg2User, pipe2ConsentOrg2Operator));

  }

  @Test
  public void writerIsApplicable_hasPipelineHuooTask() {

    boolean isApplicable = pipelineHuooWriter.writerIsApplicable(Set.of(ApplicationTask.PIPELINES_HUOO), new PwaConsent());

    assertThat(isApplicable).isTrue();

  }

  @Test
  public void writerIsApplicable_doesNotHavePipelineHuooTask() {

    boolean isApplicable = pipelineHuooWriter.writerIsApplicable(Set.of(ApplicationTask.HUOO), new PwaConsent());

    assertThat(isApplicable).isFalse();

  }

  @Test
  public void write_initialPwa() {

    var consent = new PwaConsent();

    when(consentPipelineOrgRoleService.getActiveConsentedPipelineOrgRoleLinks(any())).thenReturn(List.of());

    pipelineHuooWriter.write(detail, consent, consentWriterDto);

    verify(consentPipelineOrgRoleService, times(2)).endRoleLinks(List.of(), consent);

    var expectedAddedRoles = List.of(pipe1PadOrg1Holder, pipe1PadOrg3Owner, pipe2PadOrg3Operator, pipe2PadTreatyUser);
    verify(consentPipelineOrgRoleService, times(1)).createRoleLinks(expectedAddedRoles, consentWriterDto.getActiveConsentRoles(), consent);

  }

  @Test
  public void write_variation_changes() {

    var consent = new PwaConsent();
    pipelineHuooWriter.write(detail, consent, consentWriterDto);

    var expectedEndedRoles = List.of(pipe1ConsentOrg1Owner, pipe2ConsentOrg2User, pipe2ConsentOrg2Operator);
    verify(consentPipelineOrgRoleService, times(1)).endRoleLinks(expectedEndedRoles, consent);

    var expectedAddedRoles = List.of(pipe1PadOrg3Owner, pipe2PadOrg3Operator, pipe2PadTreatyUser);
    verify(consentPipelineOrgRoleService, times(1)).createRoleLinks(expectedAddedRoles, consentWriterDto.getActiveConsentRoles(), consent);

  }

  @Test
  public void write_variation_noChanges() {

    var consent = new PwaConsent();

    var padOrg1Owner = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OWNER, org1);
    var pipe1PadOrg1Owner = new PadPipelineOrganisationRoleLink(padOrg1Owner, pipe1);

    var padOrg2User = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.USER, org2);
    var pipe2PadOrg2User = new PadPipelineOrganisationRoleLink(padOrg2User, pipe2);
    var padOrg2Operator = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OPERATOR, org2);
    var pipe2PadOrg2Operator = new PadPipelineOrganisationRoleLink(padOrg2Operator, pipe2);

    when(padPipelinesHuooService.getPadPipelineOrgRoleLinksForDetail(detail))
        .thenReturn(List.of(pipe1PadOrg1Holder, pipe1PadOrg1Owner, pipe2PadOrg2User, pipe2PadOrg2Operator));

    pipelineHuooWriter.write(detail, consent, consentWriterDto);

    verify(consentPipelineOrgRoleService, times(2)).endRoleLinks(List.of(), consent);

    verify(consentPipelineOrgRoleService, times(1)).createRoleLinks(List.of(), consentWriterDto.getActiveConsentRoles(), consent);

  }

  @Test
  public void write_variation_splitsAppear() {

    var consent = new PwaConsent();

    var padOrg1Owner = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OWNER, org1);
    var pipe1PadOrg1Owner = new PadPipelineOrganisationRoleLink(padOrg1Owner, pipe1);

    var padOrg2Owner = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OWNER, org2);
    var pipe1PadOrg2Owner = new PadPipelineOrganisationRoleLink(padOrg2Owner, pipe1);
    var padOrg2User = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.USER, org2);
    var pipe2PadOrg2User = new PadPipelineOrganisationRoleLink(padOrg2User, pipe2);
    var padOrg2Operator = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OPERATOR, org2);
    var pipe2PadOrg2Operator = new PadPipelineOrganisationRoleLink(padOrg2Operator, pipe2);

    pipe1PadOrg1Owner.setSectionNumber(1);
    pipe1PadOrg1Owner.setFromLocation("from");
    pipe1PadOrg1Owner.setToLocation("middle");
    pipe1PadOrg1Owner.setFromLocationIdentInclusionMode(IdentLocationInclusionMode.INCLUSIVE);
    pipe1PadOrg1Owner.setToLocationIdentInclusionMode(IdentLocationInclusionMode.EXCLUSIVE);

    pipe1PadOrg2Owner.setSectionNumber(2);
    pipe1PadOrg1Owner.setFromLocation("middle");
    pipe1PadOrg1Owner.setToLocation("end");
    pipe1PadOrg1Owner.setFromLocationIdentInclusionMode(IdentLocationInclusionMode.INCLUSIVE);
    pipe1PadOrg1Owner.setToLocationIdentInclusionMode(IdentLocationInclusionMode.INCLUSIVE);

    when(padPipelinesHuooService.getPadPipelineOrgRoleLinksForDetail(detail))
        .thenReturn(List.of(pipe1PadOrg1Holder, pipe1PadOrg1Owner, pipe1PadOrg2Owner, pipe2PadOrg2User, pipe2PadOrg2Operator));

    pipelineHuooWriter.write(detail, consent, consentWriterDto);

    // org1s sole ownership of pipe1 is ended
    var expectedEndedRoles = List.of(pipe1ConsentOrg1Owner);
    verify(consentPipelineOrgRoleService, times(1)).endRoleLinks(expectedEndedRoles, consent);

    // org1 split ownership and org2 split ownership added
    var expectedAddedRoles = List.of(pipe1PadOrg1Owner, pipe1PadOrg2Owner);
    verify(consentPipelineOrgRoleService, times(1)).createRoleLinks(expectedAddedRoles, consentWriterDto.getActiveConsentRoles(), consent);

  }

  @Test
  public void write_variation_splitsChange() {

    var consent = new PwaConsent();

    pipe1ConsentOrg1Owner.setSectionNumber(1);
    pipe1ConsentOrg1Owner.setFromLocation("from");
    pipe1ConsentOrg1Owner.setToLocation("middle");
    pipe1ConsentOrg1Owner.setFromLocationIdentInclusionMode(IdentLocationInclusionMode.INCLUSIVE);
    pipe1ConsentOrg1Owner.setToLocationIdentInclusionMode(IdentLocationInclusionMode.EXCLUSIVE);

    var consentOrg2Owner = PwaConsentOrganisationRoleTestUtil
        .createOrganisationRole(consent, new OrganisationUnitId(org2.getOuId()), HuooRole.OWNER);
    var pipe1ConsentOrg2Owner = new PwaConsentPipelineOrganisationRoleLink(pipe1, consentOrg2Owner);
    pipe1ConsentOrg2Owner.setSectionNumber(2);
    pipe1ConsentOrg2Owner.setFromLocation("middle");
    pipe1ConsentOrg2Owner.setToLocation("end");
    pipe1ConsentOrg2Owner.setFromLocationIdentInclusionMode(IdentLocationInclusionMode.INCLUSIVE);
    pipe1ConsentOrg2Owner.setToLocationIdentInclusionMode(IdentLocationInclusionMode.INCLUSIVE);

    var padOrg1Owner = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OWNER, org1);
    var pipe1PadOrg1Owner = new PadPipelineOrganisationRoleLink(padOrg1Owner, pipe1);
    pipe1PadOrg1Owner.setSectionNumber(1);
    pipe1PadOrg1Owner.setFromLocation("from");
    pipe1PadOrg1Owner.setToLocation("middle");
    pipe1PadOrg1Owner.setFromLocationIdentInclusionMode(IdentLocationInclusionMode.INCLUSIVE);
    pipe1PadOrg1Owner.setToLocationIdentInclusionMode(IdentLocationInclusionMode.EXCLUSIVE);

    pipe1PadOrg3Owner.setSectionNumber(2);
    pipe1PadOrg3Owner.setFromLocation("middle");
    pipe1PadOrg3Owner.setToLocation("end");
    pipe1PadOrg3Owner.setFromLocationIdentInclusionMode(IdentLocationInclusionMode.INCLUSIVE);
    pipe1PadOrg3Owner.setToLocationIdentInclusionMode(IdentLocationInclusionMode.INCLUSIVE);

    when(consentPipelineOrgRoleService.getActiveConsentedPipelineOrgRoleLinks(any()))
        .thenReturn(List.of(pipe1ConsentOrg1Owner, pipe1ConsentOrg2Owner));

    when(padPipelinesHuooService.getPadPipelineOrgRoleLinksForDetail(detail))
        .thenReturn(List.of(pipe1PadOrg1Owner, pipe1PadOrg3Owner));

    pipelineHuooWriter.write(detail, consent, consentWriterDto);

    // the split owned by org2 is ended
    var expectedEndedRoles = List.of(pipe1ConsentOrg2Owner);
    verify(consentPipelineOrgRoleService, times(1)).endRoleLinks(expectedEndedRoles, consent);

    // new split owned by org 3 added
    var expectedAddedRoles = List.of(pipe1PadOrg3Owner);
    verify(consentPipelineOrgRoleService, times(1)).createRoleLinks(expectedAddedRoles, consentWriterDto.getActiveConsentRoles(), consent);

  }

  @Test
  public void write_rts_linksEnded() {

    var consent = new PwaConsent();

    // fake pipe1 being RTS
    consentWriterDto.getPipelineToNewDetailMap().get(pipe1).setPipelineStatus(PipelineStatus.RETURNED_TO_SHORE);

    var consentOrg3Owner = PwaConsentOrganisationRoleTestUtil
        .createOrganisationRole(consent, new OrganisationUnitId(org3.getOuId()), HuooRole.OWNER);
    var pipe1ConsentOrg3Owner = new PwaConsentPipelineOrganisationRoleLink(pipe1, consentOrg3Owner);

    var consentOrg3Operator = PwaConsentOrganisationRoleTestUtil
        .createOrganisationRole(consent, new OrganisationUnitId(org3.getOuId()), HuooRole.OPERATOR);
    var pipe2ConsentOrg3Operator = new PwaConsentPipelineOrganisationRoleLink(pipe2, consentOrg3Operator);

    var consentTreatyUser = PwaConsentOrganisationRoleTestUtil
        .createTreatyRole(consent, TreatyAgreement.ANY_TREATY_COUNTRY, HuooRole.USER);
    var pipe2ConsentTreatyUser = new PwaConsentPipelineOrganisationRoleLink(pipe2, consentTreatyUser);

    when(consentPipelineOrgRoleService.createRoleLinks(any(), any(), any()))
        .thenReturn(List.of(pipe1ConsentOrg3Owner, pipe2ConsentOrg3Operator, pipe2ConsentTreatyUser));

    pipelineHuooWriter.write(detail, consent, consentWriterDto);

    // some pipe1 roles ended as no longer on app
    var endedRolesNoLongerOnApp = List.of(pipe1ConsentOrg1Owner, pipe2ConsentOrg2User, pipe2ConsentOrg2Operator);

    verify(consentPipelineOrgRoleService, times(2)).endRoleLinks(consentOrgRoleLinksCaptor.capture(), eq(consent));

    assertThat(consentOrgRoleLinksCaptor.getAllValues().get(0)).containsExactlyInAnyOrderElementsOf(endedRolesNoLongerOnApp);

    // new pipe1 roles added
    var expectedAddedRoles = List.of(pipe1PadOrg3Owner, pipe2PadOrg3Operator, pipe2PadTreatyUser);
    verify(consentPipelineOrgRoleService, times(1)).createRoleLinks(expectedAddedRoles, consentWriterDto.getActiveConsentRoles(), consent);

    // previous pipe1 and new pipe1 roles ended due to RTS
    var rtsEnded = List.of(pipe1ConsentOrg1Holder, pipe1ConsentOrg3Owner);

    assertThat(consentOrgRoleLinksCaptor.getAllValues().get(1)).containsExactlyInAnyOrderElementsOf(rtsEnded);

  }

  @Test
  public void write_nl_linksEnded() {

    var consent = new PwaConsent();

    // fake pipe2 being NL
    consentWriterDto.getPipelineToNewDetailMap().get(pipe2).setPipelineStatus(PipelineStatus.NEVER_LAID);

    var consentOrg3Owner = PwaConsentOrganisationRoleTestUtil
        .createOrganisationRole(consent, new OrganisationUnitId(org3.getOuId()), HuooRole.OWNER);
    var pipe1ConsentOrg3Owner = new PwaConsentPipelineOrganisationRoleLink(pipe1, consentOrg3Owner);

    var consentOrg3Operator = PwaConsentOrganisationRoleTestUtil
        .createOrganisationRole(consent, new OrganisationUnitId(org3.getOuId()), HuooRole.OPERATOR);
    var pipe2ConsentOrg3Operator = new PwaConsentPipelineOrganisationRoleLink(pipe2, consentOrg3Operator);

    var consentTreatyUser = PwaConsentOrganisationRoleTestUtil
        .createTreatyRole(consent, TreatyAgreement.ANY_TREATY_COUNTRY, HuooRole.USER);
    var pipe2ConsentTreatyUser = new PwaConsentPipelineOrganisationRoleLink(pipe2, consentTreatyUser);

    when(consentPipelineOrgRoleService.createRoleLinks(any(), any(), any()))
        .thenReturn(List.of(pipe1ConsentOrg3Owner, pipe2ConsentOrg3Operator, pipe2ConsentTreatyUser));

    pipelineHuooWriter.write(detail, consent, consentWriterDto);

    // pipe2 roles ended as not on app
    var endedRolesNoLongerOnApp = List.of(pipe1ConsentOrg1Owner, pipe2ConsentOrg2User, pipe2ConsentOrg2Operator);

    verify(consentPipelineOrgRoleService, times(2)).endRoleLinks(consentOrgRoleLinksCaptor.capture(), eq(consent));

    assertThat(consentOrgRoleLinksCaptor.getAllValues().get(0)).containsExactlyInAnyOrderElementsOf(endedRolesNoLongerOnApp);

    // new pipe2 roles added
    var expectedAddedRoles = List.of(pipe1PadOrg3Owner, pipe2PadOrg3Operator, pipe2PadTreatyUser);
    verify(consentPipelineOrgRoleService, times(1)).createRoleLinks(expectedAddedRoles, consentWriterDto.getActiveConsentRoles(), consent);

    // newly added pipe2 roles ended as NL
    var nlEnded = List.of(pipe2ConsentOrg3Operator, pipe2ConsentTreatyUser);

    assertThat(consentOrgRoleLinksCaptor.getAllValues().get(1)).containsExactlyInAnyOrderElementsOf(nlEnded);

  }

  @Test
  public void write_singlePipeOnApp_rts_noError() {

    var consent = new PwaConsent();

    // fake pipe1 being RTS
    consentWriterDto.getPipelineToNewDetailMap().get(pipe1).setPipelineStatus(PipelineStatus.RETURNED_TO_SHORE);

    // remove pipe2 from the 'app'
    var map = new HashMap<Pipeline, PipelineDetail>();
    map.put(pipe1, consentWriterDto.getPipelineToNewDetailMap().get(pipe1));
    consentWriterDto.setPipelineToNewDetailMap(map);

    var consentOrg3Owner = PwaConsentOrganisationRoleTestUtil
        .createOrganisationRole(consent, new OrganisationUnitId(org3.getOuId()), HuooRole.OWNER);
    var pipe1ConsentOrg3Owner = new PwaConsentPipelineOrganisationRoleLink(pipe1, consentOrg3Owner);

    var padOrg2User = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.USER, org2);
    var pipe2PadOrg2User = new PadPipelineOrganisationRoleLink(padOrg2User, pipe2);
    var padOrg2Operator = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OPERATOR, org2);
    var pipe2PadOrg2Operator = new PadPipelineOrganisationRoleLink(padOrg2Operator, pipe2);

    when(consentPipelineOrgRoleService.createRoleLinks(any(), any(), any()))
        .thenReturn(List.of(pipe1ConsentOrg3Owner, pipe2ConsentOrg2Operator, pipe2ConsentOrg2Operator));

    when(padPipelinesHuooService.getPadPipelineOrgRoleLinksForDetail(detail))
        .thenReturn(List.of(pipe1PadOrg1Holder, pipe1PadOrg3Owner, pipe2PadOrg2User, pipe2PadOrg2Operator));

    pipelineHuooWriter.write(detail, consent, consentWriterDto);

    // some pipe1 roles ended as no longer on app
    var endedRolesNoLongerOnApp = List.of(pipe1ConsentOrg1Owner);

    verify(consentPipelineOrgRoleService, times(2)).endRoleLinks(consentOrgRoleLinksCaptor.capture(), eq(consent));

    assertThat(consentOrgRoleLinksCaptor.getAllValues().get(0)).containsExactlyInAnyOrderElementsOf(endedRolesNoLongerOnApp);

    // new pipe1 roles added
    var expectedAddedRoles = List.of(pipe1PadOrg3Owner);
    verify(consentPipelineOrgRoleService, times(1)).createRoleLinks(expectedAddedRoles, consentWriterDto.getActiveConsentRoles(), consent);

    // previous pipe1 and new pipe1 roles ended due to RTS
    var rtsEnded = List.of(pipe1ConsentOrg1Holder, pipe1ConsentOrg3Owner);

    assertThat(consentOrgRoleLinksCaptor.getAllValues().get(1)).containsExactlyInAnyOrderElementsOf(rtsEnded);

  }

}