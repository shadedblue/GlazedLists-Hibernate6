/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/

package ca.odell.glazedlists.hibernate6;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

/**
 * A factory for EventLists, that is used by instances of {@link PersistentEventListType} and {@link PersistentEventList} to
 * instantiate EventLists.
 *
 * @author Holger Brands
 */
public interface EventListFactory<E> {

	/**
	 * Default implementation that always creates new EventLists with different ReadWriteLocks and ListEventPublishers.
	 */
	EventListFactory DEFAULT = new DefaultFactory();

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
 * EventListFactory implementation that always creates new EventLists with different ReadWriteLocks and
 * ListEventPublishers.
 *
 * @author Holger Brands
 */
class DefaultFactory<E> implements EventListFactory<E> {

	/** {@inheritDoc} */
	@Override
	public EventList<E> createEventList() {
		return new BasicEventList<E>();
	}

	/** {@inheritDoc} */
	@Override
	public EventList<E> createEventList(int initalCapacity) {
		return new BasicEventList<E>(initalCapacity);
	}
}