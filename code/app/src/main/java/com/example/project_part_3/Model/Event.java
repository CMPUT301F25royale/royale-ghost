package com.example.project_part_3.Model;

import java.util.ArrayList;
import java.util.List;

public class Event {


    private String eventId;
    private String organizerId;

    private String name;
    private String description;
    private String locationName;
    private String locationAddress;
    private String posterImageUrl;    // Firebase Storage URL (can be null)


    private long registrationOpensAt;   // Milliseconds (epoch)
    private long registrationClosesAt;
    private long eventStartAt;
    private long eventEndAt;

    private int capacity;
    private int price;


    private List<String> waitlistUserIds;      // Clicked "enter" into lottery
    private List<String> selectedUserIds;      // Offered a slot
    private List<String> confirmedUserIds;     // Accepted
    private List<String> declinedUserIds;      // Declined
    private List<String> alternatesUserIds;    // Not selected but could be drawn in if people decline

    private Long seed;
    private Long lastLotteryTs;            // Timestamp of last draw


    public Event() {
        this.waitlistUserIds = new ArrayList<>();
        this.selectedUserIds = new ArrayList<>();
        this.confirmedUserIds = new ArrayList<>();
        this.declinedUserIds = new ArrayList<>();
        this.alternatesUserIds = new ArrayList<>();
    }

    public Event(
            String eventId,
            String organizerId,
            String name,
            String description,
            String locationName,
            String locationAddress,
            String posterImageUrl,
            long registrationOpensAt,
            long registrationClosesAt,
            long eventStartAt,
            long eventEndAt,
            int capacity,
            int priceCents
    ) {
        this();
        this.eventId = eventId;
        this.organizerId = organizerId;
        this.name = name;
        this.description = description;
        this.locationName = locationName;
        this.locationAddress = locationAddress;
        this.posterImageUrl = posterImageUrl;
        this.registrationOpensAt = registrationOpensAt;
        this.registrationClosesAt = registrationClosesAt;
        this.eventStartAt = eventStartAt;
        this.eventEndAt = eventEndAt;
        this.capacity = capacity;
        this.price = priceCents;
    }

   // Getters + Setters

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public String getPosterImageUrl() {
        return posterImageUrl;
    }

    public void setPosterImageUrl(String posterImageUrl) {
        this.posterImageUrl = posterImageUrl;
    }

    public long getRegistrationOpensAt() {
        return registrationOpensAt;
    }

    public void setRegistrationOpensAt(long registrationOpensAt) {
        this.registrationOpensAt = registrationOpensAt;
    }

    public long getRegistrationClosesAt() {
        return registrationClosesAt;
    }

    public void setRegistrationClosesAt(long registrationClosesAt) {
        this.registrationClosesAt = registrationClosesAt;
    }

    public long getEventStartAt() {
        return eventStartAt;
    }

    public void setEventStartAt(long eventStartAt) {
        this.eventStartAt = eventStartAt;
    }

    public long getEventEndAt() {
        return eventEndAt;
    }

    public void setEventEndAt(long eventEndAt) {
        this.eventEndAt = eventEndAt;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getPriceCents() {
        return price;
    }

    public void setPriceCents(int price) {
        this.price = price;
    }

    public List<String> getWaitlistUserIds() {
        return waitlistUserIds;
    }

    public void setWaitlistUserIds(List<String> waitlistUserIds) {
        this.waitlistUserIds = (waitlistUserIds != null) ? waitlistUserIds : new ArrayList<>();
    }

    public List<String> getSelectedUserIds() {
        return selectedUserIds;
    }

    public void setSelectedUserIds(List<String> selectedUserIds) {
        this.selectedUserIds = (selectedUserIds != null) ? selectedUserIds : new ArrayList<>();
    }

    public List<String> getConfirmedUserIds() {
        return confirmedUserIds;
    }

    public void setConfirmedUserIds(List<String> confirmedUserIds) {
        this.confirmedUserIds = (confirmedUserIds != null) ? confirmedUserIds : new ArrayList<>();
    }

    public List<String> getDeclinedUserIds() {
        return declinedUserIds;
    }

    public void setDeclinedUserIds(List<String> declinedUserIds) {
        this.declinedUserIds = (declinedUserIds != null) ? declinedUserIds : new ArrayList<>();
    }

    public List<String> getAlternatesUserIds() {
        return alternatesUserIds;
    }

    public void setAlternatesUserIds(List<String> alternatesUserIds) {
        this.alternatesUserIds = (alternatesUserIds != null) ? alternatesUserIds : new ArrayList<>();
    }

    public Long getLastLotterySeed() {
        return seed;
    }

    public void setLastLotterySeed(Long lastLotterySeed) {
        this.seed = seed;
    }

    public Long getLastLotteryRunAt() {
        return lastLotteryTs;
    }

    public void setLastLotteryRunAt(Long lastLotteryRunAt) {
        this.lastLotteryTs = lastLotteryRunAt;
    }

    // Helpers for our pleasure

    public int getConfirmedCount() {
        if (confirmedUserIds != null){
            return confirmedUserIds.size();
        }
        else
        {
            return 0;
        }

    }

    // Get remaining seats, and make sure not to return negative (though idk how that would be possible).
    public int getRemainingCapacity() {
        int remaining = capacity - getConfirmedCount();
        return Math.max(remaining, 0);
    }

    // Return true if current time is between registration open and deadline
    public boolean isRegistrationOpen(long nowMs) {
        return nowMs >= registrationOpensAt && nowMs <= registrationClosesAt;
    }
}
