package util;

import java.util.concurrent.TimeUnit;
import java.util.ListIterator;
import java.util.List;

/**
 * @TODO proper java.util.concurent.BlockingQueue implementation.
 *
 * Implements a PriorityBlocking queue.  Differs from java.util.concurrent.PriorityBlockingQueue
 * primarily in that this queue guarantees a FIFO ordering for items of equal prioirty. 
 */
public class PriorityBlockingQueue<T extends Comparable<T>> implements BlockingQueue<T>{

	/**
	 * Linked-list to back the queue.
	 */
	private List<T> list = new LinkedList<T>();

	public PriorityBlockingQueue() {
	}

	/**
 	*
 	* next()
 	*/
	public synchronized boolean offer(T item) {
		
		ListIterator<T> it = list.listIterator();

		//stops when there is no next element, or item < next.
		while ( it.hasNext() && item.compareTo(it.next()) >= 0 );

		//IF there's a previous item, we need to rewind
		//NOTE: As far as efficiency, I'm relying on the fact that I *KNOW*
		//that each node in the list contains a reference to the previous node 
		//(e.g. it doesn't need to seek)
		if (it.hasPrevious()) it.previous();

		it.add(item);

		notifyAll();

		return true;
	}

	public T poll(long timeout, TimeUnit unit) throws InterruptedException {

		T item = poll();

		if (item == null) {
			synchronized(this) {
				unit.timedWait(this,timeout);
			}

			item = poll();
		}

		return item;
	}

	public synchronized T poll() {

		if (list.size() == 0) return null;

		return list.remove(0);
	}
}
