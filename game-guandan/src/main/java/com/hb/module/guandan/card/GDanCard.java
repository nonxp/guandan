package com.hb.module.guandan.card;


import com.chester.commons.utils.TimeUtil;
import com.hb.proto.GDan;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 掼蛋牌类
 */
public class GDanCard implements Comparable<GDanCard> {

    /**
     * 花色
     */
    private CardSuit suit;
    /**
     * 牌点
     */
    private GDanCardNumber number;
    /**
     * 极牌
     */
    private boolean levelCard;
    /**
     * 供牌
     */
    private boolean tributeCard;

    /**
     * 牌的ID
     */
    private int cardId;


    /**
     * 每一张牌
     *
     * @param suit
     * @param number
     */

    private String createTime;

    public GDanCard(CardSuit suit, GDanCardNumber number) {
        this.suit = suit;
        this.number = number;
        this.createTime = new SimpleDateFormat("hh:mm:ss").format(TimeUtil.time());
    }


    public CardSuit getSuit() {
        return suit;
    }

    public void setSuit(CardSuit suit) {
        this.suit = suit;
    }

    public GDanCardNumber getNumber() {
        return number;
    }

    public void setNumber(GDanCardNumber number) {
        this.number = number;
    }

    public boolean isLevelCard() {
        return levelCard;
    }

    public void setLevelCard(boolean levelCard) {
        this.levelCard = levelCard;
    }

    public boolean isTributeCard() {
        return tributeCard;
    }

    public void setTributeCard(boolean tributeCard) {
        this.tributeCard = tributeCard;
    }

    public int getCardId() {
        return cardId;
    }

    public void setCardId(int cardId) {
        this.cardId = cardId;
    }

    /**
     * @param level 是否要极牌的等级
     * @return
     */
    public int getLevel(boolean level) {
        if (isLevelCard() && level) {
            return GDanCardNumber.GL.getLevel();
        }
        return number.getLevel();
    }

    public int getNum() {
        return number.num();
    }

    /**
     * 极牌的等级
     */
    public int getLevel() {
        return getLevel(true);
    }

    /**
     * 癞子万能牌
     */
    public boolean isWildCard() {
        return levelCard && suit == CardSuit.HEART;
    }

    @Override
    public String toString() {
        StringJoiner card = new StringJoiner("");
        card.add(suit.toString());
        card.add(isLevelCard() ? "(极)" : "");
        card.add(number.toString());
        return card.toString();
    }

    @Override
    public int compareTo(GDanCard o) {
        return o.getLevel() == getLevel() ?
                Integer.compare(suit.type(), o.suit.type())
                : Integer.compare(o.getLevel(), getLevel());
    }

    public GDanCard copy(GDanCard card) {
        GDanCard copy = new GDanCard(card.getSuit(), card.getNumber());
        if (card.isLevelCard()) {
            copy.setLevelCard(true);
        }
        return copy;
    }

    public GDan.GDanCard cardMsg() {
        GDan.GDanCard.Builder danCard = GDan.GDanCard.newBuilder();
        danCard.setLevel(levelCard);
        danCard.setSuit(suit.type());
        danCard.setTribute(tributeCard);
        danCard.setNumber(number.num());
        danCard.setCardId(cardId);
        return danCard.build();
    }

    public boolean isJoker() {
        return this.number == GDanCardNumber.GD_JOKE || this.number == GDanCardNumber.GS_JOKE;
    }
}
