package com.coldsteelstudios.util;

import java.util.LinkedList;
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
	private List<T> list;

	public PriorityBlockingQueue() {
		clear();
	}

	/**
 	*
 	* next()
 	*/
	public synchronized boolean offer(T item) {
		

		if (item == null)
			throw new NullPointerException("Cannot queue a null");

		ListIterator<T> it = list.listIterator();

		while ( true ) {

			//we insert the item at the end of the list
			if (!it.hasNext())
				break;

			//we found an item of a lower priority
			if ( item.compareTo(it.next()) < 0 ) {

				//we insert before that item.
				//I'm relying on the fact that I *know*
				//this linked list item can find the previous item
				//"quickly" (without seeking).
				if (it.hasPrevious()) it.previous();
				break;
			}
		}		

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

	public synchronized void clear() {
		 list = new LinkedList<T>();
	}
}
