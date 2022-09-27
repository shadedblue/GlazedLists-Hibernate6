/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.hibernate6;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.collection.spi.PersistentList;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.metamodel.mapping.PluralAttributeMapping;
import org.hibernate.persister.collection.CollectionPersister;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventAssembler;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.event.ListEventPublisher;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

/**
 * A Hibernate persistent list wrapper for an {@link EventList}.
 * <p>
 * Underlying collection implementation is {@link BasicEventList}.
 *
 * @author Bruce Alspaugh
 * @author Holger Brands
 * @author James Lemieux
 * @author Nathan Hapke
 */
public final class PersistentEventList<E> extends PersistentList<E> implements EventList<E>, ListEventListener<E> {

	private static final long serialVersionUID = 0L;

	/** the change event and notification system */
	protected transient ListEventAssembler<E> updates;

	/**
	 * Constructor with session.
	 *
	 * @param session     the session
	 * @param listFactory factory for EventLists
	 */
	public PersistentEventList(SharedSessionContractImplementor session, EventListFactory<E> listFactory) {
		super(session);

		final EventList<E> delegate = listFactory.createEventList();

		// instantiate list here to avoid NullPointerExceptions with lazy loading
		updates = new ListEventAssembler<E>(this, delegate.getPublisher());
		delegate.addListEventListener(this);
		list = delegate;
	}

	/**
	 * Constructor with session and EventList.
	 *
	 * @param session the session
	 * @param newList the EventList
	 */
	public PersistentEventList(SharedSessionContractImplementor session, EventList<E> newList) {
		super(session, newList);
		if (newList == null) {
			throw new IllegalArgumentException("EventList parameter may not be null");
		}

		updates = new ListEventAssembler<E>(this, newList.getPublisher());
		newList.addListEventListener(this);
	}


	/** {@inheritDoc} */
	@Override
	public ListEventPublisher getPublisher() {
		return ((EventList<E>) list).getPublisher();
	}

	/** {@inheritDoc} */
	@Override
	public ReadWriteLock getReadWriteLock() {
		return ((EventList<E>) list).getReadWriteLock();
	}

	/** {@inheritDoc} */
	@Override
	public void addListEventListener(ListEventListener<? super E> listChangeListener) {
		updates.addListEventListener(listChangeListener);
	}

	/** {@inheritDoc} */
	@Override
	public void removeListEventListener(ListEventListener<? super E> listChangeListener) {
		updates.removeListEventListener(listChangeListener);
	}

	/** {@inheritDoc} */
	@Override
	public void listChanged(ListEvent<E> listChanges) {
		// ignore ListEvents during Hibernate's initialization
		// (initialization should always appear to be transparent and thus should not
		// produce ListEvents)
		if (wasInitialized()) {
			updates.forwardEvent(listChanges);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void dispose() {
		// TODO Holger please implement me!
	}

	/**
	 * Serializes this list and all serializable listeners
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		// write out all serializable listeners
		List<ListEventListener<E>> serializableListeners = new ArrayList<>();
		for (Iterator<ListEventListener<E>> i = updates.getListEventListeners().iterator(); i.hasNext();) {
			ListEventListener<E> listener = i.next();
			if (!(listener instanceof Serializable)) {
				continue;
			}
			serializableListeners.add(listener);
		}
		@SuppressWarnings("unchecked")
		ListEventListener<E>[] listeners = serializableListeners
				.toArray(new ListEventListener[serializableListeners.size()]);
		out.writeObject(listeners);
	}

	/**
	 * Deserializes this list and all serializable listeners.
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		assert (list instanceof EventList) : "'list' member type unknown";
		updates = new ListEventAssembler<E>(this, ((EventList<E>) list).getPublisher());

		// read in the listeners
		@SuppressWarnings("unchecked")
		final ListEventListener<E>[] listeners = (ListEventListener<E>[]) in.readObject();
		for (int i = 0; i < listeners.length; i++) {
			updates.addListEventListener(listeners[i]);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void injectLoadedState(PluralAttributeMapping attributeMapping, List<?> loadingStateList) {
		assert isInitializing();
		assert list == null || list.size() == 0;

		final CollectionPersister collectionDescriptor = attributeMapping.getCollectionDescriptor();
		if (list == null) {
			this.list = (List<E>) collectionDescriptor.getCollectionSemantics().instantiateRaw(loadingStateList.size(),
					collectionDescriptor);
		}

		list.addAll((List<E>) loadingStateList);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initializeEmptyCollection(CollectionPersister persister) {
		assert list == null || list.size() == 0;

		if (list == null) {
			list = (List<E>) persister.getCollectionType().instantiate(0);
		}
		endRead();
	}
}