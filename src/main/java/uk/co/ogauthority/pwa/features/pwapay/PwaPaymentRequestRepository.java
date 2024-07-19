package uk.co.ogauthority.pwa.features.pwapay;


import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
interface PwaPaymentRequestRepository
    extends CrudRepository<PwaPaymentRequest, UUID>, PagingAndSortingRepository<PwaPaymentRequest, UUID> {

}
