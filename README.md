## Windroid

MiceWine is a project that aims to run Windows applications and games on Android smartphones.

It uses a customized build of Wine compiled for Android and Box64 to run in the best possible way.

Actually in constant development.

It's use a XServer based on Termux-X11 

### Compatible Android Versions

Support Android 10+

### Compatible GPUs

| GPU        | Support               | Driver                      |
|------------|-----------------------|-----------------------------|
| Adreno     | Supported             | Turnip/Native/Zink          |
| Xclipse    | Supported             | Native/Zink                 |
| Mali       | Supported             | Native/Zink                 |

The native driver works well for Xclipse GPUs, Adreno GPUs, Mali GPUs.

About Mali GPUs, Only tested on Mali-G615, Mali-G610, Mali G76 and Mali-G925-Immortalis.

Vulkan 1.3 is Recommended for better performance and graphical precision. 1.1 works with DXVK-Stripped-Requirements, but displays various graphical issues.

## Third Party Open Source Applications:

- [Box64](https://github.com/ptitSeb/box64)
- [WineHQ](https://gitlab.winehq.org/wine/wine)
- [Termux-X11](https://github.com/termux/termux-x11)
- [Mesa](https://gitlab.freedesktop.org/mesa/mesa)
