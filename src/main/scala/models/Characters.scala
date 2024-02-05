package models

import models.GameCharacter
object Characters {

  val SpaceMarine = GameCharacter(
    characterId = "a617321a-7ad6-11ee-afff-325096b39f47",
    ballisticSkill = 90,
    range = 4,
    weaponSkill = 95,
    movement = 10,
    avatar = "S",
    name = "Space Marine",
    rangedAttackHitMessage = "Space Marine opened fire with his Bolter and eliminated the ",
    rangedAttackMissMessage = "Space Marine Bolts missed the ",
    closeCombatHitMessage = "Space eviscerated the xeno scum with his chain sword ",
    closeCombatMissMessage = "The xeno evaded the sweep of the chain sword"

  )

  val Ork = GameCharacter(
    characterId = "a0d58842-7ad6-11ee-b72f-325096b39f47",
    ballisticSkill = 20,
    range = 5,
    weaponSkill = 60,
    movement = 30,
    avatar = "O",
    name = "Ork",
    rangedAttackHitMessage = "Ork unleashed his slugga and blasted the ",
    rangedAttackMissMessage = "Ork dakka dakka dakka dakka missed the ",
    closeCombatHitMessage = "Ork smashed the Space Marine  with his choppa, Waaaaaggh!!! ",
    closeCombatMissMessage = "The Space Marine dodged the wild of swing of the choppa"
  )

  val OrkWithBigShoota = GameCharacter(
    characterId = "666c5b72-c1ee-11ee-baab-325096b39f47",
    ballisticSkill = 40,
    range = 10,
    weaponSkill = 50,
    movement = 30,
    avatar = "9",
    name = "Ork",
    rangedAttackHitMessage = "Ork unleashed his Big Shoota and blasted the ",
    rangedAttackMissMessage = "Ork dakka dakka dakka dakka missed the ",
    closeCombatHitMessage = "Ork smashed the Space Marine  with his choppa, Waaaaaggh!!! ",
    closeCombatMissMessage = "The Space Marine dodged the wild of swing of the choppa"
  )
}
