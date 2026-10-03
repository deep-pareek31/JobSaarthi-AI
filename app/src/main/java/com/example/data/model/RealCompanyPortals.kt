package com.example.data.model

import java.net.URLEncoder

data class CareerPortal(
    val id: String,
    val name: String,
    val brandColorHex: Long,
    val badge: String,
    val description: String,
    val urlBuilder: (query: String) -> String
)

object RealCompanyPortals {
    val portals = listOf(
        CareerPortal(
            id = "google",
            name = "Google Careers",
            brandColorHex = 0xFF4285F4,
            badge = "Official Google Portal",
            description = "Direct engineering, product, AI, and campus roles at Google worldwide.",
            urlBuilder = { q ->
                val encoded = URLEncoder.encode(q.ifBlank { "Software Engineer" }, "UTF-8")
                "https://www.google.com/about/careers/applications/jobs/results/?q=$encoded"
            }
        ),
        CareerPortal(
            id = "microsoft",
            name = "Microsoft Careers",
            brandColorHex = 0xFF00A4EF,
            badge = "Global Opportunities",
            description = "Cloud, AI, Azure, and developer positions at Microsoft.",
            urlBuilder = { q ->
                val encoded = URLEncoder.encode(q.ifBlank { "Software Engineer" }, "UTF-8")
                "https://jobs.careers.microsoft.com/global/en/search?q=$encoded"
            }
        ),
        CareerPortal(
            id = "amazon",
            name = "Amazon Jobs",
            brandColorHex = 0xFFFF9900,
            badge = "Tech & AWS",
            description = "Explore AWS, retail tech, logistics, and SDE openings at Amazon.",
            urlBuilder = { q ->
                val encoded = URLEncoder.encode(q.ifBlank { "Software Development Engineer" }, "UTF-8")
                "https://www.amazon.jobs/en/search?base_query=$encoded"
            }
        ),
        CareerPortal(
            id = "apple",
            name = "Apple Careers",
            brandColorHex = 0xFF555555,
            badge = "Hardware & iOS",
            description = "iOS, macOS, machine learning, and silicon engineering at Apple.",
            urlBuilder = { q ->
                val encoded = URLEncoder.encode(q.ifBlank { "Software Engineer" }, "UTF-8")
                "https://jobs.apple.com/en-us/search?search=$encoded"
            }
        ),
        CareerPortal(
            id = "linkedin",
            name = "LinkedIn Jobs",
            brandColorHex = 0xFF0A66C2,
            badge = "Worldwide Live Feed",
            description = "Search millions of active hiring posts across all industries.",
            urlBuilder = { q ->
                val encoded = URLEncoder.encode(q.ifBlank { "Developer" }, "UTF-8")
                "https://www.linkedin.com/jobs/search/?keywords=$encoded"
            }
        ),
        CareerPortal(
            id = "indeed",
            name = "Indeed Jobs",
            brandColorHex = 0xFF2164F3,
            badge = "Top Employer Board",
            description = "Comprehensive real-time job listings and salary insights.",
            urlBuilder = { q ->
                val encoded = URLEncoder.encode(q.ifBlank { "Software Developer" }, "UTF-8")
                "https://in.indeed.com/jobs?q=$encoded"
            }
        )
    )
}
