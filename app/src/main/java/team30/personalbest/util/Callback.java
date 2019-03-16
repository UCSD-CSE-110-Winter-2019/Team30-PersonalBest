package team30.personalbest.util;

import android.support.v4.util.Consumer;

import java.util.Collection;
import java.util.HashSet;

/**
 * This is an asynchronous callback. To get the value, call onResult() with a function to be
 * executed later with the result when complete. The callback is NOT guaranteed to resolve.
 *
 * @param <T> the result type
 */
public final class Callback<T>
{
	private final Collection<Consumer<T>> callbacks = new HashSet<>();
	private T result;
	private boolean hasResolved = false;

	public Callback() {}

	public <E extends T> Callback(E result)
	{
		this.result = result;
		this.hasResolved = true;
	}

	/**
	 * Called by the async task to return the result to the listener
	 */
	public <E extends T> void resolve(E result)
	{
		if (this.hasResolved)
			throw new IllegalStateException("Already resolved with value - \'" + result + "\'");
		this.hasResolved = true;

		this.result = result;

		if (!this.callbacks.isEmpty()) this.call(result);
	}

	/**
	 * Called by the async task to reject null to the listener
	 */
	public void reject()
	{
		if (this.hasResolved)
			throw new IllegalStateException("Already resolved with value - \'" + this.result + "\'");
		this.hasResolved = true;

		if (!this.callbacks.isEmpty()) this.call(null);
	}

	/**
	 * Called by the async listener to handle the result from the task
	 */
	public void onResult(Consumer<T> callback)
	{
		this.callbacks.add(callback);

		if (this.hasResolved) callback.accept(this.result);
	}

	public boolean hasResolved()
	{
		return this.hasResolved;
	}

	private <E extends T> void call(E result)
	{
		for (Consumer<T> callback : this.callbacks)
		{
			callback.accept(result);
		}
	}
}
