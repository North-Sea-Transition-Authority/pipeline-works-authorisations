package uk.co.ogauthority.pwa.repository.pwaconsents;


import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentPipelineOrganisationRoleLink;

public interface PwaConsentPipelineOrganisationRoleLinkRepository extends
    CrudRepository<PwaConsentPipelineOrganisationRoleLink, Integer>,
    PwaConsentOrganisationPipelineRoleDtoRepository {

}