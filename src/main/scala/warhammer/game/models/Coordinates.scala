package warhammer.game.models

case class Coordinates(x: Int, y: Int) {
  def +(dx: Int, dy: Int): Coordinates = Coordinates(x + dx, y + dy)

  def adjacent: List[Coordinates] = List(up, right, down, left)

  def up: Coordinates = Coordinates(this.x, this.y + 1)

  def right: Coordinates = Coordinates(this.x + 1, this.y)

  def down: Coordinates = Coordinates(this.x, this.y - 1)

  def left: Coordinates = Coordinates(this.x - 1, this.y)
}

