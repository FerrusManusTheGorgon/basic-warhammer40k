package models

case class GameCharacter(
                      characterId: String,
                      range: Int,
                      ballisticSkill: Int,
                      weaponSkill: Int,
                      movement: Int,
                      avatar: String,
                      rangedAttackHitMessage: String,
                      rangedAttackMissMessage: String,
                      closeCombatHitMessage: String,
                      closeCombatMissMessage: String  

                    )
