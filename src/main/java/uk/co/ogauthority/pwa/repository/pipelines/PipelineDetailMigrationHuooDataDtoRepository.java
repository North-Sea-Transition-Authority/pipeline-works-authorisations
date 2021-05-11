package uk.co.ogauthority.pwa.repository.pipelines;


import java.util.List;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationPipelineRoleInstanceDto;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;

public interface PipelineDetailMigrationHuooDataDtoRepository {

  List<OrganisationPipelineRoleInstanceDto> findHuooMigrationDataByPipelineDetail(PipelineDetail pipelineDetail);

}