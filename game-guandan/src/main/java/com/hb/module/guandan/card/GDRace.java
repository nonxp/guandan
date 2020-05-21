//package com.hb.module.guandan.card;
//
//public class GDRace {
//
//    public enum GDRank {
//        BANKER(1, "上游"),
//        FOLLOWER(2, "二游"),
//        THIRD(3, "三游"),
//        DWELLER(4, "末游");
//        private int type;
//        private String desc;
//
//        GDRank(int type, String desc) {
//            this.type = type;
//            this.desc = desc;
//        }
//
//        public int getType() {
//            return type;
//        }
//
//        public void setType(int type) {
//            this.type = type;
//        }
//
//        public String getDesc() {
//            return desc;
//        }
//
//        public void setDesc(String desc) {
//            this.desc = desc;
//        }
//    }
//
//    public enum GDCampResult {
//        DOUBLE_BANKER(1, "双上游"),
//        DOUBLE_DWELLER(2, "双下游"),
//        SHINGLE_BANKER(3, "单上"),
//        SHINGLE_DWELLER(4, "单下"),
//        TIE(5, "平局");
//        private int code;
//        private String desc;
//
//        GDCampResult(int code, String desc) {
//            this.code = code;
//            this.desc = desc;
//        }
//
//        public int code() {
//            return code;
//        }
//
//        public String desc() {
//            return desc;
//        }
//    }
//
//    public enum GDCamp {
//        RED(1, "红方"),
//        BLUE(2, "蓝方");
//        int code;
//        String desc;
//
//        GDCamp(int code, String desc) {
//            this.code = code;
//            this.desc = desc;
//        }
//    }
//
//    /**
//     * 进贡的牌
//     */
//    public static class TributeCard implements Comparable<TributeCard> {
//        private GDRank userResult; //标识玩家
//        private GDanCard card; //卡牌
//
//        public TributeCard(GDRank userResult, GDanCard card) {
//            this.userResult = userResult;
//            this.card = card;
//        }
//
//        public GDRank getUserResult() {
//            return userResult;
//        }
//
//        public void setUserResult(GDRank userResult) {
//            this.userResult = userResult;
//        }
//
//        public GDanCard getCard() {
//            return card;
//        }
//
//        public void setCard(GDanCard card) {
//            this.card = card;
//        }
//
//        /**
//         * 通过牌值进行排序
//         */
//        @Override
//        public int compareTo(TributeCard o) {
//            return Integer.compare(card.getLevel(), o.getCard().getLevel());
//        }
//    }
//
//}
