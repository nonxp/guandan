package com.hb.module.guandan.game;

import com.hb.common.JoinUser;
import com.hb.db.log.entity.RoleResultLog;
import com.hb.module.guandan.card.*;
import com.hb.proto.GDan;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.hb.proto.GDan.GDCamp.BLUE;
import static com.hb.proto.GDan.GDCamp.RED;

/**
 * 掼蛋角色类
 */
public class GDanUser {
    protected JoinUser joinUser;
    /**
     * 手牌
     */
    protected GDHandCard handCard;

    /**
     * 谁给我进贡的牌
     */
    private TributeCard tributeCard;

    /**
     * 当前排名
     */
    private GDan.GDRank rank;

    /**
     * 上次排名
     */
    private GDan.GDRank lastRank;

    /**
     * 阵营
     */
    private GDan.GDCamp camp;

    /**
     * 自己可以进贡的牌集合
     */
    private Map<Integer, GDanCard> canTributeCard;

    /**
     * 地方阵营
     */
    private GDan.GDCamp enemyCamp;

    public GDan.GDCamp getEnemyCamp() {
        return enemyCamp;
    }

    public void setEnemyCamp(GDan.GDCamp enemyCamp) {
        this.enemyCamp = enemyCamp;
    }

    /**
     * 座位
     */
    private int seat;

    private GDanRoom room;

    /**
     * 状态
     */
    private int state;

    public GDHandCard getHandCard() {
        return handCard;
    }

    public Map<Integer, GDanCard> getCanTributeCard() {
        return canTributeCard;
    }

    public void setCanTributeCard(Map<Integer, GDanCard> canTributeCard) {
        this.canTributeCard = canTributeCard;
    }

    public void setHandCard(GDHandCard handCard) {
        this.handCard = handCard;
    }

    private RoleResultLog roleResultLog;

    public GDanUser(JoinUser joinUser, int seat, GDanRoom room) {
        this.joinUser = joinUser;
        this.seat = seat;
        this.room = room;
        this.camp = seat % 2 == 0 ? RED : BLUE;
        this.enemyCamp = seat % 2 == 0 ? BLUE : RED;
        roleResultLog = RoleResultLog.create(
                joinUser.getUid(),
                room.getGame().getGameType(),
                room.getInnerId(), null, 0D);
        canTributeCard = new HashMap<>();
    }

    public TributeCard getTributeCard() {
        return tributeCard;
    }

    public void setTributeCard(TributeCard tributeCard) {
        this.tributeCard = tributeCard;
    }

    public int getSeat() {
        return seat;
    }

    public void setSeat(int seat) {
        this.seat = seat;
    }

    public GDanRoom getRoom() {
        return room;
    }

