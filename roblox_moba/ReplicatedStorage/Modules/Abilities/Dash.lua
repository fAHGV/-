local AbilityBase = require(script.Parent.AbilityBase)

local Dash = setmetatable({}, AbilityBase)
Dash.__index = Dash

function Dash.new()
    return setmetatable(AbilityBase.new({ Id = "Dash", TargetType = "Point", Cooldown = 10, ManaCost = 50, Range = 450 }), Dash)
end

function Dash:Execute(ctx)
    ctx.MovementService:Dash(ctx.Caster, ctx.TargetPosition, 0.2)
end

return Dash
