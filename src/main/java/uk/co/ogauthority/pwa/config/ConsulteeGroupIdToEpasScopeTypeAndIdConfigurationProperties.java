package uk.co.ogauthority.pwa.config;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.validation.annotation.Validated;
import uk.co.fivium.energyportal.serviceproviders.epmq.ScopeType;

@ConfigurationProperties(prefix = "consultee-group-id-to-epas-scope-type-and-id")
@Validated
public record ConsulteeGroupIdToEpasScopeTypeAndIdConfigurationProperties(
    @Name("") Map<Integer, ScopeTypeAndIdDto> consulteeGroupIdToScopeIdAndType
) {
  public boolean hasAssociatedEpas(String scopeId) {
    int id;
    try {
      id = Integer.parseInt(scopeId);
    } catch (NumberFormatException e) {
      return false;
    }
    return this.consulteeGroupIdToScopeIdAndType.containsKey(id);
  }

  public ScopeTypeAndIdDto getScopeTypeAndEpasScopeId(String scopeId) {
    return this.consulteeGroupIdToScopeIdAndType.get(Integer.parseInt(scopeId));
  }

  public record ScopeTypeAndIdDto(ScopeType scopeType, Integer scopeId) {
  }
}
