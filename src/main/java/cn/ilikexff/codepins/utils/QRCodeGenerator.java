package cn.ilikexff.codepins.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码生成工具类
 */
public class QRCodeGenerator {

    /**
     * 生成二维码
     *
     * @param text 二维码内容
     * @param filePath 保存路径
     * @param width 宽度
     * @param height 高度
     * @throws WriterException 编码异常
     * @throws IOException IO异常
     */
    public static void generateQRCode(String text, String filePath, int width, int height) throws WriterException, IOException {
        // 设置二维码参数
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 2);

        // 生成二维码
        BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        // 保存为文件
        File file = new File(filePath);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", file.toPath());
    }
}
