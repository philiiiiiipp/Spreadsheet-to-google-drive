package com.tec.spreadsheettogoogledrive;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.os.Environment;
import au.com.bytecode.opencsv.CSVWriter;

public class Util {

	public static final String SPLIT_SYMBOL = "#";

	/**
	 * Converts the measurement to a .csv file.
	 * 
	 * @param m
	 * @return null if an error occured
	 */
	public static File convertToFile(final List<String> lines) {
		CSVWriter writer;
		File file = null;
		File root = Environment.getExternalStorageDirectory();
		if (root.canWrite()) {
			File dir = new File(root.getAbsolutePath()
					+ "/FaceDistanceMeasurements");
			dir.mkdirs();

			try {
				file = new File(dir, "TestFile" + System.currentTimeMillis()
						+ ".csv");
				writer = new CSVWriter(new FileWriter(file), ',');

				for (String s : lines) {
					// feed in your array (or convert your data to an array)
					String[] entries = s.split(SPLIT_SYMBOL);
					writer.writeNext(entries);
				}

				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return file;
	}

	public static List<String> getTestLines() {
		ArrayList<String> lines = new ArrayList<String>();

		lines.add("testA1" + SPLIT_SYMBOL + "TestB1" + SPLIT_SYMBOL + "TestC1");
		lines.add("testA2" + SPLIT_SYMBOL + "TestB2" + SPLIT_SYMBOL + "TestC2");
		lines.add("testA3" + SPLIT_SYMBOL + "TestB3" + SPLIT_SYMBOL + "TestC3");
		lines.add("testA4" + SPLIT_SYMBOL + "TestB4");
		lines.add("testA5" + SPLIT_SYMBOL + SPLIT_SYMBOL + "TestC5");

		return lines;
	}

}
