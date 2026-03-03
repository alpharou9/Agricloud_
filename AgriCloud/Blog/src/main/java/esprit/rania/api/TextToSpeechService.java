package esprit.rania.api;

public class TextToSpeechService {

    private static Process currentProcess = null;
    private static boolean isSpeaking = false;

    public static void speak(String text) {
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

                System.out.println("[TTS] Speaking: " + cleanText.substring(0, Math.min(60, cleanText.length())) + "...");

                // Use PowerShell inline command - same as what works in your terminal
                String command = "Add-Type -AssemblyName System.Speech; " +
                        "$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                        "$s.Rate = 0; " +
                        "$s.Volume = 100; " +
                        "$s.Speak('" + cleanText + "'); " +
                        "$s.Dispose();";

                ProcessBuilder pb = new ProcessBuilder(
                        "powershell.exe", "-Command", command
                );
                pb.inheritIO(); // Use the same audio output as the parent process
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