/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

/**
 *
 * @author mira
 */
public class Safestore
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
//        int totParts = Integer.parseInt(args[1]);
//        int reqParts = Integer.parseInt(args[2]);
//        String filepath = args[3];
//        File file = new File(filepath);
//        File path = new File(file.getParent());
//        double sizeStart = file.length();
//        double sizeEnd;
//
//        if (args[0].equalsIgnoreCase("rabin")) {
//            OutputStream[] outStreams = new OutputStream[5];
//            for (int i = 0; i < 5; i++) {
//                try {
//                    outStreams[i] = new FileOutputStream(new File(path, "bigRA" + i));
//                } catch (Exception e) {
//                }
//
//            }
//
//            EncoderRabinIDA ida = new EncoderRabinIDA(totParts, reqParts, file, outStreams);
//            ida.encode();
//            ByteArrayOutputStream[] partesSaida = ida.getWriteBufs();
//            sizeEnd = partesSaida.length * partesSaida[0].size();
//
//            File[] partesEntrada = new File[reqParts];
//
//            for (int i = 0; i < reqParts; i++) {
//                try {
//                    partesEntrada[i] = new File(path, "idapart" + i);
//                } catch (Exception e) {
//                }
//
//            }
//
//            DecoderRabinIDA dida = new DecoderRabinIDA(totParts, reqParts, partesEntrada);
//            dida.decode();
//            try {
//
//                OutputStream outputStream2 = new FileOutputStream(new File(path, "idafinal" + file.getName()));
//                dida.getWriteBuffer().writeTo(outputStream2);
//            } catch (Exception e) {
//            }
//
//            System.out.println("RABIN;" + Monitor.getInstance().getTimeToEncode() + ";" + Monitor.getInstance().getTimeToDecode() + ";" + (sizeEnd / sizeStart));
//
//        } else if (args[0].equalsIgnoreCase("reed")) {
//            int partsWithPar = (totParts - reqParts) * 2 + reqParts;
//            OutputStream[] outStreams = new OutputStream[partsWithPar];
//            for (int i = 0; i < partsWithPar; i++) {
//                try {
//                    outStreams[i] = new FileOutputStream(new File(path, "bigRS" + i));
//                } catch (Exception e) {
//                }
//
//            }            
//            
//            EncoderRS reed = new EncoderRS(totParts, reqParts, file, outStreams);
//            reed.encode();
//            ByteArrayOutputStream[] partesSaida = reed.getWriteBufs();
//            sizeEnd = partesSaida.length * partesSaida[0].size();
//            File[] partesEntrada = new File[partsWithPar];
//
//            for (int i = 0; i < partsWithPar; i++) {
//                try {
//                    partesEntrada[i] = new File(path, "rspart" + i);
//                } catch (Exception e) {
//                }
//
//            }
//
//            DecoderRS dreed = new DecoderRS(totParts, reqParts, partesEntrada);
//            dreed.decode();
//            try {
//
//                OutputStream outputStream2 = new FileOutputStream(new File(path, "rsfinal" + file.getName()));
//                dreed.getWriteBuffer().writeTo(outputStream2);
//            } catch (Exception e) {
//            }
//
//            System.out.println("REED-SOLOMON;" + Monitor.getInstance().getTimeToEncode() + ";" + Monitor.getInstance().getTimeToDecode() + ";" + (sizeEnd / sizeStart));
//        }
    }
}
