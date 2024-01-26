package models

import models.GameCharacter
object Characters {

  val SpaceMarine = GameCharacter(
    characterId = "a617321a-7ad6-11ee-afff-325096b39f47",
    ballisticSkill = 90,
    range = 10,
    weaponSkill = 95,
    movement = 10,
    avatar = "S",
    rangedAttackHitMessage = "Space Marine opened fire with his Bolter and eliminated the ",
    rangedAttackMissMessage = "Space Marine Bolts missed the "
    

  )

  val Ork = GameCharacter(
    characterId = "a0d58842-7ad6-11ee-b72f-325096b39f47",
    ballisticSkill = 20,
    range = 5,
    weaponSkill = 60,
    movement = 10,
    avatar = "O",
    rangedAttackHitMessage = "Ork unleashed his Big Shoota and blasted the ",
    rangedAttackMissMessage = "Ork dakka dakka dakka dakka missed the "
  )
}
