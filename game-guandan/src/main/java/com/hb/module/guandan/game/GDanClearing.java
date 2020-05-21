package com.hb.module.guandan.game;

import com.hb.common.LangException;
import com.hb.db.log.entity.GameResultLog;
import com.hb.module.common.clearing.IGameClearing;

public class GDanClearing implements IGameClearing {
    private GDanRoom room;
    private GameResultLog gameResultLog;
    private GDanClearing(GDanRoom room) {
        this.room = room;
        this.gameResultLog = GameResultLog.create(room.getLocaleId(),
                room.getGameType(), 0, room.getUserNum(),
                0, room.getInnerId(), 0);
    }
    public static GDanClearing getInstance(GDanRoom room) {
        return new GDanClearing(room);
    }
    @Override
    public void clearing() throws LangException {
     //等游戏条件结束，开始结算
        room.clearing();
    }
}
