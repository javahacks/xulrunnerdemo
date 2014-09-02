package com.javahacks.demo.ui.workbench;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.mozilla.xulrunner.XULRunner;

public class Application implements IApplication {

	private static final String SERVER_ID = "SIRIUS";
	private static final int JETTY_PORT = 8080;

	private static final String DATA_SPLASH_SHELL = "org.eclipse.ui.workbench.splashShell"; //$NON-NLS-1$
	private static final String PROP_SPLASH_HANDLE = "org.eclipse.equinox.launcher.splash.handle"; //$NON-NLS-1$

	@Override
	public Object start(IApplicationContext context) throws Exception {

		Display display = Display.getDefault();

		
		
		final Shell shell = new Shell(display);
		shell.setText("Desktop App Demonstration");
		shell.setImage(ImageDescriptor.createFromFile(Application.class, "sample.png").createImage(display));
		shell.setSize(800, 600);
		
		GridLayoutFactory.fillDefaults().applyTo(shell);

		
		
		final Browser browser = XULRunner.createBrowser(shell, SWT.FLAT);
		browser.setUrl("http://127.0.0.1:" + JETTY_PORT + "/index.html?test" + System.currentTimeMillis());
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(browser);

		new BrowserFunction(browser, "handle") {

			@Override
			public Object function(Object[] arguments) {

				if (arguments.length == 0)
					return false;

				if ("takeScreenshot".equals(arguments[0])) {
					takeScreenshot();
					browser.execute("updateImage('http://127.0.0.1:8080/streaming/screenshot" + System.currentTimeMillis() + ".jpg')");
				}
				
				if ("dropText".equals(arguments[0])) {
					System.out.println("drop text"+arguments[1]);
				}
				if ("loadImage".equals(arguments[0])) {
					loadImage();
					browser.execute("updateImage('http://127.0.0.1:8080/streaming/screenshot" + System.currentTimeMillis() + ".jpg')");
				}

				browser.execute("setValue('#date','" + arguments[2].toString().toUpperCase() + "')");

				return true;
			}

			
			
		};
		
		getSplashShell(display).dispose();

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();

		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {

	}

	private void takeScreenshot() {

		Display display = Display.getDefault();

		final Rectangle rect = new ClippingSelector().select();

		if (rect.height == 0 || rect.width == 0)
			return;

		// we show the selected area in a new shell. Just for demonstration!
		final Image image = new Image(display, rect);
		final GC gc = new GC(display);
		gc.copyArea(image, rect.x, rect.y);
		gc.dispose();

		StreamingServlet.setImage(image);
	}

	private void loadImage() {
		Display display = Display.getDefault();
		
		FileDialog fileDialog = new FileDialog(Display.getDefault().getActiveShell());
		fileDialog.setFilterNames(new String[]{"*.jpg"});
		
		String file = fileDialog.open();
		
		if(file!=null){
			try{
				
				ImageData[] load = new ImageLoader().load(file);			
				StreamingServlet.setImage(new Image(display, load[0]));
			}
			catch(IllegalArgumentException ex){
			}
		}
		
	}
		
	
	
	/**
	 * Get the splash shell for this workbench instance, if any. This will find
	 * the splash created by the launcher (native) code and wrap it in a SWT
	 * shell. This may have the side effect of setting data on the provided
	 * {@link Display}.
	 * 
	 * @param display
	 *            the display to parent the shell on
	 * 
	 * @return the splash shell or <code>null</code>
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws NumberFormatException
	 * @see Display#setData(String, Object)
	 * @since 3.4
	 */
	public static Shell getSplashShell(Display display) {
		
		Shell splashShell = (Shell) display.getData(DATA_SPLASH_SHELL);
		if (splashShell != null)
			return splashShell;

		String splashHandle = System.getProperty(PROP_SPLASH_HANDLE);
		if (splashHandle == null) {
			return null;
		}

		// look for the 32 bit internal_new shell method
		try {
			Method method = Shell.class.getMethod("internal_new", new Class[] { Display.class, int.class }); //$NON-NLS-1$
			// we're on a 32 bit platform so invoke it with splash
			// handle as an int
			splashShell = (Shell) method.invoke(null, new Object[] { display, new Integer(splashHandle) });
		} catch (NoSuchMethodException e) {
			// look for the 64 bit internal_new shell method
			try {
				Method method = Shell.class.getMethod("internal_new", new Class[] { Display.class, long.class }); //$NON-NLS-1$

				// we're on a 64 bit platform so invoke it with a long
				splashShell = (Shell) method.invoke(null, new Object[] { display, new Long(splashHandle) });
			} catch (Exception e2) {
				// cant find either method - don't do anything.
			}
		}
		catch(Exception ex){
			throw new IllegalArgumentException(ex);
		}

		display.setData(DATA_SPLASH_SHELL, splashShell);
		return splashShell;
	}

	/**
	 * Removes any splash shell data set on the provided display and disposes
	 * the shell if necessary.
	 * 
	 * @param display
	 *            the display to parent the shell on
	 * @since 3.4
	 */
	public static void unsetSplashShell(Display display) {
		Shell splashShell = (Shell) display.getData(DATA_SPLASH_SHELL);
		if (splashShell != null) {
			if (!splashShell.isDisposed())
				splashShell.dispose();
			display.setData(DATA_SPLASH_SHELL, null);
		}

	}

}
