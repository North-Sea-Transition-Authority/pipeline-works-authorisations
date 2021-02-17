package uk.co.ogauthority.pwa.repository.publicnotice;


import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNotice;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;

@Repository
public interface PublicNoticeRepository extends CrudRepository<PublicNotice, Integer> {

  Optional<PublicNotice> findFirstByPwaApplicationOrderByVersionDesc(PwaApplication pwaApplication);

  List<PublicNotice> findAllByPwaApplicationOrderByVersionDesc(PwaApplication pwaApplication);

}
