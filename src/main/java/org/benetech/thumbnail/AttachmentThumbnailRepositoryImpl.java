package org.benetech.thumbnail;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.benetech.util.HttpProxyUtils;
import org.benetech.util.ThumbnailUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class AttachmentThumbnailRepositoryImpl implements ThumbnailRepository {

	private static Log logger = LogFactory.getLog(AttachmentThumbnailRepositoryImpl.class);

	@Cacheable(cacheNames = "thumbnails", key = "#requestUrl")
	@Override
	public Thumbnail get(String requestUrl, HttpServletRequest request, String endpointUrl) {
		logger.info("Accepted readers");

		FileWrapper fileWrapper = HttpProxyUtils.proxyInterceptFileRequest(request, endpointUrl.toString());

		BufferedImage bufferedImage = ThumbnailUtils.thumbnail(fileWrapper.getFile());

		return new Thumbnail(fileWrapper.getContentType(), fileWrapper.getExtension(), requestUrl,
				bufferedImage);

	}

}
