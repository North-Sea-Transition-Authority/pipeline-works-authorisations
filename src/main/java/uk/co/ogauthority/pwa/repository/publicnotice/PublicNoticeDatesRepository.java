package uk.co.ogauthority.pwa.repository.publicnotice;


import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNotice;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDate;

@Repository
public interface PublicNoticeDatesRepository extends CrudRepository<PublicNoticeDate, Integer> {

  Optional<PublicNoticeDate> getByPublicNoticeAndEndedByPersonIdIsNull(PublicNotice publicNotice);

  List<PublicNoticeDate> getAllByPublicNoticeInAndPublicationStartTimestampBefore(List<PublicNotice> publicNotices,
                                                                                Instant publicationStartTimestamp);

  List<PublicNoticeDate> getAllByPublicNoticeInAndPublicationEndTimestampBefore(List<PublicNotice> publicNotices,
                                                                                Instant publicationEndTimestamp);

}
