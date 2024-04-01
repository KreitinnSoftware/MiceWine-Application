package com.micewine.emu.models;

public class GameList {
    private int titleGame;
    private int GameImage;

    public GameList(int titleGame, int GameImage) {
        this.GameImage = GameImage;
        this.titleGame = titleGame;
    }

    public int getTitleGame() {
        return titleGame;
    }

    public void setTitleGame(int titleGame) {
        this.titleGame = titleGame;
    }

    public int getImageGame() {
        return GameImage;
    }

    public void setImageGame(int GameImage) {
        this.GameImage = GameImage;
    }


}
