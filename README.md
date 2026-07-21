# Agent Integration Demo

[![CI](https://github.com/SrinathRayabarapu/agent-integration-demo/actions/workflows/ci.yml/badge.svg)](https://github.com/SrinathRayabarapu/agent-integration-demo/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Spring AI command-line demos for common AI agent integration patterns:

- Inference API basics (chat, streaming, system prompts, structured output)
- Tools and MCP
- Agent Skills
- RAG
- Chat memory
- Human in the loop

This project is based on the excellent original demos by [James Ward](https://github.com/jamesward/agent-integration-demo), adapted to run locally with [Ollama](https://ollama.com/) by default (no cloud API key required). Optional AWS Bedrock support remains available.

## Requirements

- **Java 25** (toolchain is configured in Gradle)
- **Ollama** running locally for the default setup
- Optional: AWS Bedrock bearer token if you use `-Pllm=bedrock`

## Quick start (Ollama)

1. Install and start [Ollama](https://ollama.com/).
2. Pull the models used by this project:

```bash
ollama pull llama3.2
ollama pull nomic-embed-text
```

`llama3.2` is used for chat/tools; `nomic-embed-text` is used for RAG embeddings.

3. Run a demo profile:

```bash
./gradlew systemPrompt
```

Verbose request/response logging:

```bash
./gradlew systemPromptDebug
```

Override the chat model:

```bash
./gradlew systemPrompt -Dspring.ai.ollama.chat.model=qwen2.5
```

Prefer a model with reliable native tool calling for tools/MCP/HITL demos.

## Optional: AWS Bedrock

1. [Create a Bedrock bearer token](https://us-east-1.console.aws.amazon.com/bedrock/home?region=us-east-1#/api-keys/long-term/create).
2. Export it in your shell (never commit tokens):

```bash
export AWS_BEARER_TOKEN_BEDROCK=YOUR_TOKEN
```

3. Run with Bedrock:

```bash
./gradlew systemPrompt -Pllm=bedrock
```

## Demo profiles

| Profile | Pattern |
|---------|---------|
| `default` | Basic chat |
| `structuredOutput` | Structured / typed output |
| `streaming` | Streaming response |
| `systemPrompt` | System prompt |
| `noTools` | Chat without tools |
| `basicTools` | Local tools |
| `mcpTools` | MCP tools |
| `toolSearch` | Tool search |
| `noMemory` / `inMemory` | Chat memory |
| `ragDocuments` / `ragDomain` | RAG |
| `preloadSkill` / `ondemandSkill` / `skillsjars` | Agent skills |
| `hitl` | Human in the loop |

Append `Debug` to any Gradle task name to also enable the `debug` Spring profile.

## Project layout

```
src/main/java/com/srinath/     # Demo runners (Spring profiles)
src/main/java/com/srinath/internal/  # Shared demo helpers
src/main/resources/            # application*.properties, sample skills
```

## Security notes

- Do **not** commit API keys, bearer tokens, or `.env` files.
- Set secrets via environment variables only (for example `AWS_BEARER_TOKEN_BEDROCK`).
- Account numbers in the RAG/HITL samples are synthetic demo data, not real credentials.
- See [SECURITY.md](SECURITY.md) for reporting vulnerabilities.

## License

This project is licensed under the [MIT License](LICENSE).

## Acknowledgments

- Original agent integration patterns demo: [jamesward/agent-integration-demo](https://github.com/jamesward/agent-integration-demo)
- [Spring AI](https://docs.spring.io/spring-ai/reference/)
- [Ollama](https://ollama.com/)
