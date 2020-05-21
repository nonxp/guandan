package com.hb.module.guandan.game;

import com.chester.netty.ConnectionManager;
import com.chester.netty.GamePacket;
import com.google.protobuf.MessageLite;
import com.hb.common.Game;
import com.hb.common.JoinUser;
import com.hb.common.LangException;
import com.hb.module.common.AbstractRoom;
import com.hb.module.common.clearing.IGameClearing;
import com.hb.module.guandan.card.*;
import com.hb.proto.GDan;

import java.util.*;



public class GDanRoom implements AbstractRoom, IGameClearing {
    private static final int GD = 100;
    /**
     * 房间通用数据
     */
    private GdRoomCommon roomCommon;

    /**
     * 所有玩家数据
     */
    private Map<Long, GDanUser> userMap;
    private Map<Integer, GDanUser> seatMap;

    /**
     * 比赛数据
     */
    private GDanRoomRace roomRace;

    /**
     * 桌面数据
     */
    private GDanTable gDanTable;

    public static int getGD() {
        return GD;
    }

    public GdRoomCommon getRoomCommon() {
        return roomCommon;
    }

    public GDanRoomRace getRoomRace() {
        return roomRace;
    }

    public GDanTable getgDanTable() {
        return gDanTable;
    }

    public GDanRoom(Game game) {
        this.roomCommon = new GdRoomCommon(game);
        userMapInit();
        roomInit();
        sendCards();
    }

    public void sendCards() {
        gDanTable.createCards(roomRace.getCardLevel());
        List<GDanCard> cards = gDanTable.getgDanCards();
        int size = cards.size();
        Collection<GDanUser> users = userMap.values();
        int count = 0;
        int index = size / 4;
        for (GDanUser user : users) {
            List<GDanCard> subList = new ArrayList<>(cards.subList(count, count + index));
            user.cardDistribute(subList);
            count += index;

        }
        int tributeCount = 0;
        boolean canRejectTribute = canRejectTribute(); //抗供
        for (GDanUser user : seatMap.values()) {
            if (user.getLastRank() == GDan.GDRank.DWELLER && !canRejectTribute) {
                user.tribute();
                tributeCount++;
                roomCommon.setState(GDan.GDanGameState.TRIBUTE); //设置状态为进贡
            }
        }
        System.out.println("这局进贡的人数为：" + tributeCount);
        this.gDanTable.setTributeCount(tributeCount); //设置这局进贡人数
        gDanTable.setCurrentSenderId(roomRace.getFirstSend());
    }

    /**
     * 赛事数据初始化
     */
    private void roomInit() {
        this.roomRace = new GDanRoomRace(this);
        this.gDanTable = new GDanTable();
    }

    /**
     * 玩家数据初始化
     */
    private void userMapInit() {
        userMap = new HashMap<>();
        seatMap = new HashMap<>();
        int seat = 1;
        for (JoinUser joinUser : roomCommon.getGame().getJoinUsers()) {
            GDanUser user = new GDanUser(joinUser, seat, this);
            userMap.put(user.joinUser.getUid(), user);
            seatMap.put(seat, user);
            seat++;
//            final int s = seat;
//            LogUtil.debug(() -> String.format("GDanRoom.initUserMap,seat=%s,user=%s", s, user));
        }
    }

    public GDanUser getGDanUser(long rid) {
        return userMap.get(rid);
    }

    @Override
    public Game getGame() {
        return roomCommon.getGame();
    }

    @Override
    public long getInnerId() {
        return roomCommon.getInnerId();
    }

    public int getLocaleId() {
        return getGame().getCreaterLocaleId();
    }

    public int getGameType() {
        return GD;
    }

    public int getUserNum() {
        return (int) getGame().getJoinUsers().stream().filter(joinUser -> !(joinUser.isAI() || joinUser.isInner())).count();
    }

    @Override
    public void clearing() throws LangException {
        roomRace.clearing();
        gDanTable.clearing();
        roomCommon.setState(GDan.GDanGameState.BALANCE);
        userMapClear();
    }

    private void userMapClear() {
        for (GDanUser user : seatMap.values()) {
            user.clearing();
        }
    }

    public boolean canSendCard(GDanUser gDanUser) {
        return gDanTable.getCurrentSenderId() == gDanUser.getId();
    }

