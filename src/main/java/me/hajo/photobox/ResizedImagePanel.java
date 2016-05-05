package me.hajo.photobox;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by fxtentacle on 29.04.16.
 */
public class ResizedImagePanel extends JPanel {
    private final Font calibri = new Font("Calibri", Font.BOLD, 250);
    protected BufferedImage drawme;
    protected BufferedImage drawme2;
    private String overlay;
 
    public void setOverlay(String overlay) {
        this.overlay = overlay;
    }

    public void setImage(Image img) {
        Dimension masterSize = new Dimension(img.getWidth(this), img.getHeight(this));
        drawme = getScaledInstance((BufferedImage)img, getScaleFactorToFit(masterSize, getSize()), RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
        invalidate();
        repaint();
    }
    
    public void setImage2(Image img) {
        if(img == null) {
            drawme2 = null;
            return;
        }
        Dimension masterSize = new Dimension(img.getWidth(this), img.getHeight(this));
        drawme2 = getScaledInstance((BufferedImage)img, getScaleFactorToFit(masterSize, getSize()), RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
    }

    @Override
    public Dimension getPreferredSize() {
        return drawme == null ? super.getPreferredSize() : new Dimension(drawme.getWidth(this), drawme.getHeight(this));
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (drawme != null) {
            final int x = (getWidth() - drawme.getWidth(this)) / 2;
            final int y = (getHeight() - drawme.getHeight(this)) / 2;
            g.drawImage(drawme, x, y, this);

            g.setColor(Color.black);
            g.fillRect(0,0, x, getHeight());
            final int ex = x+drawme.getWidth(this);
            g.fillRect(ex,0, getWidth()-ex, getHeight());
        }

        if (drawme2 != null) {
            final int x = (getWidth() - drawme2.getWidth(this)) / 2;
            final int y = (getHeight() - drawme2.getHeight(this)) / 2;
            g.drawImage(drawme2, x, y, this);
        }
        
        if (overlay != null) {
            FontMetrics metrics = g.getFontMetrics(calibri);
            final int h = metrics.getHeight();
            final int w = metrics.stringWidth(overlay);
            g.setColor(Color.white);
            g.setFont(calibri);

            final int x = (getWidth() - w) / 2;
            final int y = (getHeight() + h) / 2;
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.drawString(overlay, x,y);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
    }

    public double getScaleFactor(int iMasterSize, int iTargetSize) {
        return (double) iTargetSize / (double) iMasterSize;
    }

    public double getScaleFactorToFit(Dimension original, Dimension toFit) {
        double dScale = 1d;
        if (original != null && toFit != null) {
            double dScaleWidth = getScaleFactor(original.width, toFit.width);
            double dScaleHeight = getScaleFactor(original.height, toFit.height);
            dScale = Math.min(dScaleHeight, dScaleWidth);
        }
        if(dScale < 0.1) dScale = 0.1;
        return dScale;
    }

    protected BufferedImage getScaledInstance(BufferedImage img, double dScaleFactor, Object hint, boolean bHighQuality) {
        int w = (int) Math.round(img.getWidth() * dScaleFactor);
        int h = (int) Math.round(img.getHeight() * dScaleFactor);

        BufferedImage tmp = new BufferedImage(w, h, img.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = tmp.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
        g2.drawImage(img, 0, 0, w, h, null);
        g2.dispose();
        return tmp;
    }
}
