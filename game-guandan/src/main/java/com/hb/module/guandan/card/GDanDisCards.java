package com.hb.module.guandan.card;

import com.hb.proto.GDan;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * 每次打出的牌
 */
public class GDanDisCards implements Comparable<GDanDisCards> {
    /**
     * 牌型
     */
    private GDan.GDanCardType type;

    /**
     * 每次打出的牌的集合
     */
    private List<GDanCard> cards;

    /**
     * 牌的等级，取决于索引最小值
     */
    private int level;

    /**
     * 炸弹的数量
     */
    private int num;

    /**
     * 出牌人
     */
    private long rid;

    /**
     * 真实牌
     */
    private List<GDanCard> realCards;

    private CardSuit cardSuit = CardSuit.BLANK; //给同花顺用的
    /**
     * @param type      牌型
     * @param cards     算分的列表
     * @param level     等级
     * @param wildList  万能牌列表
     * @param fakerList 假牌列表
     */
    public GDanDisCards(GDan.GDanCardType type, List<GDanCard> cards, int level, List<GDanCard> wildList, List<GDanCard> fakerList) {
        this.type = type;
        this.cards = creteCards(cards,wildList,fakerList);
        this.level = level;
        this.realCards = createRealCards(cards, wildList, fakerList);
        if (cards.size() != realCards.size()) {
            throw new RuntimeException("error：手牌数对不上");
        }
    }

    /**
     * 卡牌展示
     * @param cards 传进来的
     * @param wildList 变牌
     * @param fakerList 假牌
     * @return
     */
    private List<GDanCard> creteCards(List<GDanCard> cards,List<GDanCard> wildList,List<GDanCard> fakerList) {
        List<GDanCard> showCards = new ArrayList<>(cards);
        if (wildList != null && fakerList != null) {
            if (showCards.containsAll(wildList)) { //如果包含变牌,就添加假牌进去
                showCards.removeAll(wildList);
                showCards.addAll(fakerList);
            }
        }
        return showCards;
    }


    /**
     * 得到手牌的真正牌型
     *
     * @param cards     手牌
     * @param wildList  万能牌
     * @param fakerList 假牌
     * @return
     */
    private List<GDanCard> createRealCards(List<GDanCard> cards, List<GDanCard> wildList, List<GDanCard> fakerList) {
        List<GDanCard> realList = new ArrayList<>(cards);
        if (fakerList != null) {
            if (realList.containsAll(fakerList)) { //只有当手牌中包含假牌时
                realList.removeAll(fakerList);  //才能移除掉
                if (wildList != null) {
                    realList.addAll(wildList);
                }
            }
        }
        return realList;
    }

    /**
     * 炸弹类型只比较分数大小
     *
     * @return 是否是炸弹类型
     */
    public boolean isBoomType() {
        return (type == GDan.GDanCardType.BOMB) ||
                (type == GDan.GDanCardType.JOKER_BOMB) ||
                (type == GDan.GDanCardType.FLUSH_STRAIGHTS);
    }


    public void setRealCards(List<GDanCard> realCards) {
        this.realCards = realCards;
    }

    public List<GDanCard> getRealCards() {
        return realCards;
    }

    /**
     * 牌的分数,用于压牌
     */
    public int getScore() {
        switch (type) {
            case BOMB:
                return num >= 6 ? (num * 40 + level) : (num * 20 + level);
            case FLUSH_STRAIGHTS:
                return 120 + level;
            case JOKER_BOMB:
                return Integer.MAX_VALUE;
            default:
                return level;
        }
    }

    public GDan.GDanCardType getType() {
        return type;
    }

    public List<GDanCard> getCards() {
        return cards;
    }

    public void setCards(List<GDanCard> cards) {
        this.cards = cards;
    }

    public void setType(GDan.GDanCardType type) {
        this.type = type;
    }

    public int getLevel() {
        return level;
    }

    /**
     * @return 是否是顺子
     */
    public boolean isStraight() {
        return type == GDan.GDanCardType.PLATES ||
                type == GDan.GDanCardType.TUBES ||
                type == GDan.GDanCardType.FLUSH_STRAIGHTS ||
                type == GDan.GDanCardType.STRAIGHTS;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public long getRid() {
        return rid;
    }

    public void setRid(long rid) {
        this.rid = rid;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("", "[", "]");
        joiner.add(type.name() + ":");
        for (GDanCard card : cards) {
            joiner.add(card.toString());
        }
        joiner.add("  realCard:[");
        for (GDanCard card : realCards) {
            joiner.add(card.toString());
        }
        joiner.add("]");
        return joiner.toString();
    }

    @Override
    public int compareTo(GDanDisCards o) {
        return o.getScore() - getScore();
    }

    public CardSuit getCardSuit() {
        return cardSuit;
    }

    public void setCardSuit(CardSuit cardSuit) {
        this.cardSuit = cardSuit;
    }

    public GDan.GDanDisCards.Builder disCardsMsg() {
        GDan.GDanDisCards.Builder builder = GDan.GDanDisCards.newBuilder();
        GDan.GDanCardCut.Builder cardCut = GDan.GDanCardCut.newBuilder();
        cardCut.setType(type);
        for (GDanCard card : realCards) {
            cardCut.addCards(card.getCardId());
        }
        builder.setCardCut(cardCut);
        builder.setPlayerId(rid);
        return builder;
    }

    public Integer[] getCardArr() {
        Integer[] arr = new Integer[realCards.size()];
        int count = 0;
        for (GDanCard realCard : realCards) {
            arr[count++] = realCard.getCardId();
        }
        return arr;
    }

}
