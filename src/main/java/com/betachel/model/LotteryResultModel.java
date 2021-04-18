package com.betachel.model;

import java.io.Serializable;

/**
 * @author shiqiu
 * @date 2021/04/18
 */
public class LotteryResultModel  implements Serializable {
    /**
     * 摇号标题
     */
    private String lotteryTitle;
    /**
     * 摇号顺序，选房顺序
     */
    private int lotteryOrder;
    /**
     * 登记顺序
     */
    private int registerOrder;
    /**
     * 登记文本
     */
    private String registerText;
    /**
     * 开盘建筑名
     */
    private String buildingName;
    /**
     * 公布时间
     */
    private String publicityDate;

    public String getLotteryTitle() {
        return lotteryTitle;
    }

    public void setLotteryTitle(String lotteryTitle) {
        this.lotteryTitle = lotteryTitle;
    }

    public int getLotteryOrder() {
        return lotteryOrder;
    }

    public void setLotteryOrder(int lotteryOrder) {
        this.lotteryOrder = lotteryOrder;
    }

    public int getRegisterOrder() {
        return registerOrder;
    }

    public void setRegisterOrder(int registerOrder) {
        this.registerOrder = registerOrder;
    }

    public String getRegisterText() {
        return registerText;
    }

    public void setRegisterText(String registerText) {
        this.registerText = registerText;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getPublicityDate() {
        return publicityDate;
    }

    public void setPublicityDate(String publicityDate) {
        this.publicityDate = publicityDate;
    }
}
