package uk.co.ogauthority.pwa.repository.migration;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.migration.PipelineMigrationConfig;

@Repository
public interface PipelineMigrationConfigRepository extends CrudRepository<PipelineMigrationConfig, Integer> {

}