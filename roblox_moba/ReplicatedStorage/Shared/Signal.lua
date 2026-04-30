local Signal = {}
Signal.__index = Signal

function Signal.new()
    return setmetatable({ _callbacks = {} }, Signal)
end

function Signal:Connect(fn)
    self._callbacks[fn] = true
    return {
        Disconnect = function()
            self._callbacks[fn] = nil
        end,
    }
end

function Signal:Fire(...)
    for cb in pairs(self._callbacks) do
        cb(...)
    end
end

return Signal
