/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

/**
 *
 * @author mira
 */
public class Utils
{

    /**
     * Print a matrix. The main purpose of this class is to print a Matrix for
     * initial Debug purposes.
     *
     * @param m matrix to be printed
     *
     */
    public static void printMatrix(int[][] m)
    {
        try {
            int rows = m.length;
            int columns = m[0].length;
            String str = "|\t";

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    str += m[i][j] + "\t";
                }

                System.out.println(str + "|");
                str = "|\t";
            }

        } catch (Exception e) {
            System.out.println("Matrix is empty!!");
        }
    }

    /**
     * Print a vector. This method prints a vector using the printMatrix method.
     *
     * @param m array to be printed
     */
    public static void printMatrix(int[] m)
    {
        try {
            int[][] teste = new int[1][m.length];
            teste[0] = m;

            Utils.printMatrix(teste);

        } catch (Exception e) {
            System.out.println("Matrix is empty!!");
        }
    }

    /**
     * Get the available memory of the system. This class return the amount of
     * memory the system have
     *
     * @return freememory amount of available memory
     */
    public static long getAvailableMemory()
    {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory(); // current heap allocated to the VM process
        long freeMemory = runtime.freeMemory(); // out of the current heap, how much is free
        long maxMemory = runtime.maxMemory(); // Max heap VM can use e.g. Xmx setting
        long usedMemory = totalMemory - freeMemory; // how much of the current heap the VM is using
        long availableMemory = maxMemory - usedMemory; // available memory i.e. Maximum heap size minus the current amount used
        return availableMemory;
    }

    /**
     * Convert an Int array into a Byte array. Useful for conversion between the
     * file content and the input of the Disperse function.
     *
     * @param ints int vector to be converted
     * @return byte array converted from ints array
     */
    public static byte[] intsToBytes(int[] ints)
    {
        byte[] byteArray = new byte[ints.length];

        for (int i = 0; i < ints.length; i++) {
            byteArray[i] = (byte) ints[i];
        }

        return byteArray;
    }

    /**
     * Convert a Byte array into an Int array. Useful for conversion between the
     * output of the Disperse function and the file input
     *
     * @param vbytes Byte vector to be converted
     * @return Int array converted from vbytes array
     */
    public static int[] bytesToInts(byte[] vbytes)
    {
        int[] intArray = new int[vbytes.length];

        for (int i = 0; i < vbytes.length; i++) {
            intArray[i] = (int) vbytes[i] & (0xff);
        }

        return intArray;

    }

    /**
     * Transpose a matrix. Function to transpose a matrix
     *
     * @param v int matrix to be transposed
     * @return int matrix transposed
     */
    public static int[][] transposeMatrix(int[][] V)
    {
        int result[][] = new int[V[0].length][V.length];

        for (int r = 0; r < V.length; r++) {
            for (int c = 0; c < V[0].length; c++) {
                result[c][r] = V[r][c];
            }
        }
        return result;
    }

    /**
     * Get only a part of a Matrix using offsets. Given a matrix and the columns
     * and rows offsets gets a new matrix only with the offsets from the
     * previous matrix
     *
     * @param matrix input matrix to get int vector to be converteda part of
     * @param startLine line number of matrix to begin the new part matrix
     * @param offsetLine line offset of matrix to be in the new matrix
     * @param startCol column number of matrix to begin the new matrix
     * @param offsetCol columns offset of matrix to be in the new matrix
     * @return int matrix part of the original matrix
     */
    public static int[][] partOfMatrix(int[][] matrix, int startLine, int offsetLine, int startCol, int offsetCol)
    {
        int[][] newmatrix = new int[offsetLine][offsetCol];
        for (int i = startLine; i < offsetLine + startLine; i++) {
            newmatrix[i] = Arrays.copyOfRange(matrix[i], startCol, startCol + offsetCol);
        }
        return newmatrix;
    }

    /**
     * Get only a part of a Matrix using desired index. Given a matrix and the
     * columns and rows index gets a new matrix only with the index rows and
     * columns from the previous matrix
     *
     * @param matrix input matrix to get int vector to be converted a part of
     * @param indexes int array with the index to be used in the new matrix
     * @return int matrix part of the original matrix with the desired indexes
     */
    public static int[][] partOfMatrix(int[][] matrix, int[] indexes)
    {
        try {
            int[][] newmatrix = new int[indexes.length][indexes.length];
            int aux = 0;
            for (int index : indexes) {
                newmatrix[aux] = Utils.partOfArray(matrix[aux], indexes);
                aux++;
            }
            return newmatrix;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    /**
     * Get only a part of an Array using desired index. Given an Array and the
     * columns and rows index gets a new array only with the index rows and
     * columns from the previous array
     *
     * @param array input int array to to be converted to a new array with the
     * desired indexes
     * @param indexes int array with the index to be used in the new array
     * @return int array part of the original array with the desired indexes
     */
    public static int[] partOfArray(int[] array, int[] indexes)
    {
        int[] newArray = new int[indexes.length];
        int aux = 0;
        for (int index : indexes) {
            newArray[aux] = array[index];
            if (aux < newArray.length) {
                aux++;
            }
        }
        return newArray;
    }

    public static byte[] removeNulls(byte[] array)
    {
        byte[] newArray;
        int nnulls = 0;
        int i = array.length - 1;
        while (array[i] == 0x00) {
            nnulls++;
            i--;
        }
        if (nnulls > 0) {
            newArray = Arrays.copyOf(array, array.length - nnulls);
            return newArray;
        } else {
            return array;
        }
    }
        
        

    public static String getStringFromMessageDigest(MessageDigest md)
    {
        return (new HexBinaryAdapter()).marshal(md.digest());
    }
}
