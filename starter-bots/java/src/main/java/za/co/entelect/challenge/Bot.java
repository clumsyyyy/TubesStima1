package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;


public class Bot {

    // ========== INISIALISASI VARIABEL PRIVATE ==========
    private static final int maxSpeed = 9;
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
    // public Command run() {
    //     Car myCar = gameState.player;
    //     Car opponent = gameState.opponent;
    //     //inisialisasi list blocks yang bisa dilihat di depan mobil kita
    //     //ket. p = player, o = opponent

    //     List<Object> pBlocks = getBlocksInFront(myCar.position.lane, myCar.position.block);
    //     List<Object> pNextBlocks = pBlocks.subList(0,1);

    //     // kalau damage mobil >= 5, langsung baikin
    //     // karena kalo damage >= 5, mobil langsung gabisa gerak
    //     if (myCar.damage >= 5){
    //         return FIX;
    //     }
    //     if (myCar.speed <= 3){
    //         return ACCELERATE;
    //     }

    //     if (myCar.damage == 5){
    //         return FIX;
    //     }
    //     // algoritma sederhana pengecekan apakah ada mud di depan / ada wall di depan
    //     // .contains(ELMT) dipake untuk tau apakah di dalem list ada ELMT tersebut
    //     if (pBlocks.contains(Terrain.MUD) || pNextBlocks.contains(Terrain.WALL)){
    //         if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)){
    //             return USE_LIZARD;
    //         }
    //         if (pNextBlocks.contains(Terrain.MUD) || pNextBlocks.contains(Terrain.WALL)){
    //             if (myCar.position.lane == 1){          //kalau misalnya di lane 1, turn right biar ga minus
    //                 return TURN_RIGHT;
    //             } else if (myCar.position.lane == 4){   //kalau misalnya di lane 4, turn left biar ga minus
    //                 return TURN_LEFT;
    //             } else {                                //kalau misalnya ngga di situ, bebas
    //                 return directionList.get(random.nextInt(directionList.size()));
    //             }
    //         }
    //     }

    //     //  TODO: =============== IMPLEMENTASI ALGO GEDE ===============
    //     // boost kalo nganggur dan di depan free
    //     if (hasPowerUp(PowerUps.BOOST, myCar.powerups)){
    //         return USE_BOOST;
    //     }

    //     // algo tweet, kalau misalnya powerup on dan lane musuhnya gada apa", kita ganggu
    //     if (hasPowerUp(PowerUps.TWEET, myCar.powerups)){
    //         return new TweetCommand(opponent.position.lane, opponent.position.block);
    //     }

    //     // kalau lane mobil kita sama dengan len musuh dan kita punya oil, pake
    //     if (myCar.position.lane == opponent.position.lane && hasPowerUp(PowerUps.OIL, myCar.powerups)){
    //         return USE_OIL;
    //     }

    //     // algo emp
    //     if (hasPowerUp(PowerUps.EMP, myCar.powerups)){
    //         if (myCar.position.lane == opponent.position.lane){
    //             return USE_EMP;
    //         } else {
    //             if (myCar.position.lane == 1){          //kalau misalnya di lane 1, turn right biar ga minus
    //                 return TURN_RIGHT;
    //             } else if (myCar.position.lane == 4){   //kalau misalnya di lane 4, turn left biar ga minus
    //                 return TURN_LEFT;
    //             } else {                                //kalau misalnya ngga di situ, bebas
    //                 return directionList.get(random.nextInt(directionList.size()));
    //                 //bisa dicoba ganti pake compareObstacles()
    //             }
    //         }
    //     }
    //     // kalo di depan ga ada masalah apa-apa
    //     // g a s
    //     return ACCELERATE;
    // }

    public Command run(GameState gameState) {
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;
        //inisialisasi list blocks yang bisa dilihat di depan mobil kita
        //ket. p = player, o = opponent

        List<Object> currentLane = getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed);
        List<Object> leftLane = getBlocksInFront(myCar.position.lane - 1, myCar.position.block, myCar.speed - 1);
        List<Object> rightLane = getBlocksInFront(myCar.position.lane + 1, myCar.position.block, myCar.speed - 1);

