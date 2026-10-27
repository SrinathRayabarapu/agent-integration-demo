plugins {
    java
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    // Spring AI BOM keeps all spring-ai-* artifacts on compatible versions.
    implementation(platform("org.springframework.ai:spring-ai-bom:2.0.2"))
    runtimeOnly("org.springframework.boot:spring-boot-starter-jackson")
    implementation("org.springframework.ai:spring-ai-starter-model-ollama")
    implementation("org.springframework.ai:spring-ai-starter-model-bedrock")
    implementation("org.springframework.ai:spring-ai-starter-model-bedrock-converse")
    implementation("org.springframework.ai:spring-ai-starter-mcp-client")
    implementation("org.springframework.ai:spring-ai-starter-model-chat-memory")
    implementation("org.springframework.ai:spring-ai-vector-store-advisor")
    implementation("org.springframework.ai:spring-ai-rag")
    implementation("org.springframework.ai:spring-ai-tool-search-advisor")
    implementation("org.springframework.ai:spring-ai-tool-search-tool")
    implementation("org.springaicommunity:spring-ai-agent-utils:0.10.0")
    // Third-party skills jar used by the skillsjars demo (Maven coordinate unchanged).
    runtimeOnly("com.jamesward:pooch-palace:0.0.4")
}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    standardInput = System.`in`
}

// Default LLM is Ollama (local). Use -Pllm=bedrock to switch to AWS Bedrock.
val llmProfile = (findProperty("llm") as String?)
    ?.takeIf { it.equals("bedrock", ignoreCase = true) }

val profiles = listOf(
    "default",
    "structuredOutput",
    "streaming",
    "systemPrompt",
    "noTools",
    "noMemory",
    "inMemory",
    "ragDocuments",
    "ragDomain",
    "preloadSkill",
    "ondemandSkill",
    "skillsjars",
    "basicTools",
    "mcpTools",
    "toolSearch",
    "hitl",
)

fun activeProfiles(vararg extras: String): String =
    listOfNotNull(*extras, llmProfile).joinToString(",")

fun org.springframework.boot.gradle.tasks.run.BootRun.forwardSpringAiSystemProperties() {
    System.getProperties().stringPropertyNames()
        .filter { it.startsWith("spring.ai.") }
        .forEach { key -> systemProperty(key, System.getProperty(key)) }
}

for (profile in profiles) {
    tasks.register<org.springframework.boot.gradle.tasks.run.BootRun>(profile) {
        group = "application"
        description = "Runs with the $profile Spring profile (Ollama by default; -Pllm=bedrock for Bedrock)"
        mainClass = "com.srinath.Application"
        classpath = sourceSets["main"].runtimeClasspath
        standardInput = System.`in`
        systemProperty("spring.profiles.active", activeProfiles(profile))
        forwardSpringAiSystemProperties()
    }
    tasks.register<org.springframework.boot.gradle.tasks.run.BootRun>("${profile}Debug") {
        group = "application"
        description = "Runs with the $profile and debug Spring profiles (Ollama by default; -Pllm=bedrock for Bedrock)"
        mainClass = "com.srinath.Application"
        classpath = sourceSets["main"].runtimeClasspath
        standardInput = System.`in`
        systemProperty("spring.profiles.active", activeProfiles(profile, "debug"))
        forwardSpringAiSystemProperties()
    }
}
