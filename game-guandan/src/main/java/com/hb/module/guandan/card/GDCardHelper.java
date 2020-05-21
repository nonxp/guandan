package com.hb.module.guandan.card;

import com.hb.proto.GDan;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 游戏逻辑处理类
 */
public class GDCardHelper {
    private static final int COUNT = 0; //数量索引
    private static final int START = 1; //开始索引
    private static final int END = 2; //结束索引
    private static final int LEVEL = 3; //极牌
    private static final int ALL = 0; //总数索引
    private static final int MAX_COUNT = 11; //同种牌型能打出的最大数+1
    private static List<List<GDanCard>> combinationSelect2; //万能牌组2
    private static List<List<GDanCard>> combinationSelect1; //万能牌组1
    private static List<GDanCard> cards = new ArrayList<>();

    public static List<List<GDanCard>> getCombinationSelect(int size) {
        return size == 1 ? combinationSelect1 : combinationSelect2;
    }

    static {
        for (CardSuit cardSuit : CardSuit.values()) {
            if (cardSuit == CardSuit.BLANK) {
                continue;
            }
            for (GDanCardNumber number : GDanCardNumber.values()) {
                if (number.getLevel() >= GDanCardNumber.GL.getLevel()) {
                    continue;
                }
                GDanCard card = new GDanCard(cardSuit, number);
                cards.add(card);
            }
        }
        combinationSelect2 = combinationSelect(cards, 2); //两张万能牌
        combinationSelect1 = combinationSelect(cards, 1); //一张万能牌
    }

    /**
     * 对于万能牌的处理思路
     * 玩家打出一副牌之后,里面会有万能牌
     * 让这个万能牌成为任意牌，然后得出可以生成的牌型集合，还要考虑花色
     * 两张万能牌时，要同时变化
     */
    public static Map<Integer, List<GDanDisCards>> getCardTypeListMap(List<GDanCard> cards) {
        Map<Integer, List<GDanDisCards>> cardTypeListMap = new TreeMap<>();
        List<GDanCard> wildCards = new ArrayList<>();
        for (GDanCard card : cards) {
            if (card.isWildCard()) {
                wildCards.add(card);
            }
        }
        int wildCardsSize = wildCards.size();
        List<GDanCard> cardList = new ArrayList<>(cards); //移除之前的卡牌
        cards.removeAll(wildCards); //先将万能牌移除出去
        if (wildCardsSize > 0) {
            List<List<GDanCard>> lists = getCombinationSelect(wildCardsSize); //两张万能牌的组合
            for (List<GDanCard> list : lists) {
                cards.addAll(list); //先添加
                GDanDisCards sender = getCardType(cards, wildCards, list); //获得类型
                if (sender != null) {
                    List<GDanDisCards> cardSenderList = cardTypeListMap.get(sender.getType().getNumber());
                    if (cardSenderList == null) {
                        cardSenderList = new ArrayList<>();
                        cardTypeListMap.put(sender.getType().getNumber(), cardSenderList);
                    }
//                    sender.setRealCards(cardList);
                    cardSenderList.add(sender);
                }
                cards.removeAll(list);
            }
        } else {
            GDanDisCards sender = getCardType(cards, wildCards, null);
            if (sender != null) {
                List<GDanDisCards> cardSenderList = new ArrayList<>();
                cardTypeListMap.put(sender.getType().getNumber(), cardSenderList);
//                sender.setRealCards(cardList);
                cardSenderList.add(sender);
            }
        }
        for (List<GDanDisCards> senderList : cardTypeListMap.values()) {
            Collections.sort(senderList); //按照牌型进行排序
        }
        return cardTypeListMap;
    }


    public static List<List<GDanCard>> combinationSelect(List<GDanCard> dataList, int n) {
        List<List<GDanCard>> list = new ArrayList<>();
        GDanCard[] gDanCards = new GDanCard[n];
        combinationSelect(dataList, 0, gDanCards, 0, list);
        return list;
    }

    /**
     * 组合选择
     *
     * @param dataList    待选列表
     * @param dataIndex   待选开始索引
     * @param resultList  前面（resultIndex-1）个的组合结果
     * @param resultIndex 选择索引，从0开始
     */
    private static void combinationSelect(List<GDanCard> dataList, int dataIndex,
                                          GDanCard[] resultList, int resultIndex,
                                          List<List<GDanCard>> list) {
        int resultLen = resultList.length;
        int resultCount = resultIndex + 1;
        if (resultCount > resultLen) { // 全部选择完时，输出组合结果
            List<GDanCard> cardList = new ArrayList<>();
            Collections.addAll(cardList, resultList);
            list.add(cardList);
            return;
        }
        // 递归选择下一个
        for (int i = dataIndex; i < dataList.size() + resultCount - resultLen; i++) {
            resultList[resultIndex] = dataList.get(i);
            combinationSelect(dataList, i + 1, resultList, resultIndex + 1, list);
        }
    }

