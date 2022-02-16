package za.co.entelect.challenge;

import za.co.entelect.challenge.entities.Car;
import za.co.entelect.challenge.entities.GameState;
import za.co.entelect.challenge.entities.Lane;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.Math.min;
import static java.lang.Math.max;

public class Helper {
    private final Car myCar;
    private final GameState gameState;

    public Helper(Car myCar, GameState gameState) {
        this.myCar = myCar;
        this.gameState = gameState;
    }

    public int min3(int a, int b, int c) {
        return (min(a, min(b, c)));
    }

    public int max3(int a, int b, int c){
        return (max(a, max(b, c)));
    }


    public Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

    public int Obstacles(List<Object> Lane, int flag) {
        int count = 0;
        for (int i = 0; i < Lane.size(); i++) {
            if (Lane.get(i).equals(Terrain.MUD) ||
                    Lane.get(i).equals(Terrain.OIL_SPILL)){
                count++;
            } else if (Lane.get(i).equals(Terrain.WALL)){
                count += 10;
            }
        }
        if (myCar.position.lane + flag < 0 || myCar.position.lane + flag > 3){
            flag = 0;
        }
        if (hasCyberTruck(flag) != -1) {
            count += 10;
        }

        return count;
    }

    public String compareLanes(Car myCar, List<Object> left, List<Object> curr, List<Object> right){
        boolean LPos = false; boolean RPos = false;

        if (myCar.position.lane > 1) {
            LPos = true;
        }
        if (myCar.position.lane < 4){
            RPos = true;
        }

        if (LPos && RPos){
            return "ALL";
        } else if (LPos && !RPos){
            return "CURR_LEFT";
        } else if (RPos && !LPos){
            return "CURR_RIGHT";
        } else {
            return "STAY";
        }
    }

    public int nextSpeedState (Car targetCar) {
        switch (targetCar.speed) {
            case 0:
                return 3;
            case 3:
            case 5:
                return 6;
            case 6:
                return 8;
            case 8:
                return 9;
            default:
                return targetCar.speed;
        }
    }

    public int prevSpeedState (Car targetCar) {
        switch (targetCar.speed) {
            case 9:
                return 8;
            case 8:
                return 6;
            case 6:
                return 3;
            case 5:
                return 3;
            case 3:
                return 0;
            default:
                return targetCar.speed;
        }
    }

    public int obstacleLandingBlock(List<Object> pNextBlock) {
        // dua versi

        int landingPosition = 0; // versi satunya
        if (myCar.speed > pNextBlock.size()){
            landingPosition = pNextBlock.size() - 1;
        } else {
            landingPosition = myCar.speed;
        }
//        int landingPosition = myCar.speed + 1 ; // harus ngecek ini basis 0 atau engga,kalo getBlocksInFront myCar.speed + 1

        if (myCar.speed > 0) {
            if (pNextBlock.get(landingPosition).equals(Terrain.OIL_SPILL)) {
                return 1;
            } else if (pNextBlock.get(landingPosition).equals(Terrain.MUD)) {
                return 2;
            } else if (pNextBlock.get(landingPosition).equals(Terrain.WALL)) {
                return 3;
            } else {
                return 0;
            }
        } else {
            return  0;
        }
    }

    public int currentMaxSpeed (Car myCar) {
        switch (myCar.damage) {
            case 0:
                return 15;
            case 1:
                return 9;
            case 2:
                return 8;
            case 3:
                return 6;
            case 4:
                return 3;
            case 5:
                return 0;
            default:
                return myCar.speed;
        }
    }

    public int countPowerUps(List<Object> laneList){
        int count = 0;
        for (int i = 0; i < laneList.size(); i++) {
            if (laneList.get(i).equals(Terrain.OIL_POWER) ||
                    laneList.get(i).equals(Terrain.EMP) ||
                    laneList.get(i).equals(Terrain.BOOST) ||
                    laneList.get(i).equals(Terrain.LIZARD) ||
                    laneList.get(i).equals(Terrain.TWEET)) {
                count++;
            }
        }
        return count;
    }

    public int hasCyberTruck(int flag) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;
        int lane = this.myCar.position.lane + flag;
        int block = this.myCar.position.block;
        if (lane - 1 >= 0) {
            Lane[] laneList = map.get(lane - 1);

            // kayanya dia klo car block 5, berarti indeksnya 4, makanya mulai indeks dari block lgsg aja biar
            // mulainya dari depan lgsg.
            for (int i = max(block - startBlock, 0); i <= block - startBlock + myCar.speed; i++) {
//        for (int i = max(block - startBlock, 0); i <= block - startBlock + myCar.speed + 1; i++) {
                if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                    break;
                }
                if (laneList[i].isOccupiedByCyberTruck) {
                    return i;
                }

            }
        }
        return -1;
    }
}
