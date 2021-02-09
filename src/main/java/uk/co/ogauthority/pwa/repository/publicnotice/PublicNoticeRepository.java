package uk.co.ogauthority.pwa.repository.publicnotice;


import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNotice;


public interface PublicNoticeRepository extends CrudRepository<PublicNotice, Integer> {

}
