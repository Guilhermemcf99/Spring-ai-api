package dio.springaiapi.assistant;

import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class AssistantService {

    private static final String SYSTEM_PROMPT = """
            Você é um assistente de orçamento pessoal. Responda sempre em português, de forma curta e amigável.
            Use as ferramentas disponíveis para registrar ou consultar transações; nunca invente dados.
            Valores em reais (R$). Hoje é %s.
            """;

    private final ChatClient chatClient;
    private final TranscriptionModel transcriptionModel;
    private final TextToSpeechModel speechModel;

    public AssistantService(ChatClient.Builder builder, BudgetTools tools,
                            TranscriptionModel transcriptionModel, TextToSpeechModel speechModel) {
        this.chatClient = builder.defaultTools(tools).build();
        this.transcriptionModel = transcriptionModel;
        this.speechModel = speechModel;
    }

    public String transcribe(Resource audio) {
        return transcriptionModel.transcribe(audio);
    }

    public String chat(String command) {
        return chatClient.prompt()
                .system(SYSTEM_PROMPT.formatted(LocalDate.now()))
                .user(command)
                .call()
                .content();
    }

    public byte[] speak(String text) {
        return speechModel.call(text);
    }
}
