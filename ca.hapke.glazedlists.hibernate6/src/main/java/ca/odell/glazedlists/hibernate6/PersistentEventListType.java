/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.hibernate6;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.metamodel.CollectionClassification;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.usertype.UserCollectionType;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEventPublisher;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

/**
 * A Hibernate custom collection type for mapping and persisting a
 * {@link BasicEventList} with the help of a {@link PersistentEventList}.
 * <p>
 * To create the EventLists, an {@link EventListFactory} is used. The default
 * factory simply instantiates new {@link BasicEventList}s with unshared
 * {@link ReadWriteLock}s and {@link ListEventPublisher}s. If that doesn't suit
 * your needs, you can either implement and set your own
 * {@link EventListFactory} implementation. Or you can use a so called <em>list
 * category</em>. By setting a list category on the EventListType instance, a
 * different list factory will be used which uses the category to determine the
 * publisher and lock to use for all EventLists it creates. This way, all
 * EventListType instances which use the same list category will produce
 * EventLists with the same shared lock and publisher. The desired list category
 * can be set programmatically by subclassing. When Hibernate bug <a href=
 * "http://opensource.atlassian.com/projects/hibernate/browse/HHH-2336">HHH-2336</a>
 * is fixed, you will be able to specify the category as collection type
 * parameter in your Hibernate mapping file.
 *
 * @see #setListFactory(EventListFactory)
 * @see #useListCategory(String)
 * @see #PROPERTYNAME_EVENTLIST_CATEGORY
 *
 * @author Bruce Alspaugh
 * @author Holger Brands
 * @author Nathan Hapke
 */
public class PersistentEventListType<E> implements UserCollectionType {

	/** Factory for EventLists. */
	@SuppressWarnings("unchecked")
	private EventListFactory<E> underlyingListFactory = new EventListFactory<E>() {
		@Override
		public EventList<E> createEventList() {
			return new UnderlyingPersistentEventList<>();
		}

		@Override
		public EventList<E> createEventList(int initalCapacity) {
			return new UnderlyingPersistentEventList<>(initalCapacity);
		}
	};

	public final EventListFactory<E> getUnderlyingListFactory() {
		return underlyingListFactory;
	}

	/** {@inheritDoc} */
	@Override
	public PersistentCollection<E> instantiate(SharedSessionContractImplementor session, CollectionPersister persister)
			throws HibernateException {
		return new PersistentEventList<E>(session, getUnderlyingListFactory());
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public PersistentCollection<E> wrap(SharedSessionContractImplementor session, Object collection) {
		return new PersistentEventList<E>(session, (EventList<E>) collection);
	}

	@Override
	public CollectionClassification getClassification() {
		return CollectionClassification.LIST;
	}

	@Override
	public Class<?> getCollectionClass() {
		return List.class;
	}

	@Override
	public boolean contains(Object collection, Object entity) {
		if (collection instanceof List<?>) {
			List<?> lst = (List<?>) collection;
			return lst.contains(entity);
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<E> getElementsIterator(Object collection) {
		return ((EventList<E>) collection).iterator();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object indexOf(Object collection, Object obj) {
		final int index = ((EventList<E>) collection).indexOf(obj);
		return (index < 0) ? null : Integer.valueOf(index);
	}

	@Override
	public Object instantiate(int anticipatedSize) {
		final EventListFactory<E> fac = getUnderlyingListFactory();
		return anticipatedSize < 0 ? fac.createEventList() : fac.createEventList(anticipatedSize);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object replaceElements(Object original, Object target, CollectionPersister persister, Object owner,
			Map copyCache, SharedSessionContractImplementor session) throws HibernateException {
		final CanUpdateAllElements<E> result = (CanUpdateAllElements<E>) target;
		final EventList<E> resultList = (EventList<E>) target;

		final EventList<E> source = (EventList<E>) original;
		resultList.getReadWriteLock().writeLock().lock();
		source.getReadWriteLock().readLock().lock();

		result.updateAll(source);

		resultList.getReadWriteLock().writeLock().unlock();
		source.getReadWriteLock().readLock().unlock();
		return result;
	}
}