package com.example.ui.util

import com.example.data.model.LanguageMode

/**
 * Localization strings for Settings, Cloud Sync, AI providers, and Application Preferences.
 */
object AppLocalizationSettings {
    fun getSettingsTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Einstellungen & Cloud Hub"
        LanguageMode.FRENCH -> "Paramètres & Cloud Hub"
        LanguageMode.ITALIAN -> "Impostazioni e Cloud Hub"
        LanguageMode.SPANISH -> "Ajustes y Cloud Hub"
        LanguageMode.DUTCH -> "Instellingen & Cloud Hub"
        else -> "Settings & Cloud Hub"
    }

    fun getSettingsSubtitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Synchronisation, Backups, KI-Fotografie & Präferenzen verwalten"
        LanguageMode.FRENCH -> "Gérer la synchronisation, les sauvegardes et la génération IA"
        LanguageMode.ITALIAN -> "Gestisci sincronizzazione, backup e generazione foto IA"
        LanguageMode.SPANISH -> "Gestionar sincronización, copias de seguridad y fotos con IA"
        LanguageMode.DUTCH -> "Beheer synchronisatie, back-ups en AI-fotogeneratie"
        else -> "Manage sync, backups, AI photo generation & preferences"
    }

    fun getLanguageSectionTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "App-Sprache & Übersetzung"
        LanguageMode.FRENCH -> "Langue de l'Application"
        LanguageMode.ITALIAN -> "Lingua dell'Applicazione"
        LanguageMode.SPANISH -> "Idioma de la Aplicación"
        LanguageMode.DUTCH -> "Applicatietaal"
        else -> "App Display Language"
    }

    fun getLanguageSectionSubtitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Menüs, Buttons, Einstellungen und Rezepte in Echtzeit anpassen"
        LanguageMode.FRENCH -> "Changer instantanément tous les menus, boutons et recettes"
        LanguageMode.ITALIAN -> "Cambia istantaneamente menu, pulsanti e ricette"
        LanguageMode.SPANISH -> "Cambiar menús, botones y recetas al instante"
        LanguageMode.DUTCH -> "Pas menu's, knoppen en recepten direct aan"
        else -> "Switch UI words, tabs, and recipe titles"
    }

    fun getCollectionOverviewTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Rezeptsammlung Übersicht"
        LanguageMode.FRENCH -> "Aperçu de la Collection"
        LanguageMode.ITALIAN -> "Panoramica della Collezione"
        LanguageMode.SPANISH -> "Resumen de la Colección"
        LanguageMode.DUTCH -> "Overzicht Receptcollectie"
        else -> "Cookbook Collection Overview"
    }

    fun getBatchGeneratePhotosButton(count: Int, lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "KI-Fotos im Stapel generieren ($count bereit)"
        LanguageMode.FRENCH -> "Générer les photos IA en lot ($count prêtes)"
        LanguageMode.ITALIAN -> "Genera foto IA in blocco ($count pronte)"
        LanguageMode.SPANISH -> "Generar fotos por lotes con IA ($count listas)"
        LanguageMode.DUTCH -> "AI-foto's in bulk genereren ($count gereed)"
        else -> "Batch Generate AI Photos ($count ready)"
    }

    fun getCloudSyncTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Cloud Hub & Multi-Geräte Sync"
        LanguageMode.FRENCH -> "Cloud Hub & Synchronisation Multi-Appareils"
        LanguageMode.ITALIAN -> "Cloud Hub & Sincronizzazione Multi-Dispositivo"
        LanguageMode.SPANISH -> "Cloud Hub y Sincronización Multi-Dispositivo"
        LanguageMode.DUTCH -> "Cloud Hub & Multi-Apparaat Synchronisatie"
        else -> "Cloud Hub & Multi-Device Sync"
    }

    fun getCloudSyncActiveBadge(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Aktiv • Verbunden mit privatem VPS"
        LanguageMode.FRENCH -> "Actif • Connecté au VPS privé"
        LanguageMode.ITALIAN -> "Attivo • Connesso al VPS privato"
        LanguageMode.SPANISH -> "Activo • Conectado al VPS privado"
        LanguageMode.DUTCH -> "Actief • Verbonden met privé VPS"
        else -> "Active • Connected to Private VPS"
    }

    fun getCloudSyncDisabledBadge(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Deaktiviert • Nur lokaler Speicher"
        LanguageMode.FRENCH -> "Désactivé • Stockage local uniquement"
        LanguageMode.ITALIAN -> "Disattivato • Solo archiviazione locale"
        LanguageMode.SPANISH -> "Desactivado • Solo almacenamiento local"
        LanguageMode.DUTCH -> "Uitgeschakeld • Alleen lokale opslag"
        else -> "Disabled • Local storage only"
    }

    fun getCloudSyncDescription(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Synchronisiere Kochbücher, Rezepte, Formeln & Fotos automatisch über dein privates Cloud-VPS oder Nextcloud-Konto."
        LanguageMode.FRENCH -> "Synchronisez automatiquement vos recettes, formules et photos sur votre serveur privé."
        LanguageMode.ITALIAN -> "Sincronizza automaticamente ricettari, formule e foto tramite il tuo server privato."
        LanguageMode.SPANISH -> "Sincroniza automáticamente recetas, fórmulas y fotos a través de tu servidor privado."
        LanguageMode.DUTCH -> "Synchroniseer kookboeken, formules en foto's automatisch via je privéserver."
        else -> "Auto-sync cookbooks, formulas & photos across family tablets via your private VPS or Nextcloud server."
    }

    fun getServerUrlLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Server URL / Endpunkt"
        LanguageMode.FRENCH -> "URL du Serveur / Point d'accès"
        LanguageMode.ITALIAN -> "URL Server / Endpoint"
        LanguageMode.SPANISH -> "URL del Servidor / Punto de acceso"
        LanguageMode.DUTCH -> "Server-URL / Eindpunt"
        else -> "Server URL / Endpoint"
    }

    fun getSecretTokenLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Geheimer Synchronisations-Token"
        LanguageMode.FRENCH -> "Jeton de Synchronisation Secret"
        LanguageMode.ITALIAN -> "Token di Sincronizzazione Segreto"
        LanguageMode.SPANISH -> "Token de Sincronización Secreto"
        LanguageMode.DUTCH -> "Geheime Synchronisatie-token"
        else -> "Secret Sync Token"
    }

    fun getTestConnectionButton(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Verbindung testen"
        LanguageMode.FRENCH -> "Tester Connexion"
        LanguageMode.ITALIAN -> "Verifica Connessione"
        LanguageMode.SPANISH -> "Probar Conexión"
        LanguageMode.DUTCH -> "Verbinding testen"
        else -> "Test Connection"
    }

    fun getTestingConnectionButton(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Verbindung wird geprüft..."
        LanguageMode.FRENCH -> "Vérification de la connexion..."
        LanguageMode.ITALIAN -> "Verifica della connessione..."
        LanguageMode.SPANISH -> "Verificando conexión..."
        LanguageMode.DUTCH -> "Verbinding controleren..."
        else -> "Testing Connection..."
    }

    fun getSyncNowButton(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Jetzt synchronisieren"
        LanguageMode.FRENCH -> "Synchroniser maintenant"
        LanguageMode.ITALIAN -> "Sincronizza ora"
        LanguageMode.SPANISH -> "Sincronizar ahora"
        LanguageMode.DUTCH -> "Nu synchroniseren"
        else -> "Sync Now"
    }

    fun getAutoSyncLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Automatische Hintergrund-Synchronisation"
        LanguageMode.FRENCH -> "Synchronisation automatique en arrière-plan"
        LanguageMode.ITALIAN -> "Sincronizzazione automatica in background"
        LanguageMode.SPANISH -> "Sincronización automática en segundo plano"
        LanguageMode.DUTCH -> "Automatische achtergrondsycnhronisatie"
        else -> "Automatic background sync"
    }

    fun getImmediateSyncLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Beim Speichern sofort hochladen"
        LanguageMode.FRENCH -> "Envoyer immédiatement lors de l'enregistrement"
        LanguageMode.ITALIAN -> "Carica immediatamente al salvataggio"
        LanguageMode.SPANISH -> "Subir inmediatamente al guardar"
        LanguageMode.DUTCH -> "Direct uploaden bij opslaan"
        else -> "Upload immediately upon saving"
    }

    fun getAutoWeeklyBackupTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Automatische wöchentliche Sicherung"
        LanguageMode.FRENCH -> "Sauvegardes Automatiques Hebdomadaires"
        LanguageMode.ITALIAN -> "Backup Settimanali Automatici"
        LanguageMode.SPANISH -> "Copias de Seguridad Semanales Automáticas"
        LanguageMode.DUTCH -> "Automatische Wekelijkse Back-ups"
        else -> "Weekly Automated Backups"
    }

    fun getAutoWeeklyBackupBadge(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Aktiviert • Sichert alle 7 Tage automatisch"
        LanguageMode.FRENCH -> "Activé • Sauvegarde auto tous les 7 jours"
        LanguageMode.ITALIAN -> "Attivato • Backup auto ogni 7 giorni"
        LanguageMode.SPANISH -> "Activado • Copia auto cada 7 días"
        LanguageMode.DUTCH -> "Ingeschakeld • Back-up auto elke 7 dagen"
        else -> "Enabled • Backs up automatically every 7 days"
    }

    fun getAutoWeeklyBackupDesc(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Erstellt jeden Sonntag automatisch ein vollständiges ZIP-Archiv deiner Rezeptdatenbank und speichert es lokal auf diesem Gerät."
        LanguageMode.FRENCH -> "Crée automatiquement chaque dimanche une archive ZIP complète de vos recettes sur cet appareil."
        LanguageMode.ITALIAN -> "Crea automaticamente ogni domenica un archivio ZIP completo delle tue ricette su questo dispositivo."
        LanguageMode.SPANISH -> "Crea automáticamente cada domingo un archivo ZIP completo de tus recetas en este dispositivo."
        LanguageMode.DUTCH -> "Maakt elke zondag automatisch een compleet ZIP-archief van je recepten op dit apparaat."
        else -> "Creates a full ZIP snapshot archive of your cookbook database every Sunday and keeps it saved on this device."
    }

    fun getOpenBackupManagerButton(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Datensicherungs- & Wiederherstellungs-Tool öffnen"
        LanguageMode.FRENCH -> "Ouvrir l'outil de sauvegarde & restauration"
        LanguageMode.ITALIAN -> "Apri strumento di backup e ripristino"
        LanguageMode.SPANISH -> "Abrir herramienta de copia y restauración"
        LanguageMode.DUTCH -> "Back-up- & hersteltool openen"
        else -> "Open Backup & Restore Tool"
    }

    fun getGeminiTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Google Gemini & Küchen-Assistent KI"
        LanguageMode.FRENCH -> "Google Gemini & Sous-Chef IA"
        LanguageMode.ITALIAN -> "Google Gemini e Aiuto-Cuoco IA"
        LanguageMode.SPANISH -> "Google Gemini y Ayudante de Cocina IA"
        LanguageMode.DUTCH -> "Google Gemini & Keukenhulp AI"
        else -> "Google Gemini & Sous-Chef AI"
    }

    fun getGeminiSubtitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Schlüssel erforderlich für OCR-Kartenscan & Sprachassistent"
        LanguageMode.FRENCH -> "Clé requise pour la numérisation OCR et l'assistant vocal"
        LanguageMode.ITALIAN -> "Chiave richiesta per scansione OCR e assistente vocale"
        LanguageMode.SPANISH -> "Clave requerida para escaneo OCR y asistente de voz"
        LanguageMode.DUTCH -> "Sleutel vereist voor OCR-kaartscan & spraakassistent"
        else -> "Key required for card OCR & voice assistant"
    }

    fun getGeminiDesc(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Für das automatische Auslesen handgeschriebener Rezeptkarten, Mengenumrechnungen und KI-Sprachbefehle."
        LanguageMode.FRENCH -> "Pour transcrire les fiches manuscrites, convertir les unités et exécuter les commandes vocales."
        LanguageMode.ITALIAN -> "Per trascrivere schede manoscritte, convertire unità ed eseguire comandi vocali."
        LanguageMode.SPANISH -> "Para transcribir tarjetas manuscritas, convertir unidades y ejecutar comandos de voz."
        LanguageMode.DUTCH -> "Voor het transcriberen van handgeschreven kaarten, maatomrekening en spraakopdrachten."
        else -> "Powers multi-page card vision scans, ingredient extraction, and voice assistant features."
    }

    fun getGeminiKeyLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Google Gemini API-Schlüssel"
        LanguageMode.FRENCH -> "Clé API Google Gemini"
        LanguageMode.ITALIAN -> "Chiave API Google Gemini"
        LanguageMode.SPANISH -> "Clave API de Google Gemini"
        LanguageMode.DUTCH -> "Google Gemini API-sleutel"
        else -> "Google Gemini API Key"
    }

    fun getGeminiTestButton(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Gemini API Verbindung testen"
        LanguageMode.FRENCH -> "Tester la connexion API Gemini"
        LanguageMode.ITALIAN -> "Verifica connessione API Gemini"
        LanguageMode.SPANISH -> "Probar conexión con API de Gemini"
        LanguageMode.DUTCH -> "Gemini API-verbinding testen"
        else -> "Test Gemini API Connection"
    }

    fun getGeminiKeyHelp(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "💡 Kostenlosen API-Schlüssel erstellen auf aistudio.google.com/apikey"
        LanguageMode.FRENCH -> "💡 Obtenez une clé API gratuite sur aistudio.google.com/apikey"
        LanguageMode.ITALIAN -> "💡 Ottieni una chiave API gratuita su aistudio.google.com/apikey"
        LanguageMode.SPANISH -> "💡 Obtén una clave API gratuita en aistudio.google.com/apikey"
        LanguageMode.DUTCH -> "💡 Krijg een gratis API-sleutel op aistudio.google.com/apikey"
        else -> "💡 Get a free API key at aistudio.google.com/apikey"
    }

    fun getAiPhotoEngineSubtitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Wähle Cloud Gemini KI oder lokales ComfyUI auf deinem PC"
        LanguageMode.FRENCH -> "Choisissez Gemini Cloud IA ou ComfyUI sur PC local"
        LanguageMode.ITALIAN -> "Scegli Gemini Cloud IA o ComfyUI su PC locale"
        LanguageMode.SPANISH -> "Elige Gemini Cloud IA o ComfyUI en PC local"
        LanguageMode.DUTCH -> "Kies Cloud Gemini AI of lokale PC ComfyUI"
        else -> "Choose Cloud Gemini AI or your local PC's ComfyUI"
    }

    fun getCookingAndAppPrefsTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Koch- & App-Einstellungen"
        LanguageMode.FRENCH -> "Préférences de Cuisson & App"
        LanguageMode.ITALIAN -> "Preferenze di Cucina e App"
        LanguageMode.SPANISH -> "Preferencias de Cocina y App"
        LanguageMode.DUTCH -> "Kook- & App-instellingen"
        else -> "Cooking & App Preferences"
    }

    fun getKeepScreenAwakeTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Bildschirm im Kochmodus aktiv halten"
        LanguageMode.FRENCH -> "Garder l'écran allumé en mode cuisson"
        LanguageMode.ITALIAN -> "Mantieni schermo attivo in modalità cucina"
        LanguageMode.SPANISH -> "Mantener pantalla encendida en modo cocina"
        LanguageMode.DUTCH -> "Scherm aanhouden tijdens koken"
        else -> "Keep Screen Awake in Cook Mode"
    }

    fun getKeepScreenAwakeDesc(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Verhindert Display-Abschaltung beim Kochen in der Küche"
        LanguageMode.FRENCH -> "Empêche l'écran de s'éteindre pendant la cuisine"
        LanguageMode.ITALIAN -> "Impedisce lo spegnimento dello schermo durante la preparazione"
        LanguageMode.SPANISH -> "Evita que la pantalla se apague mientras cocinas"
        LanguageMode.DUTCH -> "Voorkomt time-out van het scherm tijdens het koken"
        else -> "Prevents display timeout while cooking in the kitchen"
    }

    fun getSoundEffectsTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Soundeffekte & Umblättern"
        LanguageMode.FRENCH -> "Effets Sonores & Audio"
        LanguageMode.ITALIAN -> "Effetti Sonori e Audio"
        LanguageMode.SPANISH -> "Efectos de Sonido y Audio"
        LanguageMode.DUTCH -> "Geluidseffecten & Audio"
        else -> "Sound Effects & Page Turns"
    }

    fun getSoundEffectsDesc(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Sanfte Blätter-Sounds und Timer-Signaltöne"
        LanguageMode.FRENCH -> "Sons tactiles de feuilletage et minuteries"
        LanguageMode.ITALIAN -> "Suoni tattili di sfogliamento e suonerie timer"
        LanguageMode.SPANISH -> "Sonidos de cambio de página y alarmas de temporizador"
        LanguageMode.DUTCH -> "Voelbare pagina-omslaggeluiden en timersignalen"
        else -> "Tactile page flip sounds and timer chimes"
    }

    fun getUnitSystemTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Standard-Maßsystem & Einheiten"
        LanguageMode.FRENCH -> "Système de Mesure par Défaut"
        LanguageMode.ITALIAN -> "Sistema di Misura Preferito"
        LanguageMode.SPANISH -> "Sistema de Medidas Preferido"
        LanguageMode.DUTCH -> "Voorkeurs Maateenheden"
        else -> "Default Measuring Style & Unit System"
    }

    fun getUnitSystemDesc(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Einheiten können auch auf jeder Rezeptkarte einzeln umgeschaltet werden"
        LanguageMode.FRENCH -> "Vous pouvez aussi changer les unités sur chaque fiche recette"
        LanguageMode.ITALIAN -> "Puoi anche cambiare le unità su ogni singola scheda ricetta"
        LanguageMode.SPANISH -> "También puedes cambiar unidades en cada tarjeta de receta"
        LanguageMode.DUTCH -> "Je kunt maateenheden ook op elke receptkaart afzonderlijk wijzigen"
        else -> "You can also switch units on any individual recipe card"
    }

    fun getDangerZoneTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Gefahrenzone"
        LanguageMode.FRENCH -> "Zone de Danger"
        LanguageMode.ITALIAN -> "Zona di Pericolo"
        LanguageMode.SPANISH -> "Zona de Peligro"
        LanguageMode.DUTCH -> "Gevarenzone"
        else -> "Danger Zone"
    }

    fun getDangerZoneDesc(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Alle Rezepte und Bilder unwiderruflich von diesem Gerät entfernen"
        LanguageMode.FRENCH -> "Supprimer irréversiblement toutes les recettes de cet appareil"
        LanguageMode.ITALIAN -> "Elimina definitivamente tutte le ricette da questo dispositivo"
        LanguageMode.SPANISH -> "Eliminar irreversiblemente todas las recetas de este dispositivo"
        LanguageMode.DUTCH -> "Verwijder alle recepten en afbeeldingen definitief van dit apparaat"
        else -> "Permanently remove all recipes and local data from this device"
    }

    fun getDoneButton(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Fertig"
        LanguageMode.FRENCH -> "Terminé"
        LanguageMode.ITALIAN -> "Fatto"
        LanguageMode.SPANISH -> "Listo"
        LanguageMode.DUTCH -> "Klaar"
        else -> "Done"
    }

    fun getEditButtonLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Bearbeiten"
        LanguageMode.FRENCH -> "Modifier"
        LanguageMode.ITALIAN -> "Modifica"
        LanguageMode.SPANISH -> "Editar"
        LanguageMode.DUTCH -> "Bewerken"
        else -> "Edit"
    }

    fun getShareButtonLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Teilen"
        LanguageMode.FRENCH -> "Partager"
        LanguageMode.ITALIAN -> "Condividi"
        LanguageMode.SPANISH -> "Compartir"
        LanguageMode.DUTCH -> "Delen"
        else -> "Share"
    }

    fun getDeleteAllTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Alle Rezepte löschen"
        LanguageMode.FRENCH -> "Supprimer toutes les recettes"
        LanguageMode.ITALIAN -> "Elimina tutte le ricette"
        LanguageMode.SPANISH -> "Eliminar todas las recetas"
        LanguageMode.DUTCH -> "Verwijder alle recepten"
        else -> "Delete All Recipes"
    }

    fun getDeleteAllConfirmDialogTitle(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Wirklich alle Rezepte löschen?"
        LanguageMode.FRENCH -> "Vraiment supprimer toutes les recettes ?"
        LanguageMode.ITALIAN -> "Vuoi davvero eliminare tutte le ricette?"
        LanguageMode.SPANISH -> "¿Realmente deseas eliminar todas las recetas?"
        LanguageMode.DUTCH -> "Weet je zeker dat je alle recepten wilt verwijderen?"
        else -> "Delete all recipes permanently?"
    }

    fun getDeleteAllConfirmDialogMessage(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Diese Aktion kann nicht rückgängig gemacht werden. Alle Rezepte, Fotos und Notizen werden gelöscht."
        LanguageMode.FRENCH -> "Cette action est irréversible. Toutes les recettes, photos et notes seront effacées."
        LanguageMode.ITALIAN -> "Questa azione è irreversibile. Tutte le ricette, foto e note verranno eliminate."
        LanguageMode.SPANISH -> "Esta acción es irreversible. Todas las recetas, fotos y notas serán eliminadas."
        LanguageMode.DUTCH -> "Deze actie kan niet ongedaan worden gemaakt. Alle recepten, foto's en notities worden gewist."
        else -> "This action cannot be undone. All recipes, photos, and journal memories will be permanently wiped."
    }

    fun getCloseLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Schließen"
        LanguageMode.FRENCH -> "Fermer"
        LanguageMode.ITALIAN -> "Chiudi"
        LanguageMode.SPANISH -> "Cerrar"
        LanguageMode.DUTCH -> "Sluiten"
        else -> "Close"
    }

    fun getCancelLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Abbrechen"
        LanguageMode.FRENCH -> "Annuler"
        LanguageMode.ITALIAN -> "Annulla"
        LanguageMode.SPANISH -> "Cancelar"
        LanguageMode.DUTCH -> "Annuleren"
        else -> "Cancel"
    }

    fun getSaveLabel(lang: LanguageMode = LanguageMode.ENGLISH): String = when (lang) {
        LanguageMode.GERMAN -> "Speichern"
        LanguageMode.FRENCH -> "Enregistrer"
        LanguageMode.ITALIAN -> "Salva"
        LanguageMode.SPANISH -> "Guardar"
        LanguageMode.DUTCH -> "Opslaan"
        else -> "Save"
    }
}
