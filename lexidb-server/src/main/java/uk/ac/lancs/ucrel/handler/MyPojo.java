package uk.ac.lancs.ucrel.handler;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MyPojo {

    private String val;

    public MyPojo(String val){
        this.val = val;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }
}
