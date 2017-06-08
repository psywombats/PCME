import qbs 1.0

TiledPlugin {
    cpp.defines: ["DCSS_LIBRARY"]

    files: [
        "dcss_global.h",
        "dcssplugin.cpp",
        "dcssplugin.h",
        "plugin.json",
    ]
}
