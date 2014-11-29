package aaremm.com.sleepyhead.object;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rahul on 28-11-2014.
 */
public class Station {

    public Station(String name){
        this.name = name;
    }

    private String name;
    private List<Integer> lineNo = new ArrayList<Integer>();

    public Station(String key, List<Integer> value) {
        this.name = key;
        this.lineNo = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getLineNo() {
        return lineNo;
    }

    public void setLineNo(List<Integer> lineNo) {
        this.lineNo = lineNo;
    }
}
