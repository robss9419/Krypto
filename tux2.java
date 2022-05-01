import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;

public class tux2 {

    static ArrayList<String> lista = new ArrayList<>();
    static ArrayList<String[]> sbox = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        ReadFile();
    }

    static void ReadFile() throws Exception {
        BufferedImage bImage = ImageIO.read(new File("image.jpg"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(bImage, "jpg", bos);
        byte[] pix = ((DataBufferByte) bImage.getRaster().getDataBuffer()).getData();
        writeToFile(bImage,
                encrypt(pix),
                "img.jpg");
    }


    public static void writeToFile(BufferedImage bm, byte [] content, String fileToWrite) throws Exception
    {
        SampleModel sm = bm.getSampleModel();
        DataBuffer db = new DataBufferByte(content, content.length);
        WritableRaster wr = Raster.createWritableRaster(sm, db, null);
        bm.setData(wr);
        ImageIO.write(bm, "jpg", new File(fileToWrite));
        System.out.println("Image generated from the byte array.");
    }

    public static byte [] encrypt(byte [] data)
    {
        StringBuilder imgdata = new StringBuilder();
        for (byte b : data) {
            imgdata.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
        split(imgdata.toString());
        imgdata.delete(0, imgdata.length());
        imgdata.append(ctr());

        byte[] bval = new BigInteger(imgdata.toString(), 2).toByteArray();
        return bval;
    }

    static void split(String s) {
        lista.clear();
        int a = 0;
        int b = 12;
        if(s.length() % 12 != 0) {
            int x = s.length() % 12;
            s = s.substring(0, s.length() - x);
        }
        for(int i = 0; i < s.length() / 12; i++) {
            lista.add(s.substring(a, b));
            a+=12;
            b+=12;
        }
    }

    static String xor(String x, String y) {
        String result = "";
        for (int i = 0; i < x.length(); i++) {
            if (x.charAt(i) == y.charAt(i)) {
                result += 0;
            } else {
                result += 1;
            }
        }
        return result;
    }

    static StringBuilder ctr() {
        StringBuilder sb = new StringBuilder();
        String n = "c";
        String nonce = new BigInteger(n, 16).toString(2);
        for (int i = 0; i < lista.size(); i++) {
            String iv = (nonce + (String.format("%8s", Integer.toBinaryString(i & 0xFF)).replace(' ', '0')));
            String R = iv.substring(iv.length() / 2);
            String L = iv.substring(0, iv.length() / 2);
            String result = xor(miniDes(L,R).toString(), lista.get(i));
            sb.append(result);
        }
        return sb;
    }


    static StringBuilder cfb() {
        StringBuilder sb = new StringBuilder();
        String iv = "111011010010";
        for (int i = 0; i < lista.size(); i++) {
            String R = iv.substring(iv.length() / 2);
            String L = iv.substring(0, iv.length() / 2);
            iv = xor(miniDes(L,R).toString(), lista.get(i));
            sb.append(iv);
        }
        return sb;
    }

    static StringBuilder cbc() {
        StringBuilder sb = new StringBuilder();
        String iv = "111011010010";
        for (int i = 0; i < lista.size(); i++) {
            String tmp = lista.get(i);
            tmp = xor(tmp, iv);
            String R = tmp.substring(tmp.length() / 2);
            String L = tmp.substring(0, tmp.length() / 2);
            iv = miniDes(L,R).toString();
            sb.append(iv);
        }
        return sb;
    }

    static StringBuilder ecb() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lista.size(); i++) {
            String tmp = lista.get(i);
            String R = tmp.substring(tmp.length() / 2);
            String L = tmp.substring(0, tmp.length() / 2);
            sb.append(miniDes(L, R));
        }
        return sb;
    }

    static StringBuilder miniDes(String L, String R) {
        String key = "10101010";
        String pi = "01323245";
        String sbox1 = "101 010 001 110 011 100 111 000 001 100 110 010 000 111 101 011";
        String sbox2 = "100 000 110 101 111 001 011 010 101 011 000 111 110 010 001 100";
        sbox.add(sbox1.split(" "));
        sbox.add(sbox2.split(" "));
        StringBuilder sb = new StringBuilder();
        for (int tura = 0; tura < 8; tura++) {
            String fin = "";
            for (int j = 0; j < pi.length(); j++) {
                fin += R.charAt(Integer.parseInt(String.valueOf(pi.charAt(j))));
            }
            String k = key.substring(tura + 1) + key.substring(0, tura + 1);
            //String k = key.substring(7-tura) + key.substring(0, 7-tura);
            String result = xor(k, fin);
            String Ri = result.substring(result.length() / 2);
            String Li = result.substring(0, result.length() / 2);

            int right = Integer.parseInt(Ri, 2);
            int left = Integer.parseInt(Li, 2);

            String v = sbox.get(0)[left];
            String o = sbox.get(1)[right];

            String finish = "";
            finish = xor(L, v + o);
            L = R;
            R = finish;
            if (tura == 6) {
                sb.append(R + L);
            }
        }
        return sb;
    }
}
