package uk.ac.lancs.ucrel.conv;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;

public class BNCHandler extends DefaultHandler {

    boolean text, word = false;

    StringBuilder sb = new StringBuilder();
    Map<String, String> tags;
    StringBuilder chars;
    String file;

    public BNCHandler(String file){
        this.file = file;
    }

    public void startElement(String uri, String localName,String qName,
                             Attributes attributes) throws SAXException {
        if(qName.equals("stext") || qName.equals("wtext")) {
            text = true;
        }
        if(text && qName.equals("w")){
            word = true;
            chars = new StringBuilder();
            tags = new HashMap<String, String>();
            for(int i = 0; i < attributes.getLength(); i++){
                tags.put(attributes.getQName(i), attributes.getValue(i));
            }
        }
    }

    public void endElement(String uri, String localName,
                           String qName) throws SAXException {
        if(qName.equals("stext") || qName.equals("wtext")) {
            text = false;
        }
        if(text && qName.equals("w")){
            word = false;
            sb.append(chars.toString());
            for(String key : tags.keySet()){
                sb.append("\t").append(tags.get(key));
            }
            sb.append("\n");
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        if(text && word) {
            chars.append(ch, start, length);
        }
    }

    public String getText(){
        return sb.toString();
    }
}
