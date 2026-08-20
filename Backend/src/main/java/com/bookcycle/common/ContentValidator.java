package com.bookcycle.common;

import com.bookcycle.exception.BusinessException;

/**
 * 评论 / 私信内容校验：content 与 imageUrl 至少填一项，且 imageUrl 仅接受本站上传路径。
 */
public final class ContentValidator {

    private ContentValidator() {
    }

    /**
     * 校验内容有效性。
     *
     * @param content  文字内容（可为空）
     * @param imageUrl 图片路径（可为空，若非空必须以 /uploads/ 开头）
     */
    public static void requireContent(String content, String imageUrl) {
        boolean hasContent = content != null && !content.isBlank();
        boolean hasImage = imageUrl != null && !imageUrl.isBlank();
        if (!hasContent && !hasImage) {
            throw new BusinessException("内容不能为空");
        }
        if (hasImage && !imageUrl.startsWith("/uploads/")) {
            throw new BusinessException("图片地址无效");
        }
    }
}
