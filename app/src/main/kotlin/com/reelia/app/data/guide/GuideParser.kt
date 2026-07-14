package com.reelia.app.data.guide

/**
 * Parses the Markdown convention used by docs/guide/{fr,en}.md: a leading H1 (skipped — the
 * screen supplies its own title), an intro paragraph, then `## ` sections each containing
 * paragraphs and/or `- `/`N. ` bullet lists. Unlike [com.reelia.app.data.releasenotes.ReleaseNotesParser],
 * list items and paragraphs can wrap across multiple source lines (no leading marker on the
 * continuation lines), which this merges back into one block.
 */
object GuideParser {
    private val listItemRegex = Regex("^(?:[-*]|\\d+\\.)\\s+(.*)$")

    fun parse(markdown: String): GuideContent {
        val introBuilder = StringBuilder()
        val sections = mutableListOf<GuideSection>()
        var currentTitle: String? = null
        var currentBlocks = mutableListOf<GuideBlock>()
        var currentParagraph: StringBuilder? = null
        var currentList: MutableList<String>? = null

        fun flushParagraph() {
            val text = currentParagraph?.toString()?.trim()
            if (!text.isNullOrEmpty()) {
                if (currentTitle == null) {
                    if (introBuilder.isNotEmpty()) introBuilder.append(' ')
                    introBuilder.append(text)
                } else {
                    currentBlocks.add(GuideBlock.Paragraph(text))
                }
            }
            currentParagraph = null
        }

        fun flushList() {
            currentList?.takeIf { it.isNotEmpty() }?.let { currentBlocks.add(GuideBlock.BulletList(it.toList())) }
            currentList = null
        }

        fun flushSection() {
            flushParagraph()
            flushList()
            val title = currentTitle ?: return
            sections += GuideSection(title, currentBlocks.toList())
            currentBlocks = mutableListOf()
        }

        for (raw in markdown.lines()) {
            val line = raw.trim()
            when {
                line.startsWith("# ") -> Unit
                line.startsWith("## ") -> {
                    flushSection()
                    currentTitle = line.removePrefix("## ").trim()
                }
                line.isEmpty() -> {
                    flushParagraph()
                    flushList()
                }
                listItemRegex.matches(line) -> {
                    flushParagraph()
                    val item = listItemRegex.find(line)!!.groupValues[1].trim()
                    val list = currentList ?: mutableListOf<String>().also { currentList = it }
                    list.add(item)
                }
                else -> {
                    val list = currentList
                    if (list != null && list.isNotEmpty()) {
                        list[list.lastIndex] = list.last() + " " + line
                    } else {
                        val paragraph = currentParagraph ?: StringBuilder().also { currentParagraph = it }
                        if (paragraph.isNotEmpty()) paragraph.append(' ')
                        paragraph.append(line)
                    }
                }
            }
        }
        flushSection()

        return GuideContent(introBuilder.toString().trim(), sections)
    }
}
