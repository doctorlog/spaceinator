package au.com.f1n.spaceinator.game;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Adaptation of ArrayList that doesnt hog memory with listiterators
 * 
 * @author luke
 * 
 * @param <E>
 */
public class FArrayList<E> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int MIN_CAPACITY_INCREMENT = 12;

	/**
	 * The number of elements in this list.
	 */
	public int size;

	/**
	 * The elements in this list, followed by nulls.
	 */
	public Object[] array;

	/**
	 * Constructs a new instance of {@code ArrayList} with the specified initial
	 * capacity.
	 * 
	 * @param capacity
	 *           the initial capacity of this {@code ArrayList}.
	 */
	public FArrayList(int capacity) {
		if (capacity < 0) {
			throw new IllegalArgumentException();
		}
		array = (capacity == 0 ? new Object[MIN_CAPACITY_INCREMENT] : new Object[capacity]);
	}

	/**
	 * Constructs a new {@code ArrayList} instance with zero initial capacity.
	 */
	public FArrayList() {
		array = new Object[MIN_CAPACITY_INCREMENT];
	}

	/**
	 * Adds the specified object at the end of this {@code ArrayList}.
	 * 
	 * @param object
	 *           the object to add.
	 * @return always true
	 */

	public boolean add(E object) {
		Object[] a = array;
		int s = size;
		if (s == a.length) {
			Object[] newArray = new Object[s + (s < (MIN_CAPACITY_INCREMENT / 2) ? MIN_CAPACITY_INCREMENT : s >> 1)];
			System.arraycopy(a, 0, newArray, 0, s);
			array = a = newArray;
		}
		a[s] = object;
		size = s + 1;
		return true;
	}

	/**
	 * This method was extracted to encourage VM to inline callers.
	 */
	static IndexOutOfBoundsException throwIndexOutOfBoundsException(int index, int size) {
		throw new IndexOutOfBoundsException("Invalid index " + index + ", size is " + size);
	}

	/**
	 * Removes all elements from this {@code ArrayList}, leaving it empty.
	 * 
	 * @see #isEmpty
	 * @see #size
	 */

	public void clear() {
		if (size != 0) {
			Arrays.fill(array, 0, size, null);
			size = 0;
		}
	}

	/**
	 * Ensures that after this operation the {@code ArrayList} can hold the
	 * specified number of elements without further growing.
	 * 
	 * @param minimumCapacity
	 *           the minimum capacity asked for.
	 */
	public void ensureCapacity(int minimumCapacity) {
		Object[] a = array;
		if (a.length < minimumCapacity) {
			Object[] newArray = new Object[minimumCapacity];
			System.arraycopy(a, 0, newArray, 0, size);
			array = newArray;
		}
	}

	@SuppressWarnings("unchecked")
	public E get(int index) {
		if (index >= size) {
			throwIndexOutOfBoundsException(index, size);
		}
		return (E) array[index];
	}

	/**
	 * Returns the number of elements in this {@code ArrayList}.
	 * 
	 * @return the number of elements in this {@code ArrayList}.
	 */

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * Searches this {@code ArrayList} for the specified object.
	 * 
	 * @param object
	 *           the object to search for.
	 * @return {@code true} if {@code object} is an element of this
	 *         {@code ArrayList}, {@code false} otherwise
	 */

	public boolean contains(Object object) {
		Object[] a = array;
		int s = size;
		if (object != null) {
			for (int i = 0; i < s; i++) {
				if (object.equals(a[i])) {
					return true;
				}
			}
		} else {
			for (int i = 0; i < s; i++) {
				if (a[i] == null) {
					return true;
				}
			}
		}
		return false;
	}

	public int indexOf(Object object) {
		Object[] a = array;
		int s = size;
		if (object != null) {
			for (int i = 0; i < s; i++) {
				if (object.equals(a[i])) {
					return i;
				}
			}
		} else {
			for (int i = 0; i < s; i++) {
				if (a[i] == null) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * Removes the object at the specified location from this list.
	 * 
	 * @param index
	 *           the index of the object to remove.
	 * @return the removed object.
	 * @throws IndexOutOfBoundsException
	 *            when {@code location < 0 || location >= size()}
	 */

	public E remove(int index) {
		Object[] a = array;
		int s = size;
		if (index >= s) {
			throwIndexOutOfBoundsException(index, s);
		}
		@SuppressWarnings("unchecked")
		E result = (E) a[index];
		System.arraycopy(a, index + 1, a, index, --s - index);
		a[s] = null; // Prevent memory leak
		size = s;
		return result;
	}

	public boolean remove(Object object) {
		Object[] a = array;
		int s = size;
		if (object != null) {
			for (int i = 0; i < s; i++) {
				if (object == a[i]) {
					System.arraycopy(a, i + 1, a, i, --s - i);
					a[s] = null; // Prevent memory leak
					size = s;
					return true;
				}
			}
		} else {
			for (int i = 0; i < s; i++) {
				if (a[i] == null) {
					System.arraycopy(a, i + 1, a, i, --s - i);
					a[s] = null; // Prevent memory leak
					size = s;
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Replaces the element at the specified location in this {@code ArrayList}
	 * with the specified object.
	 * 
	 * @param index
	 *           the index at which to put the specified object.
	 * @param object
	 *           the object to add.
	 * @return the previous element at the index.
	 * @throws IndexOutOfBoundsException
	 *            when {@code location < 0 || location >= size()}
	 */

	public E set(int index, E object) {
		Object[] a = array;
		if (index >= size) {
			throwIndexOutOfBoundsException(index, size);
		}
		@SuppressWarnings("unchecked")
		E result = (E) a[index];
		a[index] = object;
		return result;
	}

	/**
	 * Replace the first null instance with the given object
	 * 
	 * @param object
	 */
	public void addToNull(E object) {
		Object[] a = array;
		int s = size;
		for (int i = 0; i < s; i++) {
			if (a[i] == null) {
				a[i] = object;
				return;
			}
		}
	}

	/**
	 * Find the object and make it null
	 * 
	 * @param object
	 */
	public void nullify(E object) {
		Object[] a = array;
		int s = size;
		for (int i = 0; i < s; i++) {
			if (a[i] == object) {
				a[i] = null;
				return;
			}
		}
	}

	public E removeLast() {
		Object[] a = array;
		@SuppressWarnings("unchecked")
		E result = (E) a[--size];
		a[size] = null;
		return result;
	}
}
