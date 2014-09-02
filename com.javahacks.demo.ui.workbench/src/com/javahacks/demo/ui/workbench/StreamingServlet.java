package com.javahacks.demo.ui.workbench;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

public class StreamingServlet extends HttpServlet {

	private static final long serialVersionUID = -1717035820122064342L;
	private static Image image;


	public synchronized void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("image/jpeg");

		if (image != null && !image.isDisposed()) {

			ImageLoader loader = new ImageLoader();
			loader.data = new ImageData[] { image.getImageData() };
			loader.save(response.getOutputStream(), SWT.IMAGE_JPEG);
		}

	}

	public static synchronized void setImage(Image image) {
		if (StreamingServlet.image != null && !StreamingServlet.image.isDisposed()) {
			StreamingServlet.image.dispose();
		}
		StreamingServlet.image = image;
	}
}
