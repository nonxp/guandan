import com.hb.common.Game;
import com.hb.common.JoinUser;
import com.hb.module.guandan.card.*;
import com.hb.module.guandan.game.GDanRoom;
import com.hb.module.guandan.game.GDanUser;
import com.hb.proto.GDan;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static com.hb.module.guandan.card.GDCardHelper.*;

public class GDanTest {

    @Test
    public void testCardSuit() {
        long time = System.currentTimeMillis();
        List<JoinUser> users = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            JoinUser user = new JoinUser(i + 100, "NICK",
                    110, 123, 1, "", 1
                    , 2, 1, (byte) 0, 1, 1, 1, null);
            users.add(user);
        }
        Game game = new Game(1, users);
        GDanRoom room = new GDanRoom(game);
        Map<Integer, GDanUser> seatMap = room.getSeatMap();
        for (int i = 1; i <= 4; i++) {
            GDanUser user = seatMap.get(i);
//            GDan.GDanFlushStraights flushStraightMsg = user.getFlushStraightMsg();
//            System.out.println(String.format("%s",flushStraightMsg));
            GDanDisCards lastCardSender = room.getgDanTable().getLastCardSender();
            if (lastCardSender != null && lastCardSender.getRid() == user.getId()) {
                lastCardSender = null;
                room.getgDanTable().setLastCardSender(null);
            }
            if (user.getHandCard() == null) {
                break;
            }
            List<GDanDisCards> bestDisCard = getBestDisCardList(lastCardSender, user.getHandCard().getCards(), false);
            if (!bestDisCard.isEmpty()) {
                Integer[] cardArr = bestDisCard.get((int)(Math.random()*bestDisCard.size())).getCardArr();
                room.playCard(cardArr, user);
            }else { //出不了牌，不要
                room.playCard(new Integer[0], user);
            }
            if (i == 4) {
                i = 0;
            }
        }
        room.sendCards();
        for (GDanUser user : seatMap.values()) {
            if (user.isNeedTribute() ) {
                List<GDanCard> list = new ArrayList<>(user.getCanTributeCard().values());
                room.tribute(user, list.get(0).getCardId());
            }
        }

        for (GDanUser user : seatMap.values()) {
            if (user.isNeedBackTribute() ) {
                List<GDanCard> list = new ArrayList<>(user.getHandCard().getCards());
                room.backTribute(user, list.get(list.size()-2).getCardId());
            }
        }

