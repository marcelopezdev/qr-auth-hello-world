package py.com.qr.auth_qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class QRAuthController {

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, Boolean> qrCodeStatus = new HashMap<>();
    private final Map<String, Instant> qrCodeExpiry = new HashMap<>();

    public QRAuthController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping(value = "/generate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> generateQRCode(HttpServletRequest request) throws Exception {
        String sessionId = UUID.randomUUID().toString();
        log.info("session_id: {}", sessionId);
        qrCodeStatus.put(sessionId, false);
        qrCodeExpiry.put(sessionId, Instant.now().plusSeconds(60)); // Expira en 60 segundos

        // Capturar datos del dispositivo
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(join(sessionId,userAgent,ipAddress), BarcodeFormat.QR_CODE, 300, 300);

        BufferedImage qrImage = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 300; x++) {
            for (int y = 0; y < 300; y++) {
                qrImage.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }

        // Cargar el logo
        Resource logoResource = new ClassPathResource("static/img/alien.png");
        BufferedImage logo = ImageIO.read(logoResource.getInputStream());

        // Calcular el tamaño del logo
        int logoWidth = 60;
        int logoHeight = 60;

        // Centrar el logo en la imagen del QR
        int x = (qrImage.getWidth() - logoWidth) / 2;
        int y = (qrImage.getHeight() - logoHeight) / 2;

        // Superponer el logo sobre la imagen QR
        Graphics2D graphics = qrImage.createGraphics();
        graphics.drawImage(logo, x, y, logoWidth, logoHeight, null);
        graphics.dispose();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(qrImage, "png", outputStream);

        Map<String, Object> response = new HashMap<>();
        response.put("qrImage", outputStream.toByteArray());
        response.put("sessionId", sessionId);
        response.put("expiresAt", qrCodeExpiry.get(sessionId));

        return ResponseEntity.ok(response);
    }

    private String join(String sessionId, String userAgent, String ip) {
        return String.join("||", sessionId, userAgent, ip);
    }


    @PostMapping("/scan/{sessionId}")
    public ResponseEntity<String> scanQRCode(@PathVariable String sessionId) {
        if (!qrCodeStatus.containsKey(sessionId)) {
            return ResponseEntity.status(404).body("Invalid QR Code");
        }
        qrCodeStatus.put(sessionId, true);

        // Notifica al cliente que el QR fue escaneado
        messagingTemplate.convertAndSend("/topic/auth-status/" + sessionId, "aca puedo mandar info: "+sessionId);

        return ResponseEntity.ok("QR Code scanned successfully");
    }

    @GetMapping("/is-expired/{sessionId}")
    public ResponseEntity<Boolean> isExpired(@PathVariable String sessionId) {
        Instant expiryTime = qrCodeExpiry.get(sessionId);
        if (expiryTime == null || Instant.now().isAfter(expiryTime)) {
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.ok(false);
    }


}