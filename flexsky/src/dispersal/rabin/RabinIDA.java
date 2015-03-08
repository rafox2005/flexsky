/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dispersal.rabin;

import java.util.Arrays;
import util.GaloisField;
import util.Utils;

/**
 * Class that implements the Rabin IDA, Vandermonde matrices generation
 *
 * @author mira
 */
public class RabinIDA {

    private final GaloisField GF = GaloisField.getInstance();
    private int totalParts;
    private int reqParts;
    private int[][] vandermondeMatrix;
    private int[][] vandTransp;

    public RabinIDA(int totalParts, int reqParts) {
        this.totalParts = totalParts;
        this.reqParts = reqParts;
        this.generateVandermonde();

    }

    /**
     * Generate Vandermonde Matrix
     *
     * @return vandermondeMatrix
     */
    private int[][] generateVandermonde() {
        this.vandermondeMatrix = new int[totalParts][reqParts];
        for (int i = 0; i < totalParts; i++) {
            for (int j = 0; j < reqParts; j++) {
                vandermondeMatrix[i][j] = GF.power(i + 2, j);
            }
        }
         this.vandTransp = Utils.transposeMatrix(this.vandermondeMatrix);
        return this.vandermondeMatrix;
    }
    
    /**
     * Get Vandermonde Matrix. Check if the Vandermonde matrix is created,
     * if not it creates then it return the matrix.
     *
     * @return vandermondeMatrix
     */
    public int[][] getVandermondeMatrix() {
        if (this.vandermondeMatrix == null) {
            return this.generateVandermonde();
            
        }
        else return this.vandermondeMatrix;
    }

    /**
     * Encode each part of the message
     *
     * @param msg msg to be encoded
     * @return encoded message
     */
    public byte[] encodeEach(byte[] msg) {
        try {
            if (msg.length != this.reqParts) {
                throw new Exception("Different encode sizes from reqParts");
            }
            int[] msgInt = Utils.bytesToInts(msg);
            int[] multiplied = GF.multiplyMatrix(this.getVandermondeMatrix(), msgInt);
            byte[] cryptmsg = Utils.intsToBytes(multiplied);
            return cryptmsg;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
    
       /**
     * Decode each part of the message
     *
     * @param cryptmsg encrypted message to be decoded
     * @return message
     */
    public byte[] decodeEach(byte[] cryptmsg) {
        int[] intcrypt = Utils.bytesToInts(cryptmsg);     
        
        //Inicio coluna altera a matriz de vandermonde a ser utilizada
        int[][] partVandTransp = Utils.partOfMatrix(vandTransp, 0, this.reqParts, 2, this.reqParts);
        int[][] partVandTranspInverse = GF.vandermondeInverse(partVandTransp[1]);
        int[] partcrypt = Arrays.copyOfRange(intcrypt, 2, 2+reqParts);
        int[] dec = GF.multiplyMatrix(partVandTranspInverse, partcrypt);
        
        byte[] msg = Utils.intsToBytes(dec);
        return msg;
    }
    
     /**
     * Decode each part of the message using different parts. With all the parts
     * create only with the enough parts and then decode it
     *
     * @param cryptmsg encrypted message to be decoded
     * @param vandermondeIndexes  index of the parts
     * @return message
     */
    public byte[] decodeEach(byte[] cryptmsg, int[] vandermondeIndexes) {
        int[] intcrypt = Utils.bytesToInts(cryptmsg);     
        
        //Inicio coluna altera a matriz de vandermonde a ser utilizada
        int[][] partVandTransp = Utils.partOfMatrix(vandTransp, vandermondeIndexes);
        int[][] partVandTranspInverse = GF.vandermondeInverse(partVandTransp[1]);
        int[] partcrypt = Utils.partOfArray(intcrypt, vandermondeIndexes);
        int[] dec = GF.multiplyMatrix(partVandTranspInverse, partcrypt);
        
        byte[] msg = Utils.intsToBytes(dec);
        return msg;
    }
    
     /**
     * Decode each part of the message using only enough amount parts. With the enough parts
     * decode the message, there is no part function for the input here
     *
     * @param cryptmsg encrypted message to be decoded
     * @param vandermondeIndexes  index of the parts
     * @return message
     */
    public byte[] decodeEachEnough(byte[] cryptmsg, int[] vandermondeIndexes) {
        int[] intcrypt = Utils.bytesToInts(cryptmsg);     
        
        //Inicio coluna altera a matriz de vandermonde a ser utilizada
        int[][] partVandTransp = Utils.partOfMatrix(vandTransp, vandermondeIndexes);
        System.out.println(partVandTransp[1]);
        int[][] partVandTranspInverse = GF.vandermondeInverse(partVandTransp[1]);
        int[] dec = GF.multiplyMatrix(partVandTranspInverse, intcrypt);
        
        byte[] msg = Utils.intsToBytes(dec);
        return msg;
    }
    
}