    /**
     * n张万能牌的所有组合
     *
     * @return
     */
    public static List<GDanCard> randCards() {
        List<GDanCard> cards = new ArrayList<>();
        for (CardSuit cardSuit : CardSuit.values()) {
            if (cardSuit == CardSuit.BLANK) {
                continue;
            }
            for (GDanCardNumber number : GDanCardNumber.values()) {
                if (number.getLevel() >= GDanCardNumber.GL.getLevel()) {
                    continue;
                }
                GDanCard card = new GDanCard(cardSuit, number);
                cards.add(card);
            }
        }
        cards.add(new GDanCard(CardSuit.BLANK, GDanCardNumber.GS_JOKE));
        cards.add(new GDanCard(CardSuit.BLANK, GDanCardNumber.GD_JOKE));
        Collections.shuffle(cards);
        return cards;
    }


    /**
     * 根据打出的牌得到其牌型
     * 兼容A 1 2 3 4 和10 J Q K A 等顺子
     *
     * @param cards 打出的牌
     * @return 牌型
     */
    private static GDanDisCards getCardType(List<GDanCard> cards, List<GDanCard> wildList, List<GDanCard> fakerList) {
        if (isJokeBoom(cards)) {
            return (new GDanDisCards(GDan.GDanCardType.JOKER_BOMB, cards, 0, wildList, fakerList)); //王炸
        }
        int[][][] levelAndNum = getCardsArr(cards);
        for (int index = 0; index < levelAndNum.length; index++) {
            int[][] arr = levelAndNum[index];
            int levelIndex = arr[ALL][LEVEL];
            int count = arr[ALL][COUNT];
            if (levelIndex != -1 && count == 1) { //是极牌且是单牌
                arr[ALL][START] = arr[ALL][LEVEL]; //单牌变极牌
                arr[ALL][END] = arr[ALL][LEVEL];
            }
            int startIndex = arr[ALL][START];
            int endIndex = arr[ALL][END];
            for (int i = 1; i < arr.length; i++) {
                if (endIndex - startIndex == (count - 1) && endIndex < GDanCardNumber.GA.getLevel()) {
                    if (i == 1 && arr[i][COUNT] == 5) {
                        GDan.GDanCardType type = sameCardSuit(cards) ? GDan.GDanCardType.FLUSH_STRAIGHTS : GDan.GDanCardType.STRAIGHTS; //顺子
                        return new GDanDisCards(type, cards, startIndex, wildList, fakerList); //同花顺
                    } else if (i == 2 && arr[i][COUNT] == 3) {
                        return new GDanDisCards(GDan.GDanCardType.TUBES, cards, startIndex, wildList, fakerList); //三连对
                    } else if (i == 3 && arr[i][COUNT] == 2) {
                        return (new GDanDisCards(GDan.GDanCardType.PLATES, cards, startIndex, wildList, fakerList)); //钢板
                    }
                }
                if (index == 0) { //通过牌值排序不参与单牌
                    continue;
                }
                if (endIndex == startIndex) {
                    if (count == 1 && arr[i][COUNT] == 1) { //同种牌的个数
                        if (i == 1) {
                            return (new GDanDisCards(GDan.GDanCardType.SINGLE_CARD, cards, startIndex, wildList, fakerList));//单牌
                        } else if (i == 2) {
                            return (new GDanDisCards(GDan.GDanCardType.PAIRS, cards, startIndex, wildList, fakerList)); //对子
                        } else if (i == 3) {
                            return (new GDanDisCards(GDan.GDanCardType.TRIPLES, cards, startIndex, wildList, fakerList));//三张
                        } else if (i <= MAX_COUNT - 1) {
                            GDanDisCards sender = new GDanDisCards(GDan.GDanCardType.BOMB, cards, startIndex, wildList, fakerList); //四张以上是炸弹
                            sender.setNum(i);
                            return (sender);
                        }
                    }
                } else if (i == 3 && arr[i][COUNT] == 1 && arr[i - 1][COUNT] == 1 && count == 2) {
                    return (new GDanDisCards(GDan.GDanCardType.FULL_HOUSES, cards, startIndex, wildList, fakerList)); //三带一对
                }
            }
        }
        return null;
    }

