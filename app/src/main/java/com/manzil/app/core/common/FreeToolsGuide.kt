package com.manzil.app.core.common

/**
 * Zero se start - Free tools se goal tak
 * User requested: zayada se zayada free wali side se nikal ker goal ki taraf jaya
 */
object FreeToolsGuide {

    data class FreeTool(val name: String, val cost: String, val useFor: String, val link: String)

    val development = listOf(
        FreeTool("VS Code", "Free", "Code editor", "https://code.visualstudio.com"),
        FreeTool("GitHub Free", "Free", "Code hosting + 100-day streak", "https://github.com"),
        FreeTool("Git", "Free", "Version control", "https://git-scm.com"),
        FreeTool("Vercel", "Free", "Portfolio hosting", "https://vercel.com"),
        FreeTool("Railway", "Free tier", "Backend hosting", "https://railway.app"),
        FreeTool("Supabase", "Free", "PostgreSQL + Auth", "https://supabase.com"),
        FreeTool("Figma", "Free", "Design", "https://figma.com"),
        FreeTool("Trello", "Free", "Task management", "https://trello.com")
    )

    val learning = listOf(
        FreeTool("CS50x Harvard", "Free", "CS foundation", "https://cs50.harvard.edu/x/"),
        FreeTool("freeCodeCamp", "Free", "Web dev certs", "https://freecodecamp.org"),
        FreeTool("DigiSkills.pk", "Free", "Freelancing Urdu", "https://digiskills.pk"),
        FreeTool("The Odin Project", "Free", "Full-stack", "https://theodinproject.com"),
        FreeTool("YouTube", "Free", "Tutorials", "https://youtube.com")
    )

    val clientHuntingFree = listOf(
        FreeTool("Instagram", "Free", "DM outreach - search #smallbusiness", "https://instagram.com"),
        FreeTool("Facebook Groups", "Free", "Sahiwal Business, Freelancers", "https://facebook.com"),
        FreeTool("LinkedIn", "Free", "20 personalized messages/day", "https://linkedin.com"),
        FreeTool("X/Twitter", "Free", "Build in public - daily post", "https://x.com"),
        FreeTool("Reddit", "Free", "r/forhire, r/freelance", "https://reddit.com"),
        FreeTool("Discord", "Free", "Dev communities", "https://discord.com"),
        FreeTool("Indie Hackers", "Free", "Startup community", "https://indiehackers.com"),
        FreeTool("Product Hunt", "Free", "Launch side projects", "https://producthunt.com"),
        FreeTool("Google Business", "Free", "Local SEO", "https://business.google.com"),
        FreeTool("WhatsApp Business", "Free", "Client communication", "https://business.whatsapp.com")
    )

    val businessFree = listOf(
        FreeTool("Wave", "Free", "Invoicing + accounting", "https://waveapps.com"),
        FreeTool("Google Workspace", "Free trial", "Email + Docs", "https://workspace.google.com"),
        FreeTool("Canva Free", "Free", "Design + proposals", "https://canva.com"),
        FreeTool("Notion Free", "Free", "Docs + SOPs", "https://notion.so"),
        FreeTool("Uptime Kuma", "Free", "Monitor internet", "https://github.com/louislam/uptime-kuma")
    )

    fun getZeroToGoalRoadmap(): String = """
ZERO SE START — FREE SE GOAL TAK:

Week 1-2: Setup (Rs 0)
- GitHub account + roz 1 commit (100-day streak)
- VS Code + Git install
- freeCodeCamp Responsive Web Design
- CS50x enroll (free)

Week 3-4: Portfolio (Rs 0)
- 3 static sites GitHub + Vercel deploy
- LinkedIn profile professional
- Figma me 1 design

Week 5-8: First Money (Rs 0)
- Instagram: Roz 10 businesses ko DM (free script from Manzil)
- Facebook Groups: Sahiwal Business me value posts
- LinkedIn: 20 personalized messages/day
- Local: 3 businesses ko free website (case study)

Week 9-12: Retainer (Rs 0)
- Existing clients ko retainer me convert
- WhatsApp Business + Trello for management
- Wave for invoicing (free)

No Fiverr/Upwork fees — direct clients = 20% bachat!
    """.trimIndent()
}
