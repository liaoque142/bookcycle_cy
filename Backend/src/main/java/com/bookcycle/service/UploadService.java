package com.bookcycle.service;

import com.bookcycle.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

/**
 * 图片上传服务：本地磁盘存储，按 yyyy/MM 分目录，UUID 文件名。
 * <p>按 scene 区分限额：message（私信/评论/论坛内容图）最大 10MB，avatar（头像）/ payQr（收款码）最大 5MB。
 * <p>仅接受 jpg / png：魔数嗅探真实字节格式并按真实格式定扩展名落盘，
 * 杜绝「扩展名与字节不符」导致浏览器按错误格式解码而花屏的问题。
 * <p>返回 Web 可访问的路径（形如 /uploads/2026/08/xxx.png），由 WebConfig 静态映射提供访问。
 */
@Slf4j
@Service
public class UploadService {

    /** 内容图（私信/评论/论坛）上限 10MB */
    private static final long MAX_SIZE_MESSAGE = 10L * 1024 * 1024;
    /** 头像上限 5MB */
    private static final long MAX_SIZE_AVATAR = 5L * 1024 * 1024;
    /** 收款二维码上限 5MB */
    private static final long MAX_SIZE_PAY_QR = 5L * 1024 * 1024;
    private static final Set<String> SCENES = Set.of("message", "avatar", "payQr");
    /** PNG 签名：89 50 4E 47 0D 0A 1A 0A */
    private static final byte[] PNG_SIG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    /** PNG 首块类型必须为 IHDR（49 48 44 52），防伪造/截断 */
    private static final byte[] PNG_IHDR = {0x49, 0x48, 0x44, 0x52};
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy/MM");

    @Value("${app.upload-dir:./uploads}")
    private String uploadDir;

    /** 保存图片并返回可访问 URL 路径；scene=message 10MB / avatar 5MB，仅 jpg、png */
    public String uploadImage(MultipartFile file, String scene) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的图片");
        }
        if (!SCENES.contains(scene)) {
            throw new BusinessException("非法的上传场景");
        }
        long maxSize = maxSizeFor(scene);
        if (file.getSize() > maxSize) {
            throw new BusinessException(maxSizeMessage(scene));
        }
        String ext;
        try {
            ext = detectExt(file);
        } catch (IOException e) {
            log.error("读取上传图片失败", e);
            throw new BusinessException(500, "图片读取失败，请稍后再试");
        }
        String datePath = LocalDate.now().format(DATE_FMT);
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path dir = Paths.get(uploadDir, datePath);
        try {
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(filename));
        } catch (IOException e) {
            log.error("上传图片失败", e);
            throw new BusinessException(500, "图片保存失败，请稍后再试");
        }
        return "/uploads/" + datePath + "/" + filename;
    }

    /** 按 scene 返回对应上限：message 10MB，avatar / payQr 5MB */
    private long maxSizeFor(String scene) {
        return "message".equals(scene) ? MAX_SIZE_MESSAGE : MAX_SIZE_AVATAR;
    }

    /** 超限提示文案（收款码单独文案，其余统一 10MB/5MB） */
    private String maxSizeMessage(String scene) {
        if ("avatar".equals(scene)) {
            return "头像图片不能超过 5MB";
        }
        if ("payQr".equals(scene)) {
            return "收款码图片不能超过 5MB";
        }
        return "图片大小不能超过 10MB";
    }

    /** 读文件头按真实字节识别格式，返回落盘扩展名（jpg / png） */
    private String detectExt(MultipartFile file) throws IOException {
        byte[] head = new byte[16];
        int len;
        try (InputStream in = file.getInputStream()) {
            len = in.readNBytes(head, 0, head.length);
        }
        // JPEG：FF D8 FF（SOI + 下一 marker 首字节）
        if (len >= 3 && (head[0] & 0xFF) == 0xFF && (head[1] & 0xFF) == 0xD8 && (head[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        // PNG：8 字节签名 + 首块类型 IHDR（防伪造/截断）
        if (len >= 16 && matches(head, 0, PNG_SIG) && matches(head, 12, PNG_IHDR)) {
            return "png";
        }
        throw new BusinessException("仅支持 jpg / png 格式");
    }

    private static boolean matches(byte[] data, int offset, byte[] sig) {
        for (int i = 0; i < sig.length; i++) {
            if (data[offset + i] != sig[i]) {
                return false;
            }
        }
        return true;
    }
}
