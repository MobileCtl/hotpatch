# 🔧 HotPatch - Android Hotpatching Framework

Android hotpatching framework inspired by Shorebird. Deploy bug fixes without recompilation.

## Status

🚧 **POC Phase** - Working proof-of-concept

- ✅ Lua runtime integration
- ✅ Method interception  
- ✅ JSON marshalling
- ✅ Local & remote patch loading
- 🚧 GitHub remote patches (in progress)
- 📋 KSP compiler plugin (planned)
- 📋 CLI tool (planned)

## Quick Start

git clone https://github.com/mobilectl/hotpatch.git
./gradlew run


Click "Enable Patch" → "Calculate Total" → See $287.50 instead of $250 (with tax applied via Lua!)

## How It Works

Native Method (buggy)<br>
↓
<br>PatchRuntime.intercept()<br>
↓
<br>Download Lua patch from GitHub<br>
↓
<br>Execute via LuaRuntime<br>
↓
<br>Return patched result<br>


## Example Patch
```
function calculateTotalFromJson(jsonStr)
    local total = 0.0
    for price, qty in jsonStr:gmatch('"price":(%-?%d+%.?%d*)[^}]*"quantity":(%d+)') do
        local itemTotal = tonumber(price) * tonumber(qty) * 1.15
        total = total + itemTotal
    end
    return total
end
```


## Tech Stack

- **Scripting:** Lua 5.1 (LuaJIT)
- **Bridge:** LuaJava JNI
- **Patches:** GitHub (MVP)
- **Serialization:** JSON

## Contributing

Issues and PRs welcome! This is early stage, feedback appreciated.

## License

MIT

