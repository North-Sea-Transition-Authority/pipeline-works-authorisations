package uk.co.ogauthority.pwa.service.teams;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Failed to construct a PwaTeam from PortalTeam")
class PwaTeamFactoryException extends RuntimeException {

  public PwaTeamFactoryException(String message) {
    super(message);
  }

  public PwaTeamFactoryException(String message, Throwable cause) {
    super(message, cause);
  }
}