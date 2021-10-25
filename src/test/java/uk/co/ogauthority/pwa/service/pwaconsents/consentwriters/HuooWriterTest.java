package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentOrganisationRole;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentOrganisationRoleTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleTestUtil;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines.ConsentWriterDto;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class HuooWriterTest {

  @Mock
  private PwaConsentOrganisationRoleService consentOrganisationRoleService;

  @Mock
  private PadOrganisationRoleService padOrganisationRoleService;

  @Mock
  private PwaConsentService pwaConsentService;

  private HuooWriter huooWriter;

  private PwaApplicationDetail detail;
  private PwaConsent pwaConsent;
  private MasterPwa masterPwa;

  private List<PwaConsent> consents;

  private PortalOrganisationUnit org1, org2, org3;

  private PwaConsentOrganisationRole consentOrg1Holder, consentOrg1Owner, consentOrg2User, consentOrg2Operator;
  private PadOrganisationRole padOrg1Holder, padOrg3Owner, padOrg3Operator, padTreatyUser;

  private List<PwaConsentOrganisationRole> consentedRoles;

  private ConsentWriterDto consentWriterDto;

  @Before
  public void setUp() throws Exception {

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    masterPwa = detail.getMasterPwa();
    pwaConsent = new PwaConsent();
    pwaConsent.setMasterPwa(masterPwa);
    pwaConsent.setSourcePwaApplication(detail.getPwaApplication());

    huooWriter = new HuooWriter(consentOrganisationRoleService, padOrganisationRoleService, pwaConsentService);

    var initialConsent = new PwaConsent();
    initialConsent.setVariationNumber(0);
    initialConsent.setConsentType(PwaConsentType.INITIAL_PWA);

    var variationConsent = new PwaConsent();
    variationConsent.setVariationNumber(1);
    variationConsent.setConsentType(PwaConsentType.VARIATION);

    consents = List.of(initialConsent, variationConsent);
    when(pwaConsentService.getConsentsByMasterPwa(detail.getMasterPwa())).thenReturn(consents);

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

    consentedRoles = List.of(consentOrg1Holder, consentOrg1Owner, consentOrg2Operator, consentOrg2User);

    when(consentOrganisationRoleService.getActiveOrgRolesAddedByConsents(consents)).thenReturn(consentedRoles);

    padOrg1Holder = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.HOLDER, org1);

    padOrg3Operator = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OPERATOR, org3);
    padOrg3Owner = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OWNER, org3);

    padTreatyUser = PadOrganisationRoleTestUtil.createTreatyRole(HuooRole.USER, TreatyAgreement.ANY_TREATY_COUNTRY);

    when(padOrganisationRoleService.getOrgRolesForDetail(detail)).thenReturn(List.of(padOrg1Holder, padOrg3Operator, padOrg3Owner, padTreatyUser));

    consentWriterDto = new ConsentWriterDto();

  }

  @Test
  public void write_initialPwa() {

    pwaConsent.setVariationNumber(0);

    when(consentOrganisationRoleService.createNewConsentOrgUnitRoles(any(), any())).thenReturn(List.of(consentOrg1Holder));

    var treatyConsentRole = PwaConsentOrganisationRoleTestUtil
        .createTreatyRole(pwaConsent, TreatyAgreement.ANY_TREATY_COUNTRY, HuooRole.USER);
    when(consentOrganisationRoleService.createNewConsentTreatyRoles(any(), any())).thenReturn(List.of(treatyConsentRole));

    huooWriter.write(detail, pwaConsent, consentWriterDto);

    // no interaction with consented data
    verifyNoInteractions(pwaConsentService);
    verify(consentOrganisationRoleService, times(0)).getActiveOrgRolesAddedByConsents(any());
    verify(consentOrganisationRoleService, times(0)).endConsentOrgRoles(eq(pwaConsent), any());

    // all app org roles added
    Multimap<OrganisationUnitId, HuooRole> expectedOrgUnitRolesCreated = HashMultimap.create();
    expectedOrgUnitRolesCreated.put(new OrganisationUnitId(org1.getOuId()), HuooRole.HOLDER);
    expectedOrgUnitRolesCreated.putAll(new OrganisationUnitId(org3.getOuId()), Set.of(HuooRole.OPERATOR, HuooRole.OWNER));

    verify(consentOrganisationRoleService, times(1)).createNewConsentOrgUnitRoles(pwaConsent, expectedOrgUnitRolesCreated);

    // all app treaty roles added
    Multimap<TreatyAgreement, HuooRole> expectedTreatyRolesCreated = HashMultimap.create();
    expectedTreatyRolesCreated.put(TreatyAgreement.ANY_TREATY_COUNTRY, HuooRole.USER);

    verify(consentOrganisationRoleService, times(1)).createNewConsentTreatyRoles(pwaConsent, expectedTreatyRolesCreated);

    assertThat(consentWriterDto.getActiveConsentRoles()).containsOnly(consentOrg1Holder, treatyConsentRole);
    assertThat(consentWriterDto.getConsentRolesEnded()).isEmpty();

  }

  @Test
  public void write_variation_noChanges() {

    pwaConsent.setVariationNumber(1);

    // recreate consent org roles in app roles
    var padOrg1Owner = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OWNER, org1);

    var padOrg2Operator = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OPERATOR, org2);
    var padOrg2User = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.USER, org2);

    when(padOrganisationRoleService.getOrgRolesForDetail(detail))
        .thenReturn(List.of(padOrg1Holder, padOrg1Owner, padOrg2Operator, padOrg2User));

    huooWriter.write(detail, pwaConsent, consentWriterDto);

    verify(consentOrganisationRoleService, times(1)).getActiveOrgRolesAddedByConsents(consents);

    // expect end method is called with empty list, nothing to do
    var expectedConsentRolesEnded = new ArrayList<PwaConsentOrganisationRole>();
    verify(consentOrganisationRoleService, times(1)).endConsentOrgRoles(pwaConsent, expectedConsentRolesEnded);

    // expect add org method is called with empty map, nothing to do
    Multimap<OrganisationUnitId, HuooRole> expectedOrgUnitRolesCreated = HashMultimap.create();
    verify(consentOrganisationRoleService, times(1)).createNewConsentOrgUnitRoles(pwaConsent, expectedOrgUnitRolesCreated);

    // expect add treaty method is called with empty map, nothing to do
    Multimap<TreatyAgreement, HuooRole> expectedTreatyRolesCreated = HashMultimap.create();
    verify(consentOrganisationRoleService, times(1)).createNewConsentTreatyRoles(pwaConsent, expectedTreatyRolesCreated);

    assertThat(consentWriterDto.getActiveConsentRoles()).containsAll(consentedRoles);
    assertThat(consentWriterDto.getConsentRolesEnded()).isEmpty();

  }

  @Test
  public void write_variation_noAdditions_existingMigratedOrgRoles() {

    var initialConsent = new PwaConsent();
    initialConsent.setVariationNumber(0);
    initialConsent.setConsentType(PwaConsentType.INITIAL_PWA);

    var migratedHolderOrg = PwaConsentOrganisationRoleTestUtil
        .createMigratedOrgRole(initialConsent, "Some org", HuooRole.HOLDER);
    var migratedUserOrg = PwaConsentOrganisationRoleTestUtil
        .createMigratedOrgRole(initialConsent, "Some org", HuooRole.USER);
    var migratedOwnerOrg = PwaConsentOrganisationRoleTestUtil
        .createMigratedOrgRole(initialConsent, "Some org", HuooRole.OWNER);
    var migratedOperatorOrg = PwaConsentOrganisationRoleTestUtil
        .createMigratedOrgRole(initialConsent, "Some org", HuooRole.OPERATOR);

    consentedRoles = List.of(migratedHolderOrg, migratedUserOrg, migratedOwnerOrg, migratedOperatorOrg);

    when(consentOrganisationRoleService.getActiveOrgRolesAddedByConsents(consents)).thenReturn(consentedRoles);

    pwaConsent.setVariationNumber(1);

    when(padOrganisationRoleService.getOrgRolesForDetail(detail))
        .thenReturn(List.of());

    huooWriter.write(detail, pwaConsent, consentWriterDto);

    verify(consentOrganisationRoleService, times(1)).getActiveOrgRolesAddedByConsents(consents);

    verify(consentOrganisationRoleService, times(1))
        .endConsentOrgRoles(pwaConsent, List.of(migratedHolderOrg, migratedUserOrg, migratedOwnerOrg, migratedOperatorOrg));

    // expect add org method is called with empty map, nothing to do
    Multimap<OrganisationUnitId, HuooRole> expectedOrgUnitRolesCreated = HashMultimap.create();
    verify(consentOrganisationRoleService, times(1)).createNewConsentOrgUnitRoles(pwaConsent, expectedOrgUnitRolesCreated);

    // expect add treaty method is called with empty map, nothing to do
    Multimap<TreatyAgreement, HuooRole> expectedTreatyRolesCreated = HashMultimap.create();
    verify(consentOrganisationRoleService, times(1)).createNewConsentTreatyRoles(pwaConsent, expectedTreatyRolesCreated);

    assertThat(consentWriterDto.getActiveConsentRoles()).isEmpty();
    assertThat(consentWriterDto.getConsentRolesEnded()).containsAll(consentedRoles);

  }

  @Test
  public void write_variation_changes() {

    pwaConsent.setVariationNumber(1);

    huooWriter.write(detail, pwaConsent, consentWriterDto);

    verify(consentOrganisationRoleService, times(1)).getActiveOrgRolesAddedByConsents(consents);

    // expect org 1 roles are reduced, org2 is removed from the consented data
    var expectedConsentRolesEnded = List.of(consentOrg1Owner, consentOrg2Operator, consentOrg2User);
    verify(consentOrganisationRoleService, times(1)).endConsentOrgRoles(pwaConsent, expectedConsentRolesEnded);

    // expect org3 is added to consented data
    Multimap<OrganisationUnitId, HuooRole> expectedOrgUnitRolesCreated = HashMultimap.create();
    expectedOrgUnitRolesCreated.putAll(new OrganisationUnitId(org3.getOuId()), Set.of(HuooRole.OPERATOR, HuooRole.OWNER));
    verify(consentOrganisationRoleService, times(1)).createNewConsentOrgUnitRoles(pwaConsent, expectedOrgUnitRolesCreated);

    // expect treaty user is added to consented data
    Multimap<TreatyAgreement, HuooRole> expectedTreatyRolesCreated = HashMultimap.create();
    expectedTreatyRolesCreated.put(TreatyAgreement.ANY_TREATY_COUNTRY, HuooRole.USER);
    verify(consentOrganisationRoleService, times(1)).createNewConsentTreatyRoles(pwaConsent, expectedTreatyRolesCreated);

    assertThat(consentWriterDto.getActiveConsentRoles()).containsOnly(consentOrg1Holder);
    assertThat(consentWriterDto.getConsentRolesEnded()).containsOnlyElementsOf(expectedConsentRolesEnded);

  }

}