package sk.vyzyvacky.model;

import java.io.Serializable;

public class LogEntry implements Serializable {
    private String time;
    private String winner;
    private String looser;

    public LogEntry(String time, String winner, String looser) {
        this.time = time;
        this.winner = winner;
        this.looser = looser;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public String getLooser() {
        return looser;
    }

    public void setLooser(String looser) {
        this.looser = looser;
    }
}
