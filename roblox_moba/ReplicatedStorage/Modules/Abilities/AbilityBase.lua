local AbilityBase = {}
AbilityBase.__index = AbilityBase

function AbilityBase.new(config)
    local self = setmetatable({}, AbilityBase)
    self.Id = config.Id
    self.TargetType = config.TargetType
    self.Cooldown = config.Cooldown or 1
    self.ManaCost = config.ManaCost or 0
    self.Range = config.Range or 0
    return self
end

function AbilityBase:CanCast(caster, state)
    if state.Mana < self.ManaCost then return false, "NotEnoughMana" end
    if state.CooldownUntil and state.CooldownUntil > os.clock() then return false, "Cooldown" end
    return true
end

function AbilityBase:Execute(_ctx)
    error("Execute not implemented for " .. self.Id)
end

return AbilityBase
