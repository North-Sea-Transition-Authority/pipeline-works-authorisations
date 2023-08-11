package uk.co.ogauthority.pwa.repository.publicnotice;


import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeDocumentType;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNotice;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDocument;

@Repository
public interface PublicNoticeDocumentRepository extends CrudRepository<PublicNoticeDocument, Integer> {

  Optional<PublicNoticeDocument> findByPublicNoticeAndDocumentType(PublicNotice publicNotice,
                                                                   PublicNoticeDocumentType publicNoticeDocumentType);

  List<PublicNoticeDocument> findAllByPublicNotice(PublicNotice publicNotice);
}
