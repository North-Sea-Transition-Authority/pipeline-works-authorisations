package uk.co.ogauthority.pwa.repository.pwaapplications.huoo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooType;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;

@Repository
public interface PadOrganisationRolesRepository extends CrudRepository<PadOrganisationRole, Integer>,
    PadOrganisationRolesDtoRepository {

  @EntityGraph(attributePaths = {"organisationUnit", "organisationUnit.portalOrganisationGroup"})
  List<PadOrganisationRole> getAllByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  @EntityGraph(attributePaths = {"organisationUnit", "organisationUnit.portalOrganisationGroup"})
  List<PadOrganisationRole> getAllByPwaApplicationDetailAndRole(PwaApplicationDetail pwaApplicationDetail,
                                                                HuooRole huooRole);

  @EntityGraph(attributePaths = {"organisationUnit", "organisationUnit.portalOrganisationGroup"})
  List<PadOrganisationRole> getAllByPwaApplicationDetailAndRoleAndType(PwaApplicationDetail pwaApplicationDetail,
                                                                       HuooRole role,
                                                                       HuooType type);

  List<PadOrganisationRole> getAllByPwaApplicationDetailAndOrganisationUnit(PwaApplicationDetail pwaApplicationDetail,
                                                                            PortalOrganisationUnit organisationUnit);

  Optional<PadOrganisationRole> getByPwaApplicationDetailAndId(PwaApplicationDetail pwaApplicationDetail, Integer id);

  long countPadOrganisationRoleByPwaApplicationDetailAndRoleAndType(PwaApplicationDetail pwaApplicationDetail,
                                                                    HuooRole huooRole,
                                                                    HuooType huooType);

}