    /**
     * 抗供
     * 抗供：如果进贡者抓到两个大王
     * （单人抗贡需要自己抓到两个大王，两人抗贡则只要两人加起来抓到两个大王），
     * 则不须进贡（抗贡），由上游先出牌。系统自动抗杠；
     * 思路：遍历所有的末游玩家,算出他们的大王总数，如果等于2就可以抗供
     */
    public boolean canRejectTribute() {
        int jokeCount = 0;//大王数量
        for (GDanUser user : seatMap.values()) {
            if (user.getLastRank() == GDan.GDRank.DWELLER) {
                jokeCount += user.getHandCard().getJokeCount();

            }
        }
        if (jokeCount==2){
            System.out.println("抗供成功");
            for (GDanUser user : seatMap.values()) {
                if (user.getLastRank() == GDan.GDRank.DWELLER) {
                    System.out.println(String.format("id=%s,cards=%s",user.getId(),user.handCard.getCards()));
                }
            }
        }
        return jokeCount == 2;
    }

    public static class DisCardsResult {
        public static final int SELF = 1; //自己
        public static final int ALL = 0; //自己
        private int type = ALL; //0群发1自己
        private int code; //错误码提示
        private GDanDisCards cards;
        public DisCardsResult() {
        }

        public void setType(int type) {
            this.type = type;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public void setCards(GDanDisCards cards) {
            this.cards = cards;
        }

        public int getType() {
            return type;
        }

        public int getCode() {
            return code;
        }

        public GDanDisCards getCards() {
            return cards;
        }
    }

    public static DisCardsResult newDisCardsResult() {
        return new DisCardsResult();
    }
    public DisCardsResult beforePlayCard(Integer[] cardArr, GDanUser gDanUser){
        DisCardsResult result = newDisCardsResult();
        if (roomCommon.getState() != GDan.GDanGameState.PLAYING) {
            System.out.println("状态不对");
            result.setCode(GDan.DisCardTip.WRONG_DIS_TYPE_VALUE);
            result.setType(1);
            return result;
        }
        if (cardArr.length == 0) {
            if (gDanTable.firstSend()) {//上次出牌者是你
                System.out.println("上次出牌的人是你,不能跳过出牌");
                result.setType(DisCardsResult.SELF);
                result.setCode(GDan.DisCardTip.FIRST_DISCARD_VALUE);
                return result;
            }
            System.out.println(String.format(" id=%s,camp=%s,PASS", gDanUser.getId(), gDanUser.getCamp()));
            GDanUser nextUser = gDanUser.getNextUser(); //设置为下一个出牌者
            while (nextUser.getRank() != null) { //如果下个玩家有排名了，设置为下下个玩家
                nextUser = nextUser.getNextUser();
            }
            sendGDanCardTipsPlayMsg(nextUser,gDanTable.getLastCardSender());
            result.setType(1);
            result.setCode(2);
            return result;
        }
        List<GDanCard> cards = GDCardHelper.indexArr2List(cardArr, gDanUser.handCard.getDanCardMap()); //获得牌集合
        if (cards == null) {
            System.out.println("找不到对应的数字");
            result.setType(DisCardsResult.SELF);
            result.setCode(GDan.DisCardTip.WRONG_DIS_TYPE_VALUE);
            return result;
        }

        if (!canSendCard(gDanUser)) {
            System.out.println("还不到你出牌");
            result.setCode(GDan.DisCardTip.NOT_CUR_PLAYER_VALUE);
            result.setType(1);
            return result;
        }
        Object o = GDCardHelper.sendCard(cards, gDanTable.getLastCardSender());
        if (o instanceof Integer) {
            int code = (int) o;
            System.out.println("出牌错误:"+code);
            result.setCode(code);
            result.setType(1);
            return result;
        }
        if (o instanceof GDanDisCards) {
            result.setCards((GDanDisCards) o);
        }
        return result;
    }

    /**
     * 广播下一个玩家的消息
     * @param nextUser
     * @param lastCardSender
     */
    private void sendGDanCardTipsPlayMsg(GDanUser nextUser, GDanDisCards lastCardSender) {
        if (lastCardSender==null){
            System.out.println();
        }
        gDanTable.setCurrentSenderId(nextUser.getId()); //设置为下一个出牌者
        GDan.GDanCardTipsPlayMsg nextPlayMsg = nextUser.GDanCardTipsPlayMsg(lastCardSender, true);
//        sendMessage(nextPlayMsg, (short) GDanCardTipsPlayMsgID_VALUE, nextUser.getId());
        GDan.GDanCardTipsPlayMsg playMsg = nextUser.GDanCardTipsPlayMsg(lastCardSender, false);
//        sendMessageToAll(true, playMsg, (short) GDanCardTipsPlayMsgID_VALUE, nextUser.getId());
    }

