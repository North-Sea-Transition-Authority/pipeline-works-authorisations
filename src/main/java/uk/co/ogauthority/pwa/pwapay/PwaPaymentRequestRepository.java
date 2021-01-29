package uk.co.ogauthority.pwa.pwapay;


import java.util.UUID;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
interface PwaPaymentRequestRepository extends  PagingAndSortingRepository<PwaPaymentRequest, UUID> {

}