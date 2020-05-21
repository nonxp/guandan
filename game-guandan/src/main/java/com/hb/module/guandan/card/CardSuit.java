package com.hb.module.guandan.card;

/**
 * 扑克花色
 */
public enum CardSuit {
    SPADE(1, "黑桃", "♠"),
    HEART(2, "红桃", "♥"),
    CLUB(3, "梅花", "♣"),
    DIAMOND(4, "方块", "♦"),
    BLANK(5, "空白", "")
    ;
    /**
     * type越小，等级越高
     */
    private int type;
    private String desc;
    private String pic;

    CardSuit(int type, String desc, String pic) {
        this.type = type;
        this.desc = desc;
        this.pic = pic;
    }

    public int type() {
        return type;
    }

    public String desc() {
        return desc;
    }

    public String pic() {
        return pic;
    }

    @Override
    public String toString() {
        return pic;
    }
}
