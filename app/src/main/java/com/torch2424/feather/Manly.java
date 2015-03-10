package com.torch2424.feather;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * API to manage files on Android, media focused. Manly the FileManager!
 * 
 * @author torch2424
 * 
 */
public class Manly
{
	// Our Natively Supported File Types we need to check
	private static String[] musicTypes = { ".mp3", ".flac", ".mid", ".xmf",
			".mxmf", ".rtttl", ".rtx", ".ota", ".imy", ".ogg", ".wav" };
	private static String[] videoTypes = { ".mp4", ".3gp", ".mkv", ".ts",
			".webm" };
	private static String[] imageTypes = { ".jpg", ".png", ".gif", ".bmp" };

	public Manly()
	{
		// Nothing needs to be initialized here, mostly Static
	}

	/**
	 * Function to check if a file is an apk
	 * 
	 * @param file
	 *            , File to check if it is an apk
	 * @return true if it is, false if not
	 */
	public static boolean isApk(File file)
	{
		if (file.getName().endsWith(".apk"))
			return true;
		else
			return false;
	}

	/**
	 * Function to check if a file is an image
	 * 
	 * @param file
	 *            , File to check if it is an image
	 * @return true if it is, false if not
	 */
	public static boolean isImage(File file)
	{
		for (int i = 0; i < imageTypes.length; i++)
		{
			if (file.getName().endsWith(imageTypes[i]))
			{
				return true;
			}
		}

		// Did not find it to be a image
		return false;
	}

	/**
	 * Function to check if a file is a music
	 * 
	 * @param file
	 *            , File to check if it is a music
	 * @return true if it is, false if not
	 */
	public static boolean isMusic(File file)
	{
		for (int i = 0; i < musicTypes.length; i++)
		{
			if (file.getName().endsWith(musicTypes[i]))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Function to check if a file is a video
	 * 
	 * @param file
	 *            , File to check if it is a video
	 * @return true if it is, false if not
	 */
	public static boolean isVideo(File file)
	{
		for (int i = 0; i < videoTypes.length; i++)
		{
			if (file.getName().endsWith(videoTypes[i]))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Function to return the contents of a directory into an array
	 * 
	 * @return A sorted string array with contents of directory
	 */
	public static String[] getDirectoryArray(File file)
	{
		// Need to check if the file is a directory
		if (file.isDirectory())
		{
			// create our array
			String[] array;

			// Fill it up and sort
			array = file.list();
			if (array != null)
				Arrays.sort(array);
			return array;
		}
		else
		{
			return null;
		}
	}

	/**
	 * This will delete all the files and the directoy in a folder
	 * 
	 * @param directory
	 *            , the folder to delete
	 */
	public static void deleteDirectory(File directory)
	{
		File[] fList = directory.listFiles();
		for (File file : fList)
		{
			if (file.isFile())
			{
				file.delete();
			}
			else if (file.isDirectory())
			{
				deleteDirectory(file);
				file.delete();
			}
		}
	}

	/**
	 * This unzips a .zip file
	 * 
	 * @param input
	 *            , the zip file we are extracting
	 * @param outputDir
	 *            , the output folder we should create and extract to
	 * @throws IOException
	 */
	// Modified Code from
	// http://www.codejava.net/java-se/file-io/programmatically-extract-a-zip-file-using-java
	public static void unZipFile(File input, File outputDir) throws IOException
	{
		File destDir = new File(outputDir.getAbsolutePath());
		if (!destDir.exists())
		{
			destDir.mkdir();
		}
		ZipInputStream zipIn = new ZipInputStream(new FileInputStream(
				input.getAbsolutePath()));
		ZipEntry entry = zipIn.getNextEntry();
		// iterates over entries in the zip file
		while (entry != null)
		{
			String filePath = outputDir.getAbsolutePath() + File.separator
					+ entry.getName();
			if (!entry.isDirectory())
			{
				// if the entry is a file, extracts it
				extractFile(zipIn, filePath);
			}
			else
			{
				// if the entry is a directory, make the directory
				File dir = new File(filePath);
				dir.mkdir();
			}
			zipIn.closeEntry();
			entry = zipIn.getNextEntry();
		}
		zipIn.close();
	}

	/**
	 * Extracts a zip entry (file entry) - Helper to public zip extractor
	 * 
	 * @param zipIn
	 * @param filePath
	 * @throws IOException
	 */
	private static void extractFile(ZipInputStream zipIn, String filePath)
			throws IOException
	{
		BufferedOutputStream bos = new BufferedOutputStream(
				new FileOutputStream(filePath));
		byte[] bytesIn = new byte[4096];
		int read = 0;
		while ((read = zipIn.read(bytesIn)) != -1)
		{
			bos.write(bytesIn, 0, read);
		}
		bos.close();
	}
}
