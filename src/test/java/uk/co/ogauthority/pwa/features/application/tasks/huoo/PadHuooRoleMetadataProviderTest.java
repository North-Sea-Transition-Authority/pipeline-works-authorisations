package uk.co.ogauthority.pwa.features.application.tasks.huoo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class PadHuooRoleMetadataProviderTest {

  @Mock
  private PadOrganisationRolesRepository padOrganisationRolesRepository;

  private PadHuooRoleMetadataProvider padHuooRoleMetadataProvider;

  private PwaApplicationDetail detail;

  @BeforeEach
  void setup() {
    padHuooRoleMetadataProvider = new PadHuooRoleMetadataProvider(padOrganisationRolesRepository);

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

  }


  @Test
  void getRoleCountMap_noUnassignedRolesPresent() {

    var unassignedUserRole = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.USER);
    unassignedUserRole.setType(HuooType.UNASSIGNED_PIPELINE_SPLIT);

    when(padOrganisationRolesRepository.getAllByPwaApplicationDetail(detail)).thenReturn(List.of(
        PadOrganisationRoleTestUtil.createOrgRole(HuooRole.HOLDER),
        PadOrganisationRoleTestUtil.createOrgRole(HuooRole.USER),
        PadOrganisationRoleTestUtil.createOrgRole(HuooRole.USER),
        unassignedUserRole,
        PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OPERATOR),
        PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OPERATOR),
        PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OPERATOR)
    ));

    var result = padHuooRoleMetadataProvider.getRoleCountMap(detail);
    assertThat(result).containsExactlyInAnyOrderEntriesOf(
        Map.ofEntries(
            entry(HuooRole.HOLDER, 1),
            entry(HuooRole.USER, 2),
            entry(HuooRole.OPERATOR, 3),
            entry(HuooRole.OWNER, 0)
        ));
  }



}