package com.hb.module.guandan.handler;

import com.chester.netty.GamePacket;
import com.chester.netty.handler.BaseHandler;
import com.hb.module.common.AbstractRoom;
import com.hb.module.common.GameData;
import com.hb.module.guandan.card.GDanCard;
import com.hb.module.guandan.card.GDanCardNumber;
import com.hb.module.guandan.card.TributeCard;
import com.hb.module.guandan.game.GDanRoom;
import com.hb.module.guandan.game.GDanUser;
import com.hb.module.service.GameDataService;
import com.hb.proto.GDan;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * 进贡和还供
 */
public class GDanTributeHandler extends BaseHandler<GamePacket> {

    protected GDanTributeHandler() {
        super((short) 2);
    }
    @Autowired
    private GameDataService gameDataService;
    @Override
    public void handle(GamePacket gamePacket) throws Exception {
        int cardId=0;
        boolean tribute = (int) (Math.random() * 2) == 1;
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
        if (tribute) { //进贡
            room.tribute(gDanUser, cardId);
        } else { //还供
            room.backTribute(gDanUser, cardId);
        }
    }
}
