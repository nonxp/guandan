package com.hb.module.guandan.card;

import com.hb.proto.GDan;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 玩家手牌
 */
public class GDHandCard {

    /**
     * 最实时玩家手牌数据
     */
    private final List<GDanCard> cards;

    /**
     * 玩家手牌映射表
     */
    Map<Integer,GDanCard> danCardMap;
    /**
     * 上一次出的牌
     */
    private GDanDisCards lastDisCards;

    public List<GDanCard> getCards() {
        return cards;
    }

    public GDHandCard(List<GDanCard> cards) {
        this.cards = cards;
        this.danCardMap = new HashMap<>();
        for (int i = 0; i < cards.size(); i++) {
            GDanCard danCard = cards.get(i);
            danCardMap.put(danCard.getCardId(), cards.get(i));
        }
    }



    public Map<Integer, GDanCard> getDanCardMap() {
        return danCardMap;
    }
    /**
     * 出牌
     *
     * @param playedCards 打出的牌
     */
    public void playCard(GDanDisCards playedCards) {
        List<GDanCard> realCards = playedCards.getRealCards();
        for (GDanCard card : realCards) {
            removeCard(card);
        }
        this.lastDisCards=playedCards;
        System.out.println(String.format("玩家%s手牌数：%s,手牌:%s", playedCards.getRid(), cards.size(), cards));
    }

    public Collection<List<GDanCard>> getHCards() {
        return GDCardHelper.getCardsList(cards);
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * @return 抓到的大王数
     */
    public int getJokeCount() {
        int count = 0;
        for (GDanCard card : cards) {
            if (card.getLevel() == GDanCardNumber.GD_JOKE.getLevel()) {
                count++;
            }
        }
        return count;
    }

    /**
     * 移除牌
     * @param card
     */
    public void removeCard(GDanCard card) {
        cards.remove(card);
        danCardMap.remove(card.getCardId());
    }


    /**
     * 移除牌
     * @param card
     */
    public void addCard(GDanCard card) {
        cards.add(card);
        danCardMap.put(card.getCardId(),card);
    }

    public Map<CardSuit, List<GDanDisCards>> getAllFlushStraights() {
        List<GDanDisCards> disCardsListByType = GDCardHelper.getDisCardsListByType(GDan.GDanCardType.FLUSH_STRAIGHTS, cards, false);//从小到大排序
        if (disCardsListByType.isEmpty()) {
            return null;
        }
        Map<Integer,GDanDisCards> mapList=new HashMap<>(); //去掉重复的
        for (GDanDisCards disCards : disCardsListByType) {
            mapList.put(disCards.getScore(), disCards);
        }
        Map<CardSuit, List<GDanDisCards>> suitListMap = mapList.values().stream().collect(Collectors.groupingBy(GDanDisCards::getCardSuit));
        if (suitListMap.isEmpty()) {
            return null;
        }
        return suitListMap;
    }

    /**
     * 获取当前所有大于这次出牌的牌
     * @param lastCardSender 上一次的出牌
     * @return 返回可以出牌的列表
     */
    public Collection<GDanDisCards> gDanTipsPlayCards(GDanDisCards lastCardSender) {

        List<GDanDisCards> bestDisCardList = GDCardHelper.getBestDisCardList(lastCardSender, cards, false);
        Map<GDan.GDanCardType, List<GDanDisCards>> typeListMap = bestDisCardList.stream().collect(Collectors.groupingBy(GDanDisCards::getType));
        List<GDanDisCards> disCardsList=null;
        if (lastCardSender != null) {
            disCardsList = typeListMap.get(lastCardSender.getType());
        }
        if (disCardsList != null) {
            Map<Integer, GDanDisCards> listMap = new HashMap<>();
            for (GDanDisCards value : bestDisCardList) {
                listMap.put(value.getScore(), value);
            }
            return  listMap.values();
        }else {
            GDan.GDanCardType[] values = GDan.GDanCardType.values();
            for (GDan.GDanCardType type : values) {
                GDan.GDanCardType temp;
                if (GDCardHelper.isBoom(type) && lastCardSender != null) {
                    temp = lastCardSender.getType();
                } else {
                    temp = type;
                }
                disCardsList = typeListMap.get(temp);
                if (disCardsList != null) {
                    Map<Integer, GDanDisCards> listMap = new HashMap<>();
                    for (GDanDisCards value : bestDisCardList) {
                        listMap.put(value.getScore(), value);
                    }
                    return listMap.values();
                }
            }
        }
        return null;
    }
}
