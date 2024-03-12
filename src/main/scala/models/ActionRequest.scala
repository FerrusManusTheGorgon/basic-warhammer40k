package models


import models.Coordinates

case class ActionRequest(
                          x: Int,
                          y: Int,
                          avatar: String
                        ) {
  def toCoordinates: Coordinates = {
    Coordinates(x, y)
  }
}