    private static boolean isJokeBoom(List<GDanCard> cards) {
        int jokeCount = 0;
        for (GDanCard card : cards) {
            if (card.getNumber() == GDanCardNumber.GD_JOKE || card.getNumber() == GDanCardNumber.GS_JOKE) {
                jokeCount++;
            }
        }
        return jokeCount == 4;
    }

    private static boolean isSameNum(List<GDanCard> cards) {
        int jokeCount = 0;
        for (GDanCard card : cards) {
            if (card.getNumber() == GDanCardNumber.GD_JOKE || card.getNumber() == GDanCardNumber.GS_JOKE) {
                jokeCount++;
            }
        }
        return jokeCount == 4;
    }

    /**
     * 加入万能牌后形成牌型组
     *
     * @param subList
     * @return
     */
    public static int[][][] getCardsArr(List<GDanCard> subList) {
        int[][][] levelAndNum = new int[2][MAX_COUNT][4];
        int[] levelTable = new int[GDanCardNumber.values().length + 1];  //一次性能打出的最大牌数量
        int[] numTable = new int[GDanCardNumber.values().length + 1];  //一次性能打出的最大牌数量
        for (GDanCard card : subList) {
            if (card.isLevelCard()) {
                levelTable[card.getLevel()]++; //极牌
            }
            levelTable[card.getLevel(false)]++; //每种牌的数量
            numTable[card.getNumber().num()]++;
        }
        int level = GDanCardNumber.GL.getLevel(); //极牌的索引
        int[][] levelArr = getLevelNumArr(levelTable, level, MAX_COUNT);//最大单牌可以10炸
        int maxNum = GDanCardNumber.G4.num(); //目前数值最多顺子是钢板
        int[][] numArr = getLevelNumArr(numTable, level, maxNum);
        levelAndNum[0] = numArr;
        levelAndNum[1] = levelArr;
        return levelAndNum;
    }


    /**
     * @param table    每种点数牌的个数数组 table[level]
     * @param level    极牌的索引
     * @param maxCount 最大的顺子(3个) 333444
     * @return arr[num][index]
     * num：点数牌，最多10张牌的炸弹
     * index： [0]：个数 [1]：起始位置 [2]：终点位置 [3]：极牌位置
     */
    public static int[][] getLevelNumArr(int[] table, int level, int maxCount) {
        int[][] levelArr = new int[maxCount][4];
        for (int i = 0; i < levelArr.length; i++) {
            for (int j = 0; j < levelArr[i].length; j++) {
                levelArr[i][j] = j == 0 ? 0 : -1;
            }
        }
        for (int levelIndex = 0; levelIndex < table.length; levelIndex++) {
            int value = table[levelIndex];
            if (value != 0) {
                fillCardInfo(levelArr, level, levelIndex, ALL);
                for (int i = 0; i < maxCount; i++) {
                    if (i == value) {
                        fillCardInfo(levelArr, level, levelIndex, value);
                    }
                }
            }
        }
        return levelArr;
    }


    /**
     * 牌的排列数组
     *
     * @param cards
     * @return
     */
    public static Collection<List<GDanCard>> getCardsList(List<GDanCard> cards) {
        Map<Integer, List<GDanCard>> map = new TreeMap<>();
        for (GDanCard danCard : cards) {
            int level = danCard.getLevel();
            List<GDanCard> eachCards = map.computeIfAbsent(level, k -> new ArrayList<>());
            eachCards.add(danCard);
        }
        return map.values();
    }


    private static void fillCardInfo(int[][] arr, int level, int index, int value) {
        if (index == level) {
            arr[value][LEVEL] = index;
        } else {
            arr[value][END] = index;
            arr[value][COUNT]++;
        }
        if (arr[value][START] == -1) {
            arr[value][START] = index;
        }
    }

    /**
     * 相同花色
     *
     * @param cards
     * @return
     */
    public static boolean sameCardSuit(List<GDanCard> cards) {
        if (cards.size() <= 0) {
            return false;
        }
        CardSuit suit = cards.get(0).getSuit();
        for (GDanCard card : cards) {
            if (card.getSuit() != suit) {
                return false;
            }
        }
        return true;
    }

    /**
     * 相同点数
     *
     * @param cards
     * @return
     */
    public static boolean sameCardNum(List<GDanCard> cards) {
        if (cards.size() <= 0) {
            return false;
        }
        GDanCardNumber number = cards.get(0).getNumber();
        for (GDanCard card : cards) {
            if (card.getNumber() != number) {
                return false;
            }
        }
        return true;
    }

