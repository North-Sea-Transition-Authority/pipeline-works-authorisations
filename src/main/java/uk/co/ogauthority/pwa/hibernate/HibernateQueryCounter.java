package uk.co.ogauthority.pwa.hibernate;

import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.springframework.stereotype.Component;

@Component
public class HibernateQueryCounter implements StatementInspector {

  private final transient ThreadLocal<Long> threadQueryCount = ThreadLocal.withInitial(() -> 0L);

  public Long getQueryCount() {
    return threadQueryCount.get();
  }

  public void clearQueryCount() {
    threadQueryCount.remove();
  }

  @Override
  public String inspect(String sql) {
    threadQueryCount.set(threadQueryCount.get() + 1);
    return sql;
  }
}
