import functionCalling.*

object toolCalls {
  def call(openAIToolCall: OpenAIToolCall): String= {
    var output = ""

    if (openAIToolCall.function.name == "numberOfRegistredCompanies") {
      output = numberOfRegistredCompanies
    }

    if (openAIToolCall.function.name == "numberOfDifferentCertifications") {
      output = numberOfDifferentCertifications
    }

    if (openAIToolCall.function.name == "mostCommonCertification") {
      output = mostCommonCertification
    }

    if (openAIToolCall.function.name == "registeredIdentifiers") {
      output = registeredIdentifiers
    }

    if (openAIToolCall.function.name == "numberOfCompaniesMissingWebsites") {
      output = numberOfCompaniesMissingWebsites
    }

    if (openAIToolCall.function.name == "mostCommonCEOFirstMame") {
      output = mostCommonCEOFirstMame
    }

    if (openAIToolCall.function.name == "mostPopularYearWorkshopIncorporate") {
      output = mostPopularYearWorkshopIncorporate
    }

    output
  }

}
