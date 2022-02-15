package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Comparison {
    // nothing / accel / decel
    private final static Command ACCELERATE = new AccelerateCommand();

    // belok kiri / kanan
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);
    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command USE_BOOST = new BoostCommand();
    private final static Command NOTHING = new DoNothingCommand();

    private Helper h;
    private final GameState gameState;
    private final Car myCar;

    public Comparison(GameState gameState) {
        this.gameState = gameState;
        this.myCar = gameState.player;
        this.h = new Helper(gameState.player, gameState);
    }

    // membandingkan obstacles dari 3 lane
    // CALL KALAU DIA GA DI LANE 1 ATAU 4


    // membandingkan obstacles 2 lane (kiri/kanan, flag = -1 berarti kiri, flag = +1 berarti kanan)
    public Command compareTwoLanes(List<Object> CenterLane, List<Object> CompLane, int flag, int trackLength) {
        int Ccount = h.Obstacles(CenterLane, 0);
        int Pcount = h.Obstacles(CompLane, flag);

        if (Pcount < Ccount) {
            if (flag == -1) {
                return TURN_LEFT;
            } else {
                return TURN_RIGHT;
            }
        } else {
            if (h.hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
                return USE_BOOST;
            } else {
                return ACCELERATE;
            }
        }
    }

    public List<Object> getBlocksInFront(int lane, int block, int speed) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        // kayanya dia klo car block 5, berarti indeksnya 4, makanya mulai indeks dari block lgsg aja biar
        // mulainya dari depan lgsg.
        for (int i = max(block - startBlock, 0); i <= block - startBlock + myCar.speed ; i++) {
//        for (int i = max(block - startBlock, 0); i <= block - startBlock + myCar.speed + 1; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);
    }
        return blocks;
    }
}

