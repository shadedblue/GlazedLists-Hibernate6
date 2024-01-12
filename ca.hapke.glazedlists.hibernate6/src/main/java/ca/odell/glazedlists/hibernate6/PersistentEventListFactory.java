/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/

package ca.odell.glazedlists.hibernate6;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.hibernate.PersistentEventList;

/**
 * A factory for EventLists, that is used by instances of
 * {@link PersistentEventListType} and {@link PersistentEventList} to
 * instantiate EventLists.
 *
 * @author Holger Brands
 */
public interface PersistentEventListFactory<E> {

	/**
	 * Default implementation that always creates new EventLists with different
	 * ReadWriteLocks and ListEventPublishers.
	 */
	PersistentEventListFactory DEFAULT = new DefaultFactory();

	/**
	 * Creates a new EventList.
	 */
	EventList<E> createEventList();

	/**
	 * Create a new EventList with an initial capacity.
	 */
	EventList<E> createEventList(int initalCapacity);
}

/**
 * EventListFactory implementation that always creates new EventLists with
 * different ReadWriteLocks and ListEventPublishers.
 *
 * @author Holger Brands
 */
class DefaultFactory<E> implements PersistentEventListFactory<E> {

	/** {@inheritDoc} */
	@Override
	public EventList<E> createEventList() {
		return new UnderlyingPersistentEventList<E>();
	}

	/** {@inheritDoc} */
	@Override
	public EventList<E> createEventList(int initalCapacity) {
		return new UnderlyingPersistentEventList<E>(initalCapacity);
	}
}