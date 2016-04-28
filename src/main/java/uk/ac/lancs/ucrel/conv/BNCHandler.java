package uk.ac.lancs.ucrel.conv;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class BNCHandler extends DefaultHandler {

    boolean text = false;

    StringBuilder sb = new StringBuilder();

    public void startElement(String uri, String localName,String qName,
                             Attributes attributes) throws SAXException {
        if(qName.equals("stext") || qName.equals("wtext"))
            text = true;
    }

    public void endElement(String uri, String localName,
                           String qName) throws SAXException {
        if(qName.equals("stext") || qName.equals("wtext"))
            text = false;
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        if(text)
            sb.append(ch, start, length);
    }

    public String getText(){
        return sb.toString();
    }
}
