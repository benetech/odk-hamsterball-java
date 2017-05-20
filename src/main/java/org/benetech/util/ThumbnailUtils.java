package org.benetech.util;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Chai Heng
 * @see https://www.techcoil.com/blog/how-to-create-a-thumbnail-of-an-image-in-java-without-using-external-libraries/
 */
public class ThumbnailUtils {

	private static Log logger = LogFactory.getLog(ThumbnailUtils.class);

	public static int WIDTH_PX = 120;

	public static BufferedImage thumbnail(File original) {
		BufferedImage originalBufferedImage = null;
		BufferedImage thumbnailBufferedImage = null;
		try {
			originalBufferedImage = ImageIO.read(original);

			int widthToScale, heightToScale;
			if (originalBufferedImage.getWidth() > originalBufferedImage.getHeight()) {

				heightToScale = (int) (1.1 * WIDTH_PX);
				widthToScale = (int) ((heightToScale * 1.0) / originalBufferedImage.getHeight()
						* originalBufferedImage.getWidth());

			} else {
				widthToScale = (int) (1.1 * WIDTH_PX);
				heightToScale = (int) ((widthToScale * 1.0) / originalBufferedImage.getWidth()
						* originalBufferedImage.getHeight());
			}

			BufferedImage resizedImage = new BufferedImage(widthToScale, heightToScale,
					originalBufferedImage.getType());
			Graphics2D g = resizedImage.createGraphics();

			g.setComposite(AlphaComposite.Src);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g.drawImage(originalBufferedImage, 0, 0, widthToScale, heightToScale, null);
			g.dispose();

			int x = (resizedImage.getWidth() - WIDTH_PX) / 2;
			int y = (resizedImage.getHeight() - WIDTH_PX) / 2;

			if (x < 0 || y < 0) {
				throw new IllegalArgumentException("Width of new thumbnail is bigger than original image");
			}

			thumbnailBufferedImage = resizedImage.getSubimage(x, y, WIDTH_PX, WIDTH_PX);
		} catch (IOException ioe) {
			logger.error("IO exception occurred while trying to read image.");

		}
		return thumbnailBufferedImage;
	}

}