        time = System.currentTimeMillis()-time;
        System.out.println("耗时:" + time + "ms");
    }

    @Test
    public void test1() {
        long time = System.currentTimeMillis();
       List<GDanCard> randCards = randCards();
        List<GDanCard> randCards2 = randCards();
            randCards.addAll(randCards2);
         randCards = new ArrayList<>(randCards.subList(0,27));
         Collections.sort(randCards);
        System.out.println(randCards);
        List<GDanCard> cards=randCards;
        List<GDanCard> wildCards = new ArrayList<>();
        for (GDanCard card : cards) {
            if (card.isWildCard()) {
                wildCards.add(card);
            }
        }
        int wildCardsSize = wildCards.size();
        cards.removeAll(wildCards); //先将万能牌移除出去
        List<Map<GDan.GDanCardType, List<GDanDisCards>>> mapList = new ArrayList<>();
        int totalCount=0;
        if (wildCardsSize > 0) {
            List<List<GDanCard>> lists = getCombinationSelect(wildCardsSize); //两张万能牌的组合
            for (List<GDanCard> list : lists) {
                cards.addAll(list); //先添加
                Map<GDan.GDanCardType, List<GDanDisCards>> allDisCards = getAllDisCards(cards,wildCards,list);
                mapList.add(allDisCards);
                totalCount += allDisCards.size();
                cards.removeAll(list);
            }
        } else {
            Map<GDan.GDanCardType, List<GDanDisCards>> allDisCards = getAllDisCards(cards,null,null);
            mapList.add(allDisCards);
            totalCount += allDisCards.size();
        }
        time = System.currentTimeMillis()-time;
        System.out.println("牌型总数："+totalCount);
        System.out.println("耗时:" + time + "ms");
        for (Map<GDan.GDanCardType, List<GDanDisCards>> typeListMap : mapList) {
            System.out.println("------------------|----------------");
            for (List<GDanDisCards> value : typeListMap.values()) {
                System.out.println(value);
                System.out.println("----------------------------------");
            }
        }
        System.out.println("牌型总数："+totalCount);
        System.out.println("耗时:" + time + "ms");
//        GDanDisCards smallestCards = getBestDisCards(GDan.GDanCardType.FLUSH_STRAIGHTS, randCards,false,wildCards);
//        if (smallestCards != null) {
//            System.out.println(smallestCards);
//        }
//        time = System.currentTimeMillis()-time;
//        System.out.println("耗时:" + time + "ms");

//        getAllDisCards(cards).forEach(System.out::println);


/*        testType(getFCards());
        testType(getFCards1());
        testType(getFCards2());
        testType(fullHouses());
        testType(getF2Cards(false));
        testType(getF2Cards(true));*/
//
//        testType(ganban2());
//        testType(sanliandui());
//        for (int i = 1; i <= 8; i++) {
//            testType(getSingleCards(i));
//        }
//        testType(ganban(true));
//        testType(getSingleLCards(8));
//        testType(ganban2());
//        List<GDanCard> list = GDCardHelper.list();
//        Collections.shuffle(list);
//        Collections.sort(list);
//        for (GDanCard danCard : list) {
//            System.out.print(danCard + "");
//        }
//        System.out.println();
//        Collection<List<GDanCard>> cardsList = GDCardHelper.getCardsList(list);
//        for (List<GDanCard> value : cardsList) {
//            System.out.println(value);
//        }
        testType(getF2Cards(true));
    }


    private void testType(List<GDanCard> cards) {
        long time = System.currentTimeMillis();
        GDanDisCards bestCardSender = getBestGDanDisCards(cards, null);
        System.out.println("best=" + bestCardSender);
        time = System.currentTimeMillis() - time;
        System.out.println(time + "ms");
    }

    /**
     * 同花顺
     *
     * @return
     */
    private static List<GDanCard> getF1Cards() {
        List<GDanCard> cards = new ArrayList<>();
        GDanCard card = new GDanCard(CardSuit.HEART, GDanCardNumber.GA); //A
        card.setLevelCard(true);
        cards.add(card);
/*        GDanCard card2 = new GDanCard(CardSuit.HEART, GDanCardNumber.GA);//A
        card2.setLevelCard(true);
        cards.add(card2);*/
        cards.add(new GDanCard(CardSuit.CLUB, GDanCardNumber.G3));
        cards.add(new GDanCard(CardSuit.CLUB, GDanCardNumber.G3));
        cards.add(new GDanCard(CardSuit.CLUB, GDanCardNumber.G4));
        cards.add(new GDanCard(CardSuit.CLUB, GDanCardNumber.G4));
        return cards;
    }


    public void testRoom() {

    }


    /**
     * 同花顺
     *
     * @return
     */
    private static List<GDanCard> getFCards() {
        List<GDanCard> cards = new ArrayList<>();
        cards.add(new GDanCard(CardSuit.BLANK, GDanCardNumber.GS_JOKE));
        cards.add(new GDanCard(CardSuit.BLANK, GDanCardNumber.GS_JOKE));
        cards.add(new GDanCard(CardSuit.BLANK, GDanCardNumber.GD_JOKE));
        cards.add(new GDanCard(CardSuit.BLANK, GDanCardNumber.GD_JOKE));
        return cards;
    }

    /**
     * 同花顺
     *
     * @return
     */
    private static List<GDanCard> getFCards1() {
        List<GDanCard> cards = new ArrayList<>();
        cards.add(new GDanCard(CardSuit.BLANK, GDanCardNumber.GS_JOKE));
        cards.add(new GDanCard(CardSuit.BLANK, GDanCardNumber.GS_JOKE));
        return cards;
    }

    /**
     * 同花顺
     *
     * @return
     */
    private static List<GDanCard> getFCards2() {
        List<GDanCard> cards = new ArrayList<>();
        cards.add(new GDanCard(CardSuit.BLANK, GDanCardNumber.GD_JOKE));
        cards.add(new GDanCard(CardSuit.BLANK, GDanCardNumber.GD_JOKE));
        return cards;
    }

    /**
     * 顺子
     *
     * @return
     */
    private static List<GDanCard> getF2Cards(boolean f) {
        List<GDanCard> cards = new ArrayList<>();
        cards.add(new GDanCard(CardSuit.HEART, GDanCardNumber.G3));
        cards.add(new GDanCard(CardSuit.HEART, GDanCardNumber.G4));
        cards.add(new GDanCard(CardSuit.HEART, GDanCardNumber.G5));
        cards.add(new GDanCard(CardSuit.HEART, GDanCardNumber.G6));
        cards.add(new GDanCard(f ? CardSuit.HEART : CardSuit.CLUB, GDanCardNumber.G7));
        return cards;
    }


    /**
     * 三带对
     *
     * @return
     */
    private static List<GDanCard> fullHouses() {
        List<GDanCard> cards = new ArrayList<>();
        cards.add(new GDanCard(CardSuit.HEART, GDanCardNumber.G3));
        cards.add(new GDanCard(CardSuit.HEART, GDanCardNumber.G3));
        cards.add(new GDanCard(CardSuit.HEART, GDanCardNumber.G3));
        cards.add(new GDanCard(CardSuit.HEART, GDanCardNumber.G6));
        cards.add(new GDanCard(CardSuit.CLUB, GDanCardNumber.G6));
        return cards;
    }

    /**
     * 三带对
     *
     * @return
     */
    private static List<GDanCard> ganban(boolean level) {
        List<GDanCard> cards = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            GDanCard card = new GDanCard(CardSuit.HEART, GDanCardNumber.G3);
            card.setLevelCard(level);
            cards.add(card);
        }
        cards.add(new GDanCard(CardSuit.HEART, GDanCardNumber.G4));
        cards.add(new GDanCard(CardSuit.HEART, GDanCardNumber.G5));
        cards.add(new GDanCard(CardSuit.HEART, GDanCardNumber.G7));
        cards.add(new GDanCard(CardSuit.HEART, GDanCardNumber.G8));
        return cards;
    }

    /**
     * 三带对
     *
     * @return
     */
    private static List<GDanCard> ganban2() {
        List<GDanCard> cards = new ArrayList<>();
        GDanCard danCard = new GDanCard(CardSuit.HEART, GDanCardNumber.GA);
        danCard.setLevelCard(true);
        GDanCard danCard2 = new GDanCard(CardSuit.HEART, GDanCardNumber.GA);
        danCard2.setLevelCard(true);
        cards.add(danCard);
        cards.add(danCard2);
        cards.add(new GDanCard(CardSuit.HEART, GDanCardNumber.G3));
        cards.add(new GDanCard(CardSuit.HEART, GDanCardNumber.G4));
        cards.add(new GDanCard(CardSuit.HEART, GDanCardNumber.G5));
        return cards;
    }

    /**
     * 三带对
     *
     * @return
     */
    private static List<GDanCard> sanliandui() {
        List<GDanCard> cards = new ArrayList<>();
        cards.add(new GDanCard(CardSuit.HEART, GDanCardNumber.G3));
        cards.add(new GDanCard(CardSuit.HEART, GDanCardNumber.G3));
        cards.add(new GDanCard(CardSuit.HEART, GDanCardNumber.G4));
        cards.add(new GDanCard(CardSuit.HEART, GDanCardNumber.G4));
        cards.add(new GDanCard(CardSuit.HEART, GDanCardNumber.G5));
        cards.add(new GDanCard(CardSuit.HEART, GDanCardNumber.G5));
        return cards;
    }

    private static List<GDanCard> getSingleCards(int index) {
        List<GDanCard> cards = new ArrayList<>();
        for (int i = 0; i < index; i++)
            cards.add(new GDanCard(CardSuit.HEART, GDanCardNumber.G3));
        return cards;
    }

    private static List<GDanCard> getSingleLCards(int index) {
        List<GDanCard> cards = new ArrayList<>();
        GDanCard card = new GDanCard(CardSuit.CLUB, GDanCardNumber.GK);
        card.setLevelCard(true);
        for (int i = 0; i < index; i++)
            cards.add(card);
        GDanCard danCard = new GDanCard(CardSuit.HEART, GDanCardNumber.GA);
        danCard.setLevelCard(true);
        GDanCard danCard2 = new GDanCard(CardSuit.HEART, GDanCardNumber.GA);
        danCard2.setLevelCard(true);
        cards.add(danCard);
        cards.add(danCard2);
        return cards;
    }


}
