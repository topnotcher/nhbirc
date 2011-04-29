package util;

import java.util.ListIterator;
import java.util.List;

public class ListSorter {

	/**
	 * Sorts a list of comparable objects in ascending order
	 * using selection sort.
	 */
	public static <T extends Comparable<T>> void sort(List<T> list) {

		//outer iterator
		//when this completes, the list list is sorted
		ListIterator<T> outer = list.listIterator();

		//this  runs once per every iteration of outer iterator,
		//and iterates over the remaining elements.
		ListIterator<T> inner;

		//temporary for swapping
		T tmp;

		int i = 0;
		while (outer.hasNext()) {

			//current position
			tmp = outer.next();

			//create an iterator starting at the next element.
			inner = list.listIterator(i+1);
			
			while (inner.hasNext() ) {
				T item;

				item = inner.next();

				//swap everytime we find an element < tmp.
				//(more swaps than necessary, but it's either this or keep setting min=)
				if ( item.compareTo( tmp ) < 0 ) {
					outer.set(item);
					inner.set(tmp);
					tmp = item;
				}

			} //end inner

			++i;
		}
	}
}
