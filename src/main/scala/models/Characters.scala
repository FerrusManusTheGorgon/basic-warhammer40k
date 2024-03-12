package models

import models.GameCharacter
object Characters {

  val SpaceMarine = GameCharacter(
    characterId = "a617321a-7ad6-11ee-afff-325096b39f47",
    ballisticSkill = 99,
    range = 15,
    weaponSkill = 75,
    movement = 10,
    avatar = "S",
    name = "Space Marine",
    rangedAttackHitMessage = "Space Marine opened fire with his Bolter and eliminated the ",
    rangedAttackMissMessage = "Space Marine Bolts missed the ",
    closeCombatHitMessage = "Space eviscerated the xeno scum with his chain sword ",
    closeCombatMissMessage = "The xeno evaded the sweep of the chain sword",
    currentPosition = Coordinates(3, 4),
    currentStateAlive = false,
    movePhaseCompleted= false,
    shootingPhaseCompleted= false,
    closeCombatPhaseCompleted= false,
    state = "alive"

  )

  val Ork = GameCharacter(
    characterId = "a0d58842-7ad6-11ee-b72f-325096b39f47",
    ballisticSkill = 25,
    range = 5,
    weaponSkill = 55,
    movement = 4,
    avatar = "O",
    name = "OrkWithSluggaAndChoppa",
    rangedAttackHitMessage = "Ork unleashed his slugga and blasted the ",
    rangedAttackMissMessage = "Ork dakka dakka dakka dakka missed the ",
    closeCombatHitMessage = "Ork smashed the Space Marine  with his choppa, Waaaaaggh!!! ",
    closeCombatMissMessage = "The Space Marine dodged the wild of swing of the choppa",
    currentPosition = Coordinates(6, 8),
    currentStateAlive = false,
    movePhaseCompleted = false,
    shootingPhaseCompleted = false,
    closeCombatPhaseCompleted = false,
    state = "alive"
  )

  val OrkWithBigShoota = GameCharacter(
    characterId = "666c5b72-c1ee-11ee-bbbb-325096b39f47",
    ballisticSkill = 66,
    range = 6,
    weaponSkill = 25,
    movement = 10,
    avatar = "9",
    name = "OrkWithBigShoota",
    rangedAttackHitMessage = "Ork unleashed his Big Shoota and blasted the ",
    rangedAttackMissMessage = "Ork dakka dakka dakka dakka missed the ",
    closeCombatHitMessage = "Ork smashed the Space Marine  with his choppa, Waaaaaggh!!! ",
    closeCombatMissMessage = "The Space Marine dodged the wild of swing of the choppa",
    currentPosition = Coordinates(8, 10),
    currentStateAlive = false,
    movePhaseCompleted = false,
    shootingPhaseCompleted = false,
    closeCombatPhaseCompleted = false,
    state = "alive"
  )

  val OrkWithScorcha = GameCharacter(
    characterId = "666c5b72-c1ee-11ee-bccb-325096b39f47",
    ballisticSkill = 66,
    range = 3,
    weaponSkill = 33,
    movement = 10,
    avatar = "8",
    name = "OrkWithScorcha",
    rangedAttackHitMessage = "Ork unleashed his Skorcha and cooked the ",
    rangedAttackMissMessage = "Ork Skorcha missed the ",
    closeCombatHitMessage = "Ork smashed the Space Marine  with his Skorcha, Waaaaaggh!!! ",
    closeCombatMissMessage = "The Space Marine dodged the wild of swing of the Skorcha",
    currentPosition = Coordinates(10, 10),
    currentStateAlive = false,
    movePhaseCompleted = false,
    shootingPhaseCompleted = false,
    closeCombatPhaseCompleted = false,
    state = "alive"
  )

  val OrkWithBigChoppa = GameCharacter(
    characterId = "666c5b72-c1ee-11ee-bxxb-325096b39f47",
    ballisticSkill = 1,
    range = 5,
    weaponSkill = 66,
    movement = 20,
    avatar = "7",
    name = "OrkWithBigChoppa",
    rangedAttackHitMessage = "Ork unleashed his Skorcha and cooked the ",
    rangedAttackMissMessage = "Ork Skorcha missed the ",
    closeCombatHitMessage = "Ork smashed the Space Marine  with his Skorcha, Waaaaaggh!!! ",
    closeCombatMissMessage = "The Space Marine dodged the wild of swing of the Skorcha",
    currentPosition = Coordinates(1, 10),
    currentStateAlive = false,
    movePhaseCompleted = false,
    shootingPhaseCompleted = false,
    closeCombatPhaseCompleted = false,
    state = "alive"
  )
}
