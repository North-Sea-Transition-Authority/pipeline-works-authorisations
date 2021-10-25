package uk.co.ogauthority.pwa.service.pwaapplications.huoo;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.repository.pwaapplications.huoo.PadOrganisationRolesRepository;

/**
 * Provides general use case methods that report metadata about an application's HUOO's.
 */
@Service
public class PadHuooRoleMetadataProvider {

  private final PadOrganisationRolesRepository padOrganisationRolesRepository;

  @Autowired
  public PadHuooRoleMetadataProvider(PadOrganisationRolesRepository padOrganisationRolesRepository) {
    this.padOrganisationRolesRepository = padOrganisationRolesRepository;
  }

  /**
   * Return a count of all organisation roles currently on the application.
   *
   * @param pwaApplicationDetail The application detail.
   * @return A map with the role as key, and count as value.
   */
  public Map<HuooRole, Integer> getRoleCountMap(PwaApplicationDetail pwaApplicationDetail) {

    var padOrganisationRoleList = padOrganisationRolesRepository.getAllByPwaApplicationDetail(pwaApplicationDetail).stream()
        .filter(role -> !role.getType().equals(HuooType.UNASSIGNED_PIPELINE_SPLIT))
        .collect(Collectors.toList());

    EnumMap<HuooRole, Integer> map = new EnumMap<>(HuooRole.class);
    HuooRole.stream()
        .forEach(role -> map.put(role, 0));

    padOrganisationRoleList.stream()
        .map(PadOrganisationRole::getRole)
        .forEach(role -> map.put(role, map.get(role) + 1));

    return map;
  }
}
