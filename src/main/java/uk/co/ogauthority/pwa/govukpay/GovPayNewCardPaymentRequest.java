package uk.co.ogauthority.pwa.govukpay;


import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Client apps use this object to create a new card payment journey.
 */
public final class GovPayNewCardPaymentRequest {

  private static final int MINIMUM_AMOUNT = 0;
  private static final int MAXIMUM_AMOUNT = 10000000;

  private final Integer amount;

  private final String reference;

  private final String description;

  private final String returnUrl;

  private final Map<String, String> metadata;

  /*
   * Payment request with metadata
   */
  public GovPayNewCardPaymentRequest(Integer amount,
                                     String reference,
                                     String description,
                                     String returnUrl,
                                     Map<String, String> metadata) {
    Objects.requireNonNull(amount);
    Objects.requireNonNull(reference);
    Objects.requireNonNull(description);
    Objects.requireNonNull(returnUrl);
    Objects.requireNonNull(metadata);

    if (!(amount >= MINIMUM_AMOUNT && amount <= MAXIMUM_AMOUNT)) {
      throw new IllegalArgumentException(
          String.format(
              "payment amount must be a between %s and %s ",
              MINIMUM_AMOUNT,
              MAXIMUM_AMOUNT
          )
      );
    }

    if (reference.isBlank()) {
      throw new IllegalArgumentException("Reference must not be blank");
    }

    if (returnUrl.isBlank()) {
      throw new IllegalArgumentException("return url must not be blank");
    }

    validateMetadataMap(metadata);

    this.amount = amount;
    this.reference = reference;
    this.description = description;
    this.returnUrl = returnUrl;
    this.metadata = Collections.unmodifiableMap(metadata);
  }

  /*
   * Payment request with minimal data
   */
  public GovPayNewCardPaymentRequest(Integer amount, String reference, String description, String returnUrl) {
    this(amount, reference, description, returnUrl, Map.of());
  }

  /**
   * amount in pence.
   * minimum: 0
   * maximum: 10000000
   *
   * @return amount
   **/
  public Integer getAmount() {
    return amount;
  }

  /**
   * payment reference.
   *
   * @return reference
   **/
  public String getReference() {
    return reference;
  }

  /**
   * payment description.
   *
   * @return description
   **/
  public String getDescription() {
    return description;
  }

  /**
   * service return url.
   *
   * @return returnUrl
   **/
  public String getReturnUrl() {
    return returnUrl;
  }


  /**
   * Additional metadata - up to 10 name/value pairs - on the payment.
   * Each key must be between 1 and 30 characters long. The value, if a string, must be no greater than 50 characters long.
   *
   * @return metadata
   **/
  public Map<String, String> getMetadata() {
    return metadata;
  }

  private void validateMetadataMap(Map<String, String> metadata) {
    if (!metadata.keySet().stream().allMatch(s -> s.length() > 0 && s.length() <= 30)) {
      throw new IllegalArgumentException("Metadata map must only contain keys 1 to 30 chars.");
    }

    if (!metadata.values().stream().allMatch(s -> s.length() > 0 && s.length() <= 50)) {
      throw new IllegalArgumentException("Metadata map must only contain values between 1 to 50 chars.");
    }


  }


}

