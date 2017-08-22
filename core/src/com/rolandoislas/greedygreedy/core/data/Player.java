package com.rolandoislas.greedygreedy.core.data;

public class Player {
    private String name;
    private boolean bot;
    private BotType botType;
    private int score = 0;
    private boolean active;
    private int zilchAmount;

    public void setName(String name) {
        this.name = name;
    }

    public void setBot(boolean bot) {
        this.bot = bot;
    }

    public void setBotType(BotType botType) {
        this.botType = botType;
    }

    public BotType getBotType() {
        return botType;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isBot() {
        return bot;
    }

    public int getZilchAmount() {
        return zilchAmount;
    }

    public void setZilchAmount(int zilchAmount) {
        this.zilchAmount = zilchAmount;
    }

    public enum BotType {
        GREEDY(1500, false, "GreedyBot"),
        CAUTIOUS(0, true, "NervousBot"),
        NORMAL(500, true, "GoodEnoughBot"),
        SKILLED(1000, true, "L33tBot"),
        SOME_POINTS(200, true, "SmallBot"),
        HALF_GREEDY(1250, true, "CallMeGreedyBot");

        public final int stopThreshold;
        public final boolean avoidsZilch;
        public final String username;

        BotType(int stopThreshold, boolean avoidsZilch, String username) {
            this.stopThreshold = stopThreshold;
            this.avoidsZilch = avoidsZilch;
            this.username = username;
        }
    }
}
