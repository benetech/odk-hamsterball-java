package org.benetech.model.display;

import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.benetech.controller.ProxyController;
import org.opendatakit.aggregate.odktables.rest.entity.OdkTablesFileManifestEntry;

public class OdkTablesFileManifestEntryDisplay extends OdkTablesFileManifestEntry {

  private String relativeUrl;
  private static Log logger = LogFactory.getLog(OdkTablesFileManifestEntryDisplay.class);


  public OdkTablesFileManifestEntryDisplay(OdkTablesFileManifestEntry manifestEntry) {
    this.contentLength = manifestEntry.contentLength;
    this.contentType = manifestEntry.contentType;
    this.downloadUrl = manifestEntry.downloadUrl;
    this.filename = manifestEntry.filename;
    this.md5hash = manifestEntry.md5hash;
  }


  public String getRelativeUrl() {
    if (relativeUrl == null) {

      URL url;
      try {
        url = new URL(this.downloadUrl);
        relativeUrl = url.getPath() + "?" + url.getQuery();

      } catch (MalformedURLException e) {
        logger.info("Can't get relative URL:  " + this.downloadUrl);
        relativeUrl = "";
      }
    }
    return relativeUrl;
  }
  
  public boolean isSupportsThumbnail() {
	  return ArrayUtils.contains(ImageIO.getReaderMIMETypes(), contentType);
  }


}
