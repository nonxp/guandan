package com.hb.module.guandan.handler;

import com.chester.netty.ConnectionManager;
import com.chester.netty.GamePacket;
import com.chester.netty.handler.BaseHandler;
import com.hb.common.AbstractHandler;
import com.hb.common.PromptMessage;
import com.hb.module.common.AbstractRoom;
import com.hb.module.common.GameData;
import com.hb.module.guandan.card.GDCardHelper;
import com.hb.module.guandan.card.GDanCard;
import com.hb.module.guandan.card.GDanDisCards;
import com.hb.module.guandan.game.GDanRoom;
import com.hb.module.guandan.game.GDanUser;
import com.hb.module.service.GameDataService;
import com.hb.proto.GDan;
import com.sun.org.apache.regexp.internal.RE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



public class GDanPlayCardsHandler extends BaseHandler<GamePacket> {

    @Autowired
    private GameDataService gameDataService;

    protected GDanPlayCardsHandler(short cmd) {
        super((short) 1);
    }

    @Override
    public void handle(GamePacket gamePacket) throws Exception {
        Integer[] arr = new Integer[0];
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
        room.playCard(arr, gDanUser);
    }
}
