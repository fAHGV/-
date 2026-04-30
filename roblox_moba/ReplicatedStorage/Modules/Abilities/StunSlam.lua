local AbilityBase = require(script.Parent.AbilityBase)

local StunSlam = setmetatable({}, AbilityBase)
StunSlam.__index = StunSlam

function StunSlam.new()
    return setmetatable(AbilityBase.new({ Id = "StunSlam", TargetType = "Self", Cooldown = 14, ManaCost = 110, Range = 260 }), StunSlam)
end

function StunSlam:Execute(ctx)
    local targets = ctx.QueryService:GetEnemiesInRadius(ctx.Caster, ctx.Origin, self.Range)
    for _, enemy in ipairs(targets) do
        ctx.DamageService:ApplyDamage(ctx.Caster, enemy, "Physical", 120)
        ctx.CrowdControlService:ApplyStun(enemy, 1.4)
    end
end

return StunSlam
