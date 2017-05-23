package org.benetech.thumbnail;
import java.awt.image.BufferedImage;

public class Thumbnail {
	
	private String contentType;
	private String extension;
	private String key;
	
	private byte[] thumbnail;
	
	

	public Thumbnail(String contentType, String extension, String key, byte[] thumbnail) {
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

	public byte[] getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(byte[] thumbnail) {
		this.thumbnail = thumbnail;
	}
	
	

}
