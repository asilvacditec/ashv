package com.egantt.awt.image.encoder;

import com.egantt.awt.image.ImageEncoder;
import java.awt.image.BufferedImage;

import java.io.IOException;
import java.io.OutputStream;

/**
 * JPEG encoding through the standard Java ImageIO API.
 * Modernization contribution: Aparecido Silva; original E-Gantt API preserved.
 */
public class BasicJPEGEncoder implements ImageEncoder
{
	private static final String MIME_TYPE = "image/jpeg";

	private static final byte [] EMPTY_SET = new byte[0];

	protected static BasicJPEGEncoder instance;

	// __________________________________________________________________________

	public static ImageEncoder getInstance()
	{
		if (instance == null)
			instance = new BasicJPEGEncoder();
		return instance;
	}

	// __________________________________________________________________________

	@Override
	public String getType()
	{
		return MIME_TYPE;
	}

	// __________________________________________________________________________

	@Override
	public byte[] encode(BufferedImage image)
	{
		java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
		try
		{
			if (!javax.imageio.ImageIO.write(image, "jpeg", out)) return EMPTY_SET;
		}
		catch (IOException io)
		{
			return EMPTY_SET;
		}
		return out.toByteArray();
	}

	// __________________________________________________________________________

	protected final class ByteOutputStream extends OutputStream
	{
		/**
		 * The buffer where data is stored.
		 */
		protected byte buffer[];

		/**
		 * The number of valid bytes in the buffer.
		 */
		protected int count;

		/**
		 * Creates a new byte array output stream. The buffer capacity is
		 * initially the value you specify though its size increases if necessary.
		 * it is quite inefficient in doing so.
		 */
		public ByteOutputStream(int size)
		{
			this.buffer = new byte[size];
		}

		// _______________________________________________________________________

		/**
		 * Writes the specified byte to this byte array output stream.
		 * @param value the byte to be written.
		 */
		@Override
		public synchronized void write(int value)
		{
			int size = count + 1;
			if (size > buffer.length)
			{
				byte newBuffer[] = new byte[size];
				System.arraycopy(buffer, 0, newBuffer, 0, count);
				buffer = newBuffer;
			}
			buffer[count] = (byte) value;
			count = size;
		}
	}
}