    /**
     * 出牌
     * @param cardArr  卡牌集合
     * @param gDanUser 玩家
     */
    public void playCard(Integer[] cardArr, GDanUser gDanUser) {
        DisCardsResult result = beforePlayCard(cardArr, gDanUser);
        GDanDisCards disCards = result.getCards();
        if (disCards != null) {
            System.out.println(String.format("出牌成功: id=%s,camp=%s,cards=%s", gDanUser.getId(), gDanUser.getCamp(), disCards));
            gDanUser.playCard(disCards);
            result.setCards(disCards);
            result.setCode(GDan.DisCardTip.SUCCESS_VALUE);
//          sendDisCardsMsg(result, gDanUser.getId());
            afterPlayCard(gDanUser, disCards);
//            return;
        }
//        sendDisCardsMsg(result, gDanUser.getId());
    }

    /**
     * 发送出牌成功消息
     *
     * @param result
     * @param rid
     */
    public void sendDisCardsMsg(DisCardsResult result, long rid) {
        int code = result.getCode();
        int type = result.getType();
        GDan.GDanDisCardsMsg.Builder msg = GDan.GDanDisCardsMsg.newBuilder();
        if (type == 0) { //广播，不要，出牌成功
            if (code == GDan.DisCardTip.PASS_VALUE) {
                msg.setCode(GDan.DisCardTip.forNumber(code));
            } else {
                GDanUser gDanUser = getGDanUser(rid);
                int size = gDanUser.getHandCard().getCards().size();
                GDanDisCards danDisCards = result.getCards();
                if (danDisCards != null) {
                    GDan.GDanDisCards.Builder builder = danDisCards.disCardsMsg();
                    builder.setCurrentCardNum(size);
                    msg.setDisCards(builder);
                }
            }
            sendMessageToAll(false, msg.build(), 1, rid);
        } else { //指定玩家，出牌错误
            msg.setCode(GDan.DisCardTip.forNumber(code));
            sendMessage(msg.build(), 1, rid);
        }
    }

    public void afterPlayCard(GDanUser gDanUser, GDanDisCards sender) {
        boolean isEmpty =false;
        if (gDanUser.getHandCard().isEmpty()) { //手牌空了
            isEmpty=true;
            GDan.GDRank rank = roomRace.getRank();
            gDanUser.setRank(rank);
            gDanUser.setLastRank(rank);
            roomRace.resultPlayerRank(gDanUser.getCamp(), gDanUser.getRank());
            System.out.println(String.format("玩家%s打完了手里的牌，获得%s",gDanUser.getId(),gDanUser.getRank()));
            if (roomRace.getCommitCount() == 1) {
                for (GDanUser user : seatMap.values()) {
                    if (user.getCamp() == gDanUser.getCamp() && user.getRank() == null) {
                        System.out.println(String.format("上游玩家:%s打完了手里的牌，队友:%s,成为出牌者", gDanUser.getId(),user.getId()));
                        gDanTable.setLastCardSender(sender);
                        gDanTable.setLastSenderId(gDanUser.getId());
                    }
                }
            }
            if (roomRace.isOver(gDanUser.getCamp())) {
                if (roomRace.getCommitCount() == 2) {
                    System.out.println(String.format("游戏结束%s是二游", gDanUser.getId()));
                    roomRace.getRank();
                    GDan.GDRank myRank = roomRace.getRank(); //执行两次操作，让名次变到末游
                    for (GDanUser user : seatMap.values()) {  //剩下的两位走三游和末游
                        if (user.getCamp() == gDanUser.getEnemyCamp()) {
                            user.setRank(myRank);
                            user.setLastRank(myRank);
                            roomRace.resultPlayerRank(user.getCamp(), user.getRank());
                            System.out.println(String.format("游戏结束%s是末游", user.getId()));
                            roomCommon.setState(GDan.GDanGameState.OVER);
                        }
                    }
                } else if (roomRace.getCommitCount() == 3) {
                    System.out.println(String.format("游戏结束%s是三游", gDanUser.getId()));
                    for (GDanUser user : seatMap.values()) {
                        if (user.getCamp() == gDanUser.getEnemyCamp() && user.getRank() == null) {
                            GDan.GDRank myRank = roomRace.getRank();
                            user.setRank(myRank);
                            user.setLastRank(myRank);
                            roomRace.resultPlayerRank(user.getCamp(), user.getRank());
                            roomCommon.setState(GDan.GDanGameState.OVER);
                            System.out.println(String.format("游戏结束%s是末游", user.getId()));
                        }
                    }
                }
            }
        }
        if (isOver()) {
            try {
                clearing();
                return;
            } catch (LangException e) {
                e.printStackTrace();
            }
        }
        GDanUser nextUser = gDanUser.getNextUser(); //设置为下一个出牌者
        while (nextUser.getRank() != null) { //如果下个玩家有排名了，设置为下下个玩家
            nextUser = nextUser.getNextUser();
        }
        if (!isEmpty) {
            gDanTable.setLastCardSender(sender);
            gDanTable.setLastSenderId(gDanUser.getId());
        }
        sendGDanCardTipsPlayMsg(nextUser,gDanTable.getLastCardSender());
    }


