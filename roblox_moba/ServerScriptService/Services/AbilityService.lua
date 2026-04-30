local ReplicatedStorage = game:GetService("ReplicatedStorage")

local AbilityModules = {
    Fireball = require(ReplicatedStorage.Modules.Abilities.Fireball),
    Dash = require(ReplicatedStorage.Modules.Abilities.Dash),
    StunSlam = require(ReplicatedStorage.Modules.Abilities.StunSlam),
}

local AbilityService = {}
AbilityService.__index = AbilityService

function AbilityService.new(deps)
    return setmetatable({
        _deps = deps,
        _cooldowns = {},
    }, AbilityService)
end

function AbilityService:Cast(player, heroState, abilityId, payload)
    local abilityCtor = AbilityModules[abilityId]
    if not abilityCtor then return false, "UnknownAbility" end
    local ability = abilityCtor.new()
    local state = {
        Mana = heroState.Mana,
        CooldownUntil = (self._cooldowns[player] and self._cooldowns[player][abilityId]) or 0,
    }
    local ok, reason = ability:CanCast(player, state)
    if not ok then return false, reason end

    -- anti-exploit validation
    if payload.TargetPosition and (payload.TargetPosition - payload.Origin).Magnitude > ability.Range then
        return false, "OutOfRange"
    end

    heroState.Mana -= ability.ManaCost
    self._cooldowns[player] = self._cooldowns[player] or {}
    self._cooldowns[player][abilityId] = os.clock() + ability.Cooldown

    ability:Execute(self._deps:BuildAbilityContext(player, heroState, payload))
    return true
end

return AbilityService