    public static List<GDanCard> getShuffleCards(int start, int end) {
        List<GDanCard> list = new ArrayList<>();
        Collections.shuffle(list);
        return list.subList(start, end);
    }

    public static List<GDanCard> copy(List<GDanCard> list) {
        List<GDanCard> cards = new ArrayList<>();
        for (GDanCard card : list) {
            GDanCard copy = card.copy(card);
            cards.add(copy);
        }
        return cards;
    }

    /**
     * 得到分数最高的牌型
     *
     * @param cards
     * @param type
     * @return
     */
    public static GDanDisCards getBestGDanDisCards(List<GDanCard> cards, GDan.GDanCardType type) {
        final int BEST = 0;
        Map<Integer, List<GDanDisCards>> cardTypeListMap = getCardTypeListMap(cards);
        if (type == null) {
            for (List<GDanDisCards> value : cardTypeListMap.values()) {
                return value.get(BEST); //取到最大的值
            }
            return null;
        }
        List<GDanDisCards> senderList = cardTypeListMap.get(type.getNumber());
        GDanDisCards bestSender = null;
        if (senderList == null) {
            List<GDanDisCards> boom = cardTypeListMap.get(GDan.GDanCardType.BOMB.getNumber());
            List<GDanDisCards> joke = cardTypeListMap.get(GDan.GDanCardType.JOKER_BOMB.getNumber());
            List<GDanDisCards> flush = cardTypeListMap.get(GDan.GDanCardType.FLUSH_STRAIGHTS.getNumber());
            if (boom == null && joke == null && flush == null) { //一个炸弹都没有
                return null;
            }
            if (boom != null) {
                bestSender = boom.get(BEST);
            }
            if (flush != null) {
                bestSender = flush.get(BEST);
            }
            if (joke != null) {
                bestSender = joke.get(BEST);
            }
        } else if (!senderList.isEmpty()) {
            bestSender = senderList.get(BEST);
        }
        return bestSender;
    }


    /**
     * @param cards
     * @return
     */
    public static Object sendCard(List<GDanCard> cards, GDanDisCards current) {
        GDan.GDanCardType type = current == null ? null : current.getType();
        GDanDisCards bestCardSender = getBestGDanDisCards(cards, type); //获得分值最高的出牌
        if (bestCardSender == null) {
            return GDan.DisCardTip.WRONG_DIS_TYPE_VALUE; //出牌类型不对
        }
        if (current == null) { //主动出牌
            return bestCardSender;
        }
        int currentScore = current.getScore();
        int sendScore = bestCardSender.getScore();
        if (sendScore <= currentScore) { //分值太低
            return GDan.DisCardTip.LOWER_LEVEL_CARD_VALUE; //牌太小
        }
        return bestCardSender; //压牌
    }

    /**
     * @param arr 前端发来的牌组
     * @param map 玩家牌组表
     * @return 玩家牌集合
     */
    public static List<GDanCard> indexArr2List(Integer[] arr, Map<Integer, GDanCard> map) {
        List<GDanCard> cards = new ArrayList<>();
        for (int i : arr) {
            GDanCard danCard = map.get(i);
            if (danCard == null) {
                return null;
            }
            cards.add(danCard);
        }
        return cards;
    }







    public static void addAllSingleDisCards(List<GDanDisCards> disCardsList, List<GDanCard> cards, List<GDanCard> wildList, List<GDanCard> fakerList) {
        int count = 0;
        int lastLevel = -1;
        List<GDanCard> cardList = new ArrayList<>();
        for (int index = 0; index < cards.size(); index++) {
            GDanCard card = cards.get(index);
            int level = card.getLevel();
            if (lastLevel == -1) {
                ++count;
            } else {
                if (level == lastLevel) {
                    ++count;
                } else {
                    count = 1;
                    cardList.clear();
                }
            }
            cardList.add(card);
            if (count == 1) {
                disCardsList.add(new GDanDisCards(GDan.GDanCardType.SINGLE_CARD, new ArrayList<>(cardList), card.getLevel(), wildList, fakerList));
            } else if (count == 2) {
                disCardsList.add(new GDanDisCards(GDan.GDanCardType.PAIRS, new ArrayList<>(cardList), card.getLevel(), wildList, fakerList));
            } else if (count == 3) {
                disCardsList.add(new GDanDisCards(GDan.GDanCardType.TRIPLES, new ArrayList<>(cardList), card.getLevel(), wildList, fakerList));
            } else if (count >= 4) {
                GDanDisCards boom = new GDanDisCards(GDan.GDanCardType.BOMB, new ArrayList<>(cardList), card.getLevel(), wildList, fakerList);
                boom.setNum(count); //设置炸弹的个数，算分时用
                disCardsList.add(boom);
            }
            lastLevel = level;
        }
    }

