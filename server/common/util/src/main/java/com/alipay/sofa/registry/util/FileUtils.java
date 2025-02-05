/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.registry.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author shangyu.wh
 * @version $Id: FileUtils.java, v 0.1 2018-08-23 12:02 shangyu.wh Exp $
 */
public class FileUtils {

  private static final int EOF = -1;

  /**
   * write file
   *
   * @param file file
   * @param data data
   * @param append append
   * @throws IOException IOException
   */
  public static void writeByteArrayToFile(File file, byte[] data, boolean append)
      throws IOException {
    OutputStream out = null;
    try {
      out = openOutputStream(file, append);
      out.write(data);
      out.close(); // don't swallow close Exception if copy completes
      // normally
    } finally {
      closeQuietly(out);
    }
  }

  /**
   * read file
   *
   * @param file file
   * @return byte[]
   * @throws IOException IOException
   */
  public static byte[] readFileToByteArray(File file) throws IOException {
    InputStream in = null;
    try {
      in = openInputStream(file);
      return toByteArray(in, file.length());
    } finally {
      closeQuietly(in);
    }
  }

  /**
   * create dir
   *
   * @param directory directory
   * @throws IOException IOException
   */
  public static void forceMkdir(File directory) throws IOException {
    if (directory.exists()) {
      if (!directory.isDirectory()) {
        String message =
            "File "
                + directory
                + " exists and is "
                + "not a directory. Unable to create directory.";
        throw new IOException(message);
      }
    } else {
      if (!directory.mkdirs()) {
        // Double-check that some other thread or process hasn't made
        // the directory in the background
        if (!directory.isDirectory()) {
          String message = "Unable to create directory " + directory;
          throw new IOException(message);
        }
      }
    }
  }

  /**
   * file output stream
   *
   * @param file file
   * @param append append
   * @return FileOutputStream
   * @throws IOException IOException
   */
  public static FileOutputStream openOutputStream(File file, boolean append) throws IOException {
    if (file.exists()) {
      if (file.isDirectory()) {
        throw new IOException("File '" + file + "' exists but is a directory");
      }
      if (!file.canWrite()) {
        throw new IOException("File '" + file + "' cannot be written to");
      }
    } else {
      File parent = file.getParentFile();
      if (parent != null) {
        if (!parent.mkdirs() && !parent.isDirectory()) {
          throw new IOException("Directory '" + parent + "' could not be created");
        }
      }
    }
    return new FileOutputStream(file, append);
  }

  /**
   * file to inputStream
   *
   * @param file file
   * @return FileInputStream
   * @throws IOException IOException
   */
  public static FileInputStream openInputStream(File file) throws IOException {
    if (file.exists()) {
      if (file.isDirectory()) {
        throw new IOException("File '" + file + "' exists but is a directory");
      }
      if (!file.canRead()) {
        throw new IOException("File '" + file + "' cannot be read");
      }
    } else {
      throw new FileNotFoundException("File '" + file + "' does not exist");
    }
    return new FileInputStream(file);
  }

  /**
   * transfer InputStream to byteArray
   *
   * @param input input
   * @param size size
   * @return byte[]
   * @throws IOException IOException
   */
  public static byte[] toByteArray(InputStream input, long size) throws IOException {

    if (size > Integer.MAX_VALUE) {
      throw new IllegalArgumentException("Size cannot be greater than Integer max value: " + size);
    }

    return toByteArray(input, (int) size);
  }

  /**
   * transfer InputStream to byteArray
   *
   * @param input input
   * @param size size
   * @return byte[]
   * @throws IOException IOException
   */
  public static byte[] toByteArray(InputStream input, int size) throws IOException {

    if (size < 0) {
      throw new IllegalArgumentException("Size must be equal or greater than zero: " + size);
    }

    if (size == 0) {
      return new byte[0];
    }

    byte[] data = new byte[size];
    int offset = 0;
    int readed;

    while (offset < size && (readed = input.read(data, offset, size - offset)) != EOF) {
      offset += readed;
    }

    if (offset != size) {
      throw new IOException("Unexpected readed size. current: " + offset + ", excepted: " + size);
    }

    return data;
  }

  /** @param closeable closeable */
  public static void closeQuietly(Closeable closeable) {
    try {
      if (closeable != null) {
        closeable.close();
      }
    } catch (IOException ioe) {
      // ignore
    }
  }

  public static void forceDelete(File file) throws IOException {
    if (!file.exists()) {
      return;
    }

    if (file.isDirectory()) {
      deleteDirectory(file);
    } else {
      if (!file.delete()) {
        String message = "Unable to delete file: " + file;
        throw new IOException(message);
      }
    }
  }

  public static void deleteDirectory(File directory) throws IOException {
    if (!directory.exists()) {
      return;
    }

    cleanDirectory(directory);
    if (!directory.delete()) {
      String message = "Unable to delete directory " + directory + ".";
      throw new IOException(message);
    }
  }

  public static void cleanDirectory(File directory) throws IOException {
    if (!directory.exists()) {
      String message = directory + " does not exist";
      throw new IllegalArgumentException(message);
    }

    if (!directory.isDirectory()) {
      String message = directory + " is not a directory";
      throw new IllegalArgumentException(message);
    }

    File[] files = directory.listFiles();
    if (files == null) { // null if security restricted
      throw new IOException("Failed to list contents of " + directory);
    }

    IOException exception = null;
    for (int i = 0; i < files.length; i++) {
      File file = files[i];
      try {
        forceDelete(file);
      } catch (IOException ioe) {
        exception = ioe;
      }
    }

    if (null != exception) {
      throw exception;
    }
  }
}
