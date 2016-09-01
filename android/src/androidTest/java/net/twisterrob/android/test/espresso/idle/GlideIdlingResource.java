package net.twisterrob.android.test.espresso.idle;

import java.lang.reflect.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.*;

import android.support.annotation.*;
import android.support.test.espresso.IdlingResource;

import static android.support.test.InstrumentationRegistry.*;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.Engine;
import com.bumptech.glide.load.engine.executor.Prioritized;

import net.twisterrob.java.utils.ReflectionTools;

import static net.twisterrob.java.utils.ReflectionTools.*;

/**
 * CONSIDER https://gist.github.com/stefanodacchille/9995163
 */
public class GlideIdlingResource implements IdlingResource, Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(GlideIdlingResource.class);
	private static final Class<?> EngineJobFactory =
			ReflectionTools.forName("com.bumptech.glide.load.engine.Engine$EngineJobFactory");
	private static final Field mEngine =
			trySetAccessible(tryFindDeclaredField(Glide.class, "engine"));
	private static final Field mEngineJobFactory =
			trySetAccessible(tryFindDeclaredField(Engine.class, "engineJobFactory"));
	private static final Field mDiskCacheService =
			trySetAccessible(tryFindDeclaredField(EngineJobFactory, "diskCacheService"));
	private static final Field mSourceService =
			trySetAccessible(tryFindDeclaredField(EngineJobFactory, "sourceService"));

	private ResourceCallback resourceCallback;

	@Override public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
		this.resourceCallback = resourceCallback;
	}

	@Override public String getName() {
		return "Glide";
	}

	private ThreadPoolExecutor sourceExecutor;
	private ThreadPoolExecutor diskExecutor;
	private boolean initialized = false;
	private final AtomicBoolean checkInProgress = new AtomicBoolean();

	private ExecutorService service = Executors.newSingleThreadExecutor(new ThreadFactory() {
		@Override public Thread newThread(@NonNull Runnable r) {
			return new Thread(r, "Glide checker");
		}
	});

	private void ensureInitialized() {
		if (!initialized) {
			try {
				doInit();
			} catch (Exception ex) {
				throw new IllegalStateException("Cannot access Glide executors", ex);
			}
		}
	}

	@MainThread
	private void doInit() throws IllegalAccessException, InvocationTargetException {
		Glide glide = Glide.get(getTargetContext());
		Engine engine = (Engine)mEngine.get(glide);
		ExecutorService sourceService = (ExecutorService)mSourceService.get(mEngineJobFactory.get(engine));
		ExecutorService diskService = (ExecutorService)mDiskCacheService.get(mEngineJobFactory.get(engine));
		if (!(sourceService instanceof ThreadPoolExecutor) || !(diskService instanceof ThreadPoolExecutor)) {
			throw new IllegalArgumentException("No way to determine idleness.");
		}
		sourceExecutor = (ThreadPoolExecutor)sourceService;
		diskExecutor = (ThreadPoolExecutor)diskService;
		initialized = true;
	}

	@Override public boolean isIdleNow() {
		ensureInitialized();
		return doCheck();
	}

	private boolean doCheck() {
		if (syncCheck()) {
			if (resourceCallback != null) {
				resourceCallback.onTransitionToIdle();
			}
			return true;
		} else {
			asyncCheck();
			return false;
		}
	}
	private boolean syncCheck() {
		int sourceActive = sourceExecutor.getActiveCount();
		int sourceQueue = sourceExecutor.getQueue().size();
		int sourceCount = sourceActive + sourceQueue;
		int diskActive = diskExecutor.getActiveCount();
		int diskQueue = diskExecutor.getQueue().size();
		int diskCount = diskActive + diskQueue;
		//LOG.trace("sourceCount={}={}+{}, diskCount={}={}+{}",
		//      sourceCount, sourceActive, sourceQueue, diskCount, diskActive, diskQueue);
		return sourceCount + diskCount == 0;
	}
	private void asyncCheck() {
		if (checkInProgress.compareAndSet(false, true)) {
			LOG.trace("Scheduling a check for later");
			service.submit(this);
		} else {
			LOG.trace("Check already in progress");
		}
	}

	@Override public void run() {
		Future<?> sourceFuture = sourceExecutor.submit(EmptyTask.INSTANCE);
		Future<?> diskFuture = diskExecutor.submit(EmptyTask.INSTANCE);
		try {
			sourceFuture.get();
			diskFuture.get();
			try {
				// wait a little so the executors have time to report active correctly.
				Thread.sleep(1);
			} catch (InterruptedException ex) {
				Thread.interrupted();
				return;
			}
		} catch (InterruptedException ex) {
			Thread.interrupted();
			return;
		} catch (ExecutionException ex) {
			throw new IllegalStateException(ex);
		}
		if (!checkInProgress.compareAndSet(true, false)) {
			throw new IllegalStateException("Who else could've set it to false already?");
		}
		if (doCheck()) {
			LOG.trace("onTransitionToIdle");
		}
	}

	private static class EmptyTask implements Runnable, Prioritized {
		private static final Runnable INSTANCE = new EmptyTask();
		@Override public int getPriority() {
			// safer, FifoPriorityThreadPoolExecutor.LoadTask.compareTo() uses minus
			return Integer.MAX_VALUE / 2; // lowest priority, see com.bumptech.glide.Priority
		}
		@Override public void run() {
			// NO OP
		}
	}
}
