package io.cequence.openaiscala.service

object StreamedServiceTypes {
  type OpenAIChatCompletionStreamedService = OpenAIChatCompletionService
    & OpenAIChatCompletionStreamedServiceExtra

  type OpenAICoreStreamedService = OpenAICoreService & OpenAIStreamedServiceExtra

  type OpenAIStreamedService = OpenAIService & OpenAIStreamedServiceExtra
}