    public void setRoom(GDanRoom room) {
        this.room = room;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public RoleResultLog getRoleResultLog() {
        return roleResultLog;
    }

    /**
     * 发牌
     */
    public void cardDistribute(List<GDanCard> cards) {
        Collections.sort(cards);
        this.handCard = new GDHandCard(cards);
    }

    /**
     * 获取下一个玩家
     *
     * @return
     */
    public GDanUser getNextUser() {
        if (seat == 4) { //最大的那个了
            return getRoom().getSeatMap().get(1);
        }
        return getRoom().getSeatMap().get(seat + 1);
    }

    public GDan.GDRank getLastRank() {
        return lastRank;
    }

    public void setLastRank(GDan.GDRank lastRank) {
        this.lastRank = lastRank;
    }

    /**
     * 获取前一个玩家
     *
     * @return
     */
    public GDanUser getPreUser() {
        if (seat == 1) { //最小值
            return getRoom().getSeatMap().get(4);
        }
        return getRoom().getSeatMap().get(seat - 1);
    }

    public GDan.GDRank getRank() {
        return rank;
    }

    public JoinUser getJoinUser() {
        return joinUser;
    }

    public GDan.GDCamp getCamp() {
        return camp;
    }

    public void setJoinUser(JoinUser joinUser) {
        this.joinUser = joinUser;
    }

    public void setRank(GDan.GDRank rank) {
        this.rank = rank;
    }

    public void setCamp(GDan.GDCamp camp) {
        this.camp = camp;
    }

    public void setRoleResultLog(RoleResultLog roleResultLog) {
        this.roleResultLog = roleResultLog;
    }

    public void clearing() {
        rank = null;
        handCard = null;
    }

    /**
     * 进贡，将手上可以可以进贡的牌都设置好，发送给前端
     */
    public void tribute() {
        List<GDanCard> cards = getHandCard().getCards();
        int maxLevel = 0;
        boolean hasFind = true;
        for (GDanCard card : cards) {
            if (card.isJoker()) {
                continue;
            }
            if (hasFind) {
                maxLevel = card.getLevel();
                hasFind = false;
            }
            if (card.getLevel() < maxLevel) {
                break;
            }
            if (card.isWildCard()) {
                continue;
            }
            card.setTributeCard(true); //将牌设置为可以进贡的
            canTributeCard.put(card.getCardId(), card);
            needTribute = true;
        }
        System.out.println(String.format("玩家%s可以进贡的牌为:%s", getId(), canTributeCard.values()));
        //发送信息提示玩家进贡
    }
    private  boolean needTribute;

    private  boolean needBackTribute;

    public boolean isNeedBackTribute() {
        return needBackTribute;
    }

    public void setNeedBackTribute(boolean needBackTribute) {
        this.needBackTribute = needBackTribute;
    }

    public boolean isNeedTribute() {
        return needTribute;
    }

    public void setNeedTribute(boolean needTribute) {
        this.needTribute = needTribute;
    }

    public long getId() {
        return joinUser.getUid();
    }

    public void backTribute(GDanCard myCard) {
        handCard.addCard(myCard);
    }



    public GDan.GDanFlushStraights getFlushStraightMsg() {
        if (handCard != null) {
            GDan.GDanFlushStraights.Builder flushStraights = GDan.GDanFlushStraights.newBuilder();
            Map<CardSuit, List<GDanDisCards>> allFlushStraights = handCard.getAllFlushStraights();
            if (allFlushStraights != null) {
                for (Map.Entry<CardSuit, List<GDanDisCards>> entry : allFlushStraights.entrySet()) {
                    CardSuit key = entry.getKey();
                    List<GDanDisCards> value = entry.getValue();
                    GDan.GDanFlushStraight.Builder flushStraight = GDan.GDanFlushStraight.newBuilder();
                    for (GDanDisCards disCards : value) {
                        GDan.GDanCardCut.Builder cut = GDan.GDanCardCut.newBuilder();
                        cut.setType(disCards.getType()); //设置类型
                        List<Integer> cardIds = disCards.getRealCards().stream().map(GDanCard::getCardId).collect(Collectors.toList()); //手牌ID
                        cut.addAllCards(cardIds);
                        flushStraight.addCut(cut);
                    }
                    flushStraight.setSuit(GDan.CardSuit.forNumber(key.type())); //设置花色
                    flushStraights.addFlush(flushStraight);
                }
            }
            return flushStraights.build();
        }
        return null;
    }

    public void playCard(GDanDisCards disCards) {
        disCards.setRid(getId());
        this.handCard.playCard(disCards);
    }

    /**
     * @param lastCardSender 上一次出的牌
     * @param self           发送给提示的玩家
     * @return 提示消息
     */
    public GDan.GDanCardTipsPlayMsg GDanCardTipsPlayMsg(GDanDisCards lastCardSender,boolean self) {
        GDan.GDanCardTipsPlayMsg.Builder playMsg = GDan.GDanCardTipsPlayMsg.newBuilder();
        playMsg.setPlayerId(getId());
        if (self) {
            if (handCard != null) {
                Collection<GDanDisCards> gDanDisCards = handCard.gDanTipsPlayCards(lastCardSender);
                if (gDanDisCards != null) {
                    for (GDanDisCards disCards : gDanDisCards) {
                        GDan.GDanCardCut.Builder cardCut = GDan.GDanCardCut.newBuilder();
                        cardCut.setType(disCards.getType());
                        cardCut.addAllCards(Arrays.asList(disCards.getCardArr()));
                        playMsg.addTipsCards(cardCut);
                    }
                }
            }
        }
        return playMsg.build();
    }
}