    public static void addFlushStraight(List<GDanDisCards> disCardsList, List<GDanCard> cards, List<GDanCard> wildList, List<GDanCard> fakerList, CardSuit key) {
        List<GDanDisCards> disCards = new ArrayList<>();
        int count = 0;
        int lastLevel = -1;
        List<GDanCard> cardList = new ArrayList<>(4);
        for (int index = 0; index < cards.size(); index++) {
            GDanCard card = cards.get(index);
            int level = card.getLevel();
            if (lastLevel == -1) {
                ++count;
            } else {
                if (level == lastLevel) {
                    ++count;
                } else {
                    count = 1;
                    cardList.clear();
                }
            }
            cardList.add(card);
            if (count == 1) {
                disCards.add(new GDanDisCards(GDan.GDanCardType.SINGLE_CARD, cardList, card.getLevel(), wildList, fakerList));
            }
            lastLevel = level;
        }
        List<GDanDisCards> list = new ArrayList<>(disCards);
        addStraight(disCards, GDan.GDanCardType.SINGLE_CARD, true, wildList, fakerList);
        addStraight(disCards, GDan.GDanCardType.SINGLE_CARD, false, wildList, fakerList);
        disCards.removeAll(list);
        for (GDanDisCards disCard : disCards) {
            disCard.setCardSuit(key); //设置这手牌的花色
        }
        disCardsList.addAll(disCards);
    }


    /**
     * @param cards 一副手牌
     * @return 所有的牌型
     */
    public static Map<GDan.GDanCardType, List<GDanDisCards>> getAllDisCards(List<GDanCard> cards, List<GDanCard> wildList, List<GDanCard> fakerList) {
        List<GDanDisCards> disCardsList = new ArrayList<>(); //所有牌型列表
        Collections.sort(cards, Comparator.comparingInt(GDanCard::getLevel)); //通过牌值大小排序
        addAllSingleDisCards(disCardsList, cards, wildList, fakerList); //处理所有同点数的牌，包括，单，双，三，炸
        addJokeBoomAndFlushStraight(disCardsList, cards, wildList, fakerList); //处理同花顺和王炸,通过花色区分
        addAllStraight(disCardsList, wildList, fakerList);
        addFullHouse(disCardsList, wildList, fakerList); //三带一对
        return disCardsList.stream().sorted(GDanDisCards::compareTo).collect(Collectors.groupingBy(GDanDisCards::getType));
    }

    private static void addAllStraight(List<GDanDisCards> disCardsList, List<GDanCard> wildCards, List<GDanCard> fakerList) {
        addStraight(disCardsList, GDan.GDanCardType.SINGLE_CARD, true, wildCards, fakerList); //单顺子，A最大
        addStraight(disCardsList, GDan.GDanCardType.PAIRS, true, wildCards, fakerList);  //双顺子 A最大
        addStraight(disCardsList, GDan.GDanCardType.TRIPLES, true, wildCards, fakerList); //三顺子 A最大
        addStraight(disCardsList, GDan.GDanCardType.SINGLE_CARD, false, wildCards, fakerList); //单顺子，A最小
        addStraight(disCardsList, GDan.GDanCardType.PAIRS, false, wildCards, fakerList);//双顺子 A最小
        addStraight(disCardsList, GDan.GDanCardType.TRIPLES, false, wildCards, fakerList);//三顺子 A最小
    }

    /**
     * @param type  牌型
     * @param cards 手牌
     * @param big   最大的
     * @return 通过指定牌型去获取最大/最小的那手牌
     */
    public static List<GDanDisCards> getBestDisCardsList(GDan.GDanCardType type, List<GDanCard> cards, boolean big, List<GDanCard> wildList, List<GDanCard> fakerList) {
        List<GDanDisCards> disCardsList = new ArrayList<>(); //所有牌型列表
        cards.sort(Comparator.comparingInt(GDanCard::getLevel)); //通过牌值大小排序
        addAllSingleDisCards(disCardsList, cards, wildList, fakerList); //处理所有同点数的牌，包括，单，双，三，炸
        if (isSameCardNum(type)) {
        } else if (isStraight(type)) {
            addAllStraight(disCardsList, wildList, fakerList);
        } else if (isFullHouse(type)) {
            addFullHouse(disCardsList, wildList, fakerList);
        } else if (isBoom(type)) {
            addJokeBoomAndFlushStraight(disCardsList, cards, wildList, fakerList);
        }
        Map<GDan.GDanCardType, List<GDanDisCards>> typeListMap = disCardsList.stream().sorted(GDanDisCards::compareTo).collect(Collectors.groupingBy(GDanDisCards::getType));
        List<GDanDisCards> bestDisCardsList = typeListMap.get(type);
        if (bestDisCardsList == null) {
            return null;
        }
        bestDisCardsList.sort((o1, o2) -> (big) ? (o2.getScore() - o1.getScore()) : (o1.getScore() - o2.getScore()));
        return bestDisCardsList;
    }


