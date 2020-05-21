package com.hb.module.guandan.game;

import com.chester.commons.utils.TimeUtil;
import com.hb.common.Game;
import com.hb.proto.GDan.*;

/**
 * 掼蛋房的通用属性类
 */
public class GdRoomCommon {
    /**
     * 创建时间
     */
    private int createTime;
    /**
     * 游戏对象
     */
    private Game game;

    private long innerId;

    private GDanGameState state;

    public GdRoomCommon(Game game) {
        this.createTime = TimeUtil.time();
        this.game = game;
        state=GDanGameState.PLAYING;
    }

    public int getCreateTime() {
        return createTime;
    }

    public void setCreateTime(int createTime) {
        this.createTime = createTime;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public long getInnerId() {
        return innerId;
    }

    public void setInnerId(long innerId) {
        this.innerId = innerId;
    }

    public void setState(GDanGameState state) {
        this.state = state;
    }

    public GDanGameState getState() {
        return state;
    }
}
