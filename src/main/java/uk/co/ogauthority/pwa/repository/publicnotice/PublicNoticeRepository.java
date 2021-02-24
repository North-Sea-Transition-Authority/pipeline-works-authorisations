package uk.co.ogauthority.pwa.repository.publicnotice;


import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNotice;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;

@Repository
public interface PublicNoticeRepository extends CrudRepository<PublicNotice, Integer> {

  Optional<PublicNotice> findFirstByPwaApplicationOrderByVersionDesc(PwaApplication pwaApplication);

  List<PublicNotice> findAllByPwaApplicationOrderByVersionDesc(PwaApplication pwaApplication);

  List<PublicNotice> findAllByStatusNotIn(Set<PublicNoticeStatus> statuses);

  List<PublicNotice> findAllByPwaApplication(PwaApplication application);

}
