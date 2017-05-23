package org.benetech.thumbnail;

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

    FileWrapper fileWrapper =
        HttpProxyUtils.proxyInterceptFileRequest(request, endpointUrl.toString());

    // byteArray is already compressed for compressible formats.
    byte[] byteArray = ThumbnailUtils.thumbnail(fileWrapper);

    return new Thumbnail(fileWrapper.getContentType(), fileWrapper.getExtension(), requestUrl,
        byteArray);

  }


}
