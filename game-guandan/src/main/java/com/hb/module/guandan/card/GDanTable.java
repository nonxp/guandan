package com.hb.module.guandan.card;


import com.hb.common.LangException;
import com.hb.module.common.clearing.IGameClearing;
import com.hb.proto.GDan.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 掼蛋桌面
 * 和牌相关的一些公共数据,这个对象在每次新开一局时会重置
 */
public class GDanTable implements IGameClearing {
    /**
     * 进贡的牌,每局开始时，会将进贡者的牌放入牌组中
     */
    private List<TributeCard> tributeCards;

    /**
     * 最后一次出的牌
     */
    private GDanDisCards lastCardSender;

    /**
     * 所有的牌
     */
    private Map<Integer, GDanCard> cardMap = new HashMap<>();

    /**
     * 所有的牌
     */
    private List<GDanCard> gDanCards;
    /**
     * 最后一次出牌的玩家ID
     */
    private long lastSenderId;

    /**
     * 轮到谁出牌
     */
    private long currentSenderId;

    /**
     * 这局进贡的人数
     */
    private int tributeCount;

    public int getTributeCount() {
        return tributeCount;
    }

    public void setTributeCount(int tributeCount) {
        this.tributeCount = tributeCount;
    }

    public void addTributeCard(TributeCard card) {
        tributeCards.add(card);
    }

    public GDanTable() {
        tributeCards = new CopyOnWriteArrayList<>();
    }

    public List<TributeCard> getTributeCards() {
        return tributeCards;
    }

    public GDanDisCards getLastCardSender() {
        return lastCardSender;
    }

    public void setLastCardSender(GDanDisCards lastCardSender) {
        this.lastCardSender = lastCardSender;
    }

    public long getLastSenderId() {
        return lastSenderId;
    }

    public void setLastSenderId(long lastSenderId) {
        this.lastSenderId = lastSenderId;
    }

    public long getCurrentSenderId() {
        return currentSenderId;
    }

    public void setCurrentSenderId(long currentSenderId) {
        this.currentSenderId = currentSenderId;
    }

    /**
     * 根据当前打的牌的点数来确定
     */
    public void createCards(int cardLevel) {
        List<GDanCard> cards = GDCardHelper.randCards();
        cards.addAll(GDCardHelper.randCards());
        int cardId = 1;
        for (GDanCard card : cards) {
            if (card.getLevel(false) == cardLevel) {
                card.setLevelCard(true);
            }
            card.setCardId(cardId);
            cardMap.put(cardId++, card);
        }
        this.gDanCards = cards;
    }

    public Map<Integer, GDanCard> getCardMap() {
        return cardMap;
    }

    public void setCardMap(Map<Integer, GDanCard> cardMap) {
        this.cardMap = cardMap;
    }

    public List<GDanCard> getgDanCards() {
        return gDanCards;
    }

    public void setgDanCards(List<GDanCard> gDanCards) {
        this.gDanCards = gDanCards;
    }

    /**
     * 添加贡牌到牌组
     * @param id 玩家id
     */
    public void addTributeCard(long id, GDanCard card) {
        TributeCard tributeCard = new TributeCard(id, card);
        tributeCards.add(tributeCard);
        System.out.println(String.format("贡牌池收入来自于玩家:%s的卡牌:%s,牌池为:%s",id,card,tributeCards));
    }

    public boolean completeTribute() {
        boolean complete=tributeCards.size() == tributeCount;
        if (complete) {
            Collections.sort(tributeCards); //按照牌值进行排序
            System.out.println(String.format("完成进贡,将牌值排序,根据上局排名分发给上游玩家,排序后tributeCards=%s", tributeCards));
        }
        return complete;
    }


    /**
     * 玩家通过上局前排名从贡牌牌组获取牌，最大的牌给到上游玩家。
     *
     * @param rank 排名
     * @return 返回贡牌对象
     */
    public TributeCard getTributeCards(GDRank rank) {
        Collections.sort(tributeCards); //按照牌值进行排序
        if (tributeCards.isEmpty()) {
            return null;
        }
        if (rank == GDRank.BANKER) {
            return tributeCards.get(0);
        } else if (rank == GDRank.FOLLOWER && tributeCards.size() == 2) {
            return tributeCards.get(1);
        }
        return null;
    }


    public void setTributeCards(List<TributeCard> tributeCards) {
        this.tributeCards = tributeCards;
    }

    @Override
    public void clearing() throws LangException {
        tributeCards.clear();
        lastSenderId = 0;
        currentSenderId = 0;
        tributeCount=0;
        lastCardSender = null;
        gDanCards.clear();
    }

    /**
     * 是否是主动出牌
     * @return
     */
  public boolean firstSend() {
      return lastSenderId == currentSenderId;
    }
}
