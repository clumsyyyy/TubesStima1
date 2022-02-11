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
    private Helper h = new Helper();
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


    public Command run() {
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;
        //inisialisasi list blocks yang bisa dilihat di depan mobil kita
        //ket. p = player, o = opponent
        List <Object> pNextBlocks;
        List <Object> leftLane = new ArrayList<Object>();
        List <Object> rightLane = new ArrayList<Object>();
        List <Object> currentLane = getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed);

        if (currentLane.size() >= min(myCar.speed, trackLength - myCar.position.block + 1)) {
            pNextBlocks = currentLane.subList(0, min(myCar.speed, trackLength - myCar.position.block + 1));
        } else {
            pNextBlocks = currentLane;
        }
        if (myCar.position.lane > 1){
            leftLane = getBlocksInFront(myCar.position.lane - 1, myCar.position.block, myCar.speed);
        }
        if (myCar.position.lane < 4){
            rightLane = getBlocksInFront(myCar.position.lane + 1, myCar.position.block, myCar.speed);
        }
        List <Object> boostLane = getBlocksInFront(myCar.position.lane, myCar.position.block, 15);
        List <Object> accelLane = getBlocksInFront(myCar.position.lane, myCar.position.block,  h.nextSpeedState(myCar));

        // implementasi algoritma kalau di depan lane kosong / tidak ada obstacle
        int choice = h.compareLanes(myCar, leftLane, currentLane, rightLane);

        if (myCar.damage >= 4){
            return FIX;
        }

        if (myCar.speed <= 3){
            return ACCELERATE;
        }
        if (h.Obstacles(currentLane) == 0){
            if (myCar.position.lane == opponent.position.lane){
                sameLaneCommand(choice, myCar, opponent, currentLane, pNextBlocks);
            } else {
                diffLaneCommand(choice, myCar, opponent, currentLane, pNextBlocks);
            }
        } else {
            if (myCar.damage >= 4){
                return FIX;
            }

            if (myCar.speed <= 3){
                return ACCELERATE;
            }

            // wall nilainya 10, jadi ini artinya kalau dia ada boost langsung pake biar best case dapet max_speed


            // algoritma sederhana pengecekan apakah ada mud di depan / ada wall di depan
            // .contains(ELMT) dipake untuk tau apakah di dalem list ada ELMT tersebut
            if (pNextBlocks.contains(Terrain.MUD) || pNextBlocks.contains(Terrain.WALL) || pNextBlocks.contains(Terrain.OIL_SPILL)){
                if (h.hasPowerUp(PowerUps.LIZARD, myCar.powerups)){
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
                    }
                }
            }

            if (h.hasPowerUp(PowerUps.BOOST, myCar.powerups) && h.Obstacles(currentLane) < 10) {
                return USE_BOOST;
            }

            return ACCELERATE;
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


    // membandingkan obstacles dari 3 lane
    // CALL KALAU DIA GA DI LANE 1 ATAU 4
    private Command compareObstacles(){
        int Lcount = h.Obstacles(getBlocksInFront(myCar.position.lane - 1, myCar.position.block, myCar.speed - 1)
                .subList(0, min(myCar.speed, trackLength - myCar.position.block + 1)));
        int Ccount = h.Obstacles(getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed - 1)
                .subList(0, min(myCar.speed, trackLength - myCar.position.block + 1)));
        int Rcount = h.Obstacles(getBlocksInFront(myCar.position.lane + 1, myCar.position.block, myCar.speed - 1)
                .subList(0, min(myCar.speed, trackLength - myCar.position.block + 1)));

        if (Ccount < Lcount && Ccount < Rcount) {
            if (h.hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
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
        int Ccount = h.Obstacles(getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed)
                .subList(0, min(myCar.speed, trackLength - myCar.position.block + 1)));
        int Pcount = h.Obstacles(getBlocksInFront(myCar.position.lane + flag, myCar.position.block, myCar.speed - 1)
                .subList(0, min(myCar.speed, trackLength - myCar.position.block + 1)));

        if (Pcount < Ccount){
            if (flag == -1){
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

    private Command sameLaneCommand(int choice, Car myCar, Car opponent, List<Object> currentLane, List<Object> pNextBlocks){
        if (myCar.damage >= 4){
            return FIX;
        }

        if (myCar.speed <= 3){
            return ACCELERATE;
        }

        if (pNextBlocks.contains(Terrain.MUD) || pNextBlocks.contains(Terrain.WALL) || pNextBlocks.contains(Terrain.OIL_SPILL)){
            if (h.hasPowerUp(PowerUps.LIZARD, myCar.powerups)){
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
                }
            }
        }

        if (myCar.boosting){
            return (choice == -1 ? TURN_LEFT : TURN_RIGHT);
        }

        if (h.hasPowerUp(PowerUps.BOOST, myCar.powerups) && h.Obstacles(currentLane) < 10 && !myCar.boosting) {
            return USE_BOOST;
        }

        // algo tweet, kalau misalnya powerup on dan lane musuhnya gada apa", kita ganggu
        if (h.hasPowerUp(PowerUps.TWEET, myCar.powerups)){
            return new TweetCommand(opponent.position.lane, opponent.position.block);
        }

        // kalau lane mobil kita sama dengan len musuh dan kita punya oil, pake
        if (myCar.position.lane == opponent.position.lane && h.hasPowerUp(PowerUps.OIL, myCar.powerups)
                && myCar.position.block > opponent.position.block){
            return USE_OIL;
        }

        return ACCELERATE;
    }


    private Command diffLaneCommand(int choice, Car myCar, Car opponent, List<Object> currentLane, List<Object> pNextBlocks){
        if (myCar.damage >= 4){
            return FIX;
        }
        if (myCar.speed <= 3){
            return ACCELERATE;
        }

        if (pNextBlocks.contains(Terrain.MUD) || pNextBlocks.contains(Terrain.WALL) || pNextBlocks.contains(Terrain.OIL_SPILL)){
            if (h.hasPowerUp(PowerUps.LIZARD, myCar.powerups)){
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
                }
            }
        }

        if (myCar.boosting){
            if (choice == -1) return TURN_LEFT;
            if (choice == 1) return TURN_RIGHT;
        }

        if (h.hasPowerUp(PowerUps.BOOST, myCar.powerups) && h.Obstacles(currentLane) < 10 && !myCar.boosting
                && Math.abs(myCar.position.block - opponent.position.block) <= 20){
            return USE_BOOST;
        }

        if (h.hasPowerUp(PowerUps.TWEET, myCar.powerups)){
            return new TweetCommand(opponent.position.lane, opponent.position.block);
        }

        if (h.hasPowerUp(PowerUps.EMP, myCar.powerups)){
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

        // kalau lane mobil kita sama dengan len musuh dan kita punya oil, pake
        if (myCar.position.lane == opponent.position.lane && h.hasPowerUp(PowerUps.OIL, myCar.powerups)
                && myCar.position.block > opponent.position.block){
            return USE_OIL;
        }

        return ACCELERATE;
    }

    private Command switching(int choice){
        int no_accelerate = h.Obstacles(getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed));
        int with_accelerate = h.Obstacles(getBlocksInFront(myCar.position.lane, myCar.position.block,  h.nextSpeedState(myCar)));
        int leftLandingBlock = h.LaneBlock(myCar, "LEFT", gameState);
        int currLandingBlock = h.LaneBlock(myCar, "CENTER", gameState);
        int rightLandingBlock = h.LaneBlock(myCar, "RIGHT", gameState);

        switch (choice) {
            case -1:
                return TURN_LEFT;
            case 0:
                if (with_accelerate <= no_accelerate && !myCar.boosting) {
                    return ACCELERATE;
                } else {
                    return NOTHING;
                }
            case 1:
                return TURN_RIGHT;
            case 5:
                // bandingin powerup yang ada di kiri dan tengah
                // kalau sama jenisnya, cek dulu mendingan ngebut atau engga
                if (currLandingBlock >= leftLandingBlock) {
                    if (with_accelerate <= no_accelerate && !myCar.boosting) {
                        return ACCELERATE;
                    } else {
                        return NOTHING;
                    }
                } else {
                    return TURN_LEFT;
                }
            case 10:
                // bandingin powerup yang ada di kanan dan tengah
                // kalau sama jenisnya, cek dulu mendingan ngebut atau engga
                if (currLandingBlock >= rightLandingBlock) {
                    if (with_accelerate <= no_accelerate && !myCar.boosting) {
                        return ACCELERATE;
                    } else {
                        return NOTHING;
                    }
                } else {
                    return TURN_RIGHT;
                }
            case 15:
                // bandingin powerup yang ada di kiri dan tengah
                // kalau sama jenisnya, cek dulu mendingan ngebut atau engga
                if (currLandingBlock >= Math.max(leftLandingBlock, rightLandingBlock)) {
                    if (with_accelerate <= no_accelerate && !myCar.boosting) {
                        return ACCELERATE;
                    } else {
                        return NOTHING;
                    }
                } else if (leftLandingBlock > Math.max(currLandingBlock, rightLandingBlock)) {
                    return TURN_LEFT;
                } else if (rightLandingBlock > Math.max(currLandingBlock, leftLandingBlock)) {
                    return TURN_RIGHT;
                }
            default:
                return ACCELERATE;
        }
    }
}
