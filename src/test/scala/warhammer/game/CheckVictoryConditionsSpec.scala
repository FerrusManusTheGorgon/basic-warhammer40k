package warhammer.game

import warhammer.game.models._
import org.scalatest.funspec.AnyFunSpec
import warhammer.game.models.Characters.SpaceMarine

class CheckVictoryConditionsSpec extends AnyFunSpec {

  describe("checkVictory") {
    it("should return a string the battle ranges on") {

      val activePlayers = List(
        Characters.SpaceMarine,
      )
      val passivePlayers = List(
        Characters.Ork,
        Characters.OrkWithBigShoota,
        Characters.OrkWithScorcha,
        Characters.OrkWithBigChoppa
      )

      val manager = new CheckVictoryConditions

      val result = manager.checkVictory(passivePlayers, activePlayers)

      assert(result === "The Battle Rages On")
    }
    it("should return string The Green Tide is Victorious. WAAAAAGGGGH!!!!") {


      val activeUnits = List(
        Characters.Ork,
        Characters.OrkWithBigShoota,
        Characters.OrkWithScorcha,
        Characters.OrkWithBigChoppa
      )
      val passiveUnits = List(
        Characters.SpaceMarine,
      )
      val modifiedSpaceMarine = SpaceMarine.copy(state = "dead", avatar = "x")
      println(s"Modified Space Marine: $modifiedSpaceMarine")

      val modifiedPassiveUnits = passiveUnits.map {
        case character if character == Characters.SpaceMarine =>
          println("Replacing Space Marine with modified version")
          modifiedSpaceMarine
        case otherCharacter => otherCharacter
      }
      println(s"Modified Passive Units: $modifiedPassiveUnits")


      val manager = new CheckVictoryConditions

      val result = manager.checkVictory(activeUnits, modifiedPassiveUnits)


      assert(result === "The Green Tide is Victorious. WAAAAAGGGGH!!!!")
    }
  }
}