    /**
     * 同点数的牌,不包括炸弹
     *
     * @param type 牌型
     */
    private static boolean isSameCardNum(GDan.GDanCardType type) {
        return type == GDan.GDanCardType.SINGLE_CARD || type == GDan.GDanCardType.PAIRS || type == GDan.GDanCardType.TRIPLES;
    }

    /**
     * 顺子
     *
     * @param type 牌型
     */
    private static boolean isStraight(GDan.GDanCardType type) {
        return type == GDan.GDanCardType.STRAIGHTS || type == GDan.GDanCardType.TUBES || type == GDan.GDanCardType.PLATES;
    }


    /**
     * 炸弹
     *
     * @param type 牌型
     */
    public static boolean isBoom(GDan.GDanCardType type) {
        return type == GDan.GDanCardType.FLUSH_STRAIGHTS || type == GDan.GDanCardType.BOMB || type == GDan.GDanCardType.JOKER_BOMB;
    }

    /**
     * 三带对
     *
     * @param type 牌型
     */
    public static boolean isFullHouse(GDan.GDanCardType type) {
        return type == GDan.GDanCardType.FULL_HOUSES;
    }


    /**
     * 添加王炸和同花顺
     *
     * @param disCardsList
     * @param fakerList
     */
    public static void addJokeBoomAndFlushStraight(List<GDanDisCards> disCardsList, List<GDanCard> cards, List<GDanCard> wildList, List<GDanCard> fakerList) {
        Map<CardSuit, List<GDanCard>> suitListMap = cards.stream().collect(Collectors.groupingBy(GDanCard::getSuit));
        for (Map.Entry<CardSuit, List<GDanCard>> entry : suitListMap.entrySet()) {
            CardSuit key = entry.getKey();
            List<GDanCard> value = entry.getValue();
            if (key == CardSuit.BLANK) { //处理王炸
                if (value.size() == 4) {
                    disCardsList.add(new GDanDisCards(GDan.GDanCardType.JOKER_BOMB, value, 0, wildList, fakerList));
                }
            } else {
                addFlushStraight(disCardsList, value, wildList, fakerList, key);   //处理同花顺
            }
        }
    }

    private static void addFullHouse(List<GDanDisCards> disCardsList, List<GDanCard> wildList, List<GDanCard> fakerList) {
        for (int index = 0; index < disCardsList.size(); index++) {
            GDanDisCards disCards = disCardsList.get(index);
            if (disCards.getType() == GDan.GDanCardType.TRIPLES) {
                addFullHouse(disCardsList, disCards, 1, GDan.GDanCardType.PAIRS, GDan.GDanCardType.FULL_HOUSES, wildList, fakerList);
            }
        }
    }

    /**
     * 获得三带一对,可扩展
     *
     * @param disCardsList
     * @param disCards
     * @param deep
     * @param type
     * @param targetSellType
     */
    private static void addFullHouse(List<GDanDisCards> disCardsList, GDanDisCards disCards,
                                     int deep, GDan.GDanCardType type,
                                     GDan.GDanCardType targetSellType, List<GDanCard> wildList, List<GDanCard> fakerList) {
        Set<Integer> existLevelSet = new HashSet<>();
        for (GDanCard p : disCards.getCards()) {
            existLevelSet.add(p.getLevel());
        }
        addFullHouse(existLevelSet, disCardsList, new HashSet<>(), disCards, deep, type, targetSellType, wildList, fakerList);
    }

