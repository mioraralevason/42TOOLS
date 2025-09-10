package com.ecole._2.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Event {

    private String id;
    private String name;
    private String description;
    private String location;
    private String kind;

    @JsonProperty("max_people")
    private Integer maxPeople;

    @JsonProperty("nbr_subscribers")
    private Integer nbrSubscribers;

    @JsonProperty("begin_at")
    private OffsetDateTime beginAt;

    @JsonProperty("end_at")
    private OffsetDateTime endAt;

    @JsonProperty("campus_ids")
    private List<Long> campusIds;
    
    // --- Getters & Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }

    public Integer getMaxPeople() { return maxPeople; }
    public void setMaxPeople(Integer maxPeople) { this.maxPeople = maxPeople; }

    public Integer getNbrSubscribers() { return nbrSubscribers; }
    public void setNbrSubscribers(Integer nbrSubscribers) { this.nbrSubscribers = nbrSubscribers; }

    public OffsetDateTime getBeginAt() { return beginAt; }
    public void setBeginAt(OffsetDateTime beginAt) { this.beginAt = beginAt; }

    public OffsetDateTime getEndAt() { return endAt; }
    public void setEndAt(OffsetDateTime endAt) { this.endAt = endAt; }

    public List<Long> getCampusIds() { return campusIds; }
    public void setCampusIds(List<Long> campusIds) { this.campusIds = campusIds; }


}
