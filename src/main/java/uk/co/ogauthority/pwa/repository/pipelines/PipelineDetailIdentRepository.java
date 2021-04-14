package uk.co.ogauthority.pwa.repository.pipelines;


import java.util.Collection;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailIdent;

public interface PipelineDetailIdentRepository extends CrudRepository<PipelineDetailIdent, Integer> {

  List<PipelineDetailIdent> findByPipelineDetail_Pipeline_IdInAndPipelineDetail_tipFlagIsTrue(
      Collection<Integer> pipelineIds
  );

  List<PipelineDetailIdent> findAllByPipelineDetail_id(Integer pipelineDetailId);


}