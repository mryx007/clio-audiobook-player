package voice.features.settings.statistics

public sealed interface StatisticsViewEffect {
  public data class ShowMessage(val message: String) : StatisticsViewEffect
}
