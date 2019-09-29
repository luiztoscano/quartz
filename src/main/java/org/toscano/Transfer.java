package org.toscano;

public class Transfer {
    private Integer id;
    private Integer from;
    private Integer to;

    public Transfer() {
    }

    public Transfer(Integer id, Integer from, Integer to) {
        this.id = id;
        this.from = from;
        this.to = to;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFrom() {
        return from;
    }

    public void setFrom(Integer from) {
        this.from = from;
    }

    public Integer getTo() {
        return to;
    }

    public void setTo(Integer to) {
        this.to = to;
    }
}
