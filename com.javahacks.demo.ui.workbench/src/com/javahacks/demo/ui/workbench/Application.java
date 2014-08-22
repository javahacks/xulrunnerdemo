package com.javahacks.demo.ui.workbench;

import java.text.DateFormat;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.mozilla.xulrunner.XULRunner;

public class Application implements IApplication {

	private static final String SERVER_ID = "SIRIUS";
	private static final int JETTY_PORT = 8080;

	@Override
	public Object start(IApplicationContext context) throws Exception {

		startServer();

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setSize(800, 600);
		GridLayoutFactory.fillDefaults().applyTo(shell);

		final Browser browser = XULRunner.createBrowser(shell, SWT.FLAT);
		browser.setUrl("http://127.0.0.1:" + JETTY_PORT + "/index.html?test"
				+ System.currentTimeMillis());
		GridDataFactory.fillDefaults().grab(true, true)
				.align(SWT.FILL, SWT.FILL).applyTo(browser);

		ToolBarManager manager = new ToolBarManager();
		
		manager.add(new Action("Left") {
			@Override
			public void run() {
				browser.execute("left()");
				
				
			}
		});
		
		
		browser.addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent e) {

				GC gc = e.gc;
				gc.setForeground(e.gc.getDevice().getSystemColor(SWT.COLOR_GREEN));
				gc.drawLine(0, 0, 1000, 1000);
				gc.dispose();
				System.out.println("_-");
				
			}
		});
		manager.add(new Action("Right") {
			@Override
			public void run() {
				browser.execute("right()");
			}
		});
		manager.add(new Action("Up") {
			@Override
			public void run() {
				browser.execute("up()");
			}
		});
		manager.add(new Action("Down") {
			@Override
			public void run() {
				browser.execute("down()");
			}
		});
		
		
		Shell toolShell = new Shell(shell,SWT.TITLE);
		toolShell.setLayout(new FillLayout());
		ToolBar toolBar = manager.createControl(toolShell);
		toolBar.forceFocus();
		
		toolShell.open();
		toolShell.pack();

		new BrowserFunction(browser, "getDate") {

			@Override
			public Object function(Object[] arguments) {
				return DateFormat.getDateTimeInstance().format(new Date());
			}
		};

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();

		return 0;
	}

	@Override
	public void stop() {
		stopServer();

	}

	/**
	 * Start embedded jetty server
	 */
	private void startServer() {

		System.setProperty("JETTY_PORT", String.valueOf(JETTY_PORT));

		Dictionary<String, Object> config = new Hashtable<String, Object>();
		config.put("http.enabled", Boolean.TRUE);
		config.put(JettyConstants.HTTP_HOST, "127.0.0.1");
		config.put(JettyConstants.HTTP_PORT, JETTY_PORT);
		config.put("https.enabled", Boolean.FALSE);
		config.put("context.path", "/");

		try {
			JettyConfigurator.startServer(SERVER_ID, config);
		} catch (Exception e) {
			throw new IllegalArgumentException("could not start server", e);
		}
	}

	private synchronized static void stopServer() {

		try {
			JettyConfigurator.stopServer(SERVER_ID);
		} catch (Exception e) {
			throw new IllegalArgumentException("could not stop server", e);
		}

	}
}
