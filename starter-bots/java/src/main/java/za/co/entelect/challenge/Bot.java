package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Bot {

    // ========== INISIALISASI VARIABEL PRIVATE ==========
    private static final int maxSpeed = 9;
    private static final int trackLength = 500;
    private Random random;
    private GameState gameState;
    private Car opponent;
    private Car myCar;
    private List<Command> directionList = new ArrayList<>();

    // inisialisasi commands sesuai urutan di game-rules.md

    // nothing / accel / decel
    private final static Command NOTHING = new DoNothingCommand();
    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command DECELERATE = new DecelerateCommand();

    // belok kiri / kanan
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);
    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);

    // power ups
    // [IMPORTANT] instance tweet gapunya default constructor,
    // jadi ngecallnya pas mau pake aja

    private final static Command USE_BOOST = new BoostCommand();
    private final static Command USE_OIL = new OilCommand();
    private final static Command USE_LIZARD = new LizardCommand();
    private final static Command USE_EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();

    // ini bagian public
    // user-defined constructor
    public Bot(Random random, GameState gameState) {
        this.random = random;
        this.gameState = gameState;
        this.myCar = gameState.player;
        this.opponent = gameState.opponent;

        directionList.add(TURN_LEFT);
        directionList.add(TURN_RIGHT);
    }


    // TODO: algo ganti lane kalau mau dapetin powerup (KERJAAN GEDE) + fungsi warning
    public Command run() {
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;
        //inisialisasi list blocks yang bisa dilihat di depan mobil kita
        //ket. p = player, o = opponent
        List <Object> pNextBlocks;
        List <Object> currentLane = getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed);
        if (currentLane.size() < min(myCar.speed, trackLength - myCar.position.block + 1)) {
            pNextBlocks = currentLane.subList(0, min(myCar.speed, trackLength - myCar.position.block + 1));
        } else {
            pNextBlocks = currentLane;
        }
        List <Object> leftLane = getBlocksInFront(myCar.position.lane - 1, myCar.position.block, myCar.speed);
        List <Object> rightLane = getBlocksInFront(myCar.position.lane + 1, myCar.position.block, myCar.speed);
        List <Object> boostLane = getBlocksInFront(myCar.position.lane, myCar.position.block, 15);
        List <Object> accelLane = getBlocksInFront(myCar.position.lane, myCar.position.block, nextSpeedState(myCar));

        // implementasi algoritma kalau di depan lane kosong / tidak ada obstacle

        if (Obstacles(currentLane) == 0){
            if (isInSameLane(myCar, opponent)){

            }
        }
        // kalau damage mobil >= 5, langsung baikin
        // karena kalo damage >= 5, mobil langsung gabisa gerak
        if (myCar.damage >= 4){
            return FIX;
        }
        if (myCar.speed <= 3){
            return ACCELERATE;
        }

        if (Obstacles(currentLane) <= 1) {
            if (hasPowerUp(PowerUps.BOOST, myCar.powerups)){
                return USE_BOOST;
            }
        }

        // algoritma sederhana pengecekan apakah ada mud di depan / ada wall di depan
        // .contains(ELMT) dipake untuk tau apakah di dalem list ada ELMT tersebut
        if (pNextBlocks.contains(Terrain.MUD) || pNextBlocks.contains(Terrain.WALL) || pNextBlocks.contains(Terrain.OIL_SPILL)){
            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)){
                return USE_LIZARD;
            } else if (!pNextBlocks.contains(Terrain.WALL) && myCar.damage <= 3 && passThroughPowUp(pNextBlocks, PowerUps.BOOST)) {
                return ACCELERATE;
            } else {
                if (myCar.position.lane == 1){          //kalau misalnya di lane 1, turn right biar ga minus
                    return compareTwoLanes(1);
                } else if (myCar.position.lane == 4){   //kalau misalnya di lane 4, turn left biar ga minus
                    return compareTwoLanes(-1);
                } else {                                //kalau misalnya ngga di situ, bebas
                    return compareObstacles();
                    //return directionList.get(random.nextInt(directionList.size()));
                }
            }
        }

        //  TODO: =============== IMPLEMENTASI ALGO GEDE ===============
        // boost kalo nganggur dan di depan free

        // algo tweet, kalau misalnya powerup on dan lane musuhnya gada apa", kita ganggu
        if (hasPowerUp(PowerUps.TWEET, myCar.powerups)){
            return new TweetCommand(opponent.position.lane, opponent.position.block);
        }

        // kalau lane mobil kita sama dengan len musuh dan kita punya oil, pake
        if (myCar.position.lane == opponent.position.lane && hasPowerUp(PowerUps.OIL, myCar.powerups)
                && myCar.position.block > opponent.position.block){
            return USE_OIL;
        }

        // algo emp
        if (hasPowerUp(PowerUps.EMP, myCar.powerups)){
            if (opponent.position.lane <= myCar.position.lane + 1 && opponent.position.lane >= myCar.position.lane - 1
                    && opponent.position.block > myCar.position.block){
                return USE_EMP;
            } else {
                // TODO: benahin algo ini biar ga sembarang belok
                if (myCar.position.lane == 1){          //kalau misalnya di lane 1, turn right biar ga minus
                    return compareTwoLanes(1);
                } else if (myCar.position.lane == 4){   //kalau misalnya di lane 4, turn left biar ga minus
                    return compareTwoLanes(-1);
                } else {                                //kalau misalnya ngga di situ, bebas
                    //return directionList.get(random.nextInt(directionList.size()));
                    return compareObstacles();
                    //bisa dicoba ganti pake compareObstacles()
                }
            }
        }
        // kalo di depan ga ada masalah apa-apa
        // g a s
        return ACCELERATE;
    }



    // ========== INISIALISASI FUNGSI HELPER DI SINI ==========
    /**
     * Returns map of blocks and the objects in the for the current lanes, returns the amount of blocks that can be
     * traversed at max speed.
     **/
    private List<Object> getBlocksInFront(int lane, int block, int speed) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + speed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);
        }
        return blocks;
    }
    // fungsi untuk mengecek apakah powerup yang mau dipakai ada di list

    private Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

    private int Obstacles(List<Object> Lane) {
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
    // membandingkan obstacles dari 3 lane
    // CALL KALAU DIA GA DI LANE 1 ATAU 4
    private Command compareObstacles(){
        int Lcount = Obstacles(getBlocksInFront(myCar.position.lane - 1, myCar.position.block, myCar.speed - 1)
                .subList(0, min(myCar.speed, trackLength - myCar.position.block + 1)));
        int Ccount = Obstacles(getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed - 1)
                .subList(0, min(myCar.speed, trackLength - myCar.position.block + 1)));
        int Rcount = Obstacles(getBlocksInFront(myCar.position.lane + 1, myCar.position.block, myCar.speed - 1)
                .subList(0, min(myCar.speed, trackLength - myCar.position.block + 1)));

        if (Ccount < Lcount && Ccount < Rcount) {
            if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
                return USE_BOOST;
            } else {
                return ACCELERATE;
            }
        } else if (Rcount < Ccount && Rcount < Lcount) {
            return TURN_RIGHT;
        } else if (Lcount < Ccount && Lcount < Rcount) {
            return TURN_LEFT;
        } else {
            return ACCELERATE;
        }
    }
    // membandingkan obstacles 2 lane (kiri/kanan, flag = -1 berarti kiri, flag = +1 berarti kanan)
    private Command compareTwoLanes(int flag){
        int Ccount = Obstacles(getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed)
                .subList(0, min(myCar.speed, trackLength - myCar.position.block + 1)));
        int Pcount = Obstacles(getBlocksInFront(myCar.position.lane + flag, myCar.position.block, myCar.speed - 1)
                .subList(0, min(myCar.speed, trackLength - myCar.position.block + 1)));

        if (Pcount < Ccount){
            if (flag == -1){
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

    // return apakah blocks yang akan dilewati ronde itu mengandung objek yg kita cari
    private boolean passThroughPowUp(List<Object> Lane, PowerUps powerUp) {
        int i = 0;
        boolean found = false;
        while (i < myCar.speed && !found) {
            if (Lane.get(i).equals(powerUp)) {
                found = true;
            } else {
                i += 1;
            }
        }
        return found;
    }

    private int nextSpeedState (Car targetCar) {
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

    private int prevSpeedState (Car targetCar) {
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

    private int carGap(Car myCar, Car targetCar){
        return Math.abs(myCar.position.block - targetCar.position.block);
    }
    private boolean isInSameLane(Car myCar, Car targetCar){
        return myCar.position.lane == targetCar.position.lane;
    }

    private boolean isInEMPRange(Car myCar, Car opponent){
        return Math.abs(myCar.position.block - opponent.position.block) <= 1;
    }

    private int LaneBlock (Car myCar, String direction) {
        int flag = 0;
        if (direction == "LEFT"){
            flag = -1;
        } else if (direction == "RIGHT"){
            flag = 1;
        } else if (direction == "CENTER"){
            flag = 0;
        }
        List<Lane[]> map = gameState.lanes;
        Lane[] laneList = map.get(myCar.position.lane - 1 + flag); // tidak dikurangi 1 soalnya dia basisnya 0 (-1) dan dia ke kanan (+1)
        int landingPosition = myCar.position.block + myCar.speed; // harus ngecek ini basis 0 atau engga
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

    private int accelerateLaneBlock (Car myCar) {
        List<Lane[]> map = gameState.lanes;
        Lane[] laneList = map.get(myCar.position.lane - 1); // dikurangi 1 soalnya dia basisnya 0
        int landingPosition = myCar.position.block + nextSpeedState(myCar); // harus ngecek ini basis 0 atau engga
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

    private boolean canGoLeft (Car myCar) {
        return (myCar.position.lane > 1);
    }

    private boolean canGoRight (Car myCar) {
        return (myCar.position.lane < 4);
    }

    private int min3(int a, int b, int c) {
        return (min(a, min(b, c)));
    }

    private int compareLanes(Car myCar, List<Object> left, List<Object> curr, List<Object> right){
        boolean LPos = false; boolean RPos = false;
        int lCount = 100, rCount = 100, cCount = 100; //asumsi isinya wall semua

        if (canGoLeft(myCar)) {
            lCount = Obstacles(left);
            LPos = true;
        }
        if (canGoRight(myCar)){
            rCount = Obstacles(right);
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
}
