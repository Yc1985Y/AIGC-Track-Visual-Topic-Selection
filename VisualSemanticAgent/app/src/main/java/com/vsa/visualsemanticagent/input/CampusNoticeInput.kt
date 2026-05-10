package com.vsa.visualsemanticagent.input

import android.net.Uri

enum class NoticeSourceType(
    val value: String,
    val label: String
) {
    CAMERA("camera", "拍摄图片"),
    ALBUM("album", "相册图片"),
    SHARE_IMAGE("share_image", "分享图片"),
    SHARE_TEXT("share_text", "分享文本"),
    CLIPBOARD("clipboard", "剪贴板文本"),
    MANUAL_TEXT("manual_text", "手动输入文本")
}

data class CampusNoticeInput(
    val sourceType: NoticeSourceType,
    val rawText: String? = null,
    val imageUri: Uri? = null,
    val base64Image: String? = null,
    val userInstruction: String = "帮我把这个校园通知加入日程"
) {
    val hasImage: Boolean
        get() = !base64Image.isNullOrBlank() || imageUri != null

    val hasText: Boolean
        get() = !rawText.isNullOrBlank()
}
