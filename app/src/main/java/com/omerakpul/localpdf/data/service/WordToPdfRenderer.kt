package com.omerakpul.localpdf.data.service

import com.tom_roush.pdfbox.util.Matrix
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDFont
import org.apache.poi.xwpf.usermodel.ParagraphAlignment
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import org.apache.poi.xwpf.usermodel.XWPFTable

/**
 * Draws the content of a .docx onto PDF pages: paragraphs with bold/italic runs, font sizes,
 * headings, alignment, simple bullet lists and basic tables. Images and complex layout
 * (text boxes, columns, headers/footers) are not supported.
 */
class WordToPdfRenderer(
    private val document: PDDocument,
    private val fonts: Fonts
) {
    /**
     * Fonts per style. A style font may be missing (null): the regular font is then used with a
     * synthetic effect ([StyledFont.fauxBold] / [StyledFont.fauxItalic]).
     */
    class Fonts(
        val regular: PDFont,
        val bold: PDFont?,
        val italic: PDFont?,
        val boldItalic: PDFont?,
        /** false when only the WinAnsi-only standard fonts are available */
        val isUnicode: Boolean
    ) {
        fun pick(isBold: Boolean, isItalic: Boolean): StyledFont = when {
            isBold && isItalic -> when {
                boldItalic != null -> StyledFont(boldItalic)
                bold != null -> StyledFont(bold, fauxItalic = true)
                italic != null -> StyledFont(italic, fauxBold = true)
                else -> StyledFont(regular, fauxBold = true, fauxItalic = true)
            }
            isBold -> if (bold != null) StyledFont(bold) else StyledFont(regular, fauxBold = true)
            isItalic -> if (italic != null) StyledFont(italic) else StyledFont(regular, fauxItalic = true)
            else -> StyledFont(regular)
        }
    }

    class StyledFont(
        val font: PDFont,
        val fauxBold: Boolean = false,
        val fauxItalic: Boolean = false
    )

    private class Piece(
        val text: String,
        val font: PDFont,
        val size: Float,
        val spaceBefore: Boolean,
        val isBreak: Boolean = false,
        val fauxBold: Boolean = false,
        val fauxItalic: Boolean = false
    ) {
        val width: Float = if (isBreak) 0f else font.getStringWidth(text) / 1000f * size
    }

    private val pageSize = PDRectangle.A4
    private val margin = 50f
    private val contentWidth = pageSize.width - 2 * margin
    private val defaultSize = 12f

    private lateinit var stream: PDPageContentStream
    private var y = 0f
    private val renderable = HashMap<Char, Boolean>()

    fun render(wordDocument: XWPFDocument) {
        startPage()
        try {
            for (element in wordDocument.bodyElements) {
                when (element) {
                    is XWPFParagraph -> paragraph(element)
                    is XWPFTable -> table(element)
                    else -> {}
                }
            }
        } finally {
            stream.close()
        }
    }

    // ---------------- page handling ----------------

    private fun startPage() {
        val page = PDPage(pageSize)
        document.addPage(page)
        stream = PDPageContentStream(document, page)
        y = pageSize.height - margin
    }

    private fun ensureSpace(height: Float) {
        if (y - height < margin) {
            stream.close()
            startPage()
        }
    }

    // ---------------- text helpers ----------------

    private fun sanitize(raw: String): String {
        val sb = StringBuilder()
        for (c in raw) {
            val ch = when {
                c == '\t' -> ' '
                !fonts.isUnicode -> TURKISH_FALLBACK[c] ?: c
                else -> c
            }
            if (ch == '\n') {
                sb.append(ch)
                continue
            }
            val ok = renderable.getOrPut(ch) {
                try {
                    fonts.regular.getStringWidth(ch.toString()); true
                } catch (e: Exception) {
                    false
                }
            }
            sb.append(if (ok) ch else '?')
        }
        return sb.toString()
    }

    private fun piecesFor(paragraph: XWPFParagraph, baseSize: Float, forceBold: Boolean): List<Piece> {
        val pieces = mutableListOf<Piece>()
        var pendingSpace = false
        val token = Regex("\\s+|\\S+")

        for (run in paragraph.runs) {
            val sizeValue = run.fontSizeAsDouble
            val size = if (sizeValue != null && sizeValue > 0) sizeValue.toFloat() else baseSize
            val styled = fonts.pick(run.isBold || forceBold, run.isItalic)
            val font = styled.font
            val lines = sanitize(run.text()).split('\n')

            lines.forEachIndexed { index, line ->
                if (index > 0) {
                    pieces.add(Piece("", font, size, false, isBreak = true))
                    pendingSpace = false
                }
                for (match in token.findAll(line)) {
                    val value = match.value
                    if (value[0].isWhitespace()) {
                        pendingSpace = true
                    } else {
                        pieces.add(
                            Piece(
                                value, font, size, pendingSpace,
                                fauxBold = styled.fauxBold, fauxItalic = styled.fauxItalic
                            )
                        )
                        pendingSpace = false
                    }
                }
            }
        }
        return pieces
    }

    private fun spaceWidth(piece: Piece) = piece.font.getStringWidth(" ") / 1000f * piece.size

    /** Greedy line breaking; words wider than a line are split by characters. */
    private fun layout(pieces: List<Piece>, maxWidth: Float): List<List<Piece>> {
        val lines = mutableListOf<List<Piece>>()
        var current = mutableListOf<Piece>()
        var currentWidth = 0f

        for (piece in pieces) {
            if (piece.isBreak) {
                lines.add(current)
                current = mutableListOf()
                currentWidth = 0f
                continue
            }

            var gap = if (current.isNotEmpty() && piece.spaceBefore) spaceWidth(piece) else 0f
            if (current.isNotEmpty() && currentWidth + gap + piece.width > maxWidth) {
                lines.add(current)
                current = mutableListOf()
                currentWidth = 0f
                gap = 0f
            }

            if (piece.width > maxWidth) {
                var rest = piece.text
                while (rest.isNotEmpty()) {
                    var cut = rest.length
                    while (cut > 1 && Piece(rest.substring(0, cut), piece.font, piece.size, false).width > maxWidth) cut--
                    val chunk = Piece(
                        rest.substring(0, cut), piece.font, piece.size, false,
                        fauxBold = piece.fauxBold, fauxItalic = piece.fauxItalic
                    )
                    if (current.isNotEmpty()) {
                        lines.add(current)
                        current = mutableListOf()
                    }
                    current.add(chunk)
                    currentWidth = chunk.width
                    rest = rest.substring(cut)
                }
                continue
            }

            current.add(piece)
            currentWidth += gap + piece.width
        }
        if (current.isNotEmpty()) lines.add(current)
        return lines
    }

    private fun lineWidth(line: List<Piece>): Float {
        var width = 0f
        line.forEachIndexed { i, p ->
            if (i > 0 && p.spaceBefore) width += spaceWidth(p)
            width += p.width
        }
        return width
    }

    private fun drawLine(line: List<Piece>, x0: Float, baseline: Float) {
        if (line.isEmpty()) return
        var x = x0
        line.forEachIndexed { i, p ->
            if (i > 0 && p.spaceBefore) x += spaceWidth(p)
            val skew = if (p.fauxItalic) 0.21f else 0f
            stream.beginText()
            stream.setFont(p.font, p.size)
            stream.setTextMatrix(Matrix(1f, 0f, skew, 1f, x, baseline))
            stream.showText(p.text)
            if (p.fauxBold) {
                // Synthetic bold: draw the text again, shifted by a fraction of the font size.
                // (Outline rendering modes are ignored by some viewers, overstriking is not.)
                stream.setTextMatrix(Matrix(1f, 0f, skew, 1f, x + p.size * 0.035f, baseline))
                stream.showText(p.text)
            }
            stream.endText()
            x += p.width
        }
    }

    // ---------------- paragraphs ----------------

    private fun paragraph(p: XWPFParagraph) {
        val style = p.style?.lowercase().orEmpty().replace(" ", "")
        val (baseSize, headingBold) = when {
            style.contains("title") -> 24f to true
            style == "heading1" -> 20f to true
            style == "heading2" -> 16f to true
            style == "heading3" -> 14f to true
            style.startsWith("heading") -> 13f to true
            else -> defaultSize to false
        }

        val pieces = piecesFor(p, baseSize, headingBold)
        val isListItem = p.numID != null
        val leftIndent = (if (p.indentationLeft > 0) p.indentationLeft / 20f else 0f) +
            (if (isListItem) 18f else 0f)
        val maxWidth = (contentWidth - leftIndent).coerceAtLeast(50f)

        val lines = layout(pieces, maxWidth)
        if (lines.isEmpty()) {
            // empty paragraph = vertical space
            ensureSpace(baseSize * 1.25f)
            y -= baseSize * 1.25f
            return
        }

        lines.forEachIndexed { index, line ->
            val size = line.maxOfOrNull { it.size } ?: baseSize
            val lineHeight = size * 1.25f
            ensureSpace(lineHeight)
            y -= lineHeight
            val baseline = y + lineHeight * 0.2f

            if (isListItem && index == 0) {
                val bullet = sanitize("•")
                stream.beginText()
                stream.setFont(fonts.regular, size)
                stream.newLineAtOffset(margin + leftIndent - 12f, baseline)
                stream.showText(bullet)
                stream.endText()
            }

            val width = lineWidth(line)
            val offset = when (p.alignment) {
                ParagraphAlignment.CENTER -> (maxWidth - width) / 2f
                ParagraphAlignment.RIGHT -> maxWidth - width
                else -> 0f
            }
            drawLine(line, margin + leftIndent + offset.coerceAtLeast(0f), baseline)
        }
        y -= if (headingBold) 8f else 4f
    }

    // ---------------- tables ----------------

    private fun table(t: XWPFTable) {
        val padding = 4f
        val size = 10.5f
        val lineHeight = size * 1.25f

        for (row in t.rows) {
            val cells = row.tableCells
            if (cells.isEmpty()) continue
            val columnWidth = contentWidth / cells.size

            val cellLines = cells.map { cell ->
                val pieces = mutableListOf<Piece>()
                cell.paragraphs.forEachIndexed { i, para ->
                    if (i > 0) pieces.add(Piece("", fonts.regular, size, false, isBreak = true))
                    pieces.addAll(piecesFor(para, size, false))
                }
                layout(pieces, columnWidth - 2 * padding)
            }

            val maxLines = (cellLines.maxOfOrNull { it.size } ?: 1).coerceAtLeast(1)
            val rowHeight = maxLines * lineHeight + 2 * padding
            ensureSpace(rowHeight)
            val top = y

            cells.indices.forEach { i ->
                val x = margin + i * columnWidth
                stream.addRect(x, top - rowHeight, columnWidth, rowHeight)
                stream.stroke()
                cellLines[i].forEachIndexed { lineIndex, line ->
                    val baseline = top - padding - (lineIndex + 1) * lineHeight + lineHeight * 0.2f
                    drawLine(line, x + padding, baseline)
                }
            }
            y -= rowHeight
        }
        y -= 8f
    }

    private companion object {
        // Used only when no Unicode system font is available (standard fonts are WinAnsi-only)
        val TURKISH_FALLBACK = mapOf(
            'ğ' to 'g', 'Ğ' to 'G', 'ş' to 's', 'Ş' to 'S',
            'ı' to 'i', 'İ' to 'I'
        )
    }
}
