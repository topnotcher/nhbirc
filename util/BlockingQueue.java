package util;

import java.util.concurrent.TimeUnit;

/**
 * "looks" like java.util.concurrent.BlockingQueue 
 * (e.g. so I just had to change my imports to switch impementations)
 */
public interface BlockingQueue<T> {

	/**
 	*
 	* next()
 	*/
	public boolean offer(T item);
	
	public T poll(long timeout, TimeUnit unit) throws InterruptedException;

	public T poll();

	public void clear();
}
