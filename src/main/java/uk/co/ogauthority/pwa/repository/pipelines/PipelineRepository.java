package uk.co.ogauthority.pwa.repository.pipelines;


import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;

public interface PipelineRepository extends CrudRepository<Pipeline, Integer> {

}