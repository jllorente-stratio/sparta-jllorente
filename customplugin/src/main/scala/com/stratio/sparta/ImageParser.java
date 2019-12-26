package com.stratio.sparta;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageParser { 

	private static final String SEPARATOR = "x";
	private static Logger logger = LoggerFactory.getLogger(ImageParser.class);

	public static void main(String[] args) {
		//System.out.println("Empezando");
		for (String json : readFile("data2.log")) {
			getImageVector(json, "28x28",true);
		}
	}

	private static List<String> readFile(String fileName) {
		List<String> contentFile = new ArrayList<String>();
		try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
			contentFile = stream.collect(Collectors.toList());

		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println("Tamaño fichero: " + contentFile.size());
		return contentFile;
	}

	public static String getImageVector(String json, String resize) {
		return getImageVector(json,resize,false);
	}
	
	public static String getImageVector(String json, String resize, boolean generateImage) {
		logger.error("Invocado método getImageVector con "+json + ", resize: " + resize+" y generateImage: "+generateImage);
		String[] pixels = resize.split(SEPARATOR);
		int scaledWidth = Integer.parseInt(pixels[0]);
		int scaledHeight = Integer.parseInt(pixels[1]);
		StringBuilder stringVector = new StringBuilder();

		if (!json.contains("{") || !json.contains("image")) {
			logger.error("El mensaje recibido no es json o no tiene una imagen");
			stringVector.append("No llego una imagen llegó " + json);

		} else {
			logger.error("El mensaje recibido es json y contiene una imagen");
			try {
				JSONObject jsonObject = new JSONObject(json);
				String image = jsonObject.getString("image");
				String id = jsonObject.getString("id");
				stringVector.append(id + ",");

				byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(image);
				BufferedImage imageResized = createResizedCopy(ImageIO.read(new ByteArrayInputStream(imageBytes)),
						scaledWidth, scaledHeight, Boolean.TRUE);
				
				toGray(imageResized);

				int[] pixels2 =((DataBufferInt)imageResized.getRaster().getDataBuffer()).getData();
				changeDecimalToRGB(pixels2);
//				if(generateImage) {
//					generateImage(pixels2,scaledWidth,scaledHeight);
//				}
				
				//System.out.println("Tamaño pixels2: "+pixels2);
				for (int i : pixels2) {
					stringVector.append(i+ ",");
				}
				// Elimina la última coma
				stringVector.replace(stringVector.length() - 1, stringVector.length(), "");

				// logger.error("Vector generado");
			} catch (JSONException err) {
				logger.error("Error JSONException" + err.getMessage());
				// err.printStackTrace();
			} catch (IOException e) {
				logger.error("Error IOException" + e.getMessage());
				// e.printStackTrace();
			}

		}

		logger.error("devolviendo "+stringVector.toString());
		return stringVector.toString();
	}
	
	private static void changeDecimalToRGB (int[] pixels) {
		
		for (int i = 0; i < pixels.length; i++) {
			int numColor = pixels[i];
			Color color = new Color(numColor);
			int red = color.getRed();
			int green = color.getGreen();
			int blue = color.getBlue();
			
			//pixels[i] = Math.abs((color.getRed()-255));
			pixels[i] = color.getBlue();
			//System.out.println(red + " " + green + " " + blue);
		}
		
	}

	public static BufferedImage getImageFromArray(int[] pixels, int width, int height) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		WritableRaster raster = (WritableRaster) image.getData();
		raster.setPixels(0, 0, width, height, pixels);

		return image;
	}

//	public static void generateImage(String[] spixels, int width, int height) {
//		logger.error("Generating image generateImage: "+spixels.length);
//		int[] pixels = new int[spixels.length-1];
//		
//		for (int i = 1; i < spixels.length; i++) {
//			pixels[i-1]=Integer.parseInt(spixels[i]);
//		}
//		
//		BufferedImage pixelImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//		pixelImage.setRGB(0, 0, width, height, pixels, 0, width);
//		
////		try {
////			ImageIO.write(pixelImage, "jpg", new File("/home/jllorente/test"+System.currentTimeMillis()+".jpg"));
////		} catch (IOException e) {
////			e.printStackTrace();
////		}
//	}
//
//	public static void generateImage(int[] pixels, int width, int height) {
//		
//		BufferedImage pixelImage = new BufferedImage(width, 28, BufferedImage.TYPE_INT_RGB);
//		pixelImage.setRGB(0, 0, width, height, pixels, 0, width);
//		
//		try {
//			ImageIO.write(pixelImage, "jpg", new File("/home/jllorente/test"+System.currentTimeMillis()+".jpg"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	/**
	 * @param args
	 */
	public static void toGray(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				Color c = new Color(image.getRGB(j, i));
				int red = (int) (c.getRed() * 0.21);
				int green = (int) (c.getGreen() * 0.72);
				int blue = (int) (c.getBlue() * 0.07);
				int sum = red + green + blue;
				Color newColor = new Color(sum, sum, sum);
				image.setRGB(j, i, newColor.getRGB());
			}
		}
	}

	public static BufferedImage createResizedCopy(BufferedImage originalImage, int scaledWidth, int scaledHeight,
			boolean preserveAlpha) {
		System.out.println("resizing...");
		int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
		BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
		Graphics2D g = scaledBI.createGraphics();
		if (preserveAlpha) {
			g.setComposite(AlphaComposite.Src);
		}
		g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
		g.dispose();
		return scaledBI;
	}
}