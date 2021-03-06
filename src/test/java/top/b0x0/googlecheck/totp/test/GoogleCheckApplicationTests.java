package top.b0x0.googlecheck.totp.test;

import com.google.zxing.WriterException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import top.b0x0.googlecheck.GoogleCheckApplication;
import top.b0x0.googlecheck.totp.common.Constants;
import top.b0x0.googlecheck.totp.util.GoogleAuthenticatorUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = GoogleCheckApplication.class)
public class GoogleCheckApplicationTests {

    @Test
    public void contextLoads() throws IOException, WriterException {
        // 生成随机的密钥
        String secretKey = GoogleAuthenticatorUtils.createSecret();
        System.out.println("随机密钥：" + secretKey);

        // 根据验证码，账户，服务商生成 TOPT 密钥的 URI
        String totpUri = GoogleAuthenticatorUtils.createQrUri(secretKey, "17688888888", Constants.COMPANY_DOMAIN);
        System.out.println("TOPT密钥URI：" + totpUri);

        // 根据 TOPT 密钥的 URI生成二维码，存储在本地
        FileOutputStream fileOutputStream = new FileOutputStream("D:\\google-auth.png");
        GoogleAuthenticatorUtils.createQrCode(totpUri, fileOutputStream, 200, 200);
        fileOutputStream.close();

        String lastCode = null;
        while (true) {
            // 根据密钥获取此刻的动态口令 30s刷新一次
            String code = GoogleAuthenticatorUtils.getTotpCode(secretKey);
            if (!code.equals(lastCode)) {
                System.out.println("刷新了验证码：" + code + " [" + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()) + "]");
            }
            lastCode = code;
//            try {
//                Thread.sleep(3000);  // 线程暂停3秒
//            } catch (InterruptedException e) {
//                System.out.println(e.getMessage());
//            }
        }
    }

    public static String createQrUri(String secret, String account, String issuer) {
        String normalizedBase32Key = secret.replace(" ", "").toUpperCase();
        String qrCodeData = "otpauth://totp/%s?secret=%s&issuer=%s";
        System.out.println("soutv qrUri = " + "otpauth://totp/" + issuer + "(" + account + ")" + "?secret=" + normalizedBase32Key + "&issuer=" + issuer);
        try {
            return "otpauth://totp/"
                    + URLEncoder.encode(issuer + "(" + account + ")", "UTF-8").replace("+", "%20")
                    + "?secret="
                    + URLEncoder.encode(normalizedBase32Key, "UTF-8").replace("+", "%20")
                    + "&issuer="
                    + URLEncoder.encode(issuer, "UTF-8").replace("+", "%20");
//            return String.format(
//                    qrCodeData,
//                    URLEncoder.encode(issuer + ":" + account, "UTF-8").replace("+", "%20"),
//                    URLEncoder.encode(normalizedBase32Key, "UTF-8").replace("+", "%20"),
//                    URLEncoder.encode(issuer, "UTF-8").replace("+", "%20"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void test1() {
        String secret = "AM4XYVANNGQUWJ5G63MYXQQLVCBIOHTC";
        String account = "17688888888";
        String issuer = Constants.COMPANY_NAME;

        String qrUri = createQrUri(secret, account, issuer);
        System.out.println("qrUri = " + qrUri);
    }

    @Test
    public void test2() throws UnsupportedEncodingException {
        String s = "A+M4XYVANNGQUWJ5G63MYXQQLVCBIOHT+C";
        String replace = s.replace("+", "%20");
        System.out.println("replace = " + replace);

//        String encode = URLEncoder.encode(s,"UTF-8");
//        System.out.println("encode = " + encode);
//        String replace1 = encode.replace("+", "%20");
//        System.out.println("replace1 = " + replace1);
    }

    @Test
    public void test3() throws UnsupportedEncodingException {
        long time = (System.currentTimeMillis() / 1000) / 30;
        System.out.println("time = " + time); // 53833293
    }

}


