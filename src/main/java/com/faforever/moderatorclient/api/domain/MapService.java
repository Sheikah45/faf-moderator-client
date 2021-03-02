package com.faforever.moderatorclient.api.domain;

import com.faforever.commons.api.dto.AbstractEntity;
import com.faforever.commons.api.dto.Ladder1v1Map;
import com.faforever.commons.api.dto.Map;
import com.faforever.commons.api.dto.MapVersion;
import com.faforever.commons.api.elide.ElideNavigator;
import com.faforever.commons.api.elide.ElideNavigatorOnCollection;
import com.faforever.moderatorclient.api.FafApiCommunicationService;
import com.faforever.moderatorclient.common.MapPool;
import com.faforever.moderatorclient.common.MatchmakerQueue;
import com.faforever.moderatorclient.common.MatchmakerQueueMapPool;
import com.faforever.moderatorclient.mapstruct.MapPoolMapper;
import com.faforever.moderatorclient.mapstruct.MapVersionMapper;
import com.faforever.moderatorclient.mapstruct.MatchmakerQueueMapPoolMapper;
import com.faforever.moderatorclient.ui.domain.MapPoolFX;
import com.faforever.moderatorclient.ui.domain.MapVersionFX;
import com.faforever.moderatorclient.ui.domain.MatchmakerQueueFX;
import com.faforever.moderatorclient.ui.domain.MatchmakerQueueMapPoolFX;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MapService {
    private final FafApiCommunicationService fafApi;
    private final MapVersionMapper mapVersionMapper;
    private final MapPoolMapper mapPoolMapper;
    private final MatchmakerQueueMapPoolMapper matchmakerQueueMapPoolMapper;

    private List<Map> findMapsByAttribute(@NotNull String attribute, @NotNull String pattern, boolean excludeHidden) {
        log.debug("Searching for maps by attribute '{}' with pattern: {}", attribute, pattern);
        ElideNavigatorOnCollection<MapVersion> routeBuilder = ElideNavigator.of(MapVersion.class)
                .collection()
                .addIncludeOnCollection("map")
                .addIncludeOnCollection("map.author");

        if (excludeHidden) {
            routeBuilder.addFilter(ElideNavigator.qBuilder().string("map." + attribute).eq(pattern)
                    .and().bool("hidden").isFalse());
        } else {
            routeBuilder.addFilter(ElideNavigator.qBuilder().string("map." + attribute).eq(pattern));
        }

        List<Map> result = fafApi.getAll(MapVersion.class, routeBuilder).stream()
                .map(MapVersion::getMap)
                .distinct()
                .collect(Collectors.toList());

        // filter empty mapVersions that were created by map relationships
        for (Map map : result) {
            map.getVersions().removeIf(mapVersion -> mapVersion.getMap() == null);
        }

        log.trace("found {} maps", result.size());
        return result;
    }

    public List<Map> findMapsByName(@NotNull String pattern, boolean excludeHidden) {
        return findMapsByAttribute("displayName", pattern, excludeHidden);
    }

    public List<Map> findMapsByAuthorId(@NotNull String pattern, boolean excludeHidden) {
        return findMapsByAttribute("author.id", pattern, excludeHidden);
    }

    public List<Map> findMapsByAuthorName(@NotNull String pattern, boolean excludeHidden) {
        return findMapsByAttribute("author.login", pattern, excludeHidden);
    }

    public List<Map> findMaps(String mapNamePattern) {
        log.debug("Searching for maps with pattern: {}", mapNamePattern);
        ElideNavigatorOnCollection<Map> routeBuilder = ElideNavigator.of(Map.class)
                .collection()
                .addIncludeOnCollection("versions");

        if (mapNamePattern != null && mapNamePattern.length() > 0) {
            routeBuilder.addFilter(ElideNavigator.qBuilder().string("displayName").eq(mapNamePattern));
        }

        List<Map> result = fafApi.getAll(Map.class, routeBuilder);
        log.trace("found {} maps", result.size());
        return result;
    }

    public List<MatchmakerQueue> getAllMatchmakerQueues() {
        log.debug("Searching for all matchmaker queues");
        ElideNavigatorOnCollection<MatchmakerQueue> routeBuilder = ElideNavigator.of(MatchmakerQueue.class)
                .collection();
        List<MatchmakerQueue> queues = fafApi.getAll(MatchmakerQueue.class, routeBuilder);
        log.debug("found {} matchmaker queues", queues.size());
        return queues;
    }

    public List<MatchmakerQueueMapPool> getListOfBracketsInMatchmakerQueue(MatchmakerQueue queue) {
        log.debug("Searching for all brackets in queue {}", queue.getId());
        ElideNavigatorOnCollection<MatchmakerQueueMapPool> routeBuilder = ElideNavigator.of(MatchmakerQueueMapPool.class)
                .collection()
                .addFilter(ElideNavigator.qBuilder().string("matchmakerQueue.id").eq(queue.getId()))
                .addIncludeOnCollection("mapPool")
                .addIncludeOnCollection("mapPool.mapVersions")
                .addIncludeOnCollection("mapPool.mapVersions.map");
        List<MatchmakerQueueMapPool> brackets = fafApi.getAll(MatchmakerQueueMapPool.class, routeBuilder);
        for (MatchmakerQueueMapPool bracket : brackets) {
            log.info("{}", bracket);
        }
        return brackets;
    }

    public void patchMapPool(MapPoolFX mapPoolFX) {
        patchMapPool(mapPoolMapper.mapToDto(mapPoolFX));

    }

    public void patchMapPool(MapPool mapPool) {
        log.debug("Updating mapPool id: {}", mapPool.getId());
        fafApi.patch(ElideNavigator.of(mapPool),
                (MapPool) new MapPool()
                        .setMapVersions(mapPool.getMapVersions())
                        .setName(mapPool.getName())
                        .setId(mapPool.getId()));
    }


    public void patchBracket(MatchmakerQueueMapPoolFX bracketFX) {
        log.debug("Updating matchmakerQueueMapPool (bracket) id: {}", bracketFX.getId());
        var bracket = matchmakerQueueMapPoolMapper.mapToDto(bracketFX);
        fafApi.patch(ElideNavigator.of(bracket),
                (MatchmakerQueueMapPool) new MatchmakerQueueMapPool()
                        .setMapPool(bracket.getMapPool())
                        .setMaxRating(bracket.getMaxRating())
                        .setMinRating(bracket.getMinRating())
                        .setId(bracket.getId()));
    }

    public void patchMapVersion(MapVersionFX mapVersionFX) {
        patchMapVersion(mapVersionMapper.map(mapVersionFX));
    }

    public void patchMapVersion(MapVersion mapVersion) {
        log.debug("Updating mapVersion id: {}", mapVersion.getId());
        fafApi.patch(ElideNavigator.of(mapVersion),
                (MapVersion) new MapVersion()
                        .setHidden(mapVersion.isHidden())
                        .setRanked(mapVersion.isRanked())
                        .setId(mapVersion.getId()
                        ));
    }

    public boolean doesMapVersionExist(int id) {
        log.debug("Requesting Mapversion with id: {}", id);
        return !fafApi.getAll(MapVersion.class, ElideNavigator.of(MapVersion.class)
                .collection()
                .addFilter(ElideNavigator.qBuilder().string("id").eq(String.valueOf(id))))
                .isEmpty();
    }

    public List<MapVersionFX> findLatestMapVersions() {
        log.debug("Searching for latest mapVersions ");
        ElideNavigatorOnCollection<MapVersion> navigator = ElideNavigator.of(MapVersion.class)
                .collection()
                .addIncludeOnCollection("map")
                .addIncludeOnCollection("map.author")
                .addSortingRule("id", false);

        List<MapVersion> result = fafApi.getPage(MapVersion.class, navigator, 50, 1, Collections.emptyMap());
        log.trace("found {} teamkills", result.size());
        return mapVersionMapper.mapToFX(result);
    }
}
