package com.example.model

enum class ClipCategory {
  OTP,
  URL,
  EMAIL,
  PHONE,
  COLOR_HEX,
  TEXT,
}

enum class ClipboardFilter {
  ALL,
  PINNED,
  SMART,
}

data class ClipboardItem(
  val id: String,
  val text: String,
  val timestamp: Long,
  val isPinned: Boolean = false,
  val category: ClipCategory = ClipCategory.TEXT,
  val smartExtract: String? = null,
) {
  val isSmartEntity: Boolean
    get() = category != ClipCategory.TEXT
}

object SmartClipboardAnalyzer {

  private val urlRegex = Regex(
    pattern = """(https?://[^\s]+|www\.[^\s]+)""",
    option = RegexOption.IGNORE_CASE,
  )
  private val emailRegex = Regex(
    pattern = """[A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}""",
  )
  private val hexColorRegex = Regex(
    pattern = """(?:^|\s)(#(?:[0-9a-fA-F]{6}|[0-9a-fA-F]{8}))(?=\s|$|[.,;])""",
  )
  private val standaloneOtpRegex = Regex(
    pattern = """^\d{4,8}$""",
  )
  private val otpContextRegex = Regex(
    pattern = """(?:код|code|otp|pin|пароль|verification|confirm|подтвержден\S*|авторизац\S*)[^\d\n]{0,32}?([0-9]{4,8})(?!\d)""",
    option = RegexOption.IGNORE_CASE,
  )
  private val phoneRegex = Regex(
    pattern = """^\+?[0-9][0-9\-\s()]{7,17}[0-9]$""",
  )
  private val trackingQueryParams = setOf(
    "utm_source",
    "utm_medium",
    "utm_campaign",
    "utm_term",
    "utm_content",
    "fbclid",
    "gclid",
    "yclid",
    "igshid",
    "si",
  )

  fun analyze(rawText: String): Pair<ClipCategory, String?> {
    val trimmed = rawText.trim()
    if (trimmed.isEmpty()) return ClipCategory.TEXT to null

    if (standaloneOtpRegex.matches(trimmed)) {
      return ClipCategory.OTP to trimmed
    }

    otpContextRegex.find(trimmed)?.let { match ->
      val code = match.groupValues.getOrNull(1)
      if (!code.isNullOrBlank()) {
        return ClipCategory.OTP to code
      }
    }

    urlRegex.find(trimmed)?.let { match ->
      val url = match.value.trimEnd('.', ',', ';', ')', ']')
      val cleaned = cleanTrackingParams(url)
      val smart = if (cleaned != url) cleaned else url
      return ClipCategory.URL to smart
    }

    if (trimmed.length <= 64) {
      hexColorRegex.find(trimmed)?.let { match ->
        val hex = match.groupValues.getOrNull(1)
        if (!hex.isNullOrBlank()) {
          return ClipCategory.COLOR_HEX to hex.uppercase()
        }
      }
    }

    emailRegex.find(trimmed)?.let { match ->
      if (trimmed.length <= 120) {
        return ClipCategory.EMAIL to match.value
      }
    }

    if (phoneRegex.matches(trimmed)) {
      val digitsCount = trimmed.count { it.isDigit() }
      if (digitsCount in 8..15) {
        return ClipCategory.PHONE to trimmed
      }
    }

    return ClipCategory.TEXT to null
  }

  private fun cleanTrackingParams(url: String): String {
    val qIndex = url.indexOf('?')
    if (qIndex == -1) return url
    val base = url.substring(0, qIndex)
    val fragmentSplit = url.substring(qIndex + 1).split('#', limit = 2)
    val query = fragmentSplit[0]
    val fragment = if (fragmentSplit.size > 1) "#${fragmentSplit[1]}" else ""

    val kept = query
      .split('&')
      .filter { pair ->
        val key = pair.substringBefore('=').lowercase()
        key.isNotEmpty() && key !in trackingQueryParams
      }

    return if (kept.isEmpty()) {
      base + fragment
    } else {
      "$base?${kept.joinToString("&")}$fragment"
    }
  }
}
