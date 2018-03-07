package vmazurok.study.controllers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import vmazurok.study.models.PackageConfiguration;

@RestController
public class MakeZipController {

	private static final String ATTACHMENT_TEMPLATE = "attachment; filename=\"%s.zip\"";

	/*
	 * POST to http://localhost:8080/makezip Test Body JSON { "name" :
	 * "imagePackage", "images" :
	 * ["http://www.richardcorbridge.com/wp-content/uploads/2018/02/free.jpg",
	 * "https://optimistaker.com/wp-content/uploads/2017/12/I-love-free-images.jpg"]
	 * }
	 * 
	 * Or simple GET will pass the same JSON http://localhost:8080/testzip
	 */

	@RequestMapping(value = "/makezip", method = RequestMethod.POST, produces = "application/zip", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void makezip(HttpServletResponse response, @RequestBody PackageConfiguration packageConfiguration) {
		if (isPackageConfigurationCorrect(packageConfiguration)) {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

			List<String> images = packageConfiguration.getImages();
			boolean atLeastOneImageAdded = addImages(zipOutputStream, images);
			closeZipOutputStream(zipOutputStream);

			if (atLeastOneImageAdded) {
				try {
					setOkResponseAndSendIt(response, packageConfiguration, byteArrayOutputStream);
				} catch (IOException e) {
					e.printStackTrace();
					setBadRequestResponse(response);
				}
			} else {
				setBadRequestResponse(response);
			}

		} else {
			setBadRequestResponse(response);
		}
	}

	private boolean isPackageConfigurationCorrect(PackageConfiguration packageConfiguration) {
		return (packageConfiguration != null) && (packageConfiguration.getName() != null)
				&& (packageConfiguration.getImages() != null) && (packageConfiguration.getImages().size() > 0);
	}

	private boolean addImages(ZipOutputStream zipOutputStream, List<String> images) {
		boolean atLeastOneImageAdded = false;
		for (String imageURL : images) {
			String fileName = getFileNameFromURL(imageURL);

			try {
				addImageToZip(zipOutputStream, imageURL, fileName);
				atLeastOneImageAdded = true;
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return atLeastOneImageAdded;
	}

	private String getFileNameFromURL(String imageURL) {
		return imageURL.substring(imageURL.lastIndexOf('/') + 1);
	}

	private void closeZipOutputStream(ZipOutputStream zipOutputStream) {
		if (zipOutputStream != null) {
			try {
				zipOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void setOkResponseAndSendIt(HttpServletResponse response, PackageConfiguration packageConfiguration,
			ByteArrayOutputStream byteArrayOutputStream) throws IOException {
		response.setStatus(HttpServletResponse.SC_OK);
		String attachmentHeader = String.format(ATTACHMENT_TEMPLATE, packageConfiguration.getName());
		response.addHeader("Content-Disposition", attachmentHeader);
		response.setContentLength(byteArrayOutputStream.size());
		response.getOutputStream().write(byteArrayOutputStream.toByteArray());
		response.flushBuffer();
		response.getOutputStream().close();
	}

	private void setBadRequestResponse(HttpServletResponse response) {
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		try {
			response.getOutputStream().close();
			response.setContentLength(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addImageToZip(ZipOutputStream zipOutputStream, String imageURL, String fileName)
			throws MalformedURLException, IOException {
		try (InputStream inputStream = new URL(imageURL).openStream()) {
			zipOutputStream.putNextEntry(new ZipEntry(fileName));
			IOUtils.copy(inputStream, zipOutputStream);
			zipOutputStream.closeEntry();
		}
	}

	@RequestMapping(value = "/testzip", method = RequestMethod.GET, produces = "application/zip")
	public void testzip(HttpServletResponse response) throws IOException {

		PackageConfiguration packageConfiguration = new PackageConfiguration();
		packageConfiguration.setName("testPack");
		ArrayList<String> imagesUrls = new ArrayList<>();
		imagesUrls.add("http://www.richardcorbridge.com/wp-content/uploads/2018/02/free.jpg");
		imagesUrls.add("https://optimistaker.com/wp-content/uploads/2017/12/I-love-free-images.jpg");
		packageConfiguration.setImages(imagesUrls);

		if (isPackageConfigurationCorrect(packageConfiguration)) {

			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

			List<String> images = packageConfiguration.getImages();
			boolean atLeastOneImageAdded = addImages(zipOutputStream, images);
			closeZipOutputStream(zipOutputStream);

			if (atLeastOneImageAdded) {
				try {
					setOkResponseAndSendIt(response, packageConfiguration, byteArrayOutputStream);
				} catch (IOException e) {
					e.printStackTrace();
					setBadRequestResponse(response);
				}
			} else {
				setBadRequestResponse(response);
			}

		} else {
			setBadRequestResponse(response);
		}
	}
}
