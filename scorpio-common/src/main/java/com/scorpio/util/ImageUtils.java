package com.scorpio.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.regex.PatternSyntaxException;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author guosk
 *
 * create at 2023年07月05日, machloop
 */
public final class ImageUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImageUtils.class);

  private static final String TMP_ROOT_PATH = "/tmp";
  private static final String IMAGE_BASE64_PREFIX = "data:image/%s;base64,";

  private ImageUtils() {
    throw new IllegalStateException("Utility class");
  }

  public static void base64ToImage(String imageBase64, File targetFile) {

    try {
      String[] parts = imageBase64.split(",");
      String encodedImage = parts[1].replace("\r", "").replace("\n", "");
      byte[] decodedBytes = Base64.getDecoder().decode(encodedImage);
      try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
        outputStream.write(decodedBytes);
      }
    } catch (IOException | PatternSyntaxException e) {
      LOGGER.warn("base64 to image failed.", e);
    }
  }

  public static void base64ToImageResponse(String imageBase64, String contentType,
      HttpServletResponse response) throws IOException {

    try {
      response.setHeader("Cache-Control", "no-store");
      response.setHeader("Pragma", "no-cache");
      response.setDateHeader("Expires", 0);
      response.setContentType(contentType);

      ServletOutputStream responseOutputStream = response.getOutputStream();

      String[] parts = imageBase64.split(",");
      if (parts.length != 2) {
        // IMAGE_BASE64_PREFIX + base64_content
        return;
      }

      String encodedImage = parts[1].replace("\r", "").replace("\n", "");
      responseOutputStream.write(Base64.getDecoder().decode(encodedImage));
      responseOutputStream.flush();
      responseOutputStream.close();
    } catch (IOException | PatternSyntaxException e) {
      LOGGER.warn("base64 to image response failed.", e);
      throw new IOException("server error");
    }
  }

  public static String image2Base64(File image) {
    String imageBase64 = "";

    try {
      FileInputStream inputStream = new FileInputStream(image);
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      byte[] buffer = new byte[4096];
      int bytesRead = -1;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }
      byte[] bytes = outputStream.toByteArray();
      imageBase64 = Base64.getEncoder().encodeToString(bytes);
      imageBase64 = String.format(IMAGE_BASE64_PREFIX, FilenameUtils.getExtension(image.getName()))
          + imageBase64;

      inputStream.close();
      outputStream.close();
    } catch (IOException e) {
      LOGGER.warn("image to base64 failed.", e);
    }

    return imageBase64;
  }

  public static void imageCropper(File sourceImage, File targetImage, int x, int y, int cropWidth,
      int cropHeight) {

    try {
      BufferedImage originalImage = ImageIO.read(sourceImage);

      int width = originalImage.getWidth();
      int height = originalImage.getHeight();
      x = x > width || x < 0 ? 0 : x;
      y = y > height || y < 0 ? 0 : y;
      cropWidth = width - x < cropWidth || cropWidth < 0 ? width - x : cropWidth;
      cropHeight = height - y < cropHeight || cropHeight < 0 ? height - y : cropHeight;

      LOGGER.info("cropped image: coordinate[x: {}, y: {}], width: {}, height: {}", x, y, cropWidth,
          cropHeight);
      BufferedImage croppedImage = originalImage.getSubimage(x, y, cropWidth, cropHeight);
      String extension = FilenameUtils.getExtension(sourceImage.getPath());
      ImageIO.write(croppedImage, extension, targetImage);
    } catch (IOException e) {
      LOGGER.warn("cropped image failed.", e);
    }
  }

  public static void imageResizer(File sourceImage, File targetImage, int scaledWidth,
      int scaledHeight) throws IOException {

    BufferedImage originalImage = ImageIO.read(sourceImage);

    scaledWidth = scaledWidth <= 0 ? originalImage.getWidth() : scaledWidth;
    scaledHeight = scaledHeight <= 0 ? originalImage.getHeight() : scaledHeight;
    LOGGER.info("resized image: width: {}, height: {}", scaledWidth, scaledHeight);

    Image scaledImage = originalImage.getScaledInstance(scaledWidth, scaledHeight,
        Image.SCALE_REPLICATE);
    BufferedImage outputImage = new BufferedImage(scaledWidth, scaledHeight,
        originalImage.getType());
    Graphics graphics = outputImage.getGraphics();
    graphics.drawImage(scaledImage, 0, 0, null);
    graphics.dispose();

    String extension = FilenameUtils.getExtension(sourceImage.getPath());
    ImageIO.write(outputImage, extension, targetImage);
  }

  public static String imageBase64Cropper(String imageBase64, int x, int y, int cropWidth,
      int cropHeight) {
    String cropFileBase64 = "";

    Path sourcePath = null;
    Path targetPath = null;
    try {
      // base64 to image
      String[] parts = imageBase64.split(",");
      String extension = parts[0].split("/")[1].split(";")[0];
      String encodedImage = parts[1].replace("\r", "").replace("\n", "");
      byte[] decodedBytes = Base64.getDecoder().decode(encodedImage);

      sourcePath = Paths.get(TMP_ROOT_PATH, IdGenerator.generateUUID() + "." + extension);
      File sourceImage = sourcePath.toFile();
      try (FileOutputStream outputStream = new FileOutputStream(sourceImage)) {
        outputStream.write(decodedBytes);
      }

      // cropper
      targetPath = Paths.get(TMP_ROOT_PATH, IdGenerator.generateUUID() + "." + extension);
      File targetImage = targetPath.toFile();
      imageCropper(sourceImage, targetImage, x, y, cropWidth, cropHeight);

      // image to base64
      cropFileBase64 = image2Base64(targetImage);
    } catch (IOException | PatternSyntaxException e) {
      LOGGER.warn("imageBase64 cropped failed.", e);
    } finally {
      if (sourcePath != null) {
        FileUtils.deleteQuietly(sourcePath.toFile());
      }
      if (targetPath != null) {
        FileUtils.deleteQuietly(targetPath.toFile());
      }
    }

    return cropFileBase64;
  }

  public static String imageBase64Resizer(String imageBase64, int scaledWidth, int scaledHeight) {
    String resizeFileBase64 = "";

    Path sourcePath = null;
    Path targetPath = null;
    try {
      // base64 to image
      String[] parts = imageBase64.split(",");
      String extension = parts[0].split("/")[1].split(";")[0];
      String encodedImage = parts[1].replace("\r", "").replace("\n", "");
      byte[] decodedBytes = Base64.getDecoder().decode(encodedImage);

      sourcePath = Paths.get(TMP_ROOT_PATH, IdGenerator.generateUUID() + "." + extension);
      File sourceImage = sourcePath.toFile();
      try (FileOutputStream outputStream = new FileOutputStream(sourceImage)) {
        outputStream.write(decodedBytes);
      }

      // resize
      targetPath = Paths.get(TMP_ROOT_PATH, IdGenerator.generateUUID() + "." + extension);
      File targetImage = targetPath.toFile();
      imageResizer(sourceImage, targetImage, scaledWidth, scaledHeight);

      // image to base64
      resizeFileBase64 = image2Base64(targetImage);
    } catch (IOException | PatternSyntaxException e) {
      LOGGER.warn("imageBase64 resized failed.", e);
    } finally {
      if (sourcePath != null) {
        FileUtils.deleteQuietly(sourcePath.toFile());
      }
      if (targetPath != null) {
        FileUtils.deleteQuietly(targetPath.toFile());
      }
    }

    return resizeFileBase64;
  }

}
