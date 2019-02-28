package team30.personalbest.service;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.Consumer;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import team30.personalbest.util.Callback;

public class ServiceInitializer
{
	public static final String TAG = "ServiceInitializer";

	private final IService[] services;

	private final Map<IService, IService> serviceResults = new HashMap<>();
	private final List<OnServicesReadyListener> readyListeners = new ArrayList<>();

	ServiceInitializer(IService[] services)
	{
		this.services = services;
	}

	public ServiceInitializer addOnServicesReadyListener(OnServicesReadyListener listener)
	{
		this.readyListeners.add(listener);
		return this;
	}

	public void onActivityCreate(Activity activity, Bundle savedInstanceState)
	{
		final Callback<ServiceInitializer> callback = this.getOnReadyCallback();
		this.onActivityCreateFromServiceIndex(0, activity, savedInstanceState, callback);
	}

	private void onActivityCreateFromServiceIndex(final int index, final Activity activity, final Bundle savedInstanceState, final Callback<ServiceInitializer> callback)
	{
		if (index >= this.services.length)
		{
			callback.resolve(this);
		}
		else
		{
			final ServiceInitializer serviceInitializer = this;
			final IService service = this.services[index];
			if (this.serviceResults.containsKey(service) && this.serviceResults.get(service) != null)
			{
				//Already initialized, skip it.
				this.onActivityCreateFromServiceIndex(
						index + 1,
						activity,
						savedInstanceState,
						callback);
			}
			else
			{
				final Callback<IService> serviceCallback = service.onActivityCreate(this, activity, savedInstanceState);
				if (serviceCallback == null)
				{
					Log.w(TAG, "Unable to resolve service - will implicitly fail due to null return in onActivityCreate, must return Callback(this) to resolve eventually - " + service.getClass());

					//Ignore any results from this call, and just continue.
					this.onActivityCreateFromServiceIndex(
							index + 1,
							activity,
							savedInstanceState,
							callback);
				}
				else
				{
					serviceCallback.onResult(new Consumer<IService>() {
						@Override
						public void accept(IService iService)
						{
							if (iService == null)
							{
								Log.w(TAG, "Failed to create service - " + service.getClass());
								serviceInitializer.serviceResults.remove(service);
							}
							else
							{
								Log.i(TAG, "Successfully initialized service - " + service.getClass());
								serviceInitializer.serviceResults.put(service, iService);
							}

							serviceInitializer.onActivityCreateFromServiceIndex(
									index + 1,
									activity,
									savedInstanceState,
									callback);
						}
					});
				}
			}
		}
	}

	public void onActivityResult(Activity activity, int requestCode, int resultCode, @Nullable Intent data)
	{
		final Callback<ServiceInitializer> callback = this.getOnReadyCallback();
		this.onActivityResultFromServiceIndex(0, activity, requestCode, resultCode, data, callback);
	}

	private void onActivityResultFromServiceIndex(final int index, final Activity activity, final int requestCode, final int resultCode, @Nullable final Intent data, final Callback<ServiceInitializer> callback)
	{
		if (index >= this.services.length)
		{
			callback.resolve(this);
		}
		else
		{
			final ServiceInitializer serviceInitializer = this;
			final IService service = this.services[index];
			if (this.serviceResults.containsKey(service) && this.serviceResults.get(service) != null)
			{
				//Already initialized, skip it.
				this.onActivityResultFromServiceIndex(
						index + 1,
						activity,
						requestCode,
						resultCode,
						data,
						callback);
			}
			else
			{
				final Callback<IService> serviceCallback = service.onActivityResult(this, activity, requestCode, resultCode, data);
				if (serviceCallback == null)
				{
					Log.w(TAG, "Unable to resolve service - will implicitly fail due to null return in onActivityResult, must return Callback(this) to resolve eventually");

					//Ignore any results from this call, and just continue.
					this.onActivityResultFromServiceIndex(
							index + 1,
							activity,
							requestCode,
							resultCode,
							data,
							callback);
				}
				else
				{
					serviceCallback.onResult(new Consumer<IService>()
					{
						@Override
						public void accept(IService iService)
						{
							if (iService == null)
							{
								Log.w(TAG, "Failed to create service.");
								serviceInitializer.serviceResults.remove(service);
							}
							else
							{
								Log.i(TAG, "Successfully initialized service - " + service.getClass());
								serviceInitializer.serviceResults.put(service, iService);
							}

							serviceInitializer.onActivityResultFromServiceIndex(
									index + 1,
									activity,
									requestCode,
									resultCode,
									data,
									callback);
						}
					});
				}
			}
		}
	}

	private Callback<ServiceInitializer> getOnReadyCallback()
	{
		final Callback<ServiceInitializer> callback = new Callback<>();
		callback.onResult(new Consumer<ServiceInitializer>() {
			@Override
			public void accept(ServiceInitializer serviceInitializer)
			{
				boolean flag = true;
				for(Map.Entry<IService, IService> entry : serviceInitializer.serviceResults.entrySet())
				{
					if (entry.getValue() == null)
					{
						flag = false;
						break;
					}
				}
				if (flag)
				{
					Log.i(TAG, "Services are now ready...");
					for(OnServicesReadyListener listener : serviceInitializer.readyListeners)
					{
						try
						{
							listener.onServicesReady(serviceInitializer);
						}
						catch (Exception e)
						{
							Log.w(TAG, "Unable to handle services ready event", e);
						}
					}
				}
			}
		});
		return callback;
	}

	public static class Builder
	{
		private final List<IService> services = new ArrayList<>();
		private final List<OnServicesReadyListener> listeners = new ArrayList<>();

		public Builder addService(IService service)
		{
			this.services.add(service);
			return this;
		}

		public Builder addOnServicesReadyListener(OnServicesReadyListener listener)
		{
			this.listeners.add(listener);
			return this;
		}

		public ServiceInitializer build()
		{
			final ServiceInitializer result = new ServiceInitializer(this.services.toArray(new IService[0]));
			for(OnServicesReadyListener listener : this.listeners)
			{
				result.addOnServicesReadyListener(listener);
			}
			return result;
		}
	}
}
