package com.mycompany.app;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.contentstream.operator.DrawObject;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.PDFStreamEngine;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.awt.Color;

import javax.imageio.ImageIO;

import org.apache.pdfbox.contentstream.operator.state.Concatenate;
import org.apache.pdfbox.contentstream.operator.state.Restore;
import org.apache.pdfbox.contentstream.operator.state.Save;
import org.apache.pdfbox.contentstream.operator.state.SetGraphicsStateParameters;
import org.apache.pdfbox.contentstream.operator.state.SetMatrix; 

public class GetImageLocationsAndSize extends PDFStreamEngine
{
    /**
     * @throws IOException If there is an error loading text stripper properties.
     */
    public GetImageLocationsAndSize() throws IOException
    {
        // preparing PDFStreamEngine
        addOperator(new Concatenate());
        addOperator(new DrawObject());
        addOperator(new SetGraphicsStateParameters());
        addOperator(new Save());
        addOperator(new Restore());
        addOperator(new SetMatrix());
    }
 
    /**
     * @throws IOException If there is an error parsing the document.
     */
    public static void main( String[] args ) throws IOException
    {
        PDDocument document = null;
        String fileName = "/Users/kevin/projects/pdf_java/pdf-colors.pdf";
        try
        {
            document = PDDocument.load( new File(fileName) );
            GetImageLocationsAndSize printer = new GetImageLocationsAndSize();
            int pageNum = 0;
            for( PDPage page : document.getPages() )
            {
                pageNum++;
                System.out.println( "\n\nProcessing page: " + pageNum +"\n---------------------------------");
                printer.processPage(page);
            }
        }
        finally
        {
            if( document != null )
            {
                document.close();
            }
        }
    }
 
    /**
     * @param operator The operation to perform.
     * @param operands The list of arguments.
     *
     * @throws IOException If there is an error processing the operation.
     */
    @Override
    protected void processOperator( Operator operator, List<COSBase> operands) throws IOException
    {
        String operation = operator.getName();
        if( "Do".equals(operation) )
        {
            COSName objectName = (COSName) operands.get( 0 );
            // get the PDF object
            PDXObject xobject = getResources().getXObject( objectName );
            // check if the object is an image object
            if( xobject instanceof PDImageXObject)
            {
                PDImageXObject image = (PDImageXObject)xobject;
                int imageWidth = image.getWidth();
                int imageHeight = image.getHeight();
                String rgb = Integer.toHexString(image.getImage().getRGB(0, 0));
                
                System.out.println("\nImage [" + objectName.getName() + "]");
                System.out.println("\nCOLOr: " + rgb);
 
                Matrix ctmNew = getGraphicsState().getCurrentTransformationMatrix();
                float imageXScale = ctmNew.getScalingFactorX();
                float imageYScale = ctmNew.getScalingFactorY();
                float value = ctmNew.getValue(0, 0);
                System.out.println("value = " + value);
                // position of image in the pdf in terms of user space units
                System.out.println("position in PDF = " + ctmNew.getTranslateX() + ", " + ctmNew.getTranslateY() + " in user space units");
                // raw size in pixels
                System.out.println("raw image size  = " + imageWidth + ", " + imageHeight + " in pixels");
                // displayed size in user space units
                System.out.println("displayed size  = " + imageXScale + ", " + imageYScale + " in user space units");
            }
            else if(xobject instanceof PDFormXObject)
            {
                PDFormXObject form = (PDFormXObject)xobject;
                showForm(form);
            }
        }
        else
        {
            super.processOperator( operator, operands);
        }
    }
 
} 