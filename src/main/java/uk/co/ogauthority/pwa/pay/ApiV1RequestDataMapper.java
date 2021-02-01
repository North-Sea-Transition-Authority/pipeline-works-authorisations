package uk.co.ogauthority.pwa.pay;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.pay.api.model.cardpayment.request.CreateCardPaymentRequest;
import uk.co.ogauthority.pwa.pay.api.model.cardpayment.response.CreatePaymentResult;

@Service
final class ApiV1RequestDataMapper {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApiV1RequestDataMapper.class);

  private final GovUkPayConfiguration govUkPayConfiguration;

  @Autowired
  ApiV1RequestDataMapper(GovUkPayConfiguration govUkPayConfiguration) {
    this.govUkPayConfiguration = govUkPayConfiguration;
  }

  CreateCardPaymentRequest mapNewCardPaymentRequest(NewCardPaymentRequest newCardPaymentRequest) {

    if (govUkPayConfiguration.isDebugEnabled()) {
      LOGGER.debug("mapNewCardPaymentRequest - param newCardPaymentRequest {}", newCardPaymentRequest.toString());
    }
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

  NewCardPaymentResult mapNewCardPaymentResponse(CreatePaymentResult createPaymentResult) {
    if (govUkPayConfiguration.isDebugEnabled()) {
      LOGGER.debug("mapNewCardPaymentResponse - param createPaymentResult {}", createPaymentResult.toString());
    }

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


}
