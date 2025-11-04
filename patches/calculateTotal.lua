function decodeJson(jsonStr)
-- Remove brackets and split
	local items = {}
	-- This is simplified - in production use a real JSON library
	for match in jsonStr:gmatch('{[^}]*}') do
		table.insert(items, match)
	end
	return items
end

function calculateTotalFromJson(jsonStr)
	local total = 0.0
	for price, qty in jsonStr:gmatch('"price":(%-?%d+%.?%d*)[^}]*"quantity":(%d+)') do
		local itemTotal = tonumber(price) * tonumber(qty) * 1.15
		total = total + itemTotal
	end
	return total
end
