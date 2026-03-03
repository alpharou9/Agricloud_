package esprit.rania.api;

public class TextToSpeechService {

    private static Process currentProcess = null;
    private static boolean isSpeaking = false;

    /**
     * Speak text in the given language
     * @param text Text to speak
     * @param langCode Language code e.g. "en", "fr", "ar"
     */
    public static void speak(String text, String langCode) {
        stop();

        new Thread(() -> {
            try {
                isSpeaking = true;

                String cleanText = text
                        .replace("'", " ")
                        .replace("\"", " ")
                        .replace("`", " ")
                        .replace("\\", " ")
                        .replace("\n", " ")
                        .replace("\r", " ")
                        .replace("&", "and");

                if (cleanText.length() > 500) {
                    cleanText = cleanText.substring(0, 500) + ".";
                }

                System.out.println("[TTS] Speaking in " + langCode + ": " + cleanText.substring(0, Math.min(60, cleanText.length())) + "...");

                // Map language code to Windows voice name
                String voiceHint = getVoiceHint(langCode);

                // Build PowerShell command
                // Try to select a voice matching the language, fallback to default
                String command = "Add-Type -AssemblyName System.Speech; " +
                        "$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                        "$voices = $s.GetInstalledVoices() | ForEach-Object { $_.VoiceInfo }; " +
                        "$match = $voices | Where-Object { $_.Culture.TwoLetterISOLanguageName -eq '" + langCode + "' } | Select-Object -First 1; " +
                        "if ($match) { $s.SelectVoice($match.Name); Write-Host 'Using voice: ' $match.Name } " +
                        "else { Write-Host 'No voice for " + langCode + ", using default' }; " +
                        "$s.Rate = 0; " +
                        "$s.Volume = 100; " +
                        "$s.Speak('" + cleanText + "'); " +
                        "$s.Dispose();";

                ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-Command", command);
                pb.inheritIO();
                currentProcess = pb.start();
                currentProcess.waitFor();

                isSpeaking = false;
                System.out.println("[TTS] Done speaking.");

            } catch (Exception e) {
                isSpeaking = false;
                System.err.println("[TTS] Error: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Speak in English (default)
     */
    public static void speak(String text) {
        speak(text, "en");
    }

    /**
     * Get Windows voice culture hint for a language code
     */
    private static String getVoiceHint(String langCode) {
        switch (langCode) {
            case "fr": return "fr";
            case "es": return "es";
            case "ar": return "ar";
            case "de": return "de";
            case "it": return "it";
            case "pt": return "pt";
            case "ru": return "ru";
            case "zh": return "zh";
            case "ja": return "ja";
            case "hi": return "hi";
            default:   return "en";
        }
    }

    public static void stop() {
        if (currentProcess != null && currentProcess.isAlive()) {
            currentProcess.destroyForcibly();
        }
        isSpeaking = false;
        currentProcess = null;
    }

    public static boolean isSpeaking() {
        return isSpeaking;
    }
}