/*
 *  Project name: CellSIM/Predator.java
 *  Author & email: Mirza Suljić <mirza.suljic.ba@gmail.com>
 *  Date & time: Jun 13, 2016, 5:53:57 PM
 */
package edu.lexaron.cells;

import edu.lexaron.world.Cell;
import edu.lexaron.world.World;

/**
 *
 * @author Mirza Suljić <mirza.suljic.ba@gmail.com>
 */
public class Predator extends Cell {

    /**
     *
     * @param ID
     * @param x
     * @param y
     * @param energy
     * @param vision
     * @param speed
     * @param efficiency
     * @param color
     */
    public Predator(String ID, int x, int y, double energy, int vision, double speed, double efficiency, String color, double biteSize) {
        super(ID, x, y, energy, vision, speed, efficiency, color, biteSize);
    }

    /**
     *
     * @param w
     */
    @Override
    public void hunt(World w) {
        // CELL TYPE DEPENDANT
        if (this.isAlive()) {
            upkeep(w);
            if (getPath().isEmpty()) {
                setTargetFood(lookForFood(w));
                if (getTargetFood() != null) {
                    findPathTo(getTargetFood());
                    usePath(w);
                }else {
                    moveRight(w);
                    randomStep(w);
                }
            }
            if (!getPath().isEmpty()
                    && (w.getWorld()[getTargetFood()[0]][getTargetFood()[1]].getCell() != null
                    || w.getWorld()[getTargetFood()[0]][getTargetFood()[1]].getTrail().getAmount() > 0)) {
                usePath(w);
                eat(w);
            }
            if (getEnergy() >= 100) {
                mutate(w);
                setEnergy(getEnergy() / 3);
            }
            if (getOffspring() >= 5) {
                setAlive(false);
                w.getWorld()[getY()][getX()].setDeadCell(this);
                w.getWorld()[getY()][getX()].setCell(null);
            }
        }
    }

    /**
     *
     * @param w
     */
    @Override
    public void eat(World w) {
        outterloop:
        for (int i = (getY() - 1); i <= (getY() + 1); i++) {
            for (int j = (getX() - 1); j <= (getX() + 1); j++) {
                try {
                    if (w.getWorld()[i][j].getCell() != null
                            && w.getWorld()[i][j].getCell() != this
                            && !w.getWorld()[i][j].getCell().getClass().getSimpleName().equalsIgnoreCase(getClass().getSimpleName()) //                            && !w.getWorld()[i][j].getCell().getClass().getSimpleName().equalsIgnoreCase("vulture")
                            && !w.getWorld()[i][j].getCell().getClass().getSimpleName().equalsIgnoreCase("tree")) {
                        setEnergy(getEnergy() + (w.getWorld()[i][j].getCell().getEnergy()));
                        w.getWorld()[i][j].getCell().setAlive(false);
                        w.getWorld()[i][j].setDeadCell(w.getWorld()[i][j].getCell());
                        w.getWorld()[i][j].setCell(null);
                        w.getWorld()[i][j].getSugar().setAmount(10);
//                        System.out.println("KILLED " + w.getWorld()[i][j].getCell().getGeneCode());
                        setTargetFood(null);
                        break outterloop;
                    }
                } catch (ArrayIndexOutOfBoundsException ex) {

                }
            }
        }
        setTargetFood(null);
        getPath().clear();
    }

    /**
     *
     * @param w
     * @return
     */
    @Override
    public int[] lookForFood(World w) {
        // Cell type PREDATOR is only interested in hunting other cell types.
//        System.out.println("Cell " + getGeneCode() + " looking for food from " + getX() + "," + getY() + "...");
        int[] foodLocation = new int[2];
        boolean found = false;
        int foundSmell = 0;
        outterloop:
        for (int v = 1; v <= getVision(); v++) {
            for (int i = (getY() - v); i <= (getY() + v); i++) {
                for (int j = (getX() - v); j <= (getX() + v); j++) {
                    try {
//                    System.out.print("(" + j + "," + i + ")");
                        if (w.getWorld()[i][j].getCell() != null && w.getWorld()[i][j].getCell().isAlive()) {
                            if (!w.getWorld()[i][j].getCell().getClass().getSimpleName().equalsIgnoreCase(this.getClass().getSimpleName())
                                    && !w.getWorld()[i][j].getCell().getClass().getSimpleName().equalsIgnoreCase("tree")
                                    && !w.getWorld()[i][j].getCell().getClass().getSimpleName().equalsIgnoreCase("vulture")) {
                                foodLocation[0] = i; // Y
                                foodLocation[1] = j; // X
                                found = true;
                                break outterloop;
                            }
                        } else if (w.getWorld()[i][j].getTrail().getSource() != null && !w.getWorld()[i][j].getTrail().getSource().getClass().getSimpleName().equalsIgnoreCase(this.getClass().getSimpleName())
                                && !w.getWorld()[i][j].getTrail().getSource().getClass().getSimpleName().equalsIgnoreCase("vulture")
                                && w.getWorld()[i][j].getTrail().getAmount() > foundSmell) {
                            foundSmell = w.getWorld()[i][j].getTrail().getAmount();
                            foodLocation[0] = i; // Y
                            foodLocation[1] = j; // X
                            found = true;
                        }

                    } catch (ArrayIndexOutOfBoundsException ex) {

                    }
                }
//            System.out.print("\n");
            }
        }
        if (found) {
//            System.out.println(getGeneCode() + " found food on " + foodLocation[0] + "," + foodLocation[1]);
            return foodLocation;
        } else {
//            System.out.println(getGeneCode() + " found no food.");
            return null;
        }

    }

    /**
     *
     * @param w
     */
    @Override
    public void mutate(World w) {
        int[] childLocation = findFreeTile(w);
        Predator child = new Predator(String.valueOf(getGeneCode() + "." + getOffspring()), childLocation[1], childLocation[0], (getEnergy() / 3), getVision(), getSpeed(), getEfficiency(), getColor(), getBiteSize());
        try {
            child.evolve();
            w.getNewBornCells().add(child);
            setOffspring(getOffspring() + 1);
            setEnergy(getEnergy() / 3);
        } catch (Exception ex) {
            System.out.println(getGeneCode() + " failed to divide:\n" + ex);
        }
    }

}