package org.vandeseer.easytable.util;

import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.vandeseer.easytable.util.FloatUtil.isEqualInEpsilon;

public class PdfUtilTest {

    @Test
    public void getStringWidth_noNewLines() {
        final String text = "this is a small text";

        final float actualSize = PdfUtil.getStringWidth(text, new PDType1Font(HELVETICA), 12);
        final float expectedSize = 94.692F;

        assertThat(isEqualInEpsilon(expectedSize, actualSize), is(true));
    }



    @Test
    public void getStringWidth_withNewLines_shouldReturnWidthOfLongestLine() {
        final String text = "this is a longer text\nthat has two\nnew lines in it";
        final float firstLineWidth = PdfUtil.getStringWidth("this is a longer text", new PDType1Font(HELVETICA), 12);

        assertThat(PdfUtil.getStringWidth(text, new PDType1Font(HELVETICA), 12), equalTo(firstLineWidth));
    }

    @Test
    public void getOptimalTextBreakLines_noNewLinesAndFitsInColumn_shouldReturnOneLine() {
        final String text = "this is a small text";

        // We don't have to break in case we have two times the size ;)
        final float maxWidth = 2f * PdfUtil.getStringWidth(text, new PDType1Font(HELVETICA), 12);

        assertThat(PdfUtil.getOptimalTextBreakLines(text, new PDType1Font(HELVETICA), 12, maxWidth).size(), is(1));
    }

    @Test
    public void getOptimalTextBreakLines_withNewLinesAndFitsInColumn_shouldReturnMoreThanOneLine() {
        final String text = "this is a small text\nthat has two\nnew lines in it";

        // Since we have new lines
        final float maxWidth = 2f * PdfUtil.getStringWidth(text, new PDType1Font(HELVETICA), 12);

        assertThat(PdfUtil.getOptimalTextBreakLines(text, new PDType1Font(HELVETICA), 12, maxWidth).size(), is(3));
    }

    @Test
    public void getOptimalTextBreakLines_noSpacesInText_shouldSplitOnDot() {
        final String text = "This.should.be.splitted.on.a.dot.No.spaces.in.here.";

        final float maxWidth = PdfUtil.getStringWidth("This.should.be.splitted.on.a.dot.", new PDType1Font(HELVETICA), 12);

        final List<String> lines = PdfUtil.getOptimalTextBreakLines(text, new PDType1Font(HELVETICA), 12, maxWidth);

        assertThat(lines.size(), is(2));
        assertThat(lines.get(0), is(equalTo("This.should.be.splitted.on.a.dot.")));
        assertThat(lines.get(1), is(equalTo("No.spaces.in.here.")));
    }

    @Test
    public void getOptimalTextBreakLines_noSpacesNorDotsInText_shouldSplitOnComma() {
        final String text = "This,should,be,splitted,on,a,comma,no,space,nor,dots,in,here,";

        final float maxWidth = PdfUtil.getStringWidth("This,should,be,splitted,on,a,comma,", new PDType1Font(HELVETICA), 12);

        final List<String> lines = PdfUtil.getOptimalTextBreakLines(text, new PDType1Font(HELVETICA), 12, maxWidth);

        assertThat(lines.size(), is(2));
        assertThat(lines.get(0), is(equalTo("This,should,be,splitted,on,a,comma,")));
        assertThat(lines.get(1), is(equalTo("no,space,nor,dots,in,here,")));
    }

    @Test
    public void getOptimalTextBreakLines_noSpacesNorDotsInText_shouldSplitOnSlash() {
        final String text = "This/should/be/splitted/on/a/slash/no/space/nor/dots/in/here/";

        final float maxWidth = PdfUtil.getStringWidth("This/should/be/splitted/on/a/slash/", new PDType1Font(HELVETICA), 12);

        final List<String> lines = PdfUtil.getOptimalTextBreakLines(text, new PDType1Font(HELVETICA), 12, maxWidth);

        assertThat(lines.size(), is(2));
        assertThat(lines.get(0), is(equalTo("This/should/be/splitted/on/a/slash/")));
        assertThat(lines.get(1), is(equalTo("no/space/nor/dots/in/here/")));
    }

    @Test
    public void getOptimalTextBreakLines_noSpacesNorDotsNorCommasInText_shouldSplitBySize() {
        final String text = "ThisDoesNotHaveAnyCharactersWhereWeCouldBreakMoreEasilySoWeBreakBySize";

        final float maxWidth = PdfUtil.getStringWidth("ThisDoesNotHaveAnyCharacters", new PDType1Font(HELVETICA), 12);
        final List<String> lines = PdfUtil.getOptimalTextBreakLines(text, new PDType1Font(HELVETICA), 12, maxWidth);

        assertThat(lines.size(), is(3));
        assertThat(lines.get(0), is(equalTo("ThisDoesNotHaveAnyCharacter-")));
        assertThat(lines.get(1), is(equalTo("sWhereWeCouldBreakMoreEasi-")));
        assertThat(lines.get(2), is(equalTo("lySoWeBreakBySize")));
    }


    @Test(timeout = 5000L)
    public void testVeryBigText() {
        final StringBuilder builder = new StringBuilder();
        final List<String> expectedOutput = new ArrayList<>();

        for (int i = 0; i < 50; i++) {
            builder.append("https://averylonginternetdnsnamewhich-maybe-breaks-easytable.com ");

            // optimal text-break
            expectedOutput.add("https://");
            expectedOutput.add("averylonginternet-");
            expectedOutput.add("dnsnamewhich-");
            expectedOutput.add("maybe-breaks-");
            expectedOutput.add("easytable.com");
        }
        expectedOutput.set(expectedOutput.size() - 1, "easytable.com");

        final List<String> actualOutput = PdfUtil.getOptimalTextBreakLines(builder.toString(), new PDType1Font(HELVETICA), 8, 68);

        assertThat(actualOutput, equalTo(expectedOutput));
    }

}