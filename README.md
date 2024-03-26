Getting Started
Prerequisites

    Scala 2.12.x or higher
    sbt (Scala Build Tool) 1.x

Installation

    Clone this repository to your local machine:

    bash

git clone <repository-url>

Navigate to the project directory:

bash

cd scala-game-server

Build the project using sbt:

bash

    sbt compile

Usage

    Start the server by running the MinimalApplication object:

    bash

    sbt run

    The server will start on the default port 8080.

Endpoints

    /start: GET endpoint to initiate the game. Responds with a prompt asking if the user wants to start the game.
    /jstart: POST endpoint to start the game based on JSON input. Expects a JSON object containing a start field with values 'y' or 'n' to indicate whether to start the game.
    /jmove: POST endpoint to execute a move action in the game. Expects JSON input with coordinates and avatar information.
    /shoot: GET endpoint to check shooter's actions. Responds with a message indicating if shooting actions are allowed in the current phase.
    /jshoot: POST endpoint to execute a shoot action in the game. Expects JSON input with coordinates and avatar information.
    /assault: GET endpoint to check assault actions. Responds with a message indicating if assault actions are allowed in the current phase.
    /jassault: POST endpoint to execute an assault action in the game. Expects JSON input with coordinates and avatar information.

Movement Manager

The Movement Manager handles movement-related functionalities in the game. It ensures that movements are valid within the game's rules and updates the game board accordingly.
Features

    isValidMove: Checks if a move is valid for a given game character and destination coordinates.
    getShortestPath: Calculates the shortest path from the current position to the destination using Breadth-First Search (BFS) algorithm.
    httpMove: Handles move actions initiated via HTTP requests. It validates the move, updates the character's position, and returns the updated board state along with a message.

   Range Attack Manager

The Range Attack Manager handles ranged attack-related functionalities in the game. It allows game characters to perform ranged attacks on opponents within their effective range and line of sight.
Features

    performRangedAttack: Initiates a ranged attack by prompting the player to choose a target character and randomly determining if the attack hits based on the attacker's ballistic skill.
    checkRangedAttack: Checks if any passive units are within the effective range and line of sight of an active unit, returning a list of potential targets.
    performRangedAttackIfInRange: Performs ranged attacks for all active units with potential targets within their range and line of sight.
    rangeAttackHttpIfInRange: Handles ranged attacks initiated via HTTP requests, updating the game board accordingly.
    httpShoot: Executes a shoot action in the game based on the provided coordinates and avatar information.
    getActiveUnitsAndTargets: Retrieves active units and their potential targets for ranged attacks.

    Close Combat Package

Close Combat Manager

The Close Combat Manager facilitates close combat engagements within the game, allowing characters to engage in hand-to-hand combat with nearby opponents.
Features

    performCloseCombatAttack: Initiates a close combat attack by prompting the player to select a target character and randomly determining if the attack hits based on the attacker's weapon skill.
    checkCloseCombatAttack: Checks if any passive units are within striking distance for a given active unit, returning a list of potential targets.
    performCloseCombatAttackIfInRange: Performs close combat attacks for all active units with potential targets within their striking distance.
    performCloseCombatAttackHttpIfInRange: Handles close combat attacks initiated via HTTP requests, updating the game board accordingly.
    checkCloseCombatAttackHttp: Checks for potential targets for close combat attacks initiated via HTTP requests.
    performCloseCombatAttackHttp: Executes a close combat attack based on the provided target coordinates and avatar information.
    activeUnitsNotInAssaultRange: Identifies active units that have no potential targets within striking distance and updates their status accordingly.
    getActiveUnitsAndAssaultTargets: Retrieves active units and their potential targets for close combat engagements.


Check Victory Conditions

The Check Victory Conditions package is responsible for determining the outcome of the battle based on the current state of active and passive units.
Features

    checkVictory: Analyzes the status of active and passive units to determine the outcome of the battle. It evaluates if any passive units are still alive or if the victory conditions for either side have been met.
        If there are still passive units alive, it indicates that "The Battle Rages On."
        If all passive units are defeated, it identifies the victorious faction based on the avatar of the remaining active unit:
            If the remaining active unit is of avatar "S," it signifies "The Xenos have been Purged. A Glorious Victory for the Imperium."
            If the remaining active unit is of avatar "O," "9," "8," or "7," it declares "The Green Tide is Victorious. WAAAAAGGGGH!!!!"
        If there are no units left on the battlefield, it states "No units left on the battlefield."
    
Additional Notes

    The application utilizes caching for storing and retrieving the game board state.
    Endpoints are designed to handle JSON input and provide appropriate responses based on the game's current phase.
