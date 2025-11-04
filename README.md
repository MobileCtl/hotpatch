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


---

## 🤝 Contributing

This is a proof-of-concept project. Feedback and contributions welcome!

1. Fork the repo
2. Create feature branch (`git checkout -b feature/amazing-thing`)
3. Commit changes (`git commit -m 'feat: add amazing thing'`)
4. Push to branch (`git push origin feature/amazing-thing`)
5. Open Pull Request

---

## 🗺️ Roadmap

### MVP (Phase 1)
- [ ] Remote patches from GitHub ✨ **NEXT**
- [ ] Automated patch discovery
- [ ] Patch versioning & rollback
- [ ] Production demo

### v1.0 (Phase 2)
- [ ] KSP compiler plugin
- [ ] CLI tool (`mobilectl`)
- [ ] Backend patch server
- [ ] Multiple method support

### v2.0+ (Phase 3+)
- [ ] KMM/iOS support
- [ ] Patch signing & security
- [ ] Advanced analytics
- [ ] Enterprise features

---

## ⚠️ Limitations (POC)

- Manual method interception (auto-detection via KSP coming)
- Single method patching (multiple methods planned)
- No patch signing (security coming)
- Limited type support (JSON marshalling workaround)
- Android only (KMM planned)

---

## 📝 License

MIT License - see [LICENSE](LICENSE) file for details.

You can use this for personal and commercial projects. Just give credit.

---

## 🙋 FAQ

**Q: Why Lua?**
A: LuaJIT is lightweight, fast, and proven in games/embedded systems. Perfect for runtime patches.

**Q: Can this replace app updates?**
A: No, use for hotfixes and features. Major features should go through normal update process.

**Q: Is this production-ready?**
A: No, it's a POC. Phase 2 will add security, versioning, and production safeguards.

**Q: How does it compare to Shorebird?**
A: Similar goals (hotpatching), different implementation. Lua-based vs Dart-based. Complementary approaches.

**Q: When's KMM support?**
A: Phase 3, after core Android features are solid.

**Q: Can I help?**
A: Yes! Open an issue with ideas or PRs with improvements.

---

## 📧 Contact & Support

- **GitHub Issues:** [Report bugs or suggest features](https://github.com/mobilectl/hotpatch/issues)
- **Discussions:** [Ask questions](https://github.com/mobilectl/hotpatch/discussions)

---

## 🎓 Resources

- [LuaJIT Documentation](https://luajit.org/)
- [Android Development](https://developer.android.com/)
- [Shorebird Documentation](https://docs.shorebird.dev/)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

---

## 📊 Stats

- **Language:** Kotlin
- **Platform:** Android
- **State of Development:** POC Phase
- **Lines of Code:** ~2,000
- **Test Coverage:** Core runtime ✅
- **Time to POC:** 2 weeks ⚡

---

## 🙏 Acknowledgments

- Inspired by **Shorebird** (amazing work!)
- Built with **LuaJIT** (incredible Lua VM)
- Powered by **Android** ecosystem
---

**Built with ❤️ for the Android developer community.**

*Ready to hotpatch? Let's go!* 🚀
