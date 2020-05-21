package com.hb.module.guandan.card;

import java.util.StringJoiner;

public enum GDanCardNumber {
    GA(1, "A", 13),
    G2(2, "2", 1),
    G3(3, "3", 2),
    G4(4, "4", 3),
    G5(5, "5", 4),
    G6(6, "6", 5),
    G7(7, "7", 6),
    G8(8, "8", 7),
    G9(9, "9", 8),
    G10(10, "10", 9),
    GJ(11, "J", 10),
    GQ(12, "Q", 11),
    GK(13, "K", 12),
    GL(14, "L", 14), //极牌
    GS_JOKE(15, "joker", 15),
    GD_JOKE(16, "JOKER", 16);
    /**
     * 数字
     */
    private int num;
    /**
     * 描述
     */
    private String desc;
    /**
     * 单牌大小
     */
    private int level;

    GDanCardNumber(int num, String desc, int level) {
        this.num = num;
        this.desc = desc;
        this.level = level;
    }

    public int num() {
        return num;
    }

    public String desc() {
        return desc;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("");
        joiner.add(desc);
        return joiner.toString();
    }
}