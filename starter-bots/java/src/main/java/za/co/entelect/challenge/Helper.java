package za.co.entelect.challenge;

import za.co.entelect.challenge.entities.Car;
import za.co.entelect.challenge.entities.GameState;
import za.co.entelect.challenge.entities.Lane;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.List;

import static java.lang.Math.min;

public class Helper {
    public int min3(int a, int b, int c) {
        return (min(a, min(b, c)));
    }
    public Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

    public int Obstacles(List<Object> Lane) {
        int count = 0;
        for (int i = 0; i < Lane.size(); i++) {
            if (Lane.get(i).equals(Terrain.MUD) ||
                    Lane.get(i).equals(Terrain.OIL_SPILL)){
                count++;
            } else if (Lane.get(i).equals(Terrain.WALL)){
                count += 10;
            }
        }
        return count;
    }

    public int compareLanes(Car myCar, List<Object> left, List<Object> curr, List<Object> right){
        boolean LPos = false; boolean RPos = false;
        int lCount = 100, rCount = 100, cCount = 100; //asumsi isinya wall semua

        if (myCar.position.lane > 1) {
            lCount = Obstacles(left);
            LPos = true;
        }
        if (myCar.position.lane < 4){
            rCount = Obstacles(right);
            RPos = true;
        }

        cCount = Obstacles(curr);

        if (LPos && RPos) {
            if (min3(lCount, rCount, cCount) == lCount){
                if (lCount == cCount){
                    if (lCount == rCount){
                        return 15;
                    } else {
                        return 5;
                    }
                } else {
                    return -1;
                }
            } else if (min3(lCount, rCount, cCount) == cCount){
                if (cCount == rCount){
                    return 10;
                } else {
                    return 0;
                }
            } else {
                return 1;
            }
        } else if (LPos && !RPos){
            if (min(lCount, cCount) == lCount){
                if (lCount == cCount){
                    return 5;
                } else {
                    return -1;
                }
            } else {
                return 0;
            }
        } else if (!LPos && RPos){
            if (min(rCount, cCount) == rCount){
                if (rCount == cCount){
                    return 10;
                } else {
                    return 1;
                }
            } else {
                return 0;
            }
        }
        return 0;
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


    public int LaneBlock (Car myCar, String direction, GameState gameState) {
        int flag = 0;
        if (direction.equals("LEFT")){
            flag = -1;
        } else if (direction.equals("RIGHT")){
            flag = 1;
        }
        List<Lane[]> map = gameState.lanes;
        Lane[] laneList = map.get(myCar.position.lane - 1 + flag); // tidak dikurangi 1 soalnya dia basisnya 0 (-1) dan dia ke kanan (+1)
        int landingPosition = myCar.speed; // harus ngecek ini basis 0 atau engga
        if (laneList[landingPosition].terrain == Terrain.BOOST) {
            return 1;
        } else if (laneList[landingPosition].terrain == Terrain.LIZARD) {
            return 2;
        } else if (laneList[landingPosition].terrain == Terrain.TWEET) {
            return 3;
        } else if (laneList[landingPosition].terrain == Terrain.OIL_POWER) {
            return 4;
        } else if (laneList[landingPosition].terrain == Terrain.EMP) {
            return 5;
        } else {
            return 9;
        }
    }

    public int accelerateLaneBlock (Car myCar, GameState gameState) {
        List<Lane[]> map = gameState.lanes;
        Lane[] laneList = map.get(myCar.position.lane - 1); // dikurangi 1 soalnya dia basisnya 0
        int landingPosition = myCar.position.block +  nextSpeedState(myCar); // harus ngecek ini basis 0 atau engga
        if (laneList[landingPosition].terrain == Terrain.BOOST) {
            return 1;
        } else if (laneList[landingPosition].terrain == Terrain.LIZARD) {
            return 2;
        } else if (laneList[landingPosition].terrain == Terrain.TWEET) {
            return 3;
        } else if (laneList[landingPosition].terrain == Terrain.OIL_POWER) {
            return 4;
        } else if (laneList[landingPosition].terrain == Terrain.EMP) {
            return 5;
        } else {
            return 9;
        }
    }

}
