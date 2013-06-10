/*
 * Copyright 2010, 2011 Institut Pasteur.
 * Copyright 2012, 2013 Institut National de l'Audiovisuel.
 * 
 * This file is part of NHerve Main Toolbox, which is an ICY plugin.
 * 
 * NHerve Main Toolbox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * NHerve Main Toolbox is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with NHerve Main Toolbox. If not, see <http://www.gnu.org/licenses/>.
 */
package plugins.nherve.toolbox.image.toolboxes;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;


/**
 * The Class SomeImageTools.
 * 
 * @author Nicolas HERVE - nherve@ina.fr
 */
public class ImageTools {
	
	public static BufferedImage resize(BufferedImage original, int w, int h, boolean keepOrigianlType) {
		BufferedImage resized = null;
		
		if (keepOrigianlType) {
			resized = new BufferedImage(w, h, original.getType());
		} else {
			resized = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		}

		Graphics2D g2 = (Graphics2D) resized.getGraphics();
		resizeAndDraw(original, g2, w, h);
		g2.dispose();
		
		return resized;
	}
	
	public static BufferedImage resize(BufferedImage original, int w, int h) {
		return resize(original, w, h, false);
	}
	
	public static void resizeAndDraw(BufferedImage original, Graphics2D g2, int w, int h) {
		resizeAndDraw(original, g2, w,h, 0, 0);
	}
	
	public static void resizeAndDraw(BufferedImage original, Graphics2D g2, int w, int h, int x, int y) {
		int ow = original.getWidth();
		int oh = original.getHeight();

		double wr = (double) w / (double) ow;
		double hr = (double) h / (double) oh;

		double fr = Math.min(wr, hr);
		int nw = (int) (fr * ow);
		int nh = (int) (fr * oh);

		int wo = x + (w - nw) / 2;
		int ho = y + (h - nh) / 2;

		AffineTransform t = new AffineTransform();
		t.translate(wo, ho);
		t.scale(fr, fr);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED ); 
		g2.drawImage(original, t, null);
	}

}
