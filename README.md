# API de Orçamento com Spring AI 🎙️💰

Projeto do desafio DIO baseado em [dio-spring-boot-learning-track / 05-spring-ai](https://github.com/digitalinnovationone/dio-spring-boot-learning-track/blob/main/05-spring-ai/README.md).

## O que o projeto faz

Uma API que recebe **comandos de voz** sobre finanças pessoais e os executa de verdade:

1. Recebe um arquivo de áudio (`multipart/form-data`);
2. Transcreve o áudio em texto (Whisper);
3. A IA (`ChatClient` + `gpt-4o-mini`) entende a intenção;
4. Via **Tool Calling**, chama funções reais da aplicação (criar/consultar transações);
5. Salva/consulta os dados no banco H2;
6. Devolve a resposta em texto — ou em **áudio MP3** (Text-to-Speech).

Exemplo: *"Gastei 35 reais no almoço hoje"* → cria uma despesa de R$ 35 na categoria alimentação.

## Tecnologias

- Java 21+ · Spring Boot 4 · Spring AI 2 (OpenAI: chat, transcrição e TTS)
- Spring Web · Spring Data JPA · H2 (em memória)
- JUnit 5 (`@DataJpaTest`)

## Como executar

Pré-requisito: Java 21+ e uma chave da OpenAI.

```bash
# Windows (PowerShell)
$env:OPENAI_API_KEY="sua-chave"
.\mvnw.cmd spring-boot:run

# Linux/Mac
OPENAI_API_KEY=sua-chave ./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`. Console do banco: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:budget`).

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| POST | `/assistant/audio` | **Fluxo principal**: áudio → transcrição + resposta da IA (JSON) |
| POST | `/assistant/audio/speech` | Mesmo fluxo, resposta em áudio MP3 |
| POST | `/assistant/text` | Comando em texto (testa a IA sem precisar gravar áudio) |
| POST | `/transactions` | Cria transação manualmente |
| GET | `/transactions?start=&end=` | Lista transações (datas `yyyy-MM-dd`, opcionais) |
| GET | `/transactions/balance` | Saldo (receitas − despesas) |
| GET | `/transactions/expenses-by-category` | **Novo**: despesas agrupadas por categoria |

## Como testar o fluxo principal

```bash
# 1. Grave um áudio (ex.: "recebi 3000 reais de salário") e envie:
curl -F "file=@comando.mp3" http://localhost:8080/assistant/audio

# 2. Resposta falada:
curl -F "file=@comando.mp3" http://localhost:8080/assistant/audio/speech --output resposta.mp3

# 3. Sem áudio, por texto:
curl -X POST http://localhost:8080/assistant/text -H "Content-Type: application/json" \
  -d "{\"command\":\"Gastei 50 reais de uber ontem\"}"

curl -X POST http://localhost:8080/assistant/text -H "Content-Type: application/json" \
  -d "{\"command\":\"Quanto gastei por categoria este mês?\"}"
```

Exemplo de resposta de `/assistant/audio`:

```json
{
  "transcription": "Gastei 35 reais no almoço hoje",
  "answer": "Pronto! Registrei uma despesa de R$ 35,00 em alimentação."
}
```

Validação em ação (REST):

```bash
curl -X POST http://localhost:8080/transactions -H "Content-Type: application/json" \
  -d "{\"description\":\"teste\",\"amount\":-10,\"type\":\"EXPENSE\"}"
# 400 → {"error":"O valor deve ser maior que zero."}
```

Testes automatizados (não precisam de chave da OpenAI):

```bash
./mvnw test
```

## Melhorias implementadas

1. **Validações antes de salvar** (`TransactionService.create`): descrição obrigatória, valor > 0 e ≤ 1.000.000, tipo obrigatório, data não pode ser futura, categoria normalizada (minúscula, padrão `outros`). A mesma regra vale para o REST e para a IA — quando a IA tenta salvar algo inválido, a tool devolve a mensagem de erro e a IA explica o problema ao usuário em vez de quebrar.
2. **Nova consulta financeira**: despesas por categoria no período, disponível como endpoint REST e como nova ferramenta de Tool Calling (`expensesByCategory`), ex.: *"quanto gastei com alimentação este mês?"*.
3. **Prompt de sistema** com a data atual, para a IA entender "hoje", "ontem", "este mês".
4. **Testes** dos fluxos principais do serviço (salvar, validar, saldo, agrupamento).

## Estrutura

```
src/main/java/dio/springaiapi
├── assistant/
│   ├── AssistantController.java   # endpoints de áudio/texto
│   ├── AssistantService.java      # transcrição, ChatClient, TTS
│   └── BudgetTools.java           # @Tool: funções que a IA pode chamar
└── transaction/
    ├── Transaction.java           # entidade JPA
    ├── TransactionRepository.java
    ├── TransactionService.java    # regras + validações
    └── TransactionController.java # CRUD REST
```

## O que aprendi

- A IA não deve acessar o banco diretamente: ela **chama ferramentas** (`@Tool`) que passam pela mesma camada de serviço do resto da aplicação, mantendo as regras de negócio num só lugar.
- O `ChatClient` do Spring AI deixa a integração simples: `prompt().system().user().call()`, e o Tool Calling é registrado com `defaultTools(...)`.
- Descrições claras nas tools e nos parâmetros (`@ToolParam`) fazem muita diferença na escolha correta da função pelo modelo.
- Devolver erros de validação como texto para a IA gera respostas melhores do que lançar exceções no meio da conversa.
- Transcrição (Whisper) e TTS são só mais dois "modelos" injetáveis pelo Spring, fáceis de combinar num pipeline.
