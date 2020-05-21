package com.hb.module.guandan.handler;

import com.chester.netty.ConnectionManager;
import com.chester.netty.GamePacket;
import com.chester.netty.handler.BaseHandler;
import com.hb.module.common.AbstractRoom;
import com.hb.module.common.GameData;
import com.hb.module.guandan.game.GDanRoom;
import com.hb.module.guandan.game.GDanUser;
import com.hb.module.service.GameDataService;
import com.hb.proto.GDan;
import com.hb.proto.Protocol;
import org.springframework.beans.factory.annotation.Autowired;

import static com.hb.proto.GDan.Cmd.GDanFlushStraightTipsHandler_VALUE;

/**
 *同花顺提示
 */
public class GDanFlushStraightTipsHandler extends BaseHandler<GamePacket> {

    protected GDanFlushStraightTipsHandler() {
        super((short)GDanFlushStraightTipsHandler_VALUE);
    }
    @Autowired
    private GameDataService gameDataService;
    @Override
    public void handle(GamePacket gamePacket) throws Exception {
        long rid = (Long) gamePacket.getConnection().getIdentifer();
        GameData<? extends AbstractRoom> gameData = gameDataService.getGameData(rid);
        if (gameData == null) {
            return;
        }
        GDanRoom room = (GDanRoom) gameData.getData();
        GDanUser gDanUser = room.getGDanUser(rid);
        if (gDanUser == null) {
            System.out.println("找不到玩家");
            return;
        }
        GDan.GDanFlushStraights flushStraightMsg = gDanUser.getFlushStraightMsg();
        ConnectionManager.sendMessage(rid, new GamePacket((short) GDanFlushStraightTipsHandler_VALUE, flushStraightMsg.toByteArray()));
    }
}
