package net.twisterrob.android.utils.tools;

import java.util.Locale;

import javax.microedition.khronos.egl.*;

import org.slf4j.*;

import android.annotation.TargetApi;
import android.graphics.*;
import android.opengl.*;
import android.os.Build.*;
import android.support.annotation.*;

public class CanvasTools {
	private static final Logger LOG = LoggerFactory.getLogger(CanvasTools.class);
	public static void drawTriangle(Canvas canvas, float x1, float y1, float x2, float y2, float x3, float y3,
			Paint paint) {
		Path path = new Path();
		path.moveTo(x1, y1);
		path.lineTo(x2, y2);
		path.lineTo(x3, y3);
		path.close();

		canvas.drawPath(path, paint);
	}

	@TargetApi(VERSION_CODES.ICE_CREAM_SANDWICH)
	public static @NonNull Point getMaximumBitmapSize(@Nullable Canvas canvas) {
		Point point = new Point();
		if (canvas != null
				&& VERSION_CODES.ICE_CREAM_SANDWICH <= VERSION.SDK_INT && canvas.isHardwareAccelerated()) {
			// only if these methods are available and the canvas is hardware accelerated,
			// otherwise the values are invalid. 
			point.x = canvas.getMaximumBitmapWidth();
			point.y = canvas.getMaximumBitmapHeight();
		} else {
			point.x = point.y = getMaximumTextureSize();
		}
		return point;
	}

	public static int getMaximumTextureSize() {
		int maxSize = 0;
		if (VERSION_CODES.JELLY_BEAN_MR1 <= VERSION.SDK_INT) {
			try {
				maxSize = getMaximumEGL20TextureSize();
			} catch (Throwable t) {
				LOG.warn("Cannot determine maximum texture size using EGL20.", t);
			}
		}
		if (maxSize == 0) {
			try {
				maxSize = getMaximumEGL10TextureSize();
			} catch (Throwable t) {
				LOG.warn("Cannot determine maximum texture size using EGL10.", t);
			}
		}
		if (maxSize <= 0) {
			maxSize = 2048; // reasonable fallback in the year 2016
		}
		return maxSize;
	}