    public boolean isOver() {
        return roomCommon.getState() == GDan.GDanGameState.OVER;
    }

    public Map<Long, GDanUser> getUserMap() {
        return userMap;
    }

    public Map<Integer, GDanUser> getSeatMap() {
        return seatMap;
    }

    /**
     * @param except 是否发给自己
     * @param m      消息包
     * @param cmd    消息id
     * @param id     当前发送消息的玩家id
     */
    public void sendMessageToAll(boolean except, MessageLite m, int cmd, long id) {
        GamePacket gamePacket = new GamePacket((short) cmd, m.toByteArray());
        for (Long rid : userMap.keySet()) {
            if (except && rid == id) {
                continue;
            }
            ConnectionManager.sendMessage(rid, gamePacket);
        }
    }

    /**
     * @param m
     * @param cmd
     * @param id
     */
    public void sendMessage(MessageLite m, int cmd, long id) {
        GamePacket gamePacket = new GamePacket((short) cmd, m.toByteArray());
        ConnectionManager.sendMessage(id, gamePacket);
    }

    /**
     * 进贡
     * @param gDanUser
     */
    public void tribute(GDanUser gDanUser, int cardId) {
        Map<Integer, GDanCard> canTributeCard = gDanUser.getCanTributeCard();
        GDanCard danCard = canTributeCard.get(cardId); // 取出这张贡牌
        if (danCard != null) {
            gDanUser.getHandCard().removeCard(danCard);
            System.out.println(String.format("玩家%s上贡了卡牌%s,数量为:%s", gDanUser.getId(), danCard, gDanUser.handCard.getCards().size()));
            gDanTable.addTributeCard(gDanUser.getId(), danCard); //添加到公共的牌池子中
            if (gDanTable.completeTribute()) {  //完成进贡
                int tributeCount = gDanTable.getTributeCount(); //进贡的人数
                int count = 0;
                for (GDanUser user : seatMap.values()) {
                    GDan.GDRank lastRank = user.getLastRank();
                    if (lastRank == GDan.GDRank.BANKER || lastRank == GDan.GDRank.FOLLOWER) {
                        TributeCard tributeCards = gDanTable.getTributeCards(lastRank);
                        if (tributeCards != null) {
                            user.setTributeCard(tributeCards); //设置进贡的牌
                            System.out.println(String.format("上游玩家%s到贡牌：card=%s", user.getId(), user.getTributeCard().getCard()));
                            user.getHandCard().getCards().add(tributeCards.getCard());
                            user.setNeedBackTribute(true);
                            System.out.println(user.handCard.getCards().size());
                            count++;
                        }
                        if (count == tributeCount) {
                            break;
                        }
                    }
                }
            }
        }
    }

    public void backTribute(GDanUser gDanUser, int cardId) {
        TributeCard tributeCard = gDanUser.getTributeCard();
        long playerId = tributeCard.getRid();
        GDanUser backUser = getGDanUser(playerId);
        GDanCard myCard = gDanUser.getHandCard().getDanCardMap().get(cardId);
        if (myCard == null) {
            System.out.println("进贡的牌不存在");
            return;
        }
        if (backUser.getCamp() == gDanUser.getCamp()) { //如果是同阵营的
            if (myCard.getLevel() > GDanCardNumber.G10.getLevel()) {
                System.out.println("还贡的牌太大了");
                return;
            }
        }
        backUser.backTribute(myCard);
        System.out.println(String.format("玩家%s收到还贡牌%s,总牌数:%s", backUser.getId(), myCard, backUser.handCard.getCards().size()));
    }
}
