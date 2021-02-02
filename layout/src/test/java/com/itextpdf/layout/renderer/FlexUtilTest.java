/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2021 iText Group NV
    Authors: iText Software.

    This program is offered under a commercial and under the AGPL license.
    For commercial licensing, contact us at https://itextpdf.com/sales.  For AGPL licensing, see below.

    AGPL licensing:
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.itextpdf.layout.renderer;

import com.itextpdf.io.font.constants.StandardFontFamilies;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.IBlockElement;
import com.itextpdf.layout.element.IElement;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.font.FontProvider;
import com.itextpdf.layout.property.AlignmentPropertyValue;
import com.itextpdf.layout.property.FlexWrapPropertyValue;
import com.itextpdf.layout.property.Property;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.renderer.FlexUtil.FlexItemCalculationInfo;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.UnitTest;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class FlexUtilTest extends ExtendedITextTest {

    private static final float EPS = 0.001f;

    private static final Style DEFAULT_STYLE;
    private static final Style WRAP_STYLE;

    private static final List<UnitValue> NULL_FLEX_BASIS_LIST;

    // Here one can find an html version for all of the tests
    // TODO DEVSIX-5049 Make html2pdf+layout tests from these htmls
    private static final String SOURCE_FOLDER = "./src/test/resources/com/itextpdf/layout/FlexUtilTest/";

    static {
        DEFAULT_STYLE = new Style().setWidth(400).setHeight(100);

        WRAP_STYLE = new Style().setWidth(400).setHeight(100);
        WRAP_STYLE.setProperty(Property.FLEX_WRAP, FlexWrapPropertyValue.WRAP);

        NULL_FLEX_BASIS_LIST = new ArrayList<UnitValue>();
        for (int i = 0; i < 3; i++) {
            NULL_FLEX_BASIS_LIST.add(null);
        }
    }

    @Test
    public void defaultTest01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                NULL_FLEX_BASIS_LIST,
                Arrays.asList(1f, 1f, 1f),
                Arrays.asList(0f, 0f, 0f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(400f / 3, flexItemInfo.getRectangle().getWidth(), EPS);
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
    }

    @Test
    public void retrieveSizePtTest01() {
        Div div = new Div().setWidth(UnitValue.createPercentValue(80));
        float size = FlexUtil.retrieveSize(
                div.createRendererSubTree(),
                Property.WIDTH,
                100);

        Assert.assertEquals(80f, size, EPS);
    }

    @Test
    public void retrieveSizeNoSetWidthTest01() {
        Div div = new Div();
        float size = FlexUtil.retrieveSize(
                div.createRendererSubTree(),
                Property.WIDTH,
                100);

        Assert.assertEquals(100f, size, EPS);
    }

    @Test
    public void item1BasisGtWidthGrow0Shrink01Test01() {
        Rectangle bBox = new Rectangle(545, 842);
        List<UnitValue> flexBasisValues = Arrays.<UnitValue>asList(
                UnitValue.createPointValue(150f),
                UnitValue.createPointValue(50f)
        );

        List<FlexItemCalculationInfo> flexItemCalculationInfos = new ArrayList<>();
        Div div = new Div().setWidth(100).setHeight(100);

        DocumentRenderer documentRenderer = new DocumentRenderer(
                new Document(new PdfDocument(new PdfWriter(new ByteArrayOutputStream()))));

        for (int i = 0; i < flexBasisValues.size(); i++) {
            Div flexItem = new Div().add(new Paragraph("x"));
            AbstractRenderer flexItemRenderer = (AbstractRenderer) flexItem.createRendererSubTree()
                    .setParent(documentRenderer);
            div.add(flexItem);
            flexItemCalculationInfos.add(new FlexItemCalculationInfo(flexItemRenderer,
                    flexBasisValues.get(i),
                    0,
                    0.1f,
                    bBox.getWidth()));
        }

        FlexContainerRenderer flexContainerRenderer = new FlexContainerRenderer(div);
        div.setNextRenderer(flexContainerRenderer);

        // before checks
        for (FlexItemCalculationInfo info : flexItemCalculationInfos) {
            Assert.assertNull(info.mainSize);
            Assert.assertNull(info.crossSize);
        }

        List<List<FlexItemInfo>> rectangleTable =
                FlexUtil.calculateChildrenRectangles(bBox, (FlexContainerRenderer) div.getRenderer(),
                        flexItemCalculationInfos);

        Assert.assertEquals(135f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(45f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisGrow1Shrink0MarginBorderPaddingOnContainerTest01() {
        Style style = new Style()
                .setWidth(100)
                .setHeight(100)
                .setMargin(15)
                .setBorder(new SolidBorder(10))
                .setPadding(50);
        ;
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                style,
                Arrays.<UnitValue>asList(UnitValue.createPointArray(new float[] {10f, 20f, 30f})),
                Arrays.asList(1f, 1f, 1f),
                Arrays.asList(0f, 0f, 0f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());

        Assert.assertEquals(23.333334f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(33.333336f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(43.333336f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisGrow1Shrink0MarginBorderPaddingOnContainerNoWidthTest01() {
        Style style = new Style()
                .setMargin(15)
                .setBorder(new SolidBorder(10))
                .setPadding(5);
        ;
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                style,
                Arrays.<UnitValue>asList(UnitValue.createPointArray(new float[] {50f, 100f, 150f})),
                Arrays.asList(1f, 1f, 1f),
                Arrays.asList(0f, 0f, 0f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());

        Assert.assertEquals(104.333336f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(154.33334f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(204.33334f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void simpleStretchTest01() {
        Style stretchStyle = new Style(WRAP_STYLE);
        stretchStyle.setProperty(Property.ALIGN_CONTENT, AlignmentPropertyValue.STRETCH);
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                stretchStyle,
                Arrays.<UnitValue>asList(UnitValue.createPointValue(100f)),
                Arrays.asList(0f),
                Arrays.asList(0f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(100f, flexItemInfo.getRectangle().getWidth(), EPS);
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
    }

    @Test
    public void basisGtWidthGrow0Shrink0Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                WRAP_STYLE,
                Arrays.<UnitValue>asList(UnitValue.createPointValue(500f)),
                Arrays.asList(0f),
                Arrays.asList(0f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(500f, flexItemInfo.getRectangle().getWidth(), EPS);
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
    }

    @Test
    public void basisGtWidthGrow0Shrink1Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                WRAP_STYLE,
                Arrays.<UnitValue>asList(UnitValue.createPointValue(500f)),
                Arrays.asList(0f),
                Arrays.asList(1f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(400f, flexItemInfo.getRectangle().getWidth(), EPS);
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
    }

    @Test
    public void basisMinGrow0Shrink1Item2Grow05Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                NULL_FLEX_BASIS_LIST,
                Arrays.asList(0f, 0.5f, 0f),
                Arrays.asList(1f, 1f, 1f));

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (int i = 0; i < rectangleTable.size(); i++) {
            FlexItemInfo flexItemInfo = rectangleTable.get(0).get(i);
            Assert.assertEquals(i == 1 ? 197 : 6f, flexItemInfo.getRectangle().getWidth(), EPS);
            Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
        }
    }

    @Test
    public void basisMinGrow0Shrink1Item2Grow2Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                NULL_FLEX_BASIS_LIST,
                Arrays.asList(0f, 2f, 0f),
                Arrays.asList(1f, 1f, 1f));

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (int i = 0; i < rectangleTable.size(); i++) {
            FlexItemInfo flexItemInfo = rectangleTable.get(0).get(i);
            Assert.assertEquals(i == 1 ? 388f : 6f, flexItemInfo.getRectangle().getWidth(), EPS);
            Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
        }
    }

    @Test
    public void basisMinGrow2Shrink1Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                NULL_FLEX_BASIS_LIST,
                Arrays.asList(2f, 2f, 2f),
                Arrays.asList(1f, 1f, 1f));

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(400f / 3, flexItemInfo.getRectangle().getWidth(), EPS);
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
    }

    @Test
    public void basisMinGrow05SumGt1Shrink1Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                NULL_FLEX_BASIS_LIST,
                Arrays.asList(0.5f, 0.5f, 0.5f),
                Arrays.asList(1f, 1f, 1f));

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(400f / 3, flexItemInfo.getRectangle().getWidth(), EPS);
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
    }

    @Test
    public void basisMinGrow01SumLt1Shrink1Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                NULL_FLEX_BASIS_LIST,
                Arrays.asList(0.1f, 0.1f, 0.1f),
                Arrays.asList(1f, 1f, 1f));

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(44.2f, flexItemInfo.getRectangle().getWidth(), EPS);
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
    }

    @Test
    public void basisMinGrow0Shrink05SumGt1Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                NULL_FLEX_BASIS_LIST,
                Arrays.asList(0f, 0f, 0f),
                Arrays.asList(0.5f, 0.5f, 0.5f));

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(6f, flexItemInfo.getRectangle().getWidth(), EPS);
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
    }

    @Test
    public void basisMinGrow0Shrink01SumLt1Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                NULL_FLEX_BASIS_LIST,
                Arrays.asList(0f, 0f, 0f),
                Arrays.asList(0.1f, 0.1f, 0.1f));

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(6f, flexItemInfo.getRectangle().getWidth(), EPS);
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
    }

    @Test
    public void basis50SumLtWidthGrow0Shrink1Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                Arrays.<UnitValue>asList(
                        UnitValue.createPointValue(50f),
                        UnitValue.createPointValue(50f),
                        UnitValue.createPointValue(50f)),
                Arrays.asList(0f, 0f, 0f),
                Arrays.asList(1f, 1f, 1f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(50f, flexItemInfo.getRectangle().getWidth(), EPS);
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
    }

    @Test
    public void basis250SumGtWidthGrow0Shrink1Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                Arrays.<UnitValue>asList(
                        UnitValue.createPointValue(250f),
                        UnitValue.createPointValue(250f),
                        UnitValue.createPointValue(250f)),
                Arrays.asList(0f, 0f, 0f),
                Arrays.asList(1f, 1f, 1f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(400f / 3, flexItemInfo.getRectangle().getWidth(), EPS);
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
    }

    @Test
    public void differentBasisSumLtWidthGrow0Shrink1Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                Arrays.<UnitValue>asList(
                        UnitValue.createPointValue(50f),
                        UnitValue.createPointValue(80f),
                        UnitValue.createPointValue(100f)),
                Arrays.asList(0f, 0f, 0f),
                Arrays.asList(1f, 1f, 1f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(50f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(80f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(100f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumLtWidthGrow1Shrink1Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                Arrays.<UnitValue>asList(
                        UnitValue.createPointValue(50f),
                        UnitValue.createPointValue(80f),
                        UnitValue.createPointValue(100f)),
                Arrays.asList(1f, 1f, 1f),
                Arrays.asList(1f, 1f, 1f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(106.66667f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(136.66667f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(156.66667f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumLtWidthGrow1Shrink0Item2MarginBorderPadding30Test01() {
        Rectangle bBox = new Rectangle(575, 842);
        List<UnitValue> flexBasisValues = Arrays.<UnitValue>asList(
                UnitValue.createPointValue(50f),
                UnitValue.createPointValue(80f),
                UnitValue.createPointValue(100f)
        );

        List<FlexItemCalculationInfo> flexItemCalculationInfos = new ArrayList<>();
        Div div = new Div().setWidth(400).setHeight(100);

        DocumentRenderer documentRenderer = new DocumentRenderer(
                new Document(new PdfDocument(new PdfWriter(new ByteArrayOutputStream()))));

        for (int i = 0; i < flexBasisValues.size(); i++) {
            Div flexItem = new Div().add(new Paragraph("x"));
            if (1 == i) {
                flexItem.setMargin(10).setBorder(new SolidBorder(15)).setPadding(5);
            }
            AbstractRenderer flexItemRenderer = (AbstractRenderer) flexItem.createRendererSubTree()
                    .setParent(documentRenderer);
            div.add(flexItem);
            flexItemCalculationInfos.add(new FlexItemCalculationInfo(flexItemRenderer,
                    flexBasisValues.get(i),
                    1,
                    0,
                    bBox.getWidth()));
        }

        FlexContainerRenderer flexContainerRenderer = new FlexContainerRenderer(div);
        div.setNextRenderer(flexContainerRenderer);

        // before checks
        for (FlexItemCalculationInfo info : flexItemCalculationInfos) {
            Assert.assertNull(info.mainSize);
            Assert.assertNull(info.crossSize);
        }

        List<List<FlexItemInfo>> rectangleTable =
                FlexUtil.calculateChildrenRectangles(bBox, (FlexContainerRenderer) div.getRenderer(),
                        flexItemCalculationInfos);

        Assert.assertEquals(86.66667f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(176.66667f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(136.66667f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumLtWidthGrow1Shrink1Item2MarginBorderPadding30Test01() {
        Rectangle bBox = new Rectangle(575, 842);
        List<UnitValue> flexBasisValues = Arrays.<UnitValue>asList(
                UnitValue.createPointValue(50f),
                UnitValue.createPointValue(80f),
                UnitValue.createPointValue(100f)
        );

        List<FlexItemCalculationInfo> flexItemCalculationInfos = new ArrayList<>();
        Div div = new Div().setWidth(200).setHeight(100);

        DocumentRenderer documentRenderer = new DocumentRenderer(
                new Document(new PdfDocument(new PdfWriter(new ByteArrayOutputStream()))));

        for (int i = 0; i < flexBasisValues.size(); i++) {
            Div flexItem = new Div().add(new Paragraph("x"));
            if (1 == i) {
                flexItem.setMargin(10).setBorder(new SolidBorder(15)).setPadding(5);
            }
            AbstractRenderer flexItemRenderer = (AbstractRenderer) flexItem.createRendererSubTree()
                    .setParent(documentRenderer);
            div.add(flexItem);
            flexItemCalculationInfos.add(new FlexItemCalculationInfo(flexItemRenderer,
                    flexBasisValues.get(i),
                    1,
                    1,
                    bBox.getWidth()));
        }

        FlexContainerRenderer flexContainerRenderer = new FlexContainerRenderer(div);
        div.setNextRenderer(flexContainerRenderer);

        // before checks
        for (FlexItemCalculationInfo info : flexItemCalculationInfos) {
            Assert.assertNull(info.mainSize);
            Assert.assertNull(info.crossSize);
        }

        List<List<FlexItemInfo>> rectangleTable =
                FlexUtil.calculateChildrenRectangles(bBox, (FlexContainerRenderer) div.getRenderer(),
                        flexItemCalculationInfos);

        Assert.assertEquals(30.434784f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(108.69565f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(60.869568f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumLtWidthGrow1Shrink1Item2MuchContentTest01() {
        Rectangle bBox = new Rectangle(575, 842);
        List<UnitValue> flexBasisValues = Arrays.<UnitValue>asList(
                UnitValue.createPointValue(37.5f),
                UnitValue.createPointValue(60f),
                UnitValue.createPointValue(75f)
        );

        List<FlexItemCalculationInfo> flexItemCalculationInfos = new ArrayList<>();
        Div div = new Div().setWidth(300).setHeight(100);

        // We use Courier as a monotype font to ensure that min width calculated by iText
        // is more or less the same as the width calculated by browsers
        FontProvider provider = new FontProvider();
        provider.getFontSet().addFont(StandardFonts.COURIER, null, "courier");

        DocumentRenderer documentRenderer = new DocumentRenderer(
                new Document(new PdfDocument(new PdfWriter(new ByteArrayOutputStream()))));
        documentRenderer.setProperty(Property.FONT_PROVIDER, provider);

        for (int i = 0; i < flexBasisValues.size(); i++) {
            Div flexItem = new Div().add(new Paragraph(1 == i ? "2222222222222222222222222" : Integer.toString(i)));
            if (1 == i) {
                flexItem.setFontFamily(StandardFontFamilies.COURIER);
            }
            AbstractRenderer flexItemRenderer = (AbstractRenderer) flexItem.createRendererSubTree()
                    .setParent(documentRenderer);
            div.add(flexItem);
            flexItemCalculationInfos.add(new FlexItemCalculationInfo(flexItemRenderer,
                    flexBasisValues.get(i),
                    1,
                    1,
                    bBox.getWidth()));
        }

        FlexContainerRenderer flexContainerRenderer = new FlexContainerRenderer(div);
        div.setNextRenderer(flexContainerRenderer);

        // before checks
        for (FlexItemCalculationInfo info : flexItemCalculationInfos) {
            Assert.assertNull(info.mainSize);
            Assert.assertNull(info.crossSize);
        }

        List<List<FlexItemInfo>> rectangleTable =
                FlexUtil.calculateChildrenRectangles(bBox, (FlexContainerRenderer) div.getRenderer(),
                        flexItemCalculationInfos);

        Assert.assertEquals(41.250023f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(179.99995f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(78.75002f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumLtWidthGrow1Shrink1Item2MuchContentSetMinWidthLtBasisTest01() {
        Rectangle bBox = new Rectangle(575, 842);
        List<UnitValue> flexBasisValues = Arrays.<UnitValue>asList(
                UnitValue.createPointValue(37.5f),
                UnitValue.createPointValue(60f),
                UnitValue.createPointValue(75f)
        );

        List<FlexItemCalculationInfo> flexItemCalculationInfos = new ArrayList<>();
        Div div = new Div().setWidth(300).setHeight(100);

        // We use Courier as a monotype font to ensure that min width calculated by iText
        // is more or less the same as the width calculated by browsers
        FontProvider provider = new FontProvider();
        provider.getFontSet().addFont(StandardFonts.COURIER, null, "courier");

        DocumentRenderer documentRenderer = new DocumentRenderer(
                new Document(new PdfDocument(new PdfWriter(new ByteArrayOutputStream()))));
        documentRenderer.setProperty(Property.FONT_PROVIDER, provider);

        for (int i = 0; i < flexBasisValues.size(); i++) {
            Div flexItem = new Div().add(new Paragraph(1 == i ? "2222222222222222222222222" : Integer.toString(i)));
            if (1 == i) {
                flexItem
                        .setFontFamily(StandardFontFamilies.COURIER)
                        .setMinWidth(37.5f);
            }
            AbstractRenderer flexItemRenderer = (AbstractRenderer) flexItem.createRendererSubTree()
                    .setParent(documentRenderer);
            div.add(flexItem);
            flexItemCalculationInfos.add(new FlexItemCalculationInfo(flexItemRenderer,
                    flexBasisValues.get(i),
                    1,
                    1,
                    bBox.getWidth()));
        }

        FlexContainerRenderer flexContainerRenderer = new FlexContainerRenderer(div);
        div.setNextRenderer(flexContainerRenderer);

        // before checks
        for (FlexItemCalculationInfo info : flexItemCalculationInfos) {
            Assert.assertNull(info.mainSize);
            Assert.assertNull(info.crossSize);
        }

        List<List<FlexItemInfo>> rectangleTable =
                FlexUtil.calculateChildrenRectangles(bBox, (FlexContainerRenderer) div.getRenderer(),
                        flexItemCalculationInfos);

        Assert.assertEquals(80f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(102.5f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(117.5f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumLtWidthGrow1Shrink1Item2MaxWidthLtBasisTest01() {
        Rectangle bBox = new Rectangle(575, 842);
        List<UnitValue> flexBasisValues = Arrays.<UnitValue>asList(
                UnitValue.createPointValue(50f),
                UnitValue.createPointValue(80f),
                UnitValue.createPointValue(100f)
        );

        List<FlexItemCalculationInfo> flexItemCalculationInfos = new ArrayList<>();
        Div div = new Div().setWidth(400).setHeight(100);

        DocumentRenderer documentRenderer = new DocumentRenderer(
                new Document(new PdfDocument(new PdfWriter(new ByteArrayOutputStream()))));

        for (int i = 0; i < flexBasisValues.size(); i++) {
            Div flexItem = new Div().add(new Paragraph(Integer.toString(i)));
            if (1 == i) {
                flexItem.setMaxWidth(50f);
            }
            AbstractRenderer flexItemRenderer = (AbstractRenderer) flexItem.createRendererSubTree()
                    .setParent(documentRenderer);
            div.add(flexItem);
            flexItemCalculationInfos.add(new FlexItemCalculationInfo(flexItemRenderer,
                    flexBasisValues.get(i),
                    1,
                    1,
                    bBox.getWidth()));
        }

        FlexContainerRenderer flexContainerRenderer = new FlexContainerRenderer(div);
        div.setNextRenderer(flexContainerRenderer);

        // before checks
        for (FlexItemCalculationInfo info : flexItemCalculationInfos) {
            Assert.assertNull(info.mainSize);
            Assert.assertNull(info.crossSize);
        }

        List<List<FlexItemInfo>> rectangleTable =
                FlexUtil.calculateChildrenRectangles(bBox, (FlexContainerRenderer) div.getRenderer(),
                        flexItemCalculationInfos);

        Assert.assertEquals(150f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(50f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(200f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumLtWidthGrow1Shrink1Item2MaxWidthLtBasisTest02() {
        Rectangle bBox = new Rectangle(575, 842);
        List<UnitValue> flexBasisValues = Arrays.<UnitValue>asList(
                UnitValue.createPointValue(50f),
                UnitValue.createPointValue(80f),
                UnitValue.createPointValue(100f)
        );

        List<FlexItemCalculationInfo> flexItemCalculationInfos = new ArrayList<>();
        Div div = new Div().setWidth(100).setHeight(100);

        DocumentRenderer documentRenderer = new DocumentRenderer(
                new Document(new PdfDocument(new PdfWriter(new ByteArrayOutputStream()))));

        for (int i = 0; i < flexBasisValues.size(); i++) {
            Div flexItem = new Div().add(new Paragraph(Integer.toString(i)));
            if (1 == i) {
                flexItem.setMaxWidth(30f);
            }
            AbstractRenderer flexItemRenderer = (AbstractRenderer) flexItem.createRendererSubTree()
                    .setParent(documentRenderer);
            div.add(flexItem);
            flexItemCalculationInfos.add(new FlexItemCalculationInfo(flexItemRenderer,
                    flexBasisValues.get(i),
                    1,
                    1,
                    bBox.getWidth()));
        }

        FlexContainerRenderer flexContainerRenderer = new FlexContainerRenderer(div);
        div.setNextRenderer(flexContainerRenderer);

        // before checks
        for (FlexItemCalculationInfo info : flexItemCalculationInfos) {
            Assert.assertNull(info.mainSize);
            Assert.assertNull(info.crossSize);
        }

        List<List<FlexItemInfo>> rectangleTable =
                FlexUtil.calculateChildrenRectangles(bBox, (FlexContainerRenderer) div.getRenderer(),
                        flexItemCalculationInfos);

        Assert.assertEquals(23.333332f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(30f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(46.666664f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumLtWidthGrow1Shrink1Item2MaxWidthLtBasisTest03() {
        Rectangle bBox = new Rectangle(575, 842);
        List<UnitValue> flexBasisValues = Arrays.<UnitValue>asList(
                UnitValue.createPointValue(7f),
                UnitValue.createPointValue(80f),
                UnitValue.createPointValue(7f)
        );

        List<FlexItemCalculationInfo> flexItemCalculationInfos = new ArrayList<>();
        Div div = new Div().setWidth(100).setHeight(100);

        DocumentRenderer documentRenderer = new DocumentRenderer(
                new Document(new PdfDocument(new PdfWriter(new ByteArrayOutputStream()))));

        for (int i = 0; i < flexBasisValues.size(); i++) {
            Div flexItem = new Div().add(new Paragraph(Integer.toString(i)));
            if (1 == i) {
                flexItem.setMaxWidth(30f);
            }
            AbstractRenderer flexItemRenderer = (AbstractRenderer) flexItem.createRendererSubTree()
                    .setParent(documentRenderer);
            div.add(flexItem);
            flexItemCalculationInfos.add(new FlexItemCalculationInfo(flexItemRenderer,
                    flexBasisValues.get(i),
                    1,
                    1,
                    bBox.getWidth()));
        }

        FlexContainerRenderer flexContainerRenderer = new FlexContainerRenderer(div);
        div.setNextRenderer(flexContainerRenderer);

        // before checks
        for (FlexItemCalculationInfo info : flexItemCalculationInfos) {
            Assert.assertNull(info.mainSize);
            Assert.assertNull(info.crossSize);
        }

        List<List<FlexItemInfo>> rectangleTable =
                FlexUtil.calculateChildrenRectangles(bBox, (FlexContainerRenderer) div.getRenderer(),
                        flexItemCalculationInfos);

        Assert.assertEquals(35f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(30f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(35f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumGtWidthGrow1Shrink1Item1MinWidthGtBasisTest01() {
        Rectangle bBox = new Rectangle(575, 842);
        List<UnitValue> flexBasisValues = Arrays.<UnitValue>asList(
                UnitValue.createPointValue(100f),
                UnitValue.createPointValue(150f),
                UnitValue.createPointValue(200f)
        );

        List<FlexItemCalculationInfo> flexItemCalculationInfos = new ArrayList<>();
        Div div = new Div().setWidth(400).setHeight(100);

        DocumentRenderer documentRenderer = new DocumentRenderer(
                new Document(new PdfDocument(new PdfWriter(new ByteArrayOutputStream()))));

        for (int i = 0; i < flexBasisValues.size(); i++) {
            Div flexItem = new Div().add(new Paragraph(Integer.toString(i)));
            if (0 == i) {
                flexItem.setMinWidth(150f);
            }
            AbstractRenderer flexItemRenderer = (AbstractRenderer) flexItem.createRendererSubTree()
                    .setParent(documentRenderer);
            div.add(flexItem);
            flexItemCalculationInfos.add(new FlexItemCalculationInfo(flexItemRenderer,
                    flexBasisValues.get(i),
                    1,
                    1,
                    bBox.getWidth()));
        }

        FlexContainerRenderer flexContainerRenderer = new FlexContainerRenderer(div);
        div.setNextRenderer(flexContainerRenderer);

        // before checks
        for (FlexItemCalculationInfo info : flexItemCalculationInfos) {
            Assert.assertNull(info.mainSize);
            Assert.assertNull(info.crossSize);
        }

        List<List<FlexItemInfo>> rectangleTable =
                FlexUtil.calculateChildrenRectangles(bBox, (FlexContainerRenderer) div.getRenderer(),
                        flexItemCalculationInfos);

        Assert.assertEquals(150f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(107.14285f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(142.85715f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void imgGtUsedWidthTest01() throws MalformedURLException {
        Rectangle bBox = new Rectangle(575, 842);
        List<UnitValue> flexBasisValues = Arrays.<UnitValue>asList(
                UnitValue.createPointValue(50f),
                UnitValue.createPointValue(30f)
        );

        List<FlexItemCalculationInfo> flexItemCalculationInfos = new ArrayList<>();
        Div div = new Div().setWidth(100).setHeight(100);

        DocumentRenderer documentRenderer = new DocumentRenderer(
                new Document(new PdfDocument(new PdfWriter(new ByteArrayOutputStream()))));

        for (int i = 0; i < flexBasisValues.size(); i++) {
            IElement flexItem = (0 == i)
                    ? (IElement) new Image(ImageDataFactory.create(SOURCE_FOLDER + "itis.jpg"))
                    : (IElement) new Div().add(new Paragraph(Integer.toString(i)));
            if (0 == i) {
                flexItem.setProperty(Property.MAX_WIDTH, UnitValue.createPointValue(50f));
                div.add((Image) flexItem);
            } else {
                div.add((IBlockElement) flexItem);
            }
            AbstractRenderer flexItemRenderer = (AbstractRenderer) flexItem.createRendererSubTree()
                    .setParent(documentRenderer);
            flexItemCalculationInfos.add(new FlexItemCalculationInfo(flexItemRenderer,
                    flexBasisValues.get(i),
                    0,
                    0,
                    bBox.getWidth()));
        }

        FlexContainerRenderer flexContainerRenderer = new FlexContainerRenderer(div);
        div.setNextRenderer(flexContainerRenderer);

        // before checks
        for (FlexItemCalculationInfo info : flexItemCalculationInfos) {
            Assert.assertNull(info.mainSize);
            Assert.assertNull(info.crossSize);
        }

        List<List<FlexItemInfo>> rectangleTable =
                FlexUtil.calculateChildrenRectangles(bBox, (FlexContainerRenderer) div.getRenderer(),
                        flexItemCalculationInfos);

        Assert.assertEquals(50f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(30f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumLtWidthGrow1Shrink1Item2MuchContentSetMinWidthGtBasisTest01() {
        Rectangle bBox = new Rectangle(575, 842);
        List<UnitValue> flexBasisValues = Arrays.<UnitValue>asList(
                UnitValue.createPointValue(37.5f),
                UnitValue.createPointValue(60f),
                UnitValue.createPointValue(75f)
        );

        List<FlexItemCalculationInfo> flexItemCalculationInfos = new ArrayList<>();
        Div div = new Div().setWidth(300).setHeight(100);

        // We use Courier as a monotype font to ensure that min width calculated by iText
        // is more or less the same as the width calculated by browsers
        FontProvider provider = new FontProvider();
        provider.getFontSet().addFont(StandardFonts.COURIER, null, "courier");

        DocumentRenderer documentRenderer = new DocumentRenderer(
                new Document(new PdfDocument(new PdfWriter(new ByteArrayOutputStream()))));
        documentRenderer.setProperty(Property.FONT_PROVIDER, provider);

        for (int i = 0; i < flexBasisValues.size(); i++) {
            Div flexItem = new Div().add(new Paragraph(1 == i ? "2222222222222222222222222" : Integer.toString(i)));
            if (1 == i) {
                flexItem
                        .setFontFamily(StandardFontFamilies.COURIER)
                        .setMinWidth(75f);
            }
            AbstractRenderer flexItemRenderer = (AbstractRenderer) flexItem.createRendererSubTree()
                    .setParent(documentRenderer);
            div.add(flexItem);
            flexItemCalculationInfos.add(new FlexItemCalculationInfo(flexItemRenderer,
                    flexBasisValues.get(i),
                    1,
                    1,
                    bBox.getWidth()));
        }

        FlexContainerRenderer flexContainerRenderer = new FlexContainerRenderer(div);
        div.setNextRenderer(flexContainerRenderer);

        // before checks
        for (FlexItemCalculationInfo info : flexItemCalculationInfos) {
            Assert.assertNull(info.mainSize);
            Assert.assertNull(info.crossSize);
        }

        List<List<FlexItemInfo>> rectangleTable =
                FlexUtil.calculateChildrenRectangles(bBox, (FlexContainerRenderer) div.getRenderer(),
                        flexItemCalculationInfos);

        Assert.assertEquals(80f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(102.5f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(117.5f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void basis1Grow0Test01() {
        Rectangle bBox = new Rectangle(575, 842);
        List<UnitValue> flexBasisValues = Arrays.<UnitValue>asList(
                UnitValue.createPointValue(1f),
                UnitValue.createPointValue(30f)
        );

        List<FlexItemCalculationInfo> flexItemCalculationInfos = new ArrayList<>();
        Div div = new Div().setWidth(100).setHeight(100);

        // We use Courier as a monotype font to ensure that min width calculated by iText
        // is more or less the same as the width calculated by browsers
        FontProvider provider = new FontProvider();
        provider.getFontSet().addFont(StandardFonts.COURIER, null, "courier");

        DocumentRenderer documentRenderer = new DocumentRenderer(
                new Document(new PdfDocument(new PdfWriter(new ByteArrayOutputStream()))));
        documentRenderer.setProperty(Property.FONT_PROVIDER, provider);

        for (int i = 0; i < flexBasisValues.size(); i++) {
            Div flexItem = new Div().add(new Paragraph(Integer.toString(i)))
                    .setFontFamily(StandardFontFamilies.COURIER);
            if (0 == i) {
                flexItem.setFontSize(100f);
            }
            AbstractRenderer flexItemRenderer = (AbstractRenderer) flexItem.createRendererSubTree()
                    .setParent(documentRenderer);
            div.add(flexItem);
            flexItemCalculationInfos.add(new FlexItemCalculationInfo(flexItemRenderer,
                    flexBasisValues.get(i),
                    0,
                    0,
                    bBox.getWidth()));
        }

        FlexContainerRenderer flexContainerRenderer = new FlexContainerRenderer(div);
        div.setNextRenderer(flexContainerRenderer);

        // before checks
        for (FlexItemCalculationInfo info : flexItemCalculationInfos) {
            Assert.assertNull(info.mainSize);
            Assert.assertNull(info.crossSize);
        }

        List<List<FlexItemInfo>> rectangleTable =
                FlexUtil.calculateChildrenRectangles(bBox, (FlexContainerRenderer) div.getRenderer(),
                        flexItemCalculationInfos);

        Assert.assertEquals(60f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(30f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumLtWidthGrow1Shrink1Item2MuchContentSetMinWidthGtBasisTest02() {
        Rectangle bBox = new Rectangle(575, 842);
        List<UnitValue> flexBasisValues = Arrays.<UnitValue>asList(
                UnitValue.createPointValue(37.5f),
                UnitValue.createPointValue(60f),
                UnitValue.createPointValue(75f)
        );

        List<FlexItemCalculationInfo> flexItemCalculationInfos = new ArrayList<>();
        Div div = new Div().setWidth(300).setHeight(100);

        // We use Courier as a monotype font to ensure that min width calculated by iText
        // is more or less the same as the width calculated by browsers
        FontProvider provider = new FontProvider();
        provider.getFontSet().addFont(StandardFonts.COURIER, null, "courier");

        DocumentRenderer documentRenderer = new DocumentRenderer(
                new Document(new PdfDocument(new PdfWriter(new ByteArrayOutputStream()))));
        documentRenderer.setProperty(Property.FONT_PROVIDER, provider);

        for (int i = 0; i < flexBasisValues.size(); i++) {
            Div flexItem = new Div().add(new Paragraph(1 == i ? "2222222222222222222222222" : Integer.toString(i)));
            if (1 == i) {
                flexItem
                        .setFontFamily(StandardFontFamilies.COURIER)
                        .setMinWidth(150f);
            }
            AbstractRenderer flexItemRenderer = (AbstractRenderer) flexItem.createRendererSubTree()
                    .setParent(documentRenderer);
            div.add(flexItem);
            flexItemCalculationInfos.add(new FlexItemCalculationInfo(flexItemRenderer,
                    flexBasisValues.get(i),
                    1,
                    1,
                    bBox.getWidth()));
        }

        FlexContainerRenderer flexContainerRenderer = new FlexContainerRenderer(div);
        div.setNextRenderer(flexContainerRenderer);

        // before checks
        for (FlexItemCalculationInfo info : flexItemCalculationInfos) {
            Assert.assertNull(info.mainSize);
            Assert.assertNull(info.crossSize);
        }

        List<List<FlexItemInfo>> rectangleTable =
                FlexUtil.calculateChildrenRectangles(bBox, (FlexContainerRenderer) div.getRenderer(),
                        flexItemCalculationInfos);

        Assert.assertEquals(56.25f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(150f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(93.75f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumLtWidthGrow1Shrink1Item2MuchContentSetMinWidthGtBasisTest03() {
        Rectangle bBox = new Rectangle(575, 842);
        List<UnitValue> flexBasisValues = Arrays.<UnitValue>asList(
                UnitValue.createPointValue(112.5f),
                UnitValue.createPointValue(60f),
                UnitValue.createPointValue(187.5f)
        );

        List<FlexItemCalculationInfo> flexItemCalculationInfos = new ArrayList<>();
        Div div = new Div().setWidth(300).setHeight(100);

        // We use Courier as a monotype font to ensure that min width calculated by iText
        // is more or less the same as the width calculated by browsers
        FontProvider provider = new FontProvider();
        provider.getFontSet().addFont(StandardFonts.COURIER, null, "courier");

        DocumentRenderer documentRenderer = new DocumentRenderer(
                new Document(new PdfDocument(new PdfWriter(new ByteArrayOutputStream()))));
        documentRenderer.setProperty(Property.FONT_PROVIDER, provider);

        for (int i = 0; i < flexBasisValues.size(); i++) {
            Div flexItem = new Div().add(new Paragraph(1 == i ? "2222222222222222222222222" : Integer.toString(i)));
            if (1 == i) {
                flexItem
                        .setFontFamily(StandardFontFamilies.COURIER)
                        .setMinWidth(150f);
            }
            AbstractRenderer flexItemRenderer = (AbstractRenderer) flexItem.createRendererSubTree()
                    .setParent(documentRenderer);
            div.add(flexItem);
            flexItemCalculationInfos.add(new FlexItemCalculationInfo(flexItemRenderer,
                    flexBasisValues.get(i),
                    1,
                    1,
                    bBox.getWidth()));
        }

        FlexContainerRenderer flexContainerRenderer = new FlexContainerRenderer(div);
        div.setNextRenderer(flexContainerRenderer);

        // before checks
        for (FlexItemCalculationInfo info : flexItemCalculationInfos) {
            Assert.assertNull(info.mainSize);
            Assert.assertNull(info.crossSize);
        }

        List<List<FlexItemInfo>> rectangleTable =
                FlexUtil.calculateChildrenRectangles(bBox, (FlexContainerRenderer) div.getRenderer(),
                        flexItemCalculationInfos);

        Assert.assertEquals(56.25f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(150f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(93.75f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumEqWidthGrow1Shrink1Item2Basis0Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                WRAP_STYLE,
                Arrays.<UnitValue>asList(
                        UnitValue.createPointValue(400f),
                        UnitValue.createPointValue(0f),
                        UnitValue.createPointValue(100f)),
                Arrays.asList(1f, 1f, 1f),
                Arrays.asList(1f, 1f, 1f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());

        Assert.assertEquals(1, rectangleTable.get(0).size());
        Assert.assertEquals(2, rectangleTable.get(1).size());

        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }

        Assert.assertEquals(400f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(150f, rectangleTable.get(1).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(250f, rectangleTable.get(1).get(1).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumEqWidthGrow1Shrink1Item2Basis0NoContentTest02() {
        Rectangle bBox = new Rectangle(575, 842);
        List<UnitValue> flexBasisValues = Arrays.<UnitValue>asList(
                UnitValue.createPointValue(400f),
                UnitValue.createPointValue(0f),
                UnitValue.createPointValue(100f)
        );

        List<FlexItemCalculationInfo> flexItemCalculationInfos = new ArrayList<>();
        Div div = new Div().setWidth(400).setHeight(100);
        div.setProperty(Property.FLEX_WRAP, FlexWrapPropertyValue.WRAP);

        DocumentRenderer documentRenderer = new DocumentRenderer(
                new Document(new PdfDocument(new PdfWriter(new ByteArrayOutputStream()))));

        for (int i = 0; i < flexBasisValues.size(); i++) {
            Div flexItem = new Div();
            if (1 != i) {
                flexItem.add(new Paragraph(Integer.toString(i)));
            }
            AbstractRenderer flexItemRenderer = (AbstractRenderer) flexItem.createRendererSubTree()
                    .setParent(documentRenderer);
            div.add(flexItem);
            flexItemCalculationInfos.add(new FlexItemCalculationInfo(flexItemRenderer,
                    flexBasisValues.get(i),
                    1,
                    1,
                    bBox.getWidth()));
        }

        FlexContainerRenderer flexContainerRenderer = new FlexContainerRenderer(div);
        div.setNextRenderer(flexContainerRenderer);

        // before checks
        for (FlexItemCalculationInfo info : flexItemCalculationInfos) {
            Assert.assertNull(info.mainSize);
            Assert.assertNull(info.crossSize);
        }

        List<List<FlexItemInfo>> rectangleTable =
                FlexUtil.calculateChildrenRectangles(bBox, (FlexContainerRenderer) div.getRenderer(),
                        flexItemCalculationInfos);

        Assert.assertEquals(2, rectangleTable.get(0).size());
        Assert.assertEquals(1, rectangleTable.get(1).size());

        Assert.assertEquals(400f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(0f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(400f, rectangleTable.get(1).get(0).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumLtWidthGrow0Shrink0Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                Arrays.<UnitValue>asList(
                        UnitValue.createPointValue(50f),
                        UnitValue.createPointValue(80f),
                        UnitValue.createPointValue(100f)),
                Arrays.asList(0f, 0f, 0f),
                Arrays.asList(0f, 0f, 0f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(50f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(80f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(100f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumLtWidthGrow0Shrink0Item2Grow2Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                Arrays.<UnitValue>asList(
                        UnitValue.createPointValue(50f),
                        UnitValue.createPointValue(80f),
                        UnitValue.createPointValue(100f)),
                Arrays.asList(0f, 2f, 0f),
                Arrays.asList(0f, 0f, 0f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(50f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(250f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(100f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumLtWidthGrow1Shrink0Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                Arrays.<UnitValue>asList(
                        UnitValue.createPointValue(50f),
                        UnitValue.createPointValue(80f),
                        UnitValue.createPointValue(100f)),
                Arrays.asList(1f, 1f, 1f),
                Arrays.asList(0f, 0f, 0f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(106.66667f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(136.66667f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(156.66667f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumGtWidthGrow0Shrink1Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                Arrays.<UnitValue>asList(
                        UnitValue.createPointValue(100f),
                        UnitValue.createPointValue(200f),
                        UnitValue.createPointValue(300f)),
                Arrays.asList(0f, 0f, 0f),
                Arrays.asList(1f, 1f, 1f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(200f / 3, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(400f / 3, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(600f / 3, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumGtWidthGrow0Shrink05Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                Arrays.<UnitValue>asList(
                        UnitValue.createPointValue(100f),
                        UnitValue.createPointValue(200f),
                        UnitValue.createPointValue(300f)),
                Arrays.asList(0f, 0f, 0f),
                Arrays.asList(0.5f, 0.5f, 0.5f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(200f / 3, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(400f / 3, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(600f / 3, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumGtWidthGrow0Shrink01Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                Arrays.<UnitValue>asList(
                        UnitValue.createPointValue(100f),
                        UnitValue.createPointValue(200f),
                        UnitValue.createPointValue(300f)),
                Arrays.asList(0f, 0f, 0f),
                Arrays.asList(0.1f, 0.1f, 0.1f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(90f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(180f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(270f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumGtWidthGrow0Shrink5Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                Arrays.<UnitValue>asList(
                        UnitValue.createPointValue(100f),
                        UnitValue.createPointValue(200f),
                        UnitValue.createPointValue(300f)),
                Arrays.asList(0f, 0f, 0f),
                Arrays.asList(5f, 5f, 5f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(200f / 3, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(400f / 3, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(600f / 3, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumGtWidthGrow1Shrink1Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                Arrays.<UnitValue>asList(
                        UnitValue.createPointValue(100f),
                        UnitValue.createPointValue(200f),
                        UnitValue.createPointValue(300f)),
                Arrays.asList(1f, 1f, 1f),
                Arrays.asList(1f, 1f, 1f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(200f / 3, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(400f / 3, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(600f / 3, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumGtWidthGrow1Shrink1Item3Shrink50Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                Arrays.<UnitValue>asList(
                        UnitValue.createPointValue(100f),
                        UnitValue.createPointValue(200f),
                        UnitValue.createPointValue(300f)),
                Arrays.asList(1f, 1f, 1f),
                Arrays.asList(1f, 1f, 50f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(98.69281f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(197.38562f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(103.92157f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumGtWidthGrow1Shrink1Item3Shrink5Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                Arrays.<UnitValue>asList(
                        UnitValue.createPointValue(100f),
                        UnitValue.createPointValue(200f),
                        UnitValue.createPointValue(300f)),
                Arrays.asList(1f, 1f, 1f),
                Arrays.asList(1f, 1f, 5f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(88.888885f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(177.77777f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(133.33334f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumGtWidthGrow0Shrink0Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                Arrays.<UnitValue>asList(
                        UnitValue.createPointValue(100f),
                        UnitValue.createPointValue(200f),
                        UnitValue.createPointValue(300f)),
                Arrays.asList(0f, 0f, 0f),
                Arrays.asList(0f, 0f, 0f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(100f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(200f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(300f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumGtWidthGrow1Shrink0Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                Arrays.<UnitValue>asList(
                        UnitValue.createPointValue(100f),
                        UnitValue.createPointValue(200f),
                        UnitValue.createPointValue(300f)),
                Arrays.asList(1f, 1f, 1f),
                Arrays.asList(0f, 0f, 0f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(100f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(200f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(300f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void basis250SumGtWidthGrow0Shrink1WrapTest01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                WRAP_STYLE,
                Arrays.<UnitValue>asList(
                        UnitValue.createPointValue(250f),
                        UnitValue.createPointValue(250f),
                        UnitValue.createPointValue(250f)),
                Arrays.asList(0f, 0f, 0f),
                Arrays.asList(1f, 1f, 1f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(250f, flexItemInfo.getRectangle().getWidth(), EPS);
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
    }

    @Test
    public void differentBasisSumGtWidthGrow0Shrink1WrapTest01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                WRAP_STYLE,
                Arrays.<UnitValue>asList(
                        UnitValue.createPointValue(100f),
                        UnitValue.createPointValue(200f),
                        UnitValue.createPointValue(300f)),
                Arrays.asList(0f, 0f, 0f),
                Arrays.asList(1f, 1f, 1f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(100f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(200f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(300f, rectangleTable.get(1).get(0).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumGtWidthGrow0Shrink05WrapTest01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                WRAP_STYLE,
                Arrays.<UnitValue>asList(
                        UnitValue.createPointValue(100f),
                        UnitValue.createPointValue(200f),
                        UnitValue.createPointValue(300f)),
                Arrays.asList(0f, 0f, 0f),
                Arrays.asList(0.5f, 0.5f, 0.5f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(100f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(200f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(300f, rectangleTable.get(1).get(0).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumGtWidthGrow0Shrink01WrapTest01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                WRAP_STYLE,
                Arrays.<UnitValue>asList(
                        UnitValue.createPointValue(100f),
                        UnitValue.createPointValue(200f),
                        UnitValue.createPointValue(300f)),
                Arrays.asList(0f, 0f, 0f),
                Arrays.asList(0.1f, 0.1f, 0.1f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(100f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(200f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(300f, rectangleTable.get(1).get(0).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumGtWidthGrow0Shrink5WrapTest01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                WRAP_STYLE,
                Arrays.<UnitValue>asList(
                        UnitValue.createPointValue(100f),
                        UnitValue.createPointValue(200f),
                        UnitValue.createPointValue(300f)),
                Arrays.asList(0f, 0f, 0f),
                Arrays.asList(5f, 5f, 5f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(100f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(200f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(300f, rectangleTable.get(1).get(0).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumGtWidthGrow1Shrink1WrapTest01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                WRAP_STYLE,
                Arrays.<UnitValue>asList(
                        UnitValue.createPointValue(100f),
                        UnitValue.createPointValue(200f),
                        UnitValue.createPointValue(300f)),
                Arrays.asList(1f, 1f, 1f),
                Arrays.asList(1f, 1f, 1f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(150f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(250f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(400f, rectangleTable.get(1).get(0).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumGtWidthGrow1Shrink1Item3Shrink50WrapTest01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                WRAP_STYLE,
                Arrays.<UnitValue>asList(
                        UnitValue.createPointValue(100f),
                        UnitValue.createPointValue(200f),
                        UnitValue.createPointValue(300f)),
                Arrays.asList(1f, 1f, 1f),
                Arrays.asList(1f, 1f, 50f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(150f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(250f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(400f, rectangleTable.get(1).get(0).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumGtWidthGrow1Shrink1Item3Shrink5WrapTest01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                WRAP_STYLE,
                Arrays.<UnitValue>asList(
                        UnitValue.createPointValue(100f),
                        UnitValue.createPointValue(200f),
                        UnitValue.createPointValue(300f)),
                Arrays.asList(1f, 1f, 1f),
                Arrays.asList(1f, 1f, 5f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(150f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(250f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(400f, rectangleTable.get(1).get(0).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumGtWidthGrow0Shrink0WrapTest01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                WRAP_STYLE,
                Arrays.<UnitValue>asList(
                        UnitValue.createPointValue(100f),
                        UnitValue.createPointValue(200f),
                        UnitValue.createPointValue(300f)),
                Arrays.asList(0f, 0f, 0f),
                Arrays.asList(0f, 0f, 0f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(100f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(200f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(300f, rectangleTable.get(1).get(0).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisSumGtWidthGrow1Shrink0WrapTest01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                WRAP_STYLE,
                Arrays.<UnitValue>asList(
                        UnitValue.createPointValue(100f),
                        UnitValue.createPointValue(200f),
                        UnitValue.createPointValue(300f)),
                Arrays.asList(1f, 1f, 1f),
                Arrays.asList(0f, 0f, 0f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(150f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(250f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(400f, rectangleTable.get(1).get(0).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisPercentSumLtWidthGrow0Shrink1Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                Arrays.<UnitValue>asList(
                        UnitValue.createPercentValue(10),
                        UnitValue.createPercentValue(20),
                        UnitValue.createPercentValue(30)),
                Arrays.asList(0f, 0f, 0f),
                Arrays.asList(1f, 1f, 1f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(40f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(80f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(120f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisPercentSumLtWidthGrow1Shrink1Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                Arrays.<UnitValue>asList(
                        UnitValue.createPercentValue(10),
                        UnitValue.createPercentValue(20),
                        UnitValue.createPercentValue(30)),
                Arrays.asList(1f, 1f, 1f),
                Arrays.asList(1f, 1f, 1f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(93.333336f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(133.33333f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(173.33333f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisPercentSumLtWidthGrow0Shrink0Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                Arrays.<UnitValue>asList(
                        UnitValue.createPercentValue(10),
                        UnitValue.createPercentValue(20),
                        UnitValue.createPercentValue(30)),
                Arrays.asList(0f, 0f, 0f),
                Arrays.asList(0f, 0f, 0f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(40f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(80f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(120f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisPercentSumLtWidthGrow0Shrink0Item2Grow2Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                Arrays.<UnitValue>asList(
                        UnitValue.createPercentValue(10),
                        UnitValue.createPercentValue(20),
                        UnitValue.createPercentValue(30)),
                Arrays.asList(0f, 2f, 0f),
                Arrays.asList(0f, 0f, 0f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(40f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(240f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(120f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisPercentSumLtWidthGrow1Shrink0Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                Arrays.<UnitValue>asList(
                        UnitValue.createPercentValue(10),
                        UnitValue.createPercentValue(20),
                        UnitValue.createPercentValue(30)),
                Arrays.asList(1f, 1f, 1f),
                Arrays.asList(0f, 0f, 0f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(93.333336f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(133.33333f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(173.33333f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisPercentSumGtWidthGrow0Shrink1Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                Arrays.<UnitValue>asList(
                        UnitValue.createPercentValue(30),
                        UnitValue.createPercentValue(40),
                        UnitValue.createPercentValue(50)),
                Arrays.asList(0f, 0f, 0f),
                Arrays.asList(1f, 1f, 1f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(300f / 3, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(400f / 3, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(500f / 3, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisPercentSumGtWidthGrow0Shrink05Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                Arrays.<UnitValue>asList(
                        UnitValue.createPercentValue(30),
                        UnitValue.createPercentValue(40),
                        UnitValue.createPercentValue(50)),
                Arrays.asList(0f, 0f, 0f),
                Arrays.asList(0.5f, 0.5f, 0.5f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(100f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(400f / 3, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(500f / 3, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisPercentSumGtWidthGrow0Shrink01Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                Arrays.<UnitValue>asList(
                        UnitValue.createPercentValue(30),
                        UnitValue.createPercentValue(40),
                        UnitValue.createPercentValue(50)),
                Arrays.asList(0f, 0f, 0f),
                Arrays.asList(0.1f, 0.1f, 0.1f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(114f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(152f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(190f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisPercentSumGtWidthGrow0Shrink5Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                Arrays.<UnitValue>asList(
                        UnitValue.createPercentValue(30),
                        UnitValue.createPercentValue(40),
                        UnitValue.createPercentValue(50)),
                Arrays.asList(0f, 0f, 0f),
                Arrays.asList(5f, 5f, 5f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(300f / 3, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(400f / 3, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(500f / 3, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisPercentSumGtWidthGrow1Shrink1Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                Arrays.<UnitValue>asList(
                        UnitValue.createPercentValue(30),
                        UnitValue.createPercentValue(40),
                        UnitValue.createPercentValue(50)),
                Arrays.asList(1f, 1f, 1f),
                Arrays.asList(1f, 1f, 1f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(300f / 3, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(400f / 3, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(500f / 3, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisPercentSumGtWidthGrow1Shrink1Item3Shrink50Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                Arrays.<UnitValue>asList(
                        UnitValue.createPercentValue(30),
                        UnitValue.createPercentValue(40),
                        UnitValue.createPercentValue(50)),
                Arrays.asList(1f, 1f, 1f),
                Arrays.asList(1f, 1f, 50f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(119.06615f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(158.75487f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(122.178986f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisPercentSumGtWidthGrow1Shrink1Item3Shrink5Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                Arrays.<UnitValue>asList(
                        UnitValue.createPercentValue(30),
                        UnitValue.createPercentValue(40),
                        UnitValue.createPercentValue(50)),
                Arrays.asList(1f, 1f, 1f),
                Arrays.asList(1f, 1f, 5f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(112.5f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(150f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(137.5f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisPercentSumGtWidthGrow0Shrink0Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                Arrays.<UnitValue>asList(
                        UnitValue.createPercentValue(30),
                        UnitValue.createPercentValue(40),
                        UnitValue.createPercentValue(50)),
                Arrays.asList(0f, 0f, 0f),
                Arrays.asList(0f, 0f, 0f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }
        Assert.assertEquals(120f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(160f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(200f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }

    @Test
    public void differentBasisPercentSumGtWidthGrow1Shrink0Test01() {
        List<List<FlexItemInfo>> rectangleTable = testFlex(
                Arrays.<UnitValue>asList(
                        UnitValue.createPercentValue(30),
                        UnitValue.createPercentValue(40),
                        UnitValue.createPercentValue(50)),
                Arrays.asList(1f, 1f, 1f),
                Arrays.asList(0f, 0f, 0f)
        );

        // after checks
        Assert.assertFalse(rectangleTable.isEmpty());
        for (List<FlexItemInfo> line : rectangleTable) {
            for (FlexItemInfo flexItemInfo : line) {
                Assert.assertEquals(25.976562f, flexItemInfo.getRectangle().getHeight(), EPS);
            }
        }

        Assert.assertEquals(120f, rectangleTable.get(0).get(0).getRectangle().getWidth(), EPS);
        Assert.assertEquals(160f, rectangleTable.get(0).get(1).getRectangle().getWidth(), EPS);
        Assert.assertEquals(200f, rectangleTable.get(0).get(2).getRectangle().getWidth(), EPS);
    }


    private static List<List<FlexItemInfo>> testFlex(List<UnitValue> flexBasisValues, List<Float> flexGrowValues,
            List<Float> flexShrinkValues) {
        return testFlex(DEFAULT_STYLE, flexBasisValues, flexGrowValues, flexShrinkValues);
    }

    private static List<List<FlexItemInfo>> testFlex(Style containerStyle, List<UnitValue> flexBasisValues,
            List<Float> flexGrowValues,
            List<Float> flexShrinkValues) {
        assert flexBasisValues.size() == flexGrowValues.size();
        assert flexBasisValues.size() == flexShrinkValues.size();

        Rectangle bBox = new Rectangle(PageSize.A4);
        bBox.applyMargins(36f, 36f, 36f, 36f, false);

        List<FlexItemCalculationInfo> flexItemCalculationInfos = new ArrayList<>();
        Div div = new Div();
        div.addStyle(containerStyle);

        DocumentRenderer documentRenderer = new DocumentRenderer(
                new Document(new PdfDocument(new PdfWriter(new ByteArrayOutputStream()))));

        for (int i = 0; i < flexBasisValues.size(); i++) {
            Div flexItem = new Div().add(new Paragraph("x"));
            AbstractRenderer flexItemRenderer = (AbstractRenderer) flexItem.createRendererSubTree()
                    .setParent(documentRenderer);
            div.add(flexItem);
            flexItemCalculationInfos.add(new FlexItemCalculationInfo(flexItemRenderer,
                    null == flexBasisValues.get(i) ? UnitValue
                            .createPointValue(flexItemRenderer.getMinMaxWidth().getMinWidth()) : flexBasisValues.get(i),
                    flexGrowValues.get(i),
                    flexShrinkValues.get(i),
                    bBox.getWidth()));
        }

        FlexContainerRenderer flexContainerRenderer = new FlexContainerRenderer(div);
        div.setNextRenderer(flexContainerRenderer);

        // before checks
        for (FlexItemCalculationInfo info : flexItemCalculationInfos) {
            Assert.assertNull(info.mainSize);
            Assert.assertNull(info.crossSize);
        }

        return FlexUtil.calculateChildrenRectangles(bBox, (FlexContainerRenderer) div.getRenderer(),
                flexItemCalculationInfos);
    }
}
