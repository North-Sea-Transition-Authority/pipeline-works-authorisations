package uk.co.ogauthority.pwa.repository.pipelines;


import java.util.List;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailIdentData;

public interface PipelineDetailIdentDataRepository extends CrudRepository<PipelineDetailIdentData, Integer> {

  List<PipelineDetailIdentData> getAllByPipelineDetailIdent_PipelineDetail(PipelineDetail pipelineDetail);

}