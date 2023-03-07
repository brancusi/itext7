/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 iText Group NV
    Authors: iText Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
    OF THIRD PARTY RIGHTS

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
    or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, see http://www.gnu.org/licenses or write to
    the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA, 02110-1301 USA, or download the license from the following URL:
    http://itextpdf.com/terms-of-use/

    The interactive user interfaces in modified source and object code versions
    of this program must display Appropriate Legal Notices, as required under
    Section 5 of the GNU Affero General Public License.

    In accordance with Section 7(b) of the GNU Affero General Public License,
    a covered work must retain the producer line in every PDF that is created
    or manipulated using iText.

    You can be released from the requirements of the license by purchasing
    a commercial license. Buying such a license is mandatory as soon as you
    develop commercial activities involving the iText software without
    disclosing the source code of your own applications.
    These activities include: offering paid services to customers as an ASP,
    serving PDFs on the fly in a web application, shipping iText with a closed
    source product.

    For more information, please contact iText Software Corp. at this
    address: sales@itextpdf.com
 */
package com.itextpdf.forms;

import com.itextpdf.forms.fields.CheckBoxFormFieldBuilder;
import com.itextpdf.forms.fields.PdfButtonFormField;
import com.itextpdf.forms.fields.PdfFormAnnotation;
import com.itextpdf.forms.fields.PushButtonFormFieldBuilder;
import com.itextpdf.forms.fields.RadioFormFieldBuilder;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.tagging.StandardRoles;
import com.itextpdf.kernel.pdf.tagutils.TagTreePointer;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.IntegrationTest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

@Category(IntegrationTest.class)
public class FormFieldsTaggingTest extends ExtendedITextTest {

    public static final String sourceFolder = "./src/test/resources/com/itextpdf/forms/FormFieldsTaggingTest/";
    public static final String destinationFolder = "./target/test/com/itextpdf/forms/FormFieldsTaggingTest/";

    @BeforeClass
    public static void beforeClass() {
        createOrClearDestinationFolder(destinationFolder);
    }

    /**
     * Form fields addition to the tagged document.
     */
    @Test
    public void formFieldTaggingTest01() throws IOException, InterruptedException, ParserConfigurationException, SAXException {
        String outFileName = destinationFolder + "taggedPdfWithForms01.pdf";
        String cmpFileName = sourceFolder + "cmp_taggedPdfWithForms01.pdf";

        PdfWriter writer = new PdfWriter(outFileName);
        PdfDocument pdfDoc = new PdfDocument(writer);
        pdfDoc.setTagged();

        PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, true);

        addFormFieldsToDocument(pdfDoc, form);

        pdfDoc.close();

