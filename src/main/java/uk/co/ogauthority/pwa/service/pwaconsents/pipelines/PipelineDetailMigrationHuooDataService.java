package uk.co.ogauthority.pwa.service.pwaconsents.pipelines;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.dto.huooaggregations.OrganisationRolesSummaryDto;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailMigrationHuooData;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineDetailMigrationHuooDataRepository;

@Service
public class PipelineDetailMigrationHuooDataService {

  private final PipelineDetailMigrationHuooDataRepository pipelineDetailMigrationHuooDataRepository;

  @Autowired
  public PipelineDetailMigrationHuooDataService(
      PipelineDetailMigrationHuooDataRepository pipelineDetailMigrationHuooDataRepository) {
    this.pipelineDetailMigrationHuooDataRepository = pipelineDetailMigrationHuooDataRepository;
  }

  public OrganisationRolesSummaryDto getOrganisationRoleSummaryForHuooMigratedData(PipelineDetail pipelineDetail) {

    var organisationPipelineRoles = pipelineDetailMigrationHuooDataRepository.findHuooMigrationDataByPipelineDetail(pipelineDetail);
    return OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(organisationPipelineRoles);
  }

  public List<PipelineDetailMigrationHuooData> getPipelineDetailMigratedHuoos(List<PipelineDetail> pipelineDetails) {
    return pipelineDetailMigrationHuooDataRepository.findAllByPipelineDetailIn(pipelineDetails);
  }




}
