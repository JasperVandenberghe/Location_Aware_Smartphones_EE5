package be.groept.emedialab.rank_em;

import java.io.Serializable;

public class DataSample implements Serializable {

    private final static long serialVersionUID = 1;

    private String string = "This is a string";

    public DataSample(String string){
        this.string = string;
    }

    @Override
    public String toString() {
        return string;
    }
}
