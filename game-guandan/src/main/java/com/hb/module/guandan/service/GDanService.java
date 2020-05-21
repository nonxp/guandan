package com.hb.module.guandan.service;

import com.chester.netty.Connection;
import com.chester.netty.IConnListener;
import com.hb.common.Game;
import com.hb.common.LangException;
import com.hb.module.AbstractIPlayGameAware;
import com.hb.module.DispatchPlayGame;
import com.hb.module.common.AbstractRoom;
import com.hb.module.common.GameData;
import com.hb.module.guandan.game.GDanRoom;
import com.hb.module.service.GameDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GDanService  extends AbstractIPlayGameAware implements IConnListener {
    private static GDanService instance;
    @Autowired
    private GameDataService gameDataService;

    public GDanService() {
        DispatchPlayGame.registerPlayAware(100, this);
        instance = this;
    }

    public static GDanService me() {
        return instance;
    }

    @Override
    public void startGame(Game game, long rid) throws LangException {
        super.startGame(game, rid);
        GDanRoom room = createGDanRoom(game);
        gameDataService.createGameData(game,room);
    }

    private GDanRoom createGDanRoom(Game game) {
        return new GDanRoom(game);
    }



    @Override
    public void reconnect(long rid) throws LangException {
        GameData<GDanRoom> gameData = gameDataService.getGameData(rid, GDanRoom.class);
        if (gameData != null) {
            super.reconnect(rid);
        }
        if (gameData==null){
            return;
        }
        //推送断线重连后的一些消息
        //设置状态
    }

    @Override
    public void connected(Connection connection) throws Exception {

    }

    @Override
    public void disConnect(Connection connection) throws Exception {

    }

    @Override
    public void joinGame(long rid, AbstractRoom room) throws LangException {

    }

    @Override
    public void prepare(long rid, boolean showCard) throws LangException {

    }

    @Override
    public void leave(long rid) throws LangException {

    }

    @Override
    public void changeSite(long rid) throws LangException {

    }

    @Override
    public void clearingCallback(int localeId, Object data) throws LangException {

    }
}
