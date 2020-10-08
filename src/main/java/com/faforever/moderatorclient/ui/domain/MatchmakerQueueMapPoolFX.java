package com.faforever.moderatorclient.ui.domain;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;

public class MatchmakerQueueMapPoolFX extends AbstractEntityFX {
    private DoubleProperty minRating;
    private DoubleProperty maxRating;
    private ObjectProperty<MatchmakerQueueFX> matchmakerQueue;
    private ObjectProperty<MapPoolFX> mapPool;

    public MatchmakerQueueMapPoolFX() {
        minRating = new SimpleDoubleProperty();
        maxRating = new SimpleDoubleProperty();
        matchmakerQueue = new SimpleObjectProperty<>();
        mapPool = new SimpleObjectProperty<>();
    }

    public double getMinRating() {
        return minRating.get();
    }

    public DoubleProperty minRatingProperty() {
        return minRating;
    }

    public void setMinRating(double minRating) {
        this.minRating.set(minRating);
    }

    public double getMaxRating() {
        return maxRating.get();
    }

    public DoubleProperty maxRatingProperty() {
        return maxRating;
    }

    public void setMaxRating(double maxRating) {
        this.maxRating.set(maxRating);
    }

    public MatchmakerQueueFX getMatchmakerQueue() {
        return matchmakerQueue.get();
    }

    public ObjectProperty<MatchmakerQueueFX> matchmakerQueueProperty() {
        return matchmakerQueue;
    }

    public void setMatchmakerQueue(MatchmakerQueueFX matchmakerQueue) {
        this.matchmakerQueue.set(matchmakerQueue);
    }

    public MapPoolFX getMapPool() {
        return mapPool.get();
    }

    public ObjectProperty<MapPoolFX> mapPoolProperty() {
        return mapPool;
    }

    public void setMapPool(MapPoolFX mapPool) {
        this.mapPool.set(mapPool);
    }

    @Override
    public String toString() {
        return "MatchmakerQueueMapPoolFX{" +
                "minRating=" + minRating +
                ", maxRating=" + maxRating +
                ", matchmakerQueue=" + matchmakerQueue +
                ", mapPool=" + mapPool +
                '}';
    }
}