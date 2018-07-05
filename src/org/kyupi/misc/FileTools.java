/*
 * Copyright 2013 The KyuPI project contributors. See the COPYRIGHT.md file
 * at the top-level directory of this distribution.
 * This file is part of the KyuPI project. It is subject to the license terms
 * in the LICENSE.md file found in the top-level directory of this distribution.
 * No part of the KyuPI project, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.md file.
 */
package org.kyupi.misc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2Utils;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipUtils;

public class FileTools {

	public static final int FILE_TYPE_UNKNOWN = 0;
	public static final int FILE_TYPE_ISCAS = 1;
	public static final int FILE_TYPE_BENCH = 2;
	public static final int FILE_TYPE_VHDL = 3;
	public static final int FILE_TYPE_VERILOG = 4;
	public static final int FILE_TYPE_KDB = 5;
	public static final int FILE_TYPE_DOT = 100;
	

	private FileTools() {
	}

	/**
	 * creates a new, possibly compressed file.
	 * 
	 * @param f
	 * @return
	 * @throws IOException
	 *             if file already exists or cannot be created.
	 */
	public static OutputStream fileCreate(File f, boolean allowOverwrite) throws IOException {
		if (f.exists()) {
			if (!allowOverwrite)
				throw new IOException("file already exists: " + f);
		}
		File parent = f.getParentFile();
		if (parent != null && !parent.exists()) {
			parent.mkdirs();
		}
		FileOutputStream fos = new FileOutputStream(f);
		if (BZip2Utils.isCompressedFilename(f.getName()))
			return new BZip2CompressorOutputStream(fos);
		else if (GzipUtils.isCompressedFilename(f.getName()))
			return new GzipCompressorOutputStream(fos);
		return fos;
	}

	public static String fileBasename(File f) {
		String n = f.getName();
		if (n.contains("."))
			return n.substring(0, n.lastIndexOf("."));
		return n;
	}

	/**
	 * opens a possibly compressed file.
	 * 
	 * @param f
	 * @return an appropriate InputStream for the given file.
	 * @throws IOException
	 */
	public static InputStream fileOpen(File f) throws IOException {
		FileInputStream fis = new FileInputStream(f);
		BufferedInputStream bis = new BufferedInputStream(fis);
		if (GzipUtils.isCompressedFilename(f.getName()))
			return new GzipCompressorInputStream(bis);
		if (BZip2Utils.isCompressedFilename(f.getName()))
			return new BZip2CompressorInputStream(bis);
		return bis;
	}

	/**
	 * guesses a file type from the extension.
	 * 
	 * @param f
	 * @return The FILE_TYPE constant corresponding to the recognized
	 *         type, or 0 if type is unknown.
	 */
	public static int fileType(File f) {
		String n = f.getName();
		if (BZip2Utils.isCompressedFilename(n))
			n = BZip2Utils.getUncompressedFilename(n);
		if (GzipUtils.isCompressedFilename(n))
			n = GzipUtils.getUncompressedFilename(n);
		if (n.endsWith(".isc"))
			return FileTools.FILE_TYPE_ISCAS;
		if (n.endsWith(".bench"))
			return FileTools.FILE_TYPE_BENCH;
		if (n.endsWith(".vhdl"))
			return FileTools.FILE_TYPE_VHDL;
		if (n.endsWith(".vhd"))
			return FileTools.FILE_TYPE_VHDL;
		if (n.endsWith(".kdb"))
			return FileTools.FILE_TYPE_KDB;
		if (n.endsWith(".dot"))
			return FileTools.FILE_TYPE_DOT;
		if (n.endsWith(".v"))
			return FileTools.FILE_TYPE_VERILOG;
		if (n.endsWith(".vg"))
			return FileTools.FILE_TYPE_VERILOG;
		return FileTools.FILE_TYPE_UNKNOWN;
	}
	
	
}
