# Starship Fights

## Phases of Battle

Each turn consists of several phases. Each player takes their turn simultaneously.

### Power Phase

Distribute power among the ship's subsystems:

* Weapons - Used to attack other ships
* Shields - Used to defend against attacks
* Impulse - Used to move around in space
* Emitter - Used for special abilities

Power starts off as being evenly split between all four subsystems. The ship's Grid Efficiency
is how many Power Output (PO) points can be transferred between subsystems in any given Power Phase. Once the
phase ends, the power distribution stays like it is until it is modified again, i.e. it never automatically
resets back to the default evenly-shared distribution.

Different ship tiers have different total amounts of power, as well as a certain Grid Efficiency:

1. Escort: 8 PO, 1 GE
2. *Frigate* (Vestigium only): 12 PO, 1 GE
3. Destroyer: 12 PO, 2 GE
4. Cruiser: 16 PO, 3 GE
5. *Line Ship* (Vestigium only): 20 PO, 3 GE
6. Battlecruiser: 16 PO, 4 GE
7. *Heavy Cruiser* (Isarnareykk only): 24 PO, 3 GE
8. Battleship: 24 PO, 4 GE
9. *Dreadnought* (Vestigium only): 28 PO, 5 GE
10. *Colossus* (Masra Draetsen only): 36 PO, 7 GE

The effects that different power levels have is as follows:

* Each unit of Weapon Power is consumed when a ship fires a weapon or charges a Lance battery
  * Weapon power is replenished completely after the End phase ends
* Each unit of Shield Power is consumed when a ship's shields block an attack
  * Shield power is replenished after the End phase ends
  * Each Blast Marker the ship is touching has a 50% chance of reducing the replenished shield power by 1
* Engine power modifies the ship's maximum acceleration
  * The normal Engine Power `e_0` is 25% of the total PO
  * The current Engine Power `e_1` is however many points the ship has put into its Engines
  * The movement speed factor is `η = sqrt(e_1 / e_0)`
  * The maximum distance moved during the Movement phase is `a_max = a_max_0 * η` where `a_max_0` is the default max movement
  * The maximum angle turned during the Movement phase is `α_max = α_max_0 * η`
* Emitter power modifies the power of the ship's Emitter abilities
  * Similar factor: `μ = sqrt(m_1 / m_0)`
  * The reason why the `sqrt` function is used is to represent the diminishing returns of putting more power into a subsystem

### Movement Phase

Ships change their velocity during this phase. Velocity as a property of ships is persistent,
being modified by the ship's acceleration which is decided during this phase.

The way ships move is as follows, where ship's maximum acceleration is `a_max`, current position is `x_1`, previous position is `x_0`, and current ship facing is `θ`:

1. Ship rotates a certain amount (up to maximum rotation) chosen by the player away from `θ`, this new facing angle is put into `φ`.
2. A line is drawn starting at `x_1 + (x_1 - x_0)`, this point is put into `x_2`.
3. The line traverses the vector `a_max * (î cos ((θ + φ) / 2) + ĵ sin ((θ + φ) / /2))` away from `x_2`
4. The player chooses a point along this line for the ship to travel to, this point is put into `x_new`
5. `x_1 -> x_0`, then `x_new -> x_1` and `φ -> θ`

### Action Phase

Ships call attacks against other ships during this phase,
as well as defending themselves or each other from those attacks.

### End Phase

Attacks, defenses, and results of actions are resolved.

Blast markers are created; each successfully-hit attack creates a blast marker within
a certain radius of the targeted ship.
