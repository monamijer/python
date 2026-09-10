// TmdbSeasonDto.java
package main.java.com.monprojet.series.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbSeasonDto(
        @JsonProperty("season_number") Integer numeroSaison,
        @JsonProperty("episode_count") Integer nombreEpisodes,
        String name
) {}