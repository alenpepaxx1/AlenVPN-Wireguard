# AlenVpn

AlenVpn is a modern, high-performance VPN client built with Kotlin and Jetpack Compose. It leverages the powerful WireGuard® protocol to provide fast, secure, and reliable connections.

## Features

- **WireGuard Protocol**: Native support for the state-of-the-art WireGuard VPN protocol for maximum performance and security.
- **Modern UI/UX**: Built entirely with Jetpack Compose and Material 3 design guidelines, offering a sleek, responsive, and intuitive interface.
- **Adaptive Design**: Fully optimized for various screen sizes, including mobile phones, tablets, and Android TV.
- **Profile Management**: Easily import, create, edit, and delete WireGuard profiles.
- **Real-time Connection Stats**: Monitor your download/upload speeds and data usage with integrated charts and live indicators.
- **Ping Check**: Verify connectivity and latency to your VPN endpoints directly from the app.
- **Dark Mode Support**: Seamlessly switches between light and dark themes based on your system settings or preference.
- **Local Persistence**: Securely stores your profiles locally using the Room database.

## Technical Details

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Design System**: [Material Design 3](https://m3.material.io/)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: [Room](https://developer.android.com/training/data-storage/room)
- **VPN Backend**: [WireGuard Android Tunnel Library](https://github.com/WireGuard/wireguard-android)
- **Networking**: Retrofit / Ktor (for IP detection)

## Getting Started

### Prerequisites

- Android 8.0 (API level 26) or higher.
- A WireGuard VPN profile (conf file or manual configuration details).

### Installation

1. Clone this repository.
2. Open the project in Android Studio.
3. Build and run the app on your device or emulator.

## How to Contribute

Contributions are welcome! Please feel free to submit a Pull Request or open an issue for any bugs or feature requests.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

*WireGuard is a registered trademark of Jason A. Donenfeld.*


## Developer Alen Pepa
