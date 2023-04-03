package io.github.lordgkram.blobimg;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;

import com.formdev.flatlaf.util.FontUtils;

public class JFUtil {

    private Font currentFont;

    public JFUtil() {
        currentFont = new JLabel("").getFont();
        currentFont = currentFont.deriveFont(20.0f);
    }

    public String getCurrentFont() {
        return currentFont.getFamily();
    }

    public String[] listFonts() {
        return FontUtils.getAvailableFontFamilyNames();
    }

    public boolean setFont(String name, int size) {
        currentFont = FontUtils.getCompositeFont(name, 0, size);
        return true;
    }

    public boolean setSize(float size) {
        currentFont = currentFont.deriveFont(size);
        return true;
    }

    public GLTexture render(String text, Color color, int border) {
        // java render
        FontRenderContext frc = new FontRenderContext(AffineTransform.getRotateInstance(0), false, false);
        String[] textParts = text.split("\n");
        int[] ypos = new int[textParts.length];
        int[] xpos = new int[textParts.length];
        int w = 1;
        int h = border;
        for(int i = 0; i < textParts.length; i++) {
            Rectangle2D bounds = currentFont.getStringBounds(textParts[i], frc);
            int lw = (int) Math.ceil(bounds.getWidth()),
                lh = (int) Math.ceil(bounds.getHeight());
            if(lw > w) w = lw;
            ypos[i] = h - (int) Math.round(bounds.getY());
            xpos[i] = border - (int) Math.round(bounds.getX());
            h += lh;
        }
        h += border;
        w += border * 2;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g2d.setFont(currentFont);
        g2d.setColor(new Color(0, 0, 0, 0));
        g2d.drawRect(0, 0, w, h);
        g2d.setColor(color);
        for(int i = 0; i < textParts.length; i++) g2d.drawString(textParts[i], xpos[i], ypos[i]);

        return ImgTextureUtil.textureFromBufferedImmage(img);
    }
    
}
