package uk.co.ogauthority.pwa.domain.pwa.pipelinehuoo;

import java.util.Optional;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.TreatyAgreement;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.IdentLocationInclusionMode;
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
