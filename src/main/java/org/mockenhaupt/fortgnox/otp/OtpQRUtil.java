package org.mockenhaupt.fortgnox.otp;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OtpQRUtil
{

    public static int otp (String secretKey) {
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        int otpCode = gAuth.getTotpPassword(secretKey);
        return otpCode;
    }


    public static String otpString (String secretKey) {
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        int otpCode = gAuth.getTotpPassword(secretKey);
        return String.format("%06d", otpCode);
    }



    public static OTP getOtp (String line) {
        OTP otp = null;
        String regexp = "^(\\s*)otpauth://totp/([^\\s]+)(.*)$";
        Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(line);
        if (matcher.find() && matcher.groupCount() >= 3)
        {
            String urlString =  matcher.group(2).trim();
            String parameterString;
            otp = new OTP();

            int sepIx = urlString.indexOf('?');
            if (sepIx >= 0)
            {
                otp.setName(URLDecoder.decode(urlString.substring(0, sepIx)));
                parameterString = urlString.substring(sepIx + 1);
            }
            else
            {
                parameterString = urlString;
            }


            // Parse the parameterString string into a list of name-value pairs
            List<NameValuePair> params = URLEncodedUtils.parse(parameterString, StandardCharsets.UTF_8);

            // Print the parsed parameters
            for (NameValuePair param : params) {
                String value = param.getValue().trim();
                switch (param.getName().toUpperCase())
                {
                    case "ISSUER":
                        otp.setIssuer(value);
                        break;
                    case "ALGORITHM":
                        otp.setAlgorithm(value);
                        break;

                    case "DIGITS":
                        otp.setDigits(value);
                        break;
                    case "PERIOD":
                        otp.setPeriod(value);
                        break;
                    case "SECRET":
                        otp.setSecret(value);
                        break;
                }
            }
        }
        return otp;
    }



    public static String getValidUntil (int period, AtomicReference<Integer> remainingSecondsRef)
    {
        int now = LocalTime.now().toSecondOfDay();
        int valid = (now + period) / period * period;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime = LocalTime.ofSecondOfDay(valid).format(formatter);

        int remainingSeconds = valid - now;
        if (remainingSecondsRef != null) remainingSecondsRef.set(new Integer(remainingSeconds));
        return String.format("%s", formattedTime);
    }


    public static String decodeQrCode(String qrImagePath)
    {
        try {
            File file = new File(qrImagePath); // Bild mit QR-Code
            if (!file.exists()) {
                return "";
            }
            BufferedImage bufferedImage = ImageIO.read(file);
            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