        List<Object> boostLane = getBlocksInFront(myCar.position.lane, myCar.position.block, 15);
        List<Object> accelerateLane = getBlocksInFront(myCar.position.lane, myCar.position.block, nextSpeedState(myCar));

        if (Obstacles(currentLane) == 0) { // kalau range depan mobil kosong
            if (isSameLane(myCar, opponent)) {
                if (myCar.boosting == true) { // hanya belok kanan atau kiri, kalau pilih lurus mending pake powerup
                    int choice = pickLane(myCar, leftLane, currentLane, rightLane);
                    if (choice == -1) {
                        return TURN_LEFT;
                    } else if (choice == 1) {
                        return TURN_RIGHT;
                    }
                }
                // semua kode dibawah dipakai dengan asumsi booster mati
                if (myCar.damage >= 1 && myCar.damage <= 3 && jarakAntarMobil(myCar, opponent) >= opponent.speed) {
                    return FIX;
                }
                if (myCar.damage > 3) {
                    return FIX;
                }
                if (hasPowerUp(PowerUps.OIL, myCar.powerups)) {
                    return USE_OIL;
                }
                if (hasPowerUp(PowerUps.TWEET, myCar.powerups)) {
                    return new TweetCommand(opponent.position.lane, opponent.position.block + 1);
                }
                if (hasPowerUp(PowerUps.BOOST, myCar.powerups) && Obstacles(boostLane) < 10 && myCar.boosting == false) {
                    return USE_BOOST;
                }
                int choice = pickLane(myCar, leftLane, currentLane, rightLane);
                switch (choice) {
                    case -1:
                        return TURN_LEFT;
                    case 0:
                        int no_accelerate = Obstacles(getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed));
                        int with_accelerate = Obstacles(getBlocksInFront(myCar.position.lane, myCar.position.block, nextSpeedState(myCar)));
                        if (with_accelerate <= no_accelerate && myCar.boosting == false) {
                            return ACCELERATE;
                        } else {
                            return NOTHING;
                        }
                    case 1:
                        return TURN_RIGHT;
                    case 5:
                        // bandingin powerup yang ada di kiri dan tengah
                        int atLeftLandingBlock = leftLaneBlock(myCar);
                        int atCurrentLandingBlock = currentLaneBlock(myCar);
                        int atAccelerateLandingBlock = accelerateLaneBlock(myCar);
                        // kalau sama jenisnya, cek dulu mendingan ngebut atau engga
                        if (atCurrentLandingBlock >= atLeftLandingBlock) {
                            no_accelerate = Obstacles(getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed));
                            with_accelerate = Obstacles(getBlocksInFront(myCar.position.lane, myCar.position.block, nextSpeedState(myCar)));
                            if (with_accelerate <= no_accelerate && myCar.boosting == false) {
                                return ACCELERATE;
                            } else {
                                return NOTHING;
                            }
                        } else {
                            return TURN_LEFT;
                        }
                    case 10:
                        // bandingin powerup yang ada di kanan dan tengah
                        int atRightLandingBlock = leftLaneBlock(myCar);
                        atCurrentLandingBlock = currentLaneBlock(myCar);
                        atAccelerateLandingBlock = accelerateLaneBlock(myCar);
                        // kalau sama jenisnya, cek dulu mendingan ngebut atau engga
                        if (atCurrentLandingBlock >= atRightLandingBlock) {
                            no_accelerate = Obstacles(getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed));
                            with_accelerate = Obstacles(getBlocksInFront(myCar.position.lane, myCar.position.block, nextSpeedState(myCar)));
                            if (with_accelerate <= no_accelerate && myCar.boosting == false) {
                                return ACCELERATE;
                            } else {
                                return NOTHING;
                            }
                        } else {
                            return TURN_RIGHT;
                        }
                    case 15:
                        // bandingin powerup yang ada di kiri dan tengah
                        atLeftLandingBlock = leftLaneBlock(myCar);
                        atCurrentLandingBlock = currentLaneBlock(myCar);
                        atRightLandingBlock = rightLaneBlock(myCar);
                        atAccelerateLandingBlock = accelerateLaneBlock(myCar);
                        // kalau sama jenisnya, cek dulu mendingan ngebut atau engga
                        if (atCurrentLandingBlock >= Math.max(atLeftLandingBlock, atRightLandingBlock)) {
                            no_accelerate = Obstacles(getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed));
                            with_accelerate = Obstacles(getBlocksInFront(myCar.position.lane, myCar.position.block, nextSpeedState(myCar)));
                            if (with_accelerate <= no_accelerate && myCar.boosting == false) {
                                return ACCELERATE;
                            } else {
                                return NOTHING;
                            }
                        } else if (atLeftLandingBlock > Math.max(atCurrentLandingBlock, atRightLandingBlock)) {
                            return TURN_LEFT;
                        } else if (atRightLandingBlock > Math.max(atCurrentLandingBlock, atLeftLandingBlock)) {
                            return TURN_RIGHT;
                        }
                    default:
                        return NOTHING;
                }
            }
            if (!isSameLane(myCar, opponent)) {
                if (myCar.boosting == true) { // hanya belok kanan atau kiri, kalau pilih lurus mending pake powerup
                    int choice = pickLane(myCar, leftLane, currentLane, rightLane);
                    if (choice == -1) {
                        return TURN_LEFT;
                    } else if (choice == 1) {
                        return TURN_RIGHT;
                    }
                }
                // semua kode dibawah dipakai dengan asumsi booster mati
                if (myCar.damage >= 1 && myCar.damage <= 2 && jarakAntarMobil(myCar, opponent) >= opponent.speed) {
                    return FIX;
                }
                if (myCar.damage > 2) {
                    return FIX;
                }
                if (hasPowerUp(PowerUps.TWEET, myCar.powerups)) {
                    return new TweetCommand(opponent.position.lane, opponent.position.block + 1);
                }
                if (hasPowerUp(PowerUps.BOOST, myCar.powerups) && Obstacles(boostLane) < 10 && jarakAntarMobil(myCar, opponent) < 20 && myCar.boosting == false) {
                    return USE_BOOST;
                }
                if (hasPowerUp(PowerUps.EMP, myCar.powerups) && inRangeEMP(myCar, opponent)) {
                    return USE_EMP;
                }
                int choice = pickLane(myCar, leftLane, currentLane, rightLane);
                switch (choice) {
                    case -1:
                        return TURN_LEFT;
                    case 0:
                        int no_accelerate = Obstacles(getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed));
                        int with_accelerate = Obstacles(getBlocksInFront(myCar.position.lane, myCar.position.block, nextSpeedState(myCar)));
                        if (with_accelerate <= no_accelerate && myCar.boosting == false) {
                            return ACCELERATE;
                        } else {
                            return NOTHING;
                        }
                    case 1:
                        return TURN_RIGHT;
                    case 5:
                        // bandingin powerup yang ada di kiri dan tengah
                        int atLeftLandingBlock = leftLaneBlock(myCar);
                        int atCurrentLandingBlock = currentLaneBlock(myCar);
                        int atAccelerateLandingBlock = accelerateLaneBlock(myCar);
                        // kalau sama jenisnya, cek dulu mendingan ngebut atau engga
                        if (atCurrentLandingBlock >= atLeftLandingBlock) {
                            no_accelerate = Obstacles(getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed));
                            with_accelerate = Obstacles(getBlocksInFront(myCar.position.lane, myCar.position.block, nextSpeedState(myCar)));
                            if (with_accelerate <= no_accelerate && myCar.boosting == false) {
                                return ACCELERATE;
                            } else {
                                return NOTHING;
                            }
                        } else {
                            return TURN_LEFT;
                        }
                    case 10:
                        // bandingin powerup yang ada di kanan dan tengah
                        int atRightLandingBlock = leftLaneBlock(myCar);
                        atCurrentLandingBlock = currentLaneBlock(myCar);
                        atAccelerateLandingBlock = accelerateLaneBlock(myCar);
                        // kalau sama jenisnya, cek dulu mendingan ngebut atau engga
                        if (atCurrentLandingBlock >= atRightLandingBlock) {
                            no_accelerate = Obstacles(getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed));
                            with_accelerate = Obstacles(getBlocksInFront(myCar.position.lane, myCar.position.block, nextSpeedState(myCar)));
                            if (with_accelerate <= no_accelerate && myCar.boosting == false) {
                                return ACCELERATE;
                            } else {
                                return NOTHING;
                            }
                        } else {
                            return TURN_RIGHT;
                        }
                    case 15:
                        // bandingin powerup yang ada di kiri dan tengah
                        atLeftLandingBlock = leftLaneBlock(myCar);
                        atCurrentLandingBlock = currentLaneBlock(myCar);
                        atRightLandingBlock = rightLaneBlock(myCar);
                        atAccelerateLandingBlock = accelerateLaneBlock(myCar);
                        // kalau sama jenisnya, cek dulu mendingan ngebut atau engga
                        if (atCurrentLandingBlock >= Math.max(atLeftLandingBlock, atRightLandingBlock)) {
                            no_accelerate = Obstacles(getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed));
                            with_accelerate = Obstacles(getBlocksInFront(myCar.position.lane, myCar.position.block, nextSpeedState(myCar)));
                            if (with_accelerate <= no_accelerate && myCar.boosting == false) {
                                return ACCELERATE;
                            } else {
                                return NOTHING;
                            }
                        } else if (atLeftLandingBlock > Math.max(atCurrentLandingBlock, atRightLandingBlock)) {
                            return TURN_LEFT;
                        } else if (atRightLandingBlock > Math.max(atCurrentLandingBlock, atLeftLandingBlock)) {
                            return TURN_RIGHT;
                        }
                    default:
                        return NOTHING;
                }
            }
        }
        if (Obstacles(currentLane) > 0) {
        // prioritaskan pengunaan lizard, kalau ada boost dan depannya bukan wall ya hajar aja, sisanya dodge
        // kalau gabisa dodge, pilih yg mud/oil baru wall
            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                return USE_LIZARD;
            }
            // wall nilainya 10, jadi ini artinya kalau dia ada boost langsung pake biar best case dapet max_speed
            if (hasPowerUp(PowerUps.BOOST, myCar.powerups) && Obstacles(currentLane) < 10) {
                return USE_BOOST;
            }
            int choice = pickLane(myCar, leftLane, currentLane, rightLane);
                switch (choice) {
                    case -1:
                        return TURN_LEFT;
                    case 0:
                        int no_accelerate = Obstacles(getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed));
                        int with_accelerate = Obstacles(getBlocksInFront(myCar.position.lane, myCar.position.block, nextSpeedState(myCar)));
                        if (with_accelerate <= no_accelerate && myCar.boosting == false) {
                            return ACCELERATE;
                        } else {
                            return NOTHING;
                        }
                    case 1:
                        return TURN_RIGHT;
                    case 5:
                        // bandingin powerup yang ada di kiri dan tengah
                        int atLeftLandingBlock = leftLaneBlock(myCar);
                        int atCurrentLandingBlock = currentLaneBlock(myCar);
                        int atAccelerateLandingBlock = accelerateLaneBlock(myCar);
                        // kalau sama jenisnya, cek dulu mendingan ngebut atau engga
                        if (atCurrentLandingBlock >= atLeftLandingBlock) {
                            no_accelerate = Obstacles(getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed));
                            with_accelerate = Obstacles(getBlocksInFront(myCar.position.lane, myCar.position.block, nextSpeedState(myCar)));
                            if (with_accelerate <= no_accelerate && myCar.boosting == false) {
                                return ACCELERATE;
                            } else {
                                return NOTHING;
                            }
                        } else {
                            return TURN_LEFT;
                        }
                    case 10:
                        // bandingin powerup yang ada di kanan dan tengah
                        int atRightLandingBlock = leftLaneBlock(myCar);
                        atCurrentLandingBlock = currentLaneBlock(myCar);
                        atAccelerateLandingBlock = accelerateLaneBlock(myCar);
                        // kalau sama jenisnya, cek dulu mendingan ngebut atau engga
                        if (atCurrentLandingBlock >= atRightLandingBlock) {
                            no_accelerate = Obstacles(getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed));
                            with_accelerate = Obstacles(getBlocksInFront(myCar.position.lane, myCar.position.block, nextSpeedState(myCar)));
                            if (with_accelerate <= no_accelerate && myCar.boosting == false) {
                                return ACCELERATE;
                            } else {
                                return NOTHING;
                            }
                        } else {
                            return TURN_RIGHT;
                        }
                    case 15:
                        // bandingin powerup yang ada di kiri dan tengah
                        atLeftLandingBlock = leftLaneBlock(myCar);
                        atCurrentLandingBlock = currentLaneBlock(myCar);
                        atRightLandingBlock = rightLaneBlock(myCar);
                        atAccelerateLandingBlock = accelerateLaneBlock(myCar);
                        // kalau sama jenisnya, cek dulu mendingan ngebut atau engga
                        if (atCurrentLandingBlock >= Math.max(atLeftLandingBlock, atRightLandingBlock)) {
                            no_accelerate = Obstacles(getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed));
                            with_accelerate = Obstacles(getBlocksInFront(myCar.position.lane, myCar.position.block, nextSpeedState(myCar)));
                            if (with_accelerate <= no_accelerate && myCar.boosting == false) {
                                return ACCELERATE;
                            } else {
                                return NOTHING;
                            }
                        } else if (atLeftLandingBlock > Math.max(atCurrentLandingBlock, atRightLandingBlock)) {
                            return TURN_LEFT;
                        } else if (atRightLandingBlock > Math.max(atCurrentLandingBlock, atLeftLandingBlock)) {
                            return TURN_RIGHT;
                        }
                    default:
                        return NOTHING;
                }
        }
        return NOTHING;
    }



    // ========== INISIALISASI FUNGSI HELPER DI SINI =========
    /**
     * Returns map of blocks and the objects in the for the current lanes, returns the amount of blocks that can be
     * traversed at max speed.
     **/
    // edit dari gede : nambahin current speed biar bisa pas ama algo di notes.md
    private List<Object> getBlocksInFront(int lane, int block, int carSpeed) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + carSpeed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }
            blocks.add(laneList[i].terrain);

        }
        return blocks;
    }

    // fungsi untuk ngecek jarak mobil kita dengan lawan
    private int jarakAntarMobil (Car myCar, Car opponentCar) {
        if (myCar.position.block > opponentCar.position.block) {
            return myCar.position.block - opponentCar.position.block;
        } else {
            return opponentCar.position.block - myCar.position.block;
        }
    }

    // fungsi untuk ngecek dia bisa belok kanan/kiri lane atau engga
    private boolean canGoLeft (Car myCar) {
        return (myCar.position.lane > 1);
    }

    private boolean canGoRight (Car myCar) {
        return (myCar.position.lane < 4);
    }

    // fungsi untuk milih lane mana yang mau dipilih/dihajar
    // return -1 artinya dia milih ke kiri
    // return 0 artinya dia milih ttp lurus
    // return 1 artinya dia milih ke kanan 
    // return 5 artinya bisa ke kiri atau lurus
    // return 10 artinya bisa ke kanan atau lurus
    // return 15 artinya semua jalur damagenya sama
    private int pickLane (Car myCar, List<Object> leftLane, List<Object> currentLane, List<Object> rightLane) {
        boolean possibleLeft = false;
        boolean possibleRight = false;
        int leftCount = 100, rightCount = 100, currentCount = 100;
        if (canGoLeft(myCar)) {
            leftCount = Obstacles(leftLane);
            possibleLeft = true;
        }
        if (canGoRight(myCar)) {
            rightCount = Obstacles(rightLane);
            possibleRight = true;
        }
        currentCount = Obstacles(currentLane);
        if (possibleLeft == true && possibleRight == true) {
            if (leftCount == Math.min(leftCount, Math.min(rightCount, currentCount))) {
                if (leftCount == currentCount) {
                    if (leftCount == rightCount) {
                        return 15;
                    } else {
                        return 5;
                    }
                } else {
                    return -1;
                }
            } else if (currentCount == Math.min(leftCount, Math.min(rightCount, currentCount))) {
                if (currentCount == rightCount) {
                    return 10;
                } else {
                    return 0;
                }
            } else {
                return 1;
            }
        } else if (possibleLeft == true && possibleRight == false) {
            if (leftCount == Math.min(leftCount, currentCount)) {
                if (leftCount == currentCount) {
                    return 5;
                } else {
                    return -1;
                }
            } else {
                return 0;
            }
        } else if (possibleLeft == false && possibleRight == true) {
            if (rightCount == Math.min(rightCount, currentCount)) {
                if (rightCount == currentCount) {
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

    // fungsi untuk ngecek next speed_state:
    private int nextSpeedState (Car targetCar) {
       switch (targetCar.speed) {
            case 0:
                return 3;
            case 3:
                return 6;
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

    // fungsi untuk ngecek mobil satu lane dengan lawan atau tidak
    private Boolean isSameLane (Car myCar, Car opponent) {
        if (myCar.position.lane == opponent.position.lane) {
            return true;
        } else {
            return false;
        }
    }
    // fungsi untuk ngecek mobil dalam range EMP atau tidak
    private Boolean inRangeEMP (Car myCar, Car opponent) {
        if (Math.abs(myCar.position.lane - opponent.position.lane) <= 1) {
            return true;
        } else {
            return false;
        }
    }

    // fungsi untuk ngecek previous speed_state:
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

    // fungsi untuk mencari apakah ada powerup di suatu lane mobil akan mendarat:
    // karena ada skala prioritas powerup : Boost -> Lizard -> Tweet -> OilPower -> EMP 
    // return 9 jika tidak ada powerup
    // return 1 jika ada Boost
    // return 2 jika ada Lizard
    // return 3 jika ada Tweet
    // return 4 jika ada OilPower
    // return 5 jika ada EMP

    private int leftLaneBlock (Car myCar) {
        List<Lane[]> map = gameState.lanes;
        Lane[] laneList = map.get(myCar.position.lane - 1 - 1); // dikurangi 1 karena left lane, dikurangi 1 lagi soalnya dia basisnya 0
        int landingPosition = myCar.position.block + myCar.speed - 1; // dikurangi 1 karena udah dipake buat belok, harus ngecek ini basis 0 atau engga
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

    private int currentLaneBlock (Car myCar) {
        List<Lane[]> map = gameState.lanes;
        Lane[] laneList = map.get(myCar.position.lane - 1); // dikurangi 1 soalnya dia basisnya 0
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

    private int rightLaneBlock (Car myCar) {
        List<Lane[]> map = gameState.lanes;
        Lane[] laneList = map.get(myCar.position.lane); // tidak dikurangi 1 soalnya dia basisnya 0 (-1) dan dia ke kanan (+1)
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
            if (Lane.get(i).equals(Terrain.MUD) || Lane.get(i).equals(Terrain.OIL_SPILL)) {
                count++;
            } else if (Lane.get(i).equals(Terrain.WALL)) {
                count += 10;
            }
        }
        return count;
    }
    // car lane yang jumlah obstaclesnya paling dikit
    // CALL KALAU DIA GA DI LANE 1 ATAU 4
    // TODO: pake ini kok malah kalah ya? terlalu defensif kah?
    // udah diganti sama pickLane
    // ini perlu direcycle buat nyesuain sama  algo di notes.md
    // private Command compareObstacles(){
    //     int Lcount = Obstacles(getBlocksInFront(myCar.position.lane - 1, myCar.position.block));
    //     int Ccount = Obstacles(getBlocksInFront(myCar.position.lane, myCar.position.block));
    //     int Rcount = Obstacles(getBlocksInFront(myCar.position.lane + 1, myCar.position.block));
    //     if (Lcount >= Ccount && Lcount >= Rcount) {
    //         return TURN_LEFT;
    //     } else if (Rcount >= Ccount && Rcount >= Lcount) {
    //         return TURN_RIGHT;
    //     } else {
    //         if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
    //             return USE_BOOST;
    //         } else {
    //             return ACCELERATE;
    //         }
    //     }
    // }
}
