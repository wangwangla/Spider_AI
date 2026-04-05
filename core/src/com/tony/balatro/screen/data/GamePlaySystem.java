package com.tony.balatro.screen.data;

public class GamePlaySystem {
    private static GamePlaySystem gamePlaySystem;

    private GamePlaySystem() {
    }

    public static GamePlaySystem getGamePlaySystem() {
        if (gamePlaySystem == null) {
            gamePlaySystem = new GamePlaySystem();
        }
        return gamePlaySystem;
    }
}
