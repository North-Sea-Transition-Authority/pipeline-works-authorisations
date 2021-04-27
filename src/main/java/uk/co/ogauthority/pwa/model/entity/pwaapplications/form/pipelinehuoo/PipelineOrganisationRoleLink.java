package uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinehuoo;

import java.util.Optional;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.dto.pipelines.IdentLocationInclusionMode;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;

public interface PipelineOrganisationRoleLink {

  Pipeline getPipeline();

  HuooRole getRole();

  Optional<OrganisationUnitId> getOrgUnitId();

  Optional<TreatyAgreement> getAgreement();

  String getFromLocation();

  IdentLocationInclusionMode getFromLocationIdentInclusionMode();

  String getToLocation();

  IdentLocationInclusionMode getToLocationIdentInclusionMode();

  Integer getSectionNumber();

}
