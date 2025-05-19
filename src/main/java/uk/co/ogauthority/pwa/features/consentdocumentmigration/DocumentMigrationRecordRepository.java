package uk.co.ogauthority.pwa.features.consentdocumentmigration;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentMigrationRecordRepository extends JpaRepository<DocumentMigrationRecord, Long> {

  List<DocumentMigrationRecord> findAllByMigrationSuccessfulIsFalse();

  List<DocumentMigrationRecord> findAllByMigrationSuccessfulIsFalseAndFileLocatedIsTrue();

}
