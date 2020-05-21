package com.hb.module.guandan.game;

import com.hb.common.LangException;
import com.hb.module.common.clearing.IGameClearing;
import com.hb.proto.GDan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 掼蛋的赛事数据
 */
public class GDanRoomRace implements IGameClearing {
    /**
     * 轮数
     */
    private int round;

    /**
     * 升级（阵营之间升级数）
     */
    private Map<GDan.GDCamp, Integer> levelMap;

    /**
     * 记录每轮下来玩家的输赢
     */
    private Map<GDan.GDCamp, GDan.GDCampResult> levelResultMap;

    /**
     * 玩家打完后的结果
     */
    private Map<GDan.GDCamp, List<GDan.GDRank>> campGDResultMap;

    /**
     * 上局赢的一方
     */
    private GDan.GDCamp winCamp;
    /**
     * 输的一方
     */
    private GDan.GDCamp loseCamp;

    private int commitCount;
    private GDanRoom room;

    /**
     * 通过阵营获取每一轮的结果
     *
     * @param camp 阵营
     * @return 比赛结果
     */
    public GDan.GDCampResult getResult(GDan.GDCamp camp) {
        return levelResultMap.get(camp);
    }


    public GDanRoomRace(GDanRoom room) {
        this.round = 1;
        levelMap = new HashMap<>();
        campGDResultMap = new ConcurrentHashMap<>();
        levelResultMap = new HashMap<>();
        levelMap.put(GDan.GDCamp.RED, 2);
        levelMap.put(GDan.GDCamp.BLUE, 2);
        this.room = room;
    }

    public int getRound() {
        return round;
    }

    /**
     * 每个玩家打完后都会调用一次
     *
     * @param camp   阵营
     * @param result 结果
     */
    public void resultPlayerRank(GDan.GDCamp camp, GDan.GDRank result) {
        List<GDan.GDRank> gdResults = campGDResultMap.computeIfAbsent(camp, l -> new CopyOnWriteArrayList<>());
        gdResults.add(result);
        campGDResultMap.put(camp, gdResults);
    }


    public void roundAdd() {
        round++;
    }

    public void countAdd() {
        commitCount++;
    }

    /**
     * 升级
     * 如果双下，赢家升3级；
     * 如果对手一家二游一家末游，赢家升2级；
     * 如果赢家自己队友是末游，赢家升1级
     * 每次有玩家打完后，将阵营和结果添加到map中，最后在结算时进行升级操作
     */
    public void levelUp() {
        GDan.GDCamp canLevelUp = null; //可以升级的
        GDan.GDCamp noLevelUp = null; //不可升级的
        for (Map.Entry<GDan.GDCamp, List<GDan.GDRank>> entry : campGDResultMap.entrySet()) {
            GDan.GDCamp camp = entry.getKey();
            List<GDan.GDRank> value = entry.getValue();
            if (value.contains(GDan.GDRank.BANKER)) { //上游
                canLevelUp = camp;
                this.winCamp = camp;
                if (value.contains(GDan.GDRank.FOLLOWER)) {
                    levelResultMap.put(camp, GDan.GDCampResult.DOUBLE_BANKER);//双上
                }
            } else {
                noLevelUp = camp;
                this.loseCamp = camp;
            }
        }
        List<GDan.GDRank> noLevelUpResults = campGDResultMap.get(noLevelUp); //没有升级
        int level = levelMap.get(canLevelUp); //当前等级
        if (noLevelUpResults.contains(GDan.GDRank.THIRD)) {
            if (noLevelUpResults.contains(GDan.GDRank.FOLLOWER)) { //【23游】 如果赢家自己队友是末游，赢家升1级,平局
                level += 1;
                levelResultMap.put(noLevelUp, GDan.GDCampResult.TIE);//双上
                levelResultMap.put(canLevelUp, GDan.GDCampResult.TIE);//双上
            }
        } else if (noLevelUpResults.contains(GDan.GDRank.FOLLOWER)) {
            if (noLevelUpResults.contains(GDan.GDRank.DWELLER)) { //【24游】
                level += 2;
                levelResultMap.put(noLevelUp, GDan.GDCampResult.SHINGLE_DWELLER);//双上
                levelResultMap.put(canLevelUp, GDan.GDCampResult.SHINGLE_BANKER);//双上

            }
        } else if (noLevelUpResults.contains(GDan.GDRank.DWELLER)) {
            if (!noLevelUpResults.contains(GDan.GDRank.FOLLOWER) && !noLevelUpResults.contains(GDan.GDRank.THIRD)) { //双下游
                level += 3;
                levelResultMap.put(noLevelUp, GDan.GDCampResult.DOUBLE_DWELLER);//双上
            }
        }
        levelMap.put(canLevelUp, level);
        System.out.println(levelMap);
        System.out.println(levelResultMap);
    }

    @Override
    public void clearing() throws LangException {
        levelUp();
        roundAdd();
        commitCount = 0;
    }

    /**
     * @return 目前打牌的点数
     */
    public int getCardLevel() {
        Map<GDan.GDCamp, Integer> levelMap = this.levelMap;
        Integer level = levelMap.get(winCamp);
        return level == null ? 1 : level - 1;
    }

    /**
     * @return 设置第一个出牌者
     */
    public long getFirstSend() {
        Map<Integer, GDanUser> seatMap = room.getSeatMap();
        int randSeat = 1;//((int) (Math.random() * 4) + 1);
        if (loseCamp == null) {
            return seatMap.get(randSeat).joinUser.getUid();
        } else {
            for (GDanUser user : seatMap.values()) {
                if (user.getLastRank() == GDan.GDRank.DWELLER) {
                    return user.joinUser.getUid();
                }
            }
        }
        return seatMap.get(randSeat).joinUser.getUid();
    }

    public GDan.GDRank getRank() {
        commitCount++;
        return GDan.GDRank.forNumber(commitCount);
    }

    public boolean isOver(GDan.GDCamp camp) {
        List<GDan.GDRank> gdRanks = campGDResultMap.get(camp);
        return gdRanks.size() == 2;
    }

    public Map<GDan.GDCamp, Integer> getLevelMap() {
        return levelMap;
    }

    public Map<GDan.GDCamp, List<GDan.GDRank>> getCampGDResultMap() {
        return campGDResultMap;
    }

    public GDan.GDCamp getWinCamp() {
        return winCamp;
    }

    public GDan.GDCamp getLoseCamp() {
        return loseCamp;
    }

    public int getCommitCount() {
        return commitCount;
    }

    public GDanRoom getRoom() {
        return room;
    }
}
