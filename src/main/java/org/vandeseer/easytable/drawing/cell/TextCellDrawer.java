package org.vandeseer.easytable.drawing.cell;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.vandeseer.easytable.drawing.DrawingContext;
import org.vandeseer.easytable.drawing.DrawingUtil;
import org.vandeseer.easytable.drawing.PositionedStyledText;
import org.vandeseer.easytable.structure.cell.AbstractTextCell;
import org.vandeseer.easytable.util.PdfUtil;

import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.vandeseer.easytable.settings.HorizontalAlignment.*;
import static org.vandeseer.easytable.settings.VerticalAlignment.BOTTOM;
import static org.vandeseer.easytable.settings.VerticalAlignment.MIDDLE;

@NoArgsConstructor
public class TextCellDrawer<T extends AbstractTextCell> extends AbstractCellDrawer<AbstractTextCell> {

    public TextCellDrawer(T cell) {
        this.cell = cell;
    }

    @Override
    @SneakyThrows
    public void drawContent(DrawingContext drawingContext) {
        final float startX = drawingContext.getStartingPoint().x;

        final PDFont currentFont = cell.getFont();
        final int currentFontSize = cell.getFontSize();
        final Color currentTextColor = cell.getTextColor();

        float yOffset = drawingContext.getStartingPoint().y + getAdaptionForVerticalAlignment();
        float xOffset = startX + cell.getPaddingLeft();

        final List<String> lines = calculateAndGetLines(currentFont, currentFontSize, cell.getWidthOfText());
        for (int i = 0; i < lines.size(); i++) {
            final String line = lines.get(i);

            yOffset -= calculateYOffset(currentFont, currentFontSize, i);

            final float textWidth = PdfUtil.getStringWidth(line, currentFont, currentFontSize);

            // Handle horizontal alignment by adjusting the xOffset
            if (cell.isHorizontallyAligned(RIGHT)) {
                xOffset = startX + (cell.getWidth() - (textWidth + cell.getPaddingRight()));

            } else if (cell.isHorizontallyAligned(CENTER)) {
                xOffset = startX + (cell.getWidth() - textWidth) / 2;

            } else if (cell.isHorizontallyAligned(JUSTIFY) && isNotLastLine(lines, i)) {
                drawingContext.getContentStream().setCharacterSpacing(calculateCharSpacingFor(line));
            }

            drawText(
                    drawingContext,
                    PositionedStyledText.builder()
                            .x(xOffset)
                            .y(yOffset)
                            .text(line)
                            .font(currentFont)
                            .fontSize(currentFontSize)
                            .color(currentTextColor)
                            .build()
            );
        }
    }

    private float getAdaptionForVerticalAlignment() {
        if (rowHeightIsBiggerThanOrEqualToCellHeight() || cell.getRowSpan() > 1) {

            if (cell.isVerticallyAligned(MIDDLE)) {
                return (calculateOuterHeight() / 2 + cell.getTextHeight() / 2) - getRowSpanAdaption();

            } else if (cell.isVerticallyAligned(BOTTOM)) {
                return (cell.getTextHeight() + cell.getPaddingBottom()) - getRowSpanAdaption();
            }
        }

        // top alighment (default case)
        return cell.getRow().getHeight() - cell.getPaddingTop(); // top position
    }

    private boolean rowHeightIsBiggerThanOrEqualToCellHeight() {
        return cell.getRow().getHeight() > cell.getHeight()
                || Math.abs(cell.getRow().getHeight() - cell.getHeight()) < PdfUtil.EPSILON;
    }

    private float calculateOuterHeight() {
        return cell.getRowSpan() > 1 ? cell.getHeight() : cell.getRow().getHeight();
    }

    private float calculateYOffset(PDFont currentFont, int currentFontSize, int lineIndex) {
        return PdfUtil.getFontHeight(currentFont, currentFontSize) // font height
                + (lineIndex > 0 ? PdfUtil.getFontHeight(currentFont, currentFontSize) * cell.getLineSpacing() : 0f); // line spacing
    }

    static boolean isNotLastLine(List<String> lines, int i) {
        return i != lines.size() - 1;
    }

    // Code from https://stackoverflow.com/questions/20680430/is-it-possible-to-justify-text-in-pdfbox
    protected float calculateCharSpacingFor(String line) {
        float charSpacing = 0;
        if (line.length() > 1) {
            float size = PdfUtil.getStringWidth(line, cell.getFont(), cell.getFontSize());
            float free = cell.getWidthOfText() - size;
            if (free > 0) {
                charSpacing = free / (line.length() - 1);
            }
        }
        return charSpacing;
    }

    protected List<String> calculateAndGetLines(PDFont currentFont, int currentFontSize, float maxWidth) {
        return cell.isWordBreak()
                ? PdfUtil.getOptimalTextBreakLines(cell.getText(), currentFont, currentFontSize, maxWidth)
                : Collections.singletonList(cell.getText());
    }

    private float getRowSpanAdaption() {
        return cell.getRowSpan() > 1
                ? cell.calculateHeightForRowSpan() - cell.getRow().getHeight()
                : 0;
    }

    protected void drawText(DrawingContext drawingContext, PositionedStyledText positionedStyledText) throws IOException {
        DrawingUtil.drawText(
                drawingContext.getContentStream(),
                positionedStyledText
        );
    }

}
