package ro.licj.magnus.util;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static com.jogamp.opengl.GL2.*;

public enum PNGTexture {
    FOOTBALL("res/football.png", GL_RGBA, GL_RGBA);

    private int width;
    private int height;
    private int internalFormat;
    private int format;
    private ByteBuffer byteBuffer;

    private PNGTexture(String filename, int internalFormat, int format) {
        this.internalFormat = internalFormat;
        this.format = format;
        try {
            BufferedImage bufferedImage = ImageIO.read(new File("res/football.png"));
            width = bufferedImage.getWidth();
            height = bufferedImage.getHeight();

            Raster raster = bufferedImage.getData();
            DataBufferByte data = (DataBufferByte) raster.getDataBuffer();

            byteBuffer = ByteBuffer.allocate(width * height * 4);
            for (int i = 0; i <
                    bufferedImage.getWidth() * bufferedImage.getHeight() * 4; i += 4) {
                byteBuffer.put(data.getData()[i + 1]);
                byteBuffer.put(data.getData()[i + 2]);
                byteBuffer.put(data.getData()[i + 3]);
                byteBuffer.put(data.getData()[i]);
            }
            byteBuffer.flip();
        } catch (IOException e) {
            throw new RuntimeException(
                    "An exception occured when loading the texture \"" + filename + "\".",
                    e);
        }

    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public int getInternalFormat() {
        return internalFormat;
    }

    public int getFormat() {
        return format;
    }
}