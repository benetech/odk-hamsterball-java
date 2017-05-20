package org.benetech.thumbnail;

import javax.servlet.http.HttpServletRequest;

/**
 * Simple repository interface to take advantage of Spring Boot's default caching features
 * 
 * @author Caden Howell <cadenh@benetech.org>
 */
public interface ThumbnailRepository {

	public Thumbnail get(String requestUrl, HttpServletRequest request, String endpointUrl);

}
