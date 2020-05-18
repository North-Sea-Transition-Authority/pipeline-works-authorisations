package uk.co.ogauthority.pwa.repository.pipelines;


import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailMigrationData;

public interface PipelineDetailMigrationDataRepository extends CrudRepository<PipelineDetailMigrationData, Integer> {

}