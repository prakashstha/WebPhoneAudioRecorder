/*
 * Copyright 2015 (c) Secure System Group (https://se-sy.org/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uab.cis.spies.audiorecorderdemo;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;


public final class ConversionUtil {

    public static short conv2Short(byte[] bytes) {
        if (bytes.length != 2) {
            throw new IllegalArgumentException(
                    "Failed to convert byte[] to short");
        }
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    public static int conv2Int(byte[] bytes) {
        if (bytes.length != 4) {
            throw new IllegalArgumentException(
                    "Failed to convert byte[] to integer");
        }
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public static long conv2Long(byte[] bytes) {
        if (bytes.length != 8) {
            throw new IllegalArgumentException(
                    "Failed to convert byte[] to long");
        }
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getLong();
    }

    public static double conv2Double(byte[] bytes) {
        if (bytes.length != (Double.SIZE/8)) {
            throw new IllegalArgumentException(
                    "Failed to convert byte[] to double");
        }
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getDouble();
    }

    public static double[] conv2DoubleArray(byte[] bytes) {
        int numOfElements = bytes.length / (Double.SIZE/8);
        if (bytes.length != (numOfElements * (Double.SIZE/8))) {
            throw new IllegalArgumentException(
                    "Failed to convert byte[] to double[]");
        }
        double[] dArray = new double[numOfElements];
        for (int i = 0; i < numOfElements; i++) {
            dArray[i] = conv2Double(Arrays.copyOfRange(bytes, i * (Double.SIZE/8),
                    (i + 1) * (Double.SIZE/8)));
        }
        return dArray;
    }

    public static long[] conv2LongArray(byte[] bytes) {
        int numOfElements = bytes.length / (Long.SIZE/8);
        if (bytes.length != (numOfElements * (Long.SIZE/8))) {
            throw new IllegalArgumentException(
                    "Failed to convert byte[] to long[]");
        }
        long[] lArray = new long[numOfElements];
        for (int i = 0; i < numOfElements; i++) {
            lArray[i] = conv2Long(Arrays.copyOfRange(bytes, i * (Long.SIZE/8),
                    (i + 1) * (Long.SIZE/8)));
        }
        return lArray;
    }

    public static int[] conv2IntArray(byte[] bytes) {
        int numOfElements = bytes.length / (Integer.SIZE/8);
        if (bytes.length != (numOfElements * (Integer.SIZE/8))) {
            throw new IllegalArgumentException(
                    "Failed to convert byte[] to int[]");
        }
        int[] iArray = new int[numOfElements];
        for (int i = 0; i < numOfElements; i++) {
            iArray[i] = conv2Int(Arrays.copyOfRange(bytes, i * (Integer.SIZE/8),
                    (i + 1) * (Integer.SIZE/8)));
        }
        return iArray;
    }

    public static float conv2Float(byte[] bytes) {
        if (bytes.length != (Float.SIZE/8)) {
            throw new IllegalArgumentException(
                    "Failed to convert byte[] to float");
        }
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();
    }
}