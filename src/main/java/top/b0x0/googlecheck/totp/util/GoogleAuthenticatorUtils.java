package top.b0x0.googlecheck.totp.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Google身份验证器工具类
 *
 * @author TANG
 */
public class GoogleAuthenticatorUtils {

    /**
     * 获取随机密钥
     *
     * @return /
     */
    public static String createSecret() {
        SecureRandom random = new SecureRandom();
        // SecretKey长度
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        Base32 base32 = new Base32();
        String secret = base32.encodeToString(bytes);
        // make the secret key more human-readable by lower-casing and
        // inserting spaces between each group of 4 characters
        // .replaceAll("(.{4})(?=.{4})", "$1 ");
        return secret.toUpperCase();
    }

    /**
     * 根据密钥生成验证码
     *
     * @param secret 密钥
     * @return /
     */
    public static String getTotpCode(String secret) {
        String normalizedBase32Key = secret.replace(" ", "").toUpperCase();
        Base32 base32 = new Base32();
        byte[] bytes = base32.decode(normalizedBase32Key);
        String hexKey = Hex.encodeHexString(bytes);
        // 第几个30秒
        long time = (System.currentTimeMillis() / 1000) / 30;
        String hexTime = Long.toHexString(time);
        return TOTP.generateTOTP(hexKey, hexTime, "6");
    }

    /**
     * 生成 Google Authenticator 二维码所需信息
     * Google Authenticator 约定的 Key Url 格式: otpauth://totp/{issuer}:{account}?secret={secret}&issuer={issuer}
     * 参数需要 url 编码 + 号需要替换成 %20
     *
     * @param secret  密钥 使用 [createSecretKey方法]生成
     * @param account 用户账户 如: 138XXXXXXXX
     * @param issuer  提供服务的组织/企业 如: xxx公司
     */
    public static String createQrUri(String secret, String account, String issuer) {
        String normalizedBase32Key = secret.replace(" ", "").toUpperCase();
        String qrCodeData = "otpauth://totp/%s?secret=%s&issuer=%s";
        try {
            return String.format(
                    qrCodeData,
                    URLEncoder.encode(issuer + "(" + account + ")", "UTF-8").replace("+", "%20"),
                    URLEncoder.encode(normalizedBase32Key, "UTF-8").replace("+", "%20"),
                    URLEncoder.encode(issuer, "UTF-8").replace("+", "%20"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 根据 TOPT 密钥的 URI 字符串 生成二维码
     *
     * @param barCode  TOPT密钥URI
     * @param filePath 文件路径
     * @param height   /
     * @param width    /
     * @throws WriterException /
     * @throws IOException     /
     */
    public static void createQrCode(String barCode, String filePath, int height, int width) throws WriterException, IOException {
        BitMatrix matrix = new MultiFormatWriter().encode(barCode, BarcodeFormat.QR_CODE, width, height);
        try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
            MatrixToImageWriter.writeToStream(matrix, "png", outputStream);
        }
    }

    /**
     * 根据 TOPT 密钥的 URI 字符串 生成二维码
     *
     * @param barCode      TOPT密钥URI
     * @param outputStream /
     * @param height       /
     * @param width        /
     * @throws WriterException /
     * @throws IOException     /
     */
    public static void createQrCode(String barCode, OutputStream outputStream, int height, int width) throws WriterException, IOException {
        BitMatrix matrix = new MultiFormatWriter().encode(barCode, BarcodeFormat.QR_CODE, width, height);
        MatrixToImageWriter.writeToStream(matrix, "png", outputStream);
    }

    /**
     * <p>
     * 时间前后偏移量
     * 用于防止客户端时间不精确导致生成的TOTP与服务器端的TOTP一直不一致
     * 如果为 0，当前时间为 10:10:15
     * 则表明在 10:10:00-10:10:30 之间生成的TOTP 能校验通过
     * 如果为 1，则表明在
     * 10:09:30-10:10:00
     * 10:10:00-10:10:30
     * 10:10:30-10:11:00 之间生成的TOTP 能校验通过
     * 以此类推
     * <p>
     * <p>
     * default 3 - max 17 (from google docs)最多可偏移的时间
     */
    static int window_size = 3;

    /**
     * set the windows size. This is an integer value representing the number of
     * 30 second windows we allow The bigger the window, the more tolerant of
     * clock skew we are.
     *
     * @param s window size - must be >=1 and <=17. Other values are ignored
     */
    public static void setWindowSize(int s) {
        if (s >= 1 && s <= 17) {
            window_size = s;
        }
    }

    /**
     * Check the totpCode entered by the user to see if it is valid
     *
     * @param secret   The users secret.
     * @param totpCode The totpCode displayed on the users device  修改传参类型 使用long-->string 防止001937这种验证码丢失位数导致校验不准
     * @param timeMsec The time in msec (System.currentTimeMillis() for example)
     * @return /
     */
    public static boolean checkCode(String secret, String totpCode, long timeMsec) {
        Base32 codec = new Base32();
        byte[] decodedKey = codec.decode(secret);
        // convert unix msec time into a 30 second "window"
        // this is per the TOTP spec (see the RFC for details)
        long t = (timeMsec / 1000L) / 30L;
        // Window is used to check codes generated in the near past.
        // You can use this value to tune how far you're willing to go.
        for (int i = -window_size; i <= window_size; ++i) {
            long hash;
            try {
                hash = verifyCode(decodedKey, t + i);
            } catch (Exception e) {
                // Yes, this is bad form - but
                // the exceptions thrown would be rare and a static
                // configuration problem
                // e.printStackTrace();
                throw new RuntimeException(e.getMessage());
                // return false;
            }
            // 使用string.format 补0判断 防止动态码为零开头的现象
            if (totpCode.equals(addZero(hash))) {
                return true;
            }
/*            if (hash == totpCode) {
                return true;
            }*/
        }
        // The validation totpCode is invalid.
        return false;
    }

    /**
     * @param key decodedKey 解密后的密钥
     * @param t   偏移量
     * @return /
     * @throws NoSuchAlgorithmException /
     * @throws InvalidKeyException      /
     */
    private static int verifyCode(byte[] key, long t) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] data = new byte[8];
        long value = t;
        for (int i = 8; i-- > 0; value >>>= 8) {
            data[i] = (byte) value;
        }
        SecretKeySpec signKey = new SecretKeySpec(key, "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signKey);
        byte[] hash = mac.doFinal(data);
        int offset = hash[20 - 1] & 0xF;
        // We're using a long because Java hasn't got unsigned int.
        long truncatedHash = 0;
        for (int i = 0; i < 4; ++i) {
            truncatedHash <<= 8;
            // We are dealing with signed bytes:
            // we just keep the first byte.
            truncatedHash |= (hash[offset + i] & 0xFF);
        }
        truncatedHash &= 0x7FFFFFFF;
        truncatedHash %= 1000000;
        return (int) truncatedHash;
    }

    private static String addZero(long code) {
        return String.format("%06d", code);
    }
}
