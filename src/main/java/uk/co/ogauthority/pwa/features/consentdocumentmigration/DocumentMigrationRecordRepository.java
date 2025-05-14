package uk.co.ogauthority.pwa.features.consentdocumentmigration;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentMigrationRecordRepository extends CrudRepository<DocumentMigrationRecord, Long> {

  List<DocumentMigrationRecord> findAll();

  List<DocumentMigrationRecord> findAllByMigrationSuccessfulIsFalse();

  List<DocumentMigrationRecord> findAllByMigrationSuccessfulIsFalseAndFileLocatedIsTrue();

}
