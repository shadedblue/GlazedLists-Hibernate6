package ca.odell.glazedlists.hibernate6;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;

import ca.odell.glazedlists.AbstractEventList;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.event.ListEventPublisher;
import ca.odell.glazedlists.util.concurrent.LockFactory;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

/**
 * Knockoff of {@link BasicEventList}, with a method for replacing all elements,
 * needed for PersistentEventListType::replaceElements
 * 
 * @author Nathan Hapke
 */
public class UnderlyingPersistentEventList<E> extends AbstractEventList<E>
		implements RandomAccess, CanUpdateAllElements<E> {

	private List<E> data;

	public UnderlyingPersistentEventList() {
		this(LockFactory.DEFAULT.createReadWriteLock());
	}

	public UnderlyingPersistentEventList(ReadWriteLock readWriteLock) {
		this(null, readWriteLock);
	}

	public UnderlyingPersistentEventList(int initalCapacity) {
		this(initalCapacity, null, LockFactory.DEFAULT.createReadWriteLock());
	}

	public UnderlyingPersistentEventList(ListEventPublisher publisher, ReadWriteLock readWriteLock) {
		this(10, publisher, readWriteLock);
	}

	public UnderlyingPersistentEventList(int initialCapacity, ListEventPublisher publisher,
			ReadWriteLock readWriteLock) {
		super(publisher);
		this.data = new ArrayList<E>(initialCapacity);
		this.readWriteLock = (readWriteLock == null) ? LockFactory.DEFAULT.createReadWriteLock() : readWriteLock;
	}

	/** {@inheritDoc} */
	@Override
	public void add(int index, E element) {
		// create the change event
		updates.beginEvent();
		updates.elementInserted(index, element);
		// do the actual add
		data.add(index, element);
		// fire the event
		updates.commitEvent();
	}

	/** {@inheritDoc} */
	@Override
	public boolean add(E element) {
		// create the change event
		updates.beginEvent();
		updates.elementInserted(size(), element);
		// do the actual add
		boolean result = data.add(element);
		// fire the event
		updates.commitEvent();
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean addAll(Collection<? extends E> collection) {
		return addAll(size(), collection);
	}

	/** {@inheritDoc} */
	@Override
	public boolean addAll(int index, Collection<? extends E> collection) {
		// don't do an add of an empty set
		if (collection.size() == 0)
			return false;

		// create the change event
		updates.beginEvent();
		for (Iterator<? extends E> i = collection.iterator(); i.hasNext();) {
			E value = i.next();
			updates.elementInserted(index, value);
			data.add(index, value);
			index++;
		}
		// fire the event
		updates.commitEvent();
		return !collection.isEmpty();
	}

	/**
	 * changes the elements of this list to match the provided input.
	 * 
	 * This is needed so that the set of updates can be fired as one big set.
	 */
	public boolean updateAll(List<? extends E> input) {
		if (input.size() == 0)
			return false;

		boolean changed = false;
		updates.beginEvent();

		int i = 0;
		while (i < input.size()) {
			E newValue = input.get(i);
			if (i >= data.size()) {
				data.add(newValue);
				updates.elementInserted(i, newValue);
				changed = true;
			} else {
				E oldValue = data.get(i);
				if (oldValue != newValue) {
					data.set(i, newValue);
					updates.elementUpdated(i, oldValue, newValue);
					changed = true;
				}
			}
			i++;
		}
		int j = i;
		while (data.size() > i) {
			E oldValue = data.get(j);
			data.remove(i);
			updates.elementDeleted(j, oldValue);
			j++;
			changed = true;
		}

		if (changed) {
			updates.commitEvent();
		} else {
			updates.discardEvent();
		}
		return changed;
	}

	/** {@inheritDoc} */
	@Override
	public E remove(int index) {
		// create the change event
		updates.beginEvent();
		// do the actual remove
		E removed = data.remove(index);
		// fire the event
		updates.elementDeleted(index, removed);
		updates.commitEvent();
		return removed;
	}

	/** {@inheritDoc} */
	@Override
	public boolean remove(Object element) {
		int index = data.indexOf(element);
		if (index == -1)
			return false;
		remove(index);
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public void clear() {
		// don't do a clear on an empty set
		if (isEmpty())
			return;
		// create the change event
		updates.beginEvent();
		for (int i = 0, size = size(); i < size; i++) {
			updates.elementDeleted(0, get(i));
		}
		// do the actual clear
		data.clear();
		// fire the event
		updates.commitEvent();
	}

	/** {@inheritDoc} */
	@Override
	public E set(int index, E element) {
		// create the change event
		updates.beginEvent();
		// do the actual set
		E previous = data.set(index, element);
		// fire the event
		updates.elementUpdated(index, previous);
		updates.commitEvent();
		return previous;
	}

	/** {@inheritDoc} */
	@Override
	public E get(int index) {
		return data.get(index);
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		return data.size();
	}

	/** {@inheritDoc} */
	@Override
	public boolean removeAll(Collection<?> collection) {
		boolean changed = false;
		updates.beginEvent();
		for (Iterator i = collection.iterator(); i.hasNext();) {
			Object value = i.next();
			int index = -1;
			while ((index = indexOf(value)) != -1) {
				E removed = data.remove(index);
				updates.elementDeleted(index, removed);
				changed = true;
			}
		}
		updates.commitEvent();
		return changed;
	}

	/** {@inheritDoc} */
	@Override
	public boolean retainAll(Collection<?> collection) {
		boolean changed = false;
		updates.beginEvent();
		int index = 0;
		while (index < data.size()) {
			if (collection.contains(data.get(index))) {
				index++;
			} else {
				E removed = data.remove(index);
				updates.elementDeleted(index, removed);
				changed = true;
			}
		}
		updates.commitEvent();
		return changed;
	}

	/**
	 * This method does nothing. It is not necessary to dispose a BasicEventList.
	 */
	@Override
	public void dispose() {
	}
}
