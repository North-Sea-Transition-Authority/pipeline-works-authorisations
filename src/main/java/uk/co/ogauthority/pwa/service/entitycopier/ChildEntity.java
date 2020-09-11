package uk.co.ogauthority.pwa.service.entitycopier;

/**
 * Interface to identify entities which have a single parent and allow manipulation of that relationship.
 *
 * @param <I> type used for entity Id
 * @param <P> type of entities Parent.
 */
public interface ChildEntity<I, P> {

  I getId();

  void clearId();

  void setParent(P parentEntity);

  P getParent();

}
