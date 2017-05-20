package org.benetech.thumbnail;
import java.awt.image.BufferedImage;

public class Thumbnail {
	
	private String contentType;
	private String extension;
	private String key;
	
	private BufferedImage thumbnail;
	
	

	public Thumbnail(String contentType, String extension, String key, BufferedImage thumbnail) {
		this.contentType = contentType;
		this.extension = extension;
		this.key = key;
		this.thumbnail = thumbnail;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public BufferedImage getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(BufferedImage thumbnail) {
		this.thumbnail = thumbnail;
	}
	
	

}
