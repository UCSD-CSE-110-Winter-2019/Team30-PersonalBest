package team30.personalbest.util;

import android.support.v4.util.Consumer;

/**
 * This is an asynchronous callback. To get the value, call onResult() with a function to be
 * executed later with the result when complete. The callback is NOT guaranteed to resolve.
 *
 * @param <T> the result type
 */
public final class Callback<T>
{
	private Consumer<T> callback;
	private T result;

	public Callback() {}

	public <E extends T> Callback(E result)
	{
		this.result = result;
	}

	/**
	 * Called by the async task to return the result to the listener
	 */
	public <E extends T> void resolve(E result)
	{
		this.result = result;
		if (this.callback != null)
		{
			this.callback.accept(result);
		}
	}

	/**
	 * Called by the async listener to handle the result from the task
	 */
	public void onResult(Consumer<T> callback)
	{
		this.callback = callback;
		if (this.result != null)
		{
			this.callback.accept(this.result);
		}
	}
}