	/**
	 * All the setup is need, because otherwise querying {@link GLES20#GL_MAX_TEXTURE_SIZE} gives an error:
	 * <i>E/libEGL: call to OpenGL ES API with no current context (logged once per thread)</i>
	 * The main idea is to initialize a context with a fake rendering surface that we won't use. Since there won't be any rendering, the exact attributes of the config lookup aren't very critical.
	 * The best effort is made to clean up if an error happens.
	 *
	 * @see <a href="http://stackoverflow.com/a/27092070/253468">GLES10.glGetIntegerv returns 0 in Lollipop</a>
	 * @throws IllegalStateException if one of the EGL14 operations failed.
	 */
	// throw in finally is not a problem because the flag `failed` protects against suppression 
	@SuppressWarnings("ThrowFromFinallyBlock")
	@TargetApi(VERSION_CODES.JELLY_BEAN_MR1)
	public static int getMaximumEGL20TextureSize() throws IllegalStateException {
		android.opengl.EGLDisplay display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
		int[] version = new int[2];
		if (!EGL14.eglInitialize(display, version, 0, version, 1)) {
			throw glError("Cannot initialize display.", EGL14.eglGetError());
		}
		// if this flag is true, we already have an Exception propagating, don't suppress it with another one.
		boolean failed = false;
		try {
			int[] configAttributes = {
					EGL14.EGL_COLOR_BUFFER_TYPE, EGL14.EGL_RGB_BUFFER,
					EGL14.EGL_LEVEL, 0,
					EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
					EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
					EGL14.EGL_NONE
			};
			android.opengl.EGLConfig[] configs = new android.opengl.EGLConfig[1];
			int[] numConfig = new int[1];
			if (!EGL14.eglChooseConfig(display, configAttributes, 0, configs, 0, 1, numConfig, 0)) {
				throw glError("Cannot choose config.", EGL14.eglGetError());
			}
			if (numConfig[0] == 0) {
				throw glError("No configs chosen.", EGL14.eglGetError());
			}
			android.opengl.EGLConfig config = configs[0];
			if (config == null) {
				throw glError("No config chosen.", EGL14.eglGetError());
			}
			int[] contextAttributes = {
					EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
					EGL14.EGL_NONE
			};
			android.opengl.EGLContext context =
					EGL14.eglCreateContext(display, config, EGL14.EGL_NO_CONTEXT, contextAttributes, 0);
			if (context == null) {
				throw glError("Cannot create context.", EGL14.eglGetError());
			}
			try {
				int[] surfaceAttributes = {
						EGL14.EGL_WIDTH, 64,
						EGL14.EGL_HEIGHT, 64,
						EGL14.EGL_NONE
				};
				android.opengl.EGLSurface surface =
						EGL14.eglCreatePbufferSurface(display, config, surfaceAttributes, 0);
				if (surface == null) {
					throw glError("Cannot create surface.", EGL14.eglGetError());
				}
				try {
					if (!EGL14.eglMakeCurrent(display, surface, surface, context)) {
						throw glError("Cannot make context current.", EGL14.eglGetError());
					}
					try {
						int[] maxSize = new int[1];
						GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxSize, 0);
						if (maxSize[0] <= 0) {
							throw glError("Cannot determine max texture size.", EGL14.eglGetError());
						}
						return maxSize[0];
					} catch (Throwable t) {
						failed = true;
						throw t;
					} finally {
						if (!EGL14.eglMakeCurrent(display,
								EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)) {
							if (!failed) {
								throw glError("Cannot restore to no current surface.", EGL14.eglGetError());
							}
						}
					}
				} catch (Throwable t) {
					failed = true;
					throw t;
				} finally {
					if (!EGL14.eglDestroySurface(display, surface)) {
						if (!failed) {
							throw glError("Cannot destroy surface.", EGL14.eglGetError());
						}
					}
				}
			} catch (Throwable t) {
				failed = true;
				throw t;
			} finally {
				if (!EGL14.eglDestroyContext(display, context)) {
					if (!failed) {
						throw glError("Cannot destroy context.", EGL14.eglGetError());
					}
				}
			}
		} catch (Throwable t) {
			failed = true;
			throw t;
		} finally {
			if (!EGL14.eglTerminate(display)) {
				if (!failed) {
					throw glError("Cannot terminate display.", EGL14.eglGetError());
				}
			}
		}
	}

	/**
	 * All the setup is need, because otherwise querying {@link GLES10#GL_MAX_TEXTURE_SIZE} gives an error:
	 * <i>E/libEGL: call to OpenGL ES API with no current context (logged once per thread)</i>
	 * The main idea is to initialize a context with a fake rendering surface that we won't use. Since there won't be any rendering, the exact attributes of the config lookup aren't very critical.
	 * The best effort is made to clean up if an error happens.
	 *
	 * @see <a href="http://stackoverflow.com/a/27092070/253468">GLES10.glGetIntegerv returns 0 in Lollipop</a>
	 * @throws IllegalStateException if one of the EGL10 operations failed.
	 */
	// throw in finally is not a problem because the flag `failed` protects against suppression 
	@SuppressWarnings("ThrowFromFinallyBlock")
	public static int getMaximumEGL10TextureSize() throws IllegalStateException {
		EGL eglContext = javax.microedition.khronos.egl.EGLContext.getEGL();
		if (!(eglContext instanceof EGL10)) {
			throw glError("Cannot find EGL10.", EGL10.EGL_BAD_CONTEXT);
		}
		EGL10 egl = (EGL10)eglContext;
		javax.microedition.khronos.egl.EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
		int[] version = new int[2];
		if (!egl.eglInitialize(display, version)) {
			throw glError("Cannot initialize display.", egl.eglGetError());
		}
		// if this flag is true, we already have an Exception propagating, don't suppress it with another one.
		boolean failed = false;
		try {
			int[] configAttributes = {
					EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RGB_BUFFER,
					EGL10.EGL_LEVEL, 0,
					EGL10.EGL_SURFACE_TYPE, EGL10.EGL_PBUFFER_BIT,
					EGL10.EGL_NONE
			};
			javax.microedition.khronos.egl.EGLConfig[] configs = new javax.microedition.khronos.egl.EGLConfig[1];
			int[] numConfig = new int[1];
			if (!egl.eglChooseConfig(display, configAttributes, configs, 1, numConfig)) {
				throw glError("Cannot choose config.", egl.eglGetError());
			}
			if (numConfig[0] == 0) {
				throw glError("No configs chosen.", egl.eglGetError());
			}
			javax.microedition.khronos.egl.EGLConfig config = configs[0];
			if (config == null) {
				throw glError("No config chosen.", egl.eglGetError());
			}
			int[] contextAttributes = {
					0x3098 /*EGL_CONTEXT_CLIENT_VERSION*/, 1,
					EGL10.EGL_NONE
			};
			javax.microedition.khronos.egl.EGLContext context =
					egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, contextAttributes);
			if (context == null) {
				throw glError("Cannot create context.", egl.eglGetError());
			}
			try {
				int[] surfaceAttributes = {
						EGL10.EGL_WIDTH, 64,
						EGL10.EGL_HEIGHT, 64,
						EGL10.EGL_NONE
				};
				javax.microedition.khronos.egl.EGLSurface surface =
						egl.eglCreatePbufferSurface(display, config, surfaceAttributes);
				if (surface == null) {
					throw glError("Cannot create surface.", egl.eglGetError());
				}
				try {
					if (!egl.eglMakeCurrent(display, surface, surface, context)) {
						throw glError("Cannot make context current.", egl.eglGetError());
					}
					try {
						int[] maxSize = new int[1];
						GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, maxSize, 0);
						if (maxSize[0] <= 0) {
							throw glError("Cannot determine max texture size.", egl.eglGetError());
						}
						return maxSize[0];
					} catch (Throwable t) {
						failed = true;
						throw t;
					} finally {
						if (!egl.eglMakeCurrent(display,
								EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT)) {
							if (!failed) {
								throw glError("Cannot restore to no current surface.", egl.eglGetError());
							}
						}
					}
				} catch (Throwable t) {
					failed = true;
					throw t;
				} finally {
					if (!egl.eglDestroySurface(display, surface)) {
						if (!failed) {
							throw glError("Cannot destroy surface.", egl.eglGetError());
						}
					}
				}
			} catch (Throwable t) {
				failed = true;
				throw t;
			} finally {
				if (!egl.eglDestroyContext(display, context)) {
					if (!failed) {
						throw glError("Cannot destroy context.", egl.eglGetError());
					}
				}
			}
		} catch (Throwable t) {
			failed = true;
			throw t;
		} finally {
			if (!egl.eglTerminate(display)) {
				if (!failed) {
					throw glError("Cannot terminate display.", egl.eglGetError());
				}
			}
		}
	}

	private static IllegalStateException glError(String message, int errorCode) {
		String errorDescription = GLU.gluErrorString(errorCode);
		return new IllegalStateException(String.format(Locale.ROOT, "%s OpenGL Error Code %X (%d): %s",
				message, errorCode, errorCode, errorDescription));
	}

	protected CanvasTools() {
		// static utility class
	}
}
