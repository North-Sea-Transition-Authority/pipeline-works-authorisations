package uk.co.ogauthority.pwa.repository.publicnotice;


import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDocument;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDocumentLink;

@Repository
public interface PublicNoticeDocumentLinkRepository extends CrudRepository<PublicNoticeDocumentLink, Integer> {

  Optional<PublicNoticeDocumentLink> findByAppFile(AppFile appFile);

  Optional<PublicNoticeDocumentLink> findByPublicNoticeDocument(PublicNoticeDocument publicNoticeDocument);

}
