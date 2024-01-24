package uk.co.ogauthority.pwa.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import uk.co.ogauthority.pwa.externalapi.RestError;

@Component
public class ExternalApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper mapper;

  @Autowired
  public ExternalApiAuthenticationEntryPoint(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public void commence(HttpServletRequest request,
                       HttpServletResponse response,
                       AuthenticationException authException) throws IOException {
    var error = new RestError(HttpStatus.UNAUTHORIZED.value(), "Invalid pre-shared key");
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(error.getStatus());
    var responseStream = response.getOutputStream();
    mapper.writeValue(responseStream, error);
    responseStream.flush();
  }
}