    private static void addFullHouse(Set<Integer> existLevelSet, List<GDanDisCards> pokerSells,
                                     Set<List<GDanCard>> pokersList, GDanDisCards pokerSell,
                                     int deep, GDan.GDanCardType sellType,
                                     GDan.GDanCardType targetSellType, List<GDanCard> wildList, List<GDanCard> fakerList) {
        if (deep == 0) {
            List<GDanCard> allPokers = new ArrayList<>(pokerSell.getCards());
            for (List<GDanCard> ps : pokersList) {
                allPokers.addAll(ps);
            }
            pokerSells.add(new GDanDisCards(targetSellType, allPokers, pokerSell.getLevel(), wildList, fakerList));
            return;
        }
        for (int index = 0; index < pokerSells.size(); index++) {
            GDanDisCards subSell = pokerSells.get(index);
            if (subSell.getType() == sellType && !existLevelSet.contains(subSell.getLevel())) {
                pokersList.add(subSell.getCards());
                existLevelSet.add(subSell.getLevel());
                addFullHouse(existLevelSet, pokerSells, pokersList, pokerSell, deep - 1, sellType, targetSellType, wildList, fakerList);
                existLevelSet.remove(subSell.getLevel());
                pokersList.remove(subSell.getCards());
            }
        }
    }

    /**
     * 通过单牌，双牌，三牌，得到顺子,连对，钢板
     *
     * @param disCardsList 同牌的列表
     * @param type         牌型
     * @param isBigA       A是否是最大的
     */
    private static void addStraight(List<GDanDisCards> disCardsList, GDan.GDanCardType type, boolean isBigA, List<GDanCard> wildCards, List<GDanCard> fakerList) {
        int minLength = -1; //最小长度
        int width = -1;  //索引宽度
        int maxLength = -1; //最大长度
        GDan.GDanCardType targetSellType = null;
        if (type == GDan.GDanCardType.SINGLE_CARD) { //单牌形成顺子
            minLength = 5;
            maxLength = 5;
            width = 1;
            targetSellType = GDan.GDanCardType.STRAIGHTS;
        } else if (type == GDan.GDanCardType.PAIRS) { //对牌形成三连对
            minLength = 3;
            maxLength = 3;
            width = 2;
            targetSellType = GDan.GDanCardType.TUBES;
        } else if (type == GDan.GDanCardType.TRIPLES) { //三牌形成钢板
            minLength = 2;
            width = 3;
            maxLength = 2;
            targetSellType = GDan.GDanCardType.PLATES;
        }
        int increase = 0;
        int lastLevel = -1;
        List<GDanCard> cards = new ArrayList<>();
        if (isBigA) {
            Collections.sort(disCardsList, Comparator.comparingInt(o -> o.getCards().get(0).getLevel(false))); //通过本身的等级去排序
        } else {
            Collections.sort(disCardsList, Comparator.comparingInt(o -> o.getCards().get(0).getNum()));//作为小A时，通过数字去排序
        }
        for (int index = 0; index < disCardsList.size(); index++) {
            GDanDisCards disCards = disCardsList.get(index);
            if (disCards.getType() == type) {
                int level = (isBigA ? disCards.getCards().get(0).getLevel(false) : disCards.getCards().get(0).getNum()); //级别
                if (lastLevel == -1) {
                    ++increase;
                } else {
                    if (level - 1 == lastLevel && level < (isBigA ? GDanCardNumber.GL.getLevel() : GDanCardNumber.G6.num())) {
                        ++increase;
                    } else {
                        doAddStraight(disCardsList, isBigA, minLength, width, maxLength, targetSellType, increase, cards, wildCards, fakerList);
                        increase = 1;
                        cards.clear();
                    }
                }
                cards.addAll(disCards.getRealCards());
                lastLevel = level;
            }
        }
        doAddStraight(disCardsList, isBigA, minLength, width, maxLength, targetSellType, increase, cards, wildCards, fakerList);
    }

    private static void doAddStraight(List<GDanDisCards> disCardsList,
                                      boolean isBigA, int minLength, int width,
                                      int maxLength, GDan.GDanCardType targetSellType,
                                      int increase, List<GDanCard> cards, List<GDanCard> wildList, List<GDanCard> fakerList) {
        if (increase >= minLength) {
            for (int s = 0; s <= increase - minLength; s++) {
                int len = minLength + s;
                for (int subIndex = 0; subIndex <= increase - len; subIndex++) {
                    int start = subIndex * width;
                    int end = (subIndex + len) * width;
                    if ((end - start) / width > maxLength) { //当末尾索引-开始索引 > 最大的长度
                        continue;
                    }
                    List<GDanCard> cardList = new ArrayList<>(cards.subList(start, end));
                    int disCardsLevel = isBigA ? cardList.get(0).getLevel(false) : cardList.get(0).getNum();
                    if (sameCardSuit(cardList)) {
                        disCardsList.add(new GDanDisCards(GDan.GDanCardType.FLUSH_STRAIGHTS, cardList, disCardsLevel, wildList, fakerList));
                    } else {
                        disCardsList.add(new GDanDisCards(targetSellType, cardList, disCardsLevel, wildList, fakerList));
                    }
                }
            }
        }
    }

