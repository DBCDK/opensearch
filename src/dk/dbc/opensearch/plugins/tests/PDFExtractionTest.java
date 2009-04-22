/**
 * \file PDFExtractionTest.java
 * \brief The PDFExtractionTest class
 * \package tests;
 */

package dk.dbc.opensearch.plugins.tests;

/*
   
This file is part of opensearch.
Copyright © 2009, Dansk Bibliotekscenter a/s, 
Tempovej 7-11, DK-2750 Ballerup, Denmark. CVR: 15149043

opensearch is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

opensearch is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with opensearch.  If not, see <http://www.gnu.org/licenses/>.
*/

import org.apache.pdfbox.searchengine.lucene.LucenePDFDocument;
import org.apache.lucene.document.Document;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;
import org.junit.*;

/**
 *
 */
public class PDFExtractionTest{

    /**
     *
     */
    @Test public void pdf_test() throws IOException {
        //        File file = new File( "/home/shm/139/Implementation_of_a_Water.pdf" );
        //Document luceneDocument = LucenePDFDocument.getDocument( file );
        //System.out.println(luceneDocument.toString());
    }

}
