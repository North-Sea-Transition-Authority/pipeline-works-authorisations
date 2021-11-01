package uk.co.ogauthority.pwa.features.application.tasks.pipelines.setnumber;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PipelineMigrationConfigRepository extends CrudRepository<PipelineMigrationConfig, Integer> {

}