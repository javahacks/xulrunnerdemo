package com.javahacks.demo.ui.workbench;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
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
					browser.execute("updateImage('http://127.0.0.1:8080/streaming/screenshot"+System.currentTimeMillis()+".jpg')");
				}

				browser.execute("setValue('#date','" + arguments[2].toString().toUpperCase() + "')");

				return true;
			}
		};

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

}
