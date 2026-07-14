package com.timeline.app.data.guide

sealed interface GuideBlock {
    data class Paragraph(val text: String) : GuideBlock
    data class BulletList(val items: List<String>) : GuideBlock
}

data class GuideSection(val title: String, val blocks: List<GuideBlock>)

data class GuideContent(val intro: String, val sections: List<GuideSection>)
