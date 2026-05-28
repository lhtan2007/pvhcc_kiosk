package org.example.shared.helper;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public class CustomSVGTranscoder extends ImageTranscoder{
	private ImageIcon icon;
	private BufferedImage buffered;
	public CustomSVGTranscoder() {
		// TODO Auto-generated constructor stub
	}
	public ImageIcon getIcon(String filePath, float width, float height) {
		try {
			System.out.println(getClass().getClassLoader().getResource("mainicon.svg"));
			InputStream fis = getClass().getClassLoader().getResourceAsStream(filePath);
			CustomSVGTranscoder transcoder = new CustomSVGTranscoder();
			transcoder.addTranscodingHint(KEY_WIDTH, width);
			transcoder.addTranscodingHint(KEY_HEIGHT, height);
			TranscoderInput tsi = new TranscoderInput(fis);
			transcoder.transcode(tsi, null);
			buffered = transcoder.getImage();
			if(buffered != null) {
				icon = new ImageIcon(buffered);
			}
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return icon;
	}
	@Override
	public BufferedImage createImage(int width, int height) {
		// TODO Auto-generated method stub
		return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}
	private BufferedImage getImage() {
		return buffered;
	}
	@Override
	public void writeImage(BufferedImage arg0, TranscoderOutput arg1) throws TranscoderException {
		// TODO Auto-generated method stub
		this.buffered = arg0;
	}
}
