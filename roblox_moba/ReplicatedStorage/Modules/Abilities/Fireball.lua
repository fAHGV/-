local AbilityBase = require(script.Parent.AbilityBase)

local Fireball = setmetatable({}, AbilityBase)
Fireball.__index = Fireball

function Fireball.new()
    return setmetatable(AbilityBase.new({
        Id = "Fireball",
        TargetType = "Point",
        Cooldown = 6,
        ManaCost = 90,
        Range = 900,
    }), Fireball)
end

function Fireball:Execute(ctx)
    ctx.ProjectileService:SpawnProjectile({
        Caster = ctx.Caster,
        Origin = ctx.Origin,
        TargetPosition = ctx.TargetPosition,
        Radius = 16,
        Speed = 120,
        Damage = 140 + ctx.Scaling.AbilityPower * 0.7,
        OnHit = function(victims)
            for _, victim in ipairs(victims) do
                ctx.DamageService:ApplyDamage(ctx.Caster, victim, "Magical", 140)
            end
        end,
    })
end

return Fireball
