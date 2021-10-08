package uk.co.ogauthority.pwa.config;

import java.util.Map;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SystemPropertySetter {

  private static final Logger LOGGER = LoggerFactory.getLogger(SystemPropertySetter.class);

  private final Integer imageScalingThreadCount;

  @Autowired
  public SystemPropertySetter(@Value("${pwa.image-scaling.thread-count}") Integer imageScalingThreadCount) {
    this.imageScalingThreadCount = imageScalingThreadCount;
  }

  @PostConstruct
  public void setSystemProperties() {

    var systemProperties = Map.of(
        "imgscalr.async.threadCount", String.valueOf(imageScalingThreadCount)
    );

    systemProperties.forEach(System::setProperty);

    LOGGER.info("System properties set: {}", systemProperties);

  }

}
