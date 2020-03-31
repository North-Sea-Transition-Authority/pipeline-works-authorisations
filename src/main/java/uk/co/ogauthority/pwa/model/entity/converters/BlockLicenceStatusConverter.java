package uk.co.ogauthority.pwa.model.entity.converters;

import java.util.Optional;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import org.apache.commons.lang3.StringUtils;
import uk.co.ogauthority.pwa.model.entity.enums.BlockLicenceStatus;

@Converter
public class BlockLicenceStatusConverter implements AttributeConverter<BlockLicenceStatus, String> {

  @Override
  public String convertToDatabaseColumn(BlockLicenceStatus status) {
    var converted = Optional.ofNullable(status)
        .stream()
        .map(BlockLicenceStatus::getInternalCharacter)
        .findAny()
        .orElse("");
    if (converted.isBlank()) {
      return null;
    }
    return converted;
  }

  @Override
  public BlockLicenceStatus convertToEntityAttribute(String status) {
    if (StringUtils.isBlank(status)) {
      return null;
    }
    return BlockLicenceStatus.stream()
        .filter(blockLicenceStatus -> blockLicenceStatus.getInternalCharacter().equals(status))
        .findAny()
        .orElse(null);
  }

}