        compareOutput(outFileName, cmpFileName);
    }

    /**
     * Form fields copying from the tagged document.
     */
    @Test
    public void formFieldTaggingTest02() throws IOException, InterruptedException, ParserConfigurationException, SAXException {
        String outFileName = destinationFolder + "taggedPdfWithForms02.pdf";
        String cmpFileName = sourceFolder + "cmp_taggedPdfWithForms02.pdf";

        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(outFileName));
        pdfDoc.setTagged();
        pdfDoc.initializeOutlines();

        PdfAcroForm acroForm = PdfAcroForm.getAcroForm(pdfDoc, true);
        acroForm.addField(new CheckBoxFormFieldBuilder(pdfDoc, "TestCheck")
                .setWidgetRectangle(new Rectangle(36, 560, 20, 20)).createCheckBox().setValue("1", true));

        PdfDocument docToCopyFrom = new PdfDocument(new PdfReader(sourceFolder + "cmp_taggedPdfWithForms07.pdf"));
        docToCopyFrom.copyPagesTo(1, docToCopyFrom.getNumberOfPages(), pdfDoc, new PdfPageFormCopier());

        pdfDoc.close();

        compareOutput(outFileName, cmpFileName);
    }

    /**
     * Form fields flattening in the tagged document.
     */
    @Test
    public void formFieldTaggingTest03() throws IOException, InterruptedException, ParserConfigurationException, SAXException {
        String outFileName = destinationFolder + "taggedPdfWithForms03.pdf";
        String cmpFileName = sourceFolder + "cmp_taggedPdfWithForms03.pdf";

        PdfDocument pdfDoc = new PdfDocument(new PdfReader(sourceFolder + "cmp_taggedPdfWithForms01.pdf"), new PdfWriter(outFileName));

        PdfAcroForm acroForm = PdfAcroForm.getAcroForm(pdfDoc, false);
        acroForm.flattenFields();

        pdfDoc.close();

        compareOutput(outFileName, cmpFileName);
    }

    /**
     * Removing fields from tagged document.
     */
    @Test
    public void formFieldTaggingTest04() throws IOException, InterruptedException, ParserConfigurationException, SAXException {
        String outFileName = destinationFolder + "taggedPdfWithForms04.pdf";
        String cmpFileName = sourceFolder + "cmp_taggedPdfWithForms04.pdf";

        PdfDocument pdfDoc = new PdfDocument(new PdfReader(sourceFolder + "cmp_taggedPdfWithForms01.pdf"), new PdfWriter(outFileName));

        PdfAcroForm acroForm = PdfAcroForm.getAcroForm(pdfDoc, false);
        acroForm.removeField("TestCheck");
        acroForm.removeField("push");

        pdfDoc.close();

        compareOutput(outFileName, cmpFileName);
    }

    /**
     * Form fields flattening in the tagged document (writer mode).
     */
    @Test
    public void formFieldTaggingTest05() throws IOException, InterruptedException, ParserConfigurationException, SAXException {
        String outFileName = destinationFolder + "taggedPdfWithForms05.pdf";
        String cmpFileName = sourceFolder + "cmp_taggedPdfWithForms05.pdf";

        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(outFileName));
        pdfDoc.setTagged();

        PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, true);

        addFormFieldsToDocument(pdfDoc, form);

        form.flattenFields();

        pdfDoc.close();

        compareOutput(outFileName, cmpFileName);
    }

    /**
     * Removing fields from tagged document (writer mode).
     */
    @Test
    public void formFieldTaggingTest06() throws IOException, InterruptedException, ParserConfigurationException, SAXException {
        String outFileName = destinationFolder + "taggedPdfWithForms06.pdf";
        String cmpFileName = sourceFolder + "cmp_taggedPdfWithForms06.pdf";

        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(outFileName));
        pdfDoc.setTagged();

        PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, true);

        addFormFieldsToDocument(pdfDoc, form);

        form.removeField("TestCheck");
        form.removeField("push");

        pdfDoc.close();

        compareOutput(outFileName, cmpFileName);
    }

    /**
     * Addition of the form field at the specific position in tag structure.
     */
    @Test
    public void formFieldTaggingTest07() throws IOException, InterruptedException, ParserConfigurationException, SAXException {
        String outFileName = destinationFolder + "taggedPdfWithForms07.pdf";
        String cmpFileName = sourceFolder + "cmp_taggedPdfWithForms07.pdf";

        PdfWriter writer = new PdfWriter(outFileName);
        PdfReader reader = new PdfReader(sourceFolder + "taggedDocWithFields.pdf");
        PdfDocument pdfDoc = new PdfDocument(reader, writer);

        // Original document is already tagged, so there is no need to mark it as tagged again
//        pdfDoc.setTagged();

        PdfAcroForm acroForm = PdfAcroForm.getAcroForm(pdfDoc, true);

        PdfButtonFormField pushButton = new PushButtonFormFieldBuilder(pdfDoc, "push")
                .setWidgetRectangle(new Rectangle(36, 650, 40, 20)).setCaption("Capcha").createPushButton();

        TagTreePointer tagPointer = pdfDoc.getTagStructureContext().getAutoTaggingPointer();
        tagPointer.moveToKid(StandardRoles.DIV);
        acroForm.addField(pushButton);

        pdfDoc.close();

        compareOutput(outFileName, cmpFileName);
    }

    private void addFormFieldsToDocument(PdfDocument pdfDoc, PdfAcroForm acroForm) {
        Rectangle rect = new Rectangle(36, 700, 20, 20);
        Rectangle rect1 = new Rectangle(36, 680, 20, 20);

        String formFieldName = "TestGroup";
        RadioFormFieldBuilder builder = new RadioFormFieldBuilder(pdfDoc, formFieldName);
        PdfButtonFormField group = builder.createRadioGroup();
        group.setValue("1", true);

        group.addKid(builder.createRadioButton("1", rect));
        group.addKid(builder.createRadioButton("2", rect1));


        acroForm.addField(group);

        PdfButtonFormField pushButton = new PushButtonFormFieldBuilder(pdfDoc, "push")
                .setWidgetRectangle(new Rectangle(36, 650, 40, 20)).setCaption("Capcha").createPushButton();
        PdfButtonFormField checkBox = new CheckBoxFormFieldBuilder(pdfDoc, "TestCheck")
                .setWidgetRectangle(new Rectangle(36, 560, 20, 20)).createCheckBox();
        checkBox.setValue("1", true);

        acroForm.addField(pushButton);
        acroForm.addField(checkBox);
    }

    private void compareOutput(String outFileName, String cmpFileName) throws InterruptedException, IOException, ParserConfigurationException, SAXException {
        CompareTool compareTool = new CompareTool();
        String compareResult = compareTool.compareTagStructures(outFileName, cmpFileName);
        if (compareResult != null) {
            Assert.fail(compareResult);
        }

        compareResult = compareTool.compareByContent(outFileName, cmpFileName, destinationFolder, "diff" + outFileName);

        if (compareResult != null) {
            Assert.fail(compareResult);
        }
    }
}
