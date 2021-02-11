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
class ApiV1RequestDataMapper {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApiV1RequestDataMapper.class);

  @Autowired
  ApiV1RequestDataMapper() {
  }

  CreateCardPaymentRequest mapNewCardPaymentRequest(GovPayNewCardPaymentRequest govPayNewCardPaymentRequest) {
    LOGGER.trace("mapNewCardPaymentRequest - param newCardPaymentRequest {}", govPayNewCardPaymentRequest);

    var cardPaymentRequest = new CreateCardPaymentRequest(
        govPayNewCardPaymentRequest.getAmount(),
        govPayNewCardPaymentRequest.getReference(),
        govPayNewCardPaymentRequest.getDescription(),
        govPayNewCardPaymentRequest.getReturnUrl()
    );

    if (!govPayNewCardPaymentRequest.getMetadata().isEmpty()) {
      var metadataMap = new HashMap<String, Object>(govPayNewCardPaymentRequest.getMetadata());
      cardPaymentRequest.addMetadata(metadataMap);
    }

    return cardPaymentRequest;

  }

  GovPayNewCardPaymentResult mapNewCardPaymentResult(CreatePaymentResult createPaymentResult) {
    LOGGER.trace("mapNewCardPaymentResponse - param createPaymentResult {}", createPaymentResult);

    var paymentState = new GovPayPaymentJourneyState(
        createPaymentResult.getState().getStatus(),
        createPaymentResult.getState().isFinished(),
        createPaymentResult.getState().getMessage(),
        createPaymentResult.getState().getCode()
    );

    return new GovPayNewCardPaymentResult(
        createPaymentResult.getPaymentId(),
        paymentState,
        createPaymentResult.getReturnUrl(),
        createPaymentResult.getLinks().getNextUrl().getHref(),
        createPaymentResult.getCreatedDate(),
        createPaymentResult.getMetadata()
    );

  }


  GovPayPaymentJourneyData mapGetPaymentResult(GetPaymentResult result) {

    var paymentState = new GovPayPaymentJourneyState(
        result.getState().getStatus(),
        result.getState().isFinished(),
        result.getState().getMessage(),
        result.getState().getCode()
    );

    return new GovPayPaymentJourneyData(
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
