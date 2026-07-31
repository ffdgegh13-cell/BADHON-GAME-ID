package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SecMailSummaryDto(
    @Json(name = "id") val id: Long,
    @Json(name = "from") val from: String,
    @Json(name = "subject") val subject: String,
    @Json(name = "date") val date: String
)

@JsonClass(generateAdapter = true)
data class SecMailAttachmentDto(
    @Json(name = "filename") val filename: String? = null,
    @Json(name = "contentType") val contentType: String? = null,
    @Json(name = "size") val size: Long? = null
)

@JsonClass(generateAdapter = true)
data class SecMailDetailDto(
    @Json(name = "id") val id: Long,
    @Json(name = "from") val from: String,
    @Json(name = "subject") val subject: String,
    @Json(name = "date") val date: String,
    @Json(name = "attachments") val attachments: List<SecMailAttachmentDto>? = emptyList(),
    @Json(name = "body") val body: String? = "",
    @Json(name = "textBody") val textBody: String? = "",
    @Json(name = "htmlBody") val htmlBody: String? = ""
)
