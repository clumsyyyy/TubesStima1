package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.Car;
import za.co.entelect.challenge.entities.GameState;
import za.co.entelect.challenge.entities.Lane;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.ArrayList;
import java.util.List;
import static java.lang.Math.max;

// INISIALISASI CLASS YANG MENGANDUNG FUNGSI-FUNGSI HELPER
public class Helper {
    private final Car myCar;
    private final GameState gameState;
    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);
    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command USE_BOOST = new BoostCommand();

    // CONSTRUCTOR
    public Helper(Car myCar, GameState gameState) {
        this.myCar = myCar;
        this.gameState = gameState;
    }

    // fungsi untuk mengembalikan list of blocks
    public List<Object> getBlocksInFront(int lane, int block, int speed) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        Lane[] laneList = map.get(lane - 1);

        int startBlock = map.get(0)[0].position.block;

        for (int i = max(block - startBlock, 0); i <= block - startBlock + 15 ; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }
            blocks.add(laneList[i].terrain);
        }
        return blocks;
    }

    // fungsi validasi power-up ada di list power-up mobil
    public Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

    // fungsi untuk menghitung damage total dari rintangan

    public int Obstacles(List<Object> Lane, int flag) {
        int count = 0;
        for (int i = 0; i < Lane.size(); i++) {
            if (Lane.get(i).equals(Terrain.MUD) ||
                    Lane.get(i).equals(Terrain.OIL_SPILL)){
                count++;
            } else if (Lane.get(i).equals(Terrain.WALL)){
                count += 2;
            }
        }
        if (hasCyberTruck(flag) >= 0) {
            count += 2;
        }
        return count;
    }

    // fungsi untuk mengembalikan string untuk pemilihan avoiding
    public String compareLanes(){
        boolean LPos = false; boolean RPos = false;
        if (myCar.position.lane != 1) {
            LPos = true;
        }
        if (myCar.position.lane != 4){
            RPos = true;
        }
        if (LPos){
            return (RPos ? "ALL" : "CURR_LEFT");
        } else {
            return (RPos ? "CURR_RIGHT" : "DEFAULT");
        }
    }

    // fungsi untuk memprediksi lokasi berhentinya mobil, digunakan
    // sebagai pertimbangan penggunaan fungsi lizard
    public int obstacleLandingBlock(List<Object> pNextBlock) {
        int landingPosition = 0;
        if (myCar.speed == 15){
            landingPosition = pNextBlock.size() - 1;
        } else {
            landingPosition = myCar.speed - 1;
        }
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

    // fungsi untuk menghitung jumlah power-ups dalam suatu lane, digunakan untuk
    // membandingkan jumlah power-ups antar lane

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

    // fungsi yang mengembalikan integer yang menandai adanya cybertruck dalam lane
    // return -1 = tidak ada, return -999 = lane tidak bisa diakses, lainnya = ada cybertruck
    public int hasCyberTruck(int flag) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;
        int lane = this.myCar.position.lane + flag;
        int block = this.myCar.position.block;

        if (lane + flag <= 4 && lane + flag >= 1) {
            Lane[] laneList = map.get(lane - 1);
            for (int i = max(block - startBlock, 0); i <= block - startBlock + 15; i++) {
                if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                    break;
                }
                if (laneList[i].isOccupiedByCyberTruck) {
                    return i;
                }
            }
            return -1;
        } else {
            return -999;

        }
    }

    // fungsi untuk membandingkan dua lane murni dari jumlah obstaclenya
    public Command compareTwoLanes(List<Object> CenterLane, List<Object> CompLane, int flag, int trackLength) {
        int Ccount = Obstacles(CenterLane, 0);
        int Pcount = Obstacles(CompLane, flag);

        if (Pcount < Ccount) {
            if (flag == -1) {
                return TURN_LEFT;
            } else {
                return TURN_RIGHT;
            }
        } else {
            if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
                return USE_BOOST;
            } else {
                return ACCELERATE;
            }
        }
    }

    // fungsi untuk melihat speed tertinggi yang dapat diakses
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

    // fungsi untuk mengembalikan speed state selanjutnya
    // apabila accelerate / boost
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
}
