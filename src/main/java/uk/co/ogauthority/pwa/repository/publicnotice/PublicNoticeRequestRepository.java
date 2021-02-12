package uk.co.ogauthority.pwa.repository.publicnotice;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeRequest;

@Repository
public interface PublicNoticeRequestRepository extends CrudRepository<PublicNoticeRequest, Integer> {

}