    /**
     * @param danDisCards 上次出牌
     * @param cards       玩家手牌
     * @return 通过一次出牌，获取最小的出牌
     */
    public static GDanDisCards getBestDisCard(GDanDisCards danDisCards, List<GDanCard> cards) {
        List<GDanDisCards> bestDisCard = getBestDisCardList(danDisCards, cards, false);
        if (bestDisCard != null) {
            return bestDisCard.get(0);
        }
        return null;
    }


    public static List<GDanDisCards> getBestDisCardList(GDanDisCards danDisCards, List<GDanCard> cards, boolean big) {
        List<GDanDisCards> disCardsList = new ArrayList<>();
        GDan.GDanCardType type = null;
        if (danDisCards != null) {
            type = danDisCards.getType();
        }
        List<GDanCard> wildCards = new ArrayList<>();
        for (GDanCard card : cards) {
            if (card.isWildCard()) {
                wildCards.add(card);
            }
        }
        List<GDanCard> cardsList = new ArrayList<>(cards);
        int wildCardsSize = wildCards.size();
        cardsList.removeAll(wildCards);
        if (wildCardsSize > 0&&type!=null) {
            List<List<GDanCard>> lists = getCombinationSelect(wildCardsSize); //两张万能牌的组合
            for (List<GDanCard> list : lists) {
                cardsList.addAll(list); //先添加
                List<GDanDisCards> bestDisCardsList = getBestDisCardsList(type, cardsList, big, wildCards, list);
                if (bestDisCardsList != null) {
                    for (GDanDisCards disCards : bestDisCardsList) {
                        if (disCards.getScore() > danDisCards.getScore()) { //将所有分高的都添加进去

                            disCardsList.add(disCards);
                        }
                    }
                }
                cardsList.removeAll(list);
            }
        } else {
            GDan.GDanCardType[] values = GDan.GDanCardType.values();
            if (type == null) {
                Map<GDan.GDanCardType, List<GDanDisCards>> allDisCards = getAllDisCards(cardsList, null, null);
                for (GDan.GDanCardType value : GDan.GDanCardType.values()) {
                    List<GDanDisCards> disCards = allDisCards.get(value);
                    if (disCards!=null)
                    disCardsList.addAll(disCards);
                }
            } else {
                for (GDan.GDanCardType cardType : values) {
                    if (isBoom(cardType) || type == cardType) {
                        List<GDanDisCards> bestDisCardsList = getBestDisCardsList(type, cards, big, null, null);
                        if (bestDisCardsList != null) {
                            for (GDanDisCards disCards : bestDisCardsList) {
                                if (disCards.getScore() > danDisCards.getScore()) {
                                    disCardsList.add(disCards);
                                }
                            }
                        }
                    }
                }
            }
        }
        return disCardsList;
    }


    /**
     * @param type
     * @param cards
     * @param big
     * @return 根据牌型获得所有的可以出的牌
     */
    public static List<GDanDisCards> getDisCardsListByType(GDan.GDanCardType type, List<GDanCard> cards, boolean big) {
        List<GDanCard> wildCards = getWildList(cards);
        List<GDanDisCards> typeList = new ArrayList<>();
        List<GDanCard> cardsList = new ArrayList<>(cards);
        int wildCardsSize = wildCards.size();
        cardsList.removeAll(wildCards); //先将万能牌移除出去
        if (wildCardsSize > 0) {
            List<List<GDanCard>> lists = getCombinationSelect(wildCardsSize); //两张万能牌的组合
            for (List<GDanCard> list : lists) {
                cardsList.addAll(list); //先添加
                List<GDanDisCards> bestDisCardsList = getBestDisCardsList(type, cardsList, big, wildCards, list);
                if (bestDisCardsList != null) {
                    typeList.addAll(bestDisCardsList);
                }
                cardsList.removeAll(list); //先移除
            }
        } else {
            List<GDanDisCards> bestDisCardsList = getBestDisCardsList(type, cards, big, null, null);
            if (bestDisCardsList != null) {
                typeList.addAll(bestDisCardsList);
            }
        }
        return typeList;
    }

    private static List<GDanCard> getWildList(List<GDanCard> cards) {
        List<GDanCard> wildCards = new ArrayList<>();
        for (GDanCard card : cards) {
            if (card.isWildCard()) {
                wildCards.add(card);
            }
        }
        return wildCards;
    }

}
