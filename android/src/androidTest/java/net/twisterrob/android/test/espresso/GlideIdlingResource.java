package net.twisterrob.android.test.espresso;

import java.lang.reflect.*;
import java.util.concurrent.*;

import android.support.annotation.NonNull;
import android.support.test.annotation.Beta;

import static android.support.test.InstrumentationRegistry.*;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.Engine;
import com.bumptech.glide.load.engine.executor.*;

import net.twisterrob.java.utils.ReflectionTools;

import static net.twisterrob.java.utils.ReflectionTools.*;

/**
 * @see <a href="https://gist.github.com/stefanodacchille/9995163">ThreadPoolIdlingResource.java, a simlar idler</a>
 */
@Beta
public class GlideIdlingResource extends PollingIdlingResource {
	private static final Class<?> EngineJobFactory =
			ReflectionTools.forName("com.bumptech.glide.load.engine.Engine.EngineJobFactory");
	private static final Method getEngine =
			trySetAccessible(tryFindDeclaredMethod(Glide.class, "getEngine"));
	private static final Field mEngineJobFactory =
			trySetAccessible(tryFindDeclaredField(Engine.class, "engineJobFactory"));
	private static final Field mDiskCacheService =
			trySetAccessible(tryFindDeclaredField(EngineJobFactory, "diskCacheService"));
	private static final Field mSourceService =
			trySetAccessible(tryFindDeclaredField(EngineJobFactory, "sourceService"));

	@Override public String getName() {
		return "Glide";
	}

	@SuppressWarnings("ConstantConditions")
	@Override protected boolean isIdle() {
		try {
			Glide glide = Glide.get(getTargetContext());
			Engine engine = (Engine)getEngine.invoke(glide);
			ExecutorService sourceService = (ExecutorService)mSourceService.get(mEngineJobFactory.get(engine));
			ExecutorService diskCacheService = (ExecutorService)mDiskCacheService.get(mEngineJobFactory.get(engine));
			return hasSettled(sourceService) && hasSettled(diskCacheService);
		} catch (NullPointerException ex) {
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	private boolean hasSettled(@NonNull ExecutorService service) {
		if (service instanceof FifoPriorityThreadPoolExecutor) {
			return ((FifoPriorityThreadPoolExecutor)service).getActiveCount() == 0;
		} else {
			try {
				service.submit(new EmptyTask()).get(1, TimeUnit.MILLISECONDS);
				return true;
			} catch (InterruptedException e) {
				return false;
			} catch (ExecutionException e) {
				throw new IllegalStateException(e);
			} catch (TimeoutException e) {
				return false;
			}
		}
	}
	private static class EmptyTask implements Runnable, Prioritized {
		@Override public int getPriority() {
			return Integer.MAX_VALUE / 2; // safer, FifoPriorityThreadPoolExecutor.LoadTask.compareTo() uses minus
		}
		@Override public void run() {
			// NO OP
		}
	}
}
