package util;

import java.util.ListIterator;

public class FixedStack<T> extends LinkedList<T> {

	private int BUF_SIZE = 0;

	protected Node foot;

	public FixedStack(int size) {
		BUF_SIZE = size;
	}

	public boolean add(T item) {

		Node add = new Node( item );

		if ( head != null) {
			add.prev = null;
			add.next = head;

			head.prev = add;

			head = add;

		} else {
			head = add;
			foot = add;
		}

		//if the message > the buffer size,
		//we drop the last item...
		if ( ++size > BUF_SIZE ) {
			
			//set a new foot for the list
			foot = foot.prev;
			
			//for the hell of it, remove the old foot's reference to the list
			//(Just in case it is left hanging around or something)
			foot.next.prev = null;

			//then mark the foot as the end of the list;
			foot.next = null;
		}

		return true;
	}

	public void clear() {
		head = foot = null;
		size = 0;
	}

	public void set(T o) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("set() is not supported.");
	}

	protected ListIterator<T> listIterator(final int startIdx, final Node startNode) { 
		return new FixedStackIterator(startIdx, startNode);
	} 

	private class FixedStackIterator extends LinkedListIterator {

		private FixedStackIterator(int startIdx, Node startNode) {
			super(startIdx, startNode);
		}

		public void add(T element) throws UnsupportedOperationException {
			throw new UnsupportedOperationException("Adding at index not supported. Use FixedStack.add(item).");
		}

		public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException("remove() is not supported.");
		}

		public void set() throws UnsupportedOperationException {
			throw new UnsupportedOperationException("set() is not supported supported.");
		}

	}
}
