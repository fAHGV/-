local GameConstants = {
    Teams = { Radiant = "Radiant", Dire = "Dire" },
    MatchPhases = {
        Lobby = "Lobby",
        MatchStart = "MatchStart",
        LanePhase = "LanePhase",
        MidGame = "MidGame",
        EndGame = "EndGame",
        Finished = "Finished",
    },
    Map = {
        Lanes = { "Top", "Mid", "Bottom" },
        WaveInterval = 30,
    },
    Economy = {
        BaseCreepGold = 35,
        HeroKillBase = 250,
        AssistFactor = 0.35,
        CreepBountyScalePerMinute = 0.02,
    },
    AbilitySlots = { "Q", "W", "E", "R" },
}

return GameConstants
