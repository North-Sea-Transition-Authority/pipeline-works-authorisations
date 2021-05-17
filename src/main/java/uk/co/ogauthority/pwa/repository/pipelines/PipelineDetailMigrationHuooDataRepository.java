package uk.co.ogauthority.pwa.repository.pipelines;


import java.util.List;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailMigrationHuooData;

public interface PipelineDetailMigrationHuooDataRepository extends
    CrudRepository<PipelineDetailMigrationHuooData, Integer>, PipelineDetailMigrationHuooDataDtoRepository {

  List<PipelineDetailMigrationHuooData> findAllByPipelineDetailIn(List<PipelineDetail> pipelineDetails);

}