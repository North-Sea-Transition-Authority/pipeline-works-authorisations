package uk.co.ogauthority.pwa.govukpay;

import java.util.Optional;

/**
 * Container class to avoid returning nulls or throwing client errors on bad requests.
 * Allows caller to decide what the appropriate action is.
 */
public final class GovUkResponseWrapper<T> {

  private final int httpCode;

  private final String errorBody;

  private final T contents;

  private GovUkResponseWrapper(int httpCode, String errorBody, T contents) {
    this.contents = contents;
    this.errorBody = errorBody;
    this.httpCode = httpCode;
  }

  public int getHttpCode() {
    return httpCode;
  }

  public String getErrorBody() {
    return errorBody;
  }

  public Optional<T> getContents() {
    return Optional.ofNullable(contents);
  }

  public static <Y> GovUkResponseWrapper<Y> wrap(Y contents,
                                                 int httpCode) {
    return new GovUkResponseWrapper<>(httpCode, null, contents);
  }

  public static <Y> GovUkResponseWrapper<Y> wrapError(Class<Y> anticipatedContentClass,
                                                      int httpCode,
                                                      String errorBody) {
    return new GovUkResponseWrapper<Y>(httpCode, errorBody, null);
  }
}
