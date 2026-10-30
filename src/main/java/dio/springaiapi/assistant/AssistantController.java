package dio.springaiapi.assistant;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/assistant")
public class AssistantController {

    private final AssistantService assistant;

    public AssistantController(AssistantService assistant) {
        this.assistant = assistant;
    }

    /** Comando em texto (útil para testar sem áudio). */
    @PostMapping("/text")
    public Map<String, String> text(@RequestBody Map<String, String> body) {
        return Map.of("answer", assistant.chat(body.get("command")));
    }

    /** Fluxo principal: áudio -> texto -> IA + tools -> resposta em texto. */
    @PostMapping(value = "/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> audio(@RequestParam("file") MultipartFile file) throws IOException {
        String command = assistant.transcribe(file.getResource());
        return Map.of("transcription", command, "answer", assistant.chat(command));
    }

    /** Mesmo fluxo, mas devolve a resposta falada em MP3. */
    @PostMapping(value = "/audio/speech", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "audio/mpeg")
    public byte[] audioSpeech(@RequestParam("file") MultipartFile file) throws IOException {
        String command = assistant.transcribe(file.getResource());
        return assistant.speak(assistant.chat(command));
    }
}
