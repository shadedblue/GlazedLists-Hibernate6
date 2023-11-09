package ca.odell.glazedlists.hibernate6;

import java.util.List;

/**
 * @author Nathan Hapke
 */
public interface CanUpdateAllElements<E> {

	public boolean updateAll(List<? extends E> input);
}
