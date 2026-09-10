// TmdbEpisodeDto.java
package main.java.com.monprojet.series.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbEpisodeDto(
        @JsonProperty("episode_number") Integer numeroEpisode,
        String name,
        Integer runtime
) {}