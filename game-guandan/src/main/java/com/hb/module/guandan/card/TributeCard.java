package com.hb.module.guandan.card;


import java.util.StringJoiner;

/**
 * 进贡的牌
 */
public class TributeCard implements Comparable<TributeCard> {
    private long rid; //标识玩家
    private GDanCard card; //卡牌

    public TributeCard(long rid, GDanCard card) {
        this.rid = rid;
        this.card = card;
    }

    public long getRid() {
        return rid;
    }

    public void setRid(long rid) {
        this.rid = rid;
    }

    public GDanCard getCard() {
        return card;
    }

    public void setCard(GDanCard card) {
        this.card = card;
    }

    /**
     * 通过牌值进行排序
     */
    @Override
    public int compareTo(TributeCard o) {
        return Integer.compare(card.getLevel(), o.getCard().getLevel());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ",  "[", "]")
                .add("id=" + rid)
                .add("card=" + card)
                .toString();
    }
}