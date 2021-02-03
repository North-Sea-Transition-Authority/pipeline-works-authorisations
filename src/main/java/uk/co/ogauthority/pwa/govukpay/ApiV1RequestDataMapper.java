package uk.co.ogauthority.pwa.govukpay;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.govukpay.api.cardpayment.request.CreateCardPaymentRequest;
import uk.co.ogauthority.pwa.govukpay.api.cardpayment.response.CreatePaymentResult;
import uk.co.ogauthority.pwa.govukpay.api.cardpayment.response.GetPaymentResult;

@Service
final class ApiV1RequestDataMapper {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApiV1RequestDataMapper.class);

  @Autowired
  ApiV1RequestDataMapper() {
  }

  CreateCardPaymentRequest mapNewCardPaymentRequest(NewCardPaymentRequest newCardPaymentRequest) {
    LOGGER.trace("mapNewCardPaymentRequest - param newCardPaymentRequest {}", newCardPaymentRequest);

    var cardPaymentRequest = new CreateCardPaymentRequest(
        newCardPaymentRequest.getAmount(),
        newCardPaymentRequest.getReference(),
        newCardPaymentRequest.getDescription(),
        newCardPaymentRequest.getReturnUrl()
    );

    if (!newCardPaymentRequest.getMetadata().isEmpty()) {
      var metadataMap = new HashMap<String, Object>(newCardPaymentRequest.getMetadata());
      cardPaymentRequest.addMetadata(metadataMap);
    }

    return cardPaymentRequest;

  }

  NewCardPaymentResult mapNewCardPaymentResult(CreatePaymentResult createPaymentResult) {
    LOGGER.trace("mapNewCardPaymentResponse - param createPaymentResult {}", createPaymentResult);

    var paymentState = new PaymentJourneyState(
        createPaymentResult.getState().getStatus(),
        createPaymentResult.getState().isFinished(),
        createPaymentResult.getState().getMessage(),
        createPaymentResult.getState().getCode()
    );

    return new NewCardPaymentResult(
        createPaymentResult.getPaymentId(),
        paymentState,
        createPaymentResult.getReturnUrl(),
        createPaymentResult.getLinks().getNextUrl().getHref(),
        createPaymentResult.getCreatedDate(),
        createPaymentResult.getMetadata()
    );

  }


  PaymentJourneyData mapGetPaymentResult(GetPaymentResult result) {

    var paymentState = new PaymentJourneyState(
        result.getState().getStatus(),
        result.getState().isFinished(),
        result.getState().getMessage(),
        result.getState().getCode()
    );

    return new PaymentJourneyData(
        result.getPaymentId(),
        paymentState,
        result.getAmount(),
        result.getDescription(),
        result.getReference(),
        result.getMetadata(),
        result.getEmail(),
        result.getPaymentProvider(),
        result.getCreatedDate(),
        result.getRefundSummary().toString(),
        result.getSettlementSummary().toString(),
        result.getCorporateCardSurcharge(),
        result.getTotalAmount(),
        result.getFee(),
        result.getNetAmount(),
        result.getProviderId(),
        result.getReturnUrl()
    );


  }


}
