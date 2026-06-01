package com.alenvpn.al.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.alenvpn.al.data.AppSettings

object Translator {
    val en = mapOf(
        "Settings" to "Settings",
        "Kill Switch" to "Kill Switch",
        "Auto Connect" to "Auto Connect",
        "Persistent Keepalive" to "Persistent Keepalive",
        "Default MTU" to "Default MTU",
        "Theme" to "Theme",
        "Language" to "Language",
        "System Logs" to "System Logs",
        "Check for Updates" to "Check for Updates",
        "Connect to VPN" to "CONNECT TO VPN",
        "Disconnect" to "DISCONNECT",
        "Add Profile" to "Add Profile",
        "Edit Profile" to "Edit Profile",
        "Speed Test" to "Speed Test",
        "Home" to "Home",
        "Connection" to "Connection",
        "Appearance" to "Appearance",
        "General" to "General",
        "About" to "About"
    )
    val de = mapOf(
        "Settings" to "Einstellungen",
        "Kill Switch" to "Notausschalter",
        "Auto Connect" to "Automatisch verbinden",
        "Persistent Keepalive" to "Dauerhaftes Keepalive",
        "Default MTU" to "Standard-MTU",
        "Theme" to "Thema",
        "Language" to "Sprache",
        "System Logs" to "Systemprotokolle",
        "Check for Updates" to "Auf Updates prüfen",
        "Connect to VPN" to "MIT VPN VERBINDEN",
        "Disconnect" to "TRENNEN",
        "Add Profile" to "Profil hinzufügen",
        "Edit Profile" to "Profil bearbeiten",
        "Speed Test" to "Geschwindigkeitstest",
        "Home" to "Startseite",
        "Connection" to "Verbindung",
        "Appearance" to "Erscheinungsbild",
        "General" to "Allgemeines",
        "About" to "Über"
    )
    val it = mapOf(
        "Settings" to "Impostazioni",
        "Kill Switch" to "Kill Switch",
        "Auto Connect" to "Connessione automatica",
        "Persistent Keepalive" to "Keepalive persistente",
        "Default MTU" to "MTU predefinito",
        "Theme" to "Tema",
        "Language" to "Lingua",
        "System Logs" to "Log di sistema",
        "Check for Updates" to "Controlla aggiornamenti",
        "Connect to VPN" to "CONNETTI ALLA VPN",
        "Disconnect" to "DISCONNETTI",
        "Add Profile" to "Aggiungi profilo",
        "Edit Profile" to "Modifica profilo",
        "Speed Test" to "Test di velocità",
        "Home" to "Home",
        "Connection" to "Connessione",
        "Appearance" to "Aspetto",
        "General" to "Generale",
        "About" to "Info"
    )
    val fr = mapOf(
        "Settings" to "Paramètres",
        "Kill Switch" to "Kill Switch",
        "Auto Connect" to "Connexion automatique",
        "Persistent Keepalive" to "Keepalive persistant",
        "Default MTU" to "MTU par défaut",
        "Theme" to "Thème",
        "Language" to "Langue",
        "System Logs" to "Journaux système",
        "Check for Updates" to "Vérifier les mises à jour",
        "Connect to VPN" to "SE CONNECTER AU VPN",
        "Disconnect" to "DÉCONNECTER",
        "Add Profile" to "Ajouter un profil",
        "Edit Profile" to "Modifier le profil",
        "Speed Test" to "Test de vitesse",
        "Home" to "Accueil",
        "Connection" to "Connexion",
        "Appearance" to "Apparence",
        "General" to "Général",
        "About" to "À propos"
    )
    val ru = mapOf(
        "Settings" to "Настройки",
        "Kill Switch" to "Разрывать соединение",
        "Auto Connect" to "Автоподключение",
        "Persistent Keepalive" to "Постоянный Keepalive",
        "Default MTU" to "MTU по умолчанию",
        "Theme" to "Тема",
        "Language" to "Язык",
        "System Logs" to "Системные журналы",
        "Check for Updates" to "Проверить обновления",
        "Connect to VPN" to "ПОДКЛЮЧИТЬСЯ",
        "Disconnect" to "ОТКЛЮЧИТЬСЯ",
        "Add Profile" to "Добавить профиль",
        "Edit Profile" to "Редактировать профиль",
        "Speed Test" to "Тест скорости",
        "Home" to "Главная",
        "Connection" to "Соединение",
        "Appearance" to "Внешний вид",
        "General" to "Общие",
        "About" to "О программе"
    )
    val zh = mapOf(
        "Settings" to "设置",
        "Kill Switch" to "终止开关",
        "Auto Connect" to "自动连接",
        "Persistent Keepalive" to "持久保活",
        "Default MTU" to "默认 MTU",
        "Theme" to "主题",
        "Language" to "语言",
        "System Logs" to "系统日志",
        "Check for Updates" to "检查更新",
        "Connect to VPN" to "连接 VPN",
        "Disconnect" to "断开连接",
        "Add Profile" to "添加配置",
        "Edit Profile" to "编辑配置",
        "Speed Test" to "速度测试",
        "Home" to "主页",
        "Connection" to "连接",
        "Appearance" to "外观",
        "General" to "常规",
        "About" to "关于"
    )
    val sq = mapOf(
        "Settings" to "Cilësimet",
        "Kill Switch" to "Kill Switch",
        "Auto Connect" to "Lidhu Automatikisht",
        "Persistent Keepalive" to "Keepalive i Përhershëm",
        "Default MTU" to "MTU e Parazgjedhur",
        "Theme" to "Tema",
        "Language" to "Gjuha",
        "System Logs" to "Regjistrat e Sistemit",
        "Check for Updates" to "Kontrollo për Përditësime",
        "Connect to VPN" to "LIDHU ME VPN",
        "Disconnect" to "SHKËPUTU",
        "Add Profile" to "Shto Profil",
        "Edit Profile" to "Ndrysho Profil",
        "Speed Test" to "Test i Shpejtësisë",
        "Home" to "Kreu",
        "Connection" to "Lidhja",
        "Appearance" to "Pamja",
        "General" to "Të Përgjithshme",
        "About" to "Rreth"
    )

    fun getString(key: String, lang: String): String {
        val map = when(lang) {
            "de" -> de
            "it" -> it
            "fr" -> fr
            "ru" -> ru
            "zh" -> zh
            "sq" -> sq
            else -> en
        }
        return map[key] ?: en[key] ?: key
    }
}

@Composable
fun tr(key: String): String {
    val lang = AppSettings.language.collectAsState().value
    return Translator.getString(key, lang)
}
