package ro.pub.cs.systems.eim.practicaltest02.model;

import java.util.Date;

public class Data {
    private String EUR;
    private String USD;
    private Date lastUpdate;

    public String getEUR() {
        return EUR;
    }

    public void setEUR(String EUR) {
        this.EUR = EUR;
    }

    public String getUSD() {
        return USD;
    }

    public void setUSD(String USD) {
        this.USD = USD;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Data(String eur, String usd, Date lastUpdate) {
        this.EUR = eur;
        this.USD = usd;
        this.lastUpdate = lastUpdate;
    }

    public Data() {
        this.EUR = null;
        this.USD = null;
        this.lastUpdate = null;
    }


    @Override
    public String toString() {
        return "Converted values are {" +
                "USD ='" + this.USD + '\'' +
                ", EURO ='" + this.USD +
                ", LAST UPDATE ='" + this.lastUpdate + '\'' +
                '}';
    }
}
