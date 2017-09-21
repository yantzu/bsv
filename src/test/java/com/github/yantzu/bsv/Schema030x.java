package com.github.yantzu.bsv;

import java.util.List;
import java.util.Map;

public class Schema030x implements BsvObject {
    private String              version;
    private String              s;
    private int                 i;
    private boolean             b;
    private String              n;
    private List<String>        array;
    private Map<String, String> map;

    public Schema030x() {
    	
    }
    
    public Schema030x(String version) {
        super();
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getS() {
        return s;
    }


    public void setS(String s) {
        this.s = s;
    }


    public int getI() {
        return i;
    }


    public void setI(int i) {
        this.i = i;
    }


    public boolean isB() {
        return b;
    }


    public void setB(boolean b) {
        this.b = b;
    }


    public String getN() {
        return n;
    }


    public void setN(String n) {
        this.n = n;
    }


    public List<String> getArray() {
        return array;
    }


    public void setArray(List<String> array) {
        this.array = array;
    }


    public Map<String, String> getMap() {
        return map;
    }


    public void setMap(Map<String, String> map) {
        this.map = map;
    }

}